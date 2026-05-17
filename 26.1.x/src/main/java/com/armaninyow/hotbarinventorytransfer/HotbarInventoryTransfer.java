package com.armaninyow.hotbarinventorytransfer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

// 26.1.x
public class HotbarInventoryTransfer implements ModInitializer {
	public static final String MOD_ID = "hotbarinventorytransfer";

	private static KeyMapping swapKeyBinding;

	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath(MOD_ID, "hotbar_inventory_transfer")
	);

	@Override
	public void onInitialize() {
		// Initialize overlay
		InventoryFullOverlay.init();

		// Register keybinding
		swapKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.hotbarinventorytransfer.swap",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_R,
			CATEGORY
		));

		// Register tick event to check for key presses
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			while (swapKeyBinding.consumeClick()) {
				swapHotbarItem(client);
			}
		});
	}

	private static void swapHotbarItem(Minecraft client) {
		if (client.gameMode == null || client.player == null) return;

		Inventory inventory = client.player.getInventory();
		int selectedSlot = inventory.getSelectedSlot();
		ItemStack hotbarStack = inventory.getItem(selectedSlot);

		if (hotbarStack.isEmpty()) return;

		int hotbarScreenSlot = selectedSlot + 36;
		int syncId = client.player.containerMenu.containerId;

		// Pick up the entire stack from hotbar
		client.gameMode.handleContainerInput(syncId, hotbarScreenSlot, 0, ContainerInput.PICKUP, client.player);

		boolean transferSuccessful = false;

		// Try to distribute items across inventory
		for (int attempt = 0; attempt < 10; attempt++) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}

			ItemStack cursorStack = client.player.containerMenu.getCarried();
			if (cursorStack.isEmpty()) {
				transferSuccessful = true;
				break;
			}

			int targetSlot = -1;

			// First priority: Find a non-full stack of the same item
			for (int i = 9; i < 36; i++) {
				ItemStack stackInSlot = inventory.getItem(i);
				if (!stackInSlot.isEmpty()
					&& ItemStack.isSameItemSameComponents(cursorStack, stackInSlot)
					&& stackInSlot.getCount() < stackInSlot.getMaxStackSize()) {
					targetSlot = i;
					break;
				}
			}

			// Second priority: Find an empty slot
			if (targetSlot == -1) {
				for (int i = 9; i < 36; i++) {
					if (inventory.getItem(i).isEmpty()) {
						targetSlot = i;
						break;
					}
				}
			}

			// If no suitable slot found, return to hotbar
			if (targetSlot == -1) {
				client.gameMode.handleContainerInput(syncId, hotbarScreenSlot, 0, ContainerInput.PICKUP, client.player);
				break;
			}

			client.gameMode.handleContainerInput(syncId, targetSlot, 0, ContainerInput.PICKUP, client.player);
		}

		// Safety check: if items still in cursor, force return to hotbar
		ItemStack finalCursorStack = client.player.containerMenu.getCarried();
		if (!finalCursorStack.isEmpty()) {
			client.gameMode.handleContainerInput(syncId, hotbarScreenSlot, 0, ContainerInput.PICKUP, client.player);
		}

		// Show feedback based on transfer result
		if (transferSuccessful) {
			playRandomBundleInsertSound(client);
		} else {
			InventoryFullOverlay.showMessage();
			playBundleInsertFailSound(client);
		}
	}

	private static SoundEvent getVanillaSound(String id) {
		return BuiltInRegistries.SOUND_EVENT.get(Identifier.withDefaultNamespace(id))
			.map(ref -> ref.value())
			.orElse(null);
	}

	private static void playRandomBundleInsertSound(Minecraft client) {
		if (client.player == null || client.level == null) return;

		SoundEvent sound = getVanillaSound("item.bundle.insert");
		if (sound == null) return;

		float randomPitch = 0.75f + client.player.getRandom().nextFloat() * 0.75f;

		client.level.playSound(
			client.player,
			client.player.blockPosition(),
			sound,
			SoundSource.PLAYERS,
			0.8f,
			randomPitch
		);
	}

	private static void playBundleInsertFailSound(Minecraft client) {
		if (client.player == null || client.level == null) return;

		SoundEvent sound = getVanillaSound("item.bundle.insert_fail");
		if (sound == null) return;

		client.level.playSound(
			client.player,
			client.player.blockPosition(),
			sound,
			SoundSource.PLAYERS,
			1.0f,
			1.0f
		);
	}
}
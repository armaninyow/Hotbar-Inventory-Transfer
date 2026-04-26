package com.armaninyow.hotbarinventorytransfer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.Registries;
import org.lwjgl.glfw.GLFW;

// 1.21.5_1.21.8:
public class HotbarInventoryTransfer implements ModInitializer {
	public static final String MOD_ID = "hotbarinventorytransfer";

	private static KeyBinding swapKeyBinding;

	@Override
	public void onInitialize() {
		// Initialize overlay
		InventoryFullOverlay.init();

		swapKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
    		"key.hotbarinventorytransfer.swap",
    		InputUtil.Type.KEYSYM,
    		GLFW.GLFW_KEY_R,
    		"key.category.hotbarinventorytransfer.hotbar_inventory_transfer"
		));

		// Register tick event to check for key presses
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			while (swapKeyBinding.wasPressed()) {
				swapHotbarItem(client);
			}
		});
	}

	private static void swapHotbarItem(MinecraftClient client) {
		if (client.interactionManager == null || client.player == null) return;

		PlayerInventory inventory = client.player.getInventory();
		int selectedSlot = inventory.getSelectedSlot(); // method in 1.21.5+
		ItemStack hotbarStack = inventory.getStack(selectedSlot);

		if (hotbarStack.isEmpty()) return;

		int hotbarScreenSlot = selectedSlot + 36;
		int syncId = client.player.playerScreenHandler.syncId;

		// Pick up the entire stack from hotbar
		client.interactionManager.clickSlot(syncId, hotbarScreenSlot, 0, SlotActionType.PICKUP, client.player);

		boolean transferSuccessful = false;

		// Try to distribute items across inventory
		// Repeat up to 10 times to handle overflow cases
		for (int attempt = 0; attempt < 10; attempt++) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}

			ItemStack cursorStack = client.player.currentScreenHandler.getCursorStack();
			if (cursorStack.isEmpty()) {
				transferSuccessful = true;
				break;
			}

			int targetSlot = -1;

			// First priority: Find a non-full stack of the same item
			for (int i = 9; i < 36; i++) {
				ItemStack stackInSlot = inventory.getStack(i);
				if (!stackInSlot.isEmpty()
					&& ItemStack.areItemsAndComponentsEqual(cursorStack, stackInSlot)
					&& stackInSlot.getCount() < stackInSlot.getMaxCount()) {
					targetSlot = i;
					break;
				}
			}

			// Second priority: Find an empty slot
			if (targetSlot == -1) {
				for (int i = 9; i < 36; i++) {
					if (inventory.getStack(i).isEmpty()) {
						targetSlot = i;
						break;
					}
				}
			}

			// If no suitable slot found, return to hotbar
			if (targetSlot == -1) {
				client.interactionManager.clickSlot(syncId, hotbarScreenSlot, 0, SlotActionType.PICKUP, client.player);
				break;
			}

			client.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, client.player);
		}

		// Safety check: if items still in cursor, force return to hotbar
		ItemStack finalCursorStack = client.player.currentScreenHandler.getCursorStack();
		if (!finalCursorStack.isEmpty()) {
			client.interactionManager.clickSlot(syncId, hotbarScreenSlot, 0, SlotActionType.PICKUP, client.player);
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
		return Registries.SOUND_EVENT.get(Identifier.of("minecraft", id));
	}

	private static void playRandomBundleInsertSound(MinecraftClient client) {
		if (client.player == null || client.world == null) return;

		SoundEvent sound = getVanillaSound("item.bundle.insert");
		if (sound == null) return;

		float randomPitch = 0.75f + client.player.getRandom().nextFloat() * 0.75f;

		client.world.playSound(
			client.player,
			client.player.getBlockPos(),
			sound,
			SoundCategory.PLAYERS,
			0.8f,
			randomPitch
		);
	}

	private static void playBundleInsertFailSound(MinecraftClient client) {
		if (client.player == null || client.world == null) return;

		SoundEvent sound = getVanillaSound("item.bundle.insert_fail");
		if (sound == null) return;

		client.world.playSound(
			client.player,
			client.player.getBlockPos(),
			sound,
			SoundCategory.PLAYERS,
			1.0f,
			1.0f
		);
	}
}
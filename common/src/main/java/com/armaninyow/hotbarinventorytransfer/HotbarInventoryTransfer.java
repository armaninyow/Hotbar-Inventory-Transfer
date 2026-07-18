package com.armaninyow.hotbarinventorytransfer;

import com.armaninyow.hotbarinventorytransfer.mixin.ItemInHandRendererAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HotbarInventoryTransfer implements ModInitializer {
	public static final String MOD_ID = "hotbarinventorytransfer";

	private static KeyMapping swapKeyBinding;
	private static List<Integer> pendingTransferPlan = null;
	private static int pendingHotbarScreenSlot = -1;
	private static ItemStack pendingVisibleItem = ItemStack.EMPTY;

	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath(MOD_ID, "hotbar_inventory_transfer")
	);

	@Override
	public void onInitialize() {
		InventoryFullOverlay.init();

		swapKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.hotbarinventorytransfer.swap",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_R,
			CATEGORY
		));

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.player == null) return;
			if (pendingTransferPlan != null) {
				executePendingTransfer(client);
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			while (swapKeyBinding.consumeClick()) {
				requestHotbarSwap(client);
			}
		});
	}

	private static void requestHotbarSwap(Minecraft client) {
		if (client.gameMode == null || client.player == null) return;
		if (pendingTransferPlan != null) return;

		Inventory inventory = client.player.getInventory();
		int selectedSlot = inventory.getSelectedSlot();
		ItemStack hotbarStack = inventory.getItem(selectedSlot);

		if (hotbarStack.isEmpty()) return;

		List<Integer> transferPlan = computeTransferPlan(inventory, hotbarStack);

		if (transferPlan == null) {
			InventoryFullOverlay.showMessage();
			playBundleInsertFailSound(client);
			return;
		}

		pendingTransferPlan = transferPlan;
		pendingHotbarScreenSlot = selectedSlot + 36;
		pendingVisibleItem = hotbarStack.copy();
	}

	private static void executePendingTransfer(Minecraft client) {
		List<Integer> transferPlan = pendingTransferPlan;
		int hotbarScreenSlot = pendingHotbarScreenSlot;
		ItemStack visibleItem = pendingVisibleItem;
		pendingTransferPlan = null;
		pendingHotbarScreenSlot = -1;
		pendingVisibleItem = ItemStack.EMPTY;

		if (client.gameMode == null || client.player == null) return;

		int syncId = client.player.containerMenu.containerId;

		client.gameMode.handleContainerInput(syncId, hotbarScreenSlot, 0, ContainerInput.PICKUP, client.player);

		for (int targetSlot : transferPlan) {
			client.gameMode.handleContainerInput(syncId, targetSlot, 0, ContainerInput.PICKUP, client.player);
		}

		ItemStack finalCursorStack = client.player.containerMenu.getCarried();
		if (!finalCursorStack.isEmpty()) {
			client.gameMode.handleContainerInput(syncId, hotbarScreenSlot, 0, ContainerInput.PICKUP, client.player);
			InventoryFullOverlay.showMessage();
			playBundleInsertFailSound(client);
			return;
		}

		freezeHeldItemAnimation(client, visibleItem);
		playRandomBundleInsertSound(client);
	}

	private static void freezeHeldItemAnimation(Minecraft client, ItemStack visibleItem) {
		ItemInHandRenderer renderer = client.gameRenderer.itemInHandRenderer;
		if (!(renderer instanceof ItemInHandRendererAccessor accessor)) return;

		accessor.hotbarinventorytransfer$setMainHandItem(visibleItem);
		accessor.hotbarinventorytransfer$setMainHandHeight(1.0f);
		accessor.hotbarinventorytransfer$setOMainHandHeight(1.0f);
	}

	private static List<Integer> computeTransferPlan(Inventory inventory, ItemStack hotbarStack) {
		int remaining = hotbarStack.getCount();
		int maxStackSize = hotbarStack.getMaxStackSize();

		int[] simulatedCounts = new int[36];
		for (int i = 9; i < 36; i++) {
			simulatedCounts[i] = inventory.getItem(i).getCount();
		}

		List<Integer> plan = new ArrayList<>();

		for (int i = 9; i < 36 && remaining > 0; i++) {
			ItemStack stackInSlot = inventory.getItem(i);
			if (stackInSlot.isEmpty()) continue;
			if (!ItemStack.isSameItemSameComponents(hotbarStack, stackInSlot)) continue;

			int room = maxStackSize - simulatedCounts[i];
			if (room <= 0) continue;

			int moved = Math.min(room, remaining);
			simulatedCounts[i] += moved;
			remaining -= moved;
			plan.add(i);
		}

		for (int i = 9; i < 36 && remaining > 0; i++) {
			if (!inventory.getItem(i).isEmpty()) continue;

			int moved = Math.min(maxStackSize, remaining);
			simulatedCounts[i] = moved;
			remaining -= moved;
			plan.add(i);
		}

		return remaining == 0 ? plan : null;
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
package com.armaninyow.hotbarinventorytransfer;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class InventoryFullOverlay {
	private static long messageStartTime = 0;
	private static final long DISPLAY_DURATION = 1500;
	private static final long FADE_DURATION = 500;
	private static final long TOTAL_DURATION = DISPLAY_DURATION + FADE_DURATION;

	public static void init() {
		HudElementRegistry.attachElementBefore(
			VanillaHudElements.CHAT,
			Identifier.fromNamespaceAndPath(HotbarInventoryTransfer.MOD_ID, "inventory_full_overlay"),
			InventoryFullOverlay::renderOverlay
		);
	}

	public static void showMessage() {
		messageStartTime = System.currentTimeMillis();
	}

	private static void renderOverlay(GuiGraphicsExtractor guiGraphics, DeltaTracker tickCounter) {
		if (messageStartTime == 0) return;

		long currentTime = System.currentTimeMillis();
		long elapsedTime = currentTime - messageStartTime;

		if (elapsedTime >= TOTAL_DURATION) {
			messageStartTime = 0;
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		Component message = Component.literal("Inventory full");
		int messageWidth = client.font.width(message);

		int screenWidth = guiGraphics.guiWidth();
		int screenHeight = guiGraphics.guiHeight();
		int x = (screenWidth - messageWidth) / 2;
		int y = screenHeight - 72;

		int alpha = 255;
		if (elapsedTime > DISPLAY_DURATION) {
			long fadeElapsed = elapsedTime - DISPLAY_DURATION;
			alpha = (int) (255 * (1.0f - ((float) fadeElapsed / FADE_DURATION)));
		}

		int color = (alpha << 24) | 0xFF5555;

		guiGraphics.text(client.font, message, x, y, color);
	}
}
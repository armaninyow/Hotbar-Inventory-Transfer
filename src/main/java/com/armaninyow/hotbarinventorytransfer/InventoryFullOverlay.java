package com.armaninyow.hotbarinventorytransfer;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class InventoryFullOverlay {
	private static long messageStartTime = 0;
	private static final long DISPLAY_DURATION = 1500; // 1.5 seconds
	private static final long FADE_DURATION = 500; // 0.5 seconds
	private static final long TOTAL_DURATION = DISPLAY_DURATION + FADE_DURATION; // 2 seconds total
	
	public static void init() {
		HudRenderCallback.EVENT.register(InventoryFullOverlay::renderOverlay);
	}
	
	public static void showMessage() {
		messageStartTime = System.currentTimeMillis();
	}
	
	private static void renderOverlay(DrawContext drawContext, RenderTickCounter tickCounter) {
		if (messageStartTime == 0) return;
		
		long currentTime = System.currentTimeMillis();
		long elapsedTime = currentTime - messageStartTime;
		
		if (elapsedTime >= TOTAL_DURATION) {
			messageStartTime = 0;
			return;
		}
		
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return;
		
		Text message = Text.literal("Inventory full!");
		int messageWidth = client.textRenderer.getWidth(message);
		
		// Position above hotbar, raised a few pixels to avoid overlapping the vanilla held item tooltip
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();
		int x = (screenWidth - messageWidth) / 2;
		int y = screenHeight - 68; // Raised 9px above previous position to clear the held item tooltip
		
		// Calculate alpha for fade out
		int alpha = 255;
		if (elapsedTime > DISPLAY_DURATION) {
			long fadeElapsed = elapsedTime - DISPLAY_DURATION;
			alpha = (int) (255 * (1.0f - ((float) fadeElapsed / FADE_DURATION)));
		}
		
		// Red color with alpha (0xRRGGBBAA format)
		int color = (alpha << 24) | 0xFF5555;
		
		// Draw text with shadow
		drawContext.drawTextWithShadow(client.textRenderer, message, x, y, color);
	}
}
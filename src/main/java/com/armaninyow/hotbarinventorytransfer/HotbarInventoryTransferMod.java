package com.armaninyow.hotbarinventorytransfer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotbarInventoryTransferMod implements ModInitializer {
    public static final String MOD_ID = "hotbarinventorytransfer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static KeyBinding swapKeyBinding;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Hotbar Inventory Transfer Mod");
        
        // Create custom category with Identifier
        Category customCategory = new Category(Identifier.of(MOD_ID, "hotbar_inventory_transfer"));
        
        // Register keybinding with custom category
        swapKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.hotbarinventorytransfer.swap",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R, // Default to R key
            customCategory
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
        int selectedSlot = inventory.getSelectedSlot();
        ItemStack hotbarStack = inventory.getStack(selectedSlot);
        
        if (hotbarStack.isEmpty()) return;
        
        int hotbarScreenSlot = selectedSlot + 36;
        int syncId = client.player.playerScreenHandler.syncId;
        
        // Pick up the entire stack from hotbar
        client.interactionManager.clickSlot(syncId, hotbarScreenSlot, 0, SlotActionType.PICKUP, client.player);
        
        // Try to distribute items across inventory
        // Repeat up to 10 times to handle overflow cases
        for (int attempt = 0; attempt < 10; attempt++) {
            // Small delay to let the server process
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            
            ItemStack cursorStack = client.player.currentScreenHandler.getCursorStack();
            if (cursorStack.isEmpty()) {
                break; // All items placed successfully
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
                LOGGER.debug("Inventory full, returned items to hotbar");
                break;
            }
            
            // Place items in the target slot
            client.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, client.player);
        }
        
        // Safety check: if items still in cursor, force return to hotbar
        ItemStack finalCursorStack = client.player.currentScreenHandler.getCursorStack();
        if (!finalCursorStack.isEmpty()) {
            client.interactionManager.clickSlot(syncId, hotbarScreenSlot, 0, SlotActionType.PICKUP, client.player);
            LOGGER.debug("Forced return of remaining items to hotbar");
        }
        
        LOGGER.debug("Completed item swap from hotbar slot {}", selectedSlot);
    }
}
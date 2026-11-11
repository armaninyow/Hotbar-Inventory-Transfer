package com.armaninyow.hotbarinventorytransfer.mixin;

import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FirstPersonHandsAndItems.class)
public interface FirstPersonHandsAndItemsAccessor {

	@Accessor("mainHandItem")
	void hotbarinventorytransfer$setMainHandItem(ItemStack stack);

	@Accessor("mainHandHeight")
	void hotbarinventorytransfer$setMainHandHeight(float height);

	@Accessor("oMainHandHeight")
	void hotbarinventorytransfer$setOMainHandHeight(float height);
}
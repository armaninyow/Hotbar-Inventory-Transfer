package com.armaninyow.hotbarinventorytransfer.mixin;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {

	@Accessor("mainHandItem")
	ItemStack hotbarinventorytransfer$getMainHandItem();

	@Accessor("mainHandItem")
	void hotbarinventorytransfer$setMainHandItem(ItemStack stack);

	@Accessor("mainHandHeight")
	float hotbarinventorytransfer$getMainHandHeight();

	@Accessor("mainHandHeight")
	void hotbarinventorytransfer$setMainHandHeight(float height);

	@Accessor("oMainHandHeight")
	void hotbarinventorytransfer$setOMainHandHeight(float height);
}
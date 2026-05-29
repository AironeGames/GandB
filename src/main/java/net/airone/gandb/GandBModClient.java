package net.airone.gandb;

import net.airone.gandb.EmptyLanternEntity;
import net.airone.gandb.GandBMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.airone.gandb.EmptyLantern;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.state.property.Property;

@Environment(value=EnvType.CLIENT)
public class GandBModClient
implements ClientModInitializer {
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(GandBMod.EMPTY_LANTERN, RenderLayer.getCutout());
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            int color = world != null && world.getBlockEntity(pos) != null && world.getBlockEntity(pos) instanceof EmptyLanternEntity && ((EmptyLanternEntity)world.getBlockEntity(pos)).getRenderAttachmentData() != null && (Boolean)state.get(EmptyLantern.FILLED) != false ? ((StatusEffect)((EmptyLanternEntity)world.getBlockEntity(pos)).getRenderAttachmentData()).getColor() : -1;
            return color;
        }, new Block[]{GandBMod.EMPTY_LANTERN});
    }
}


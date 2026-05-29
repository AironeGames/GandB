package net.airone.gandb;

import net.airone.gandb.EmptyLanternEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.airone.gandb.EmptyLantern;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GandBMod
implements ModInitializer {
    public static final EmptyLantern EMPTY_LANTERN = new EmptyLantern(AbstractBlock.Settings.create().strength(3.5f).sounds(BlockSoundGroup.LANTERN).luminance(state -> 15).requiresTool().nonOpaque());
    public static final BlockEntityType<EmptyLanternEntity> EMPTY_LANTERN_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of("glowandbehold", "empty_lantern_entity"), FabricBlockEntityTypeBuilder.create(EmptyLanternEntity::new, (Block[])new Block[]{EMPTY_LANTERN}).build());
    public static final String MOD_ID = "gandb";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        Registry.register(
            Registries.BLOCK,
            Identifier.of("glowandbehold", "empty_lantern"),
            EMPTY_LANTERN
        );

        Item EMPTY_LANTERN_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of("glowandbehold", "empty_lantern"),
            new BlockItem(EMPTY_LANTERN, new Item.Settings())
        );

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
            .register(content -> content.addAfter(Items.SOUL_LANTERN, EMPTY_LANTERN_ITEM));
    }
}


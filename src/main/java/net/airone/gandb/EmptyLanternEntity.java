package net.airone.gandb;

import net.airone.gandb.GandB;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.airone.gandb.EmptyLantern;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.Identifier;

public class EmptyLanternEntity
extends BlockEntity
implements RenderAttachmentBlockEntity {
    private StatusEffect effect;
    public List<PlayerEntity> oldlist = new ArrayList<PlayerEntity>();

    public void setEffect(StatusEffect effect) {
        this.effect = effect;
        this.markDirty();
    }

    public EmptyLanternEntity(BlockPos pos, BlockState state) {
        super(GandB.EMPTY_LANTERN_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        if (this.effect != null) {
            Identifier id = Registries.STATUS_EFFECT.getId(this.effect);

            if (id != null) {
                nbt.putString("effect", id.toString());
            }
        }
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        if (nbt.contains("effect")) {
            this.effect = Registries.STATUS_EFFECT.get(
                Identifier.of(nbt.getString("effect"))
            );
        } else {
            this.effect = null;
        }
    }

    public void markDirty() {
        super.markDirty();
    }

    public StatusEffect getEffect() {
        return this.effect;
    }

    @Nullable
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createNbt(registries);
    }

    public static void tick(World world, BlockPos pos, BlockState state, EmptyLanternEntity blockEntity) {
        block4: {
            BlockState bs;
            block3: {
                bs = world.getBlockState(pos);
                if (!((Boolean)bs.get(EmptyLantern.FILLED)).booleanValue()) break block3;
                Box box = new Box(pos).expand(10.0, 10.0, 10.0);
                List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, box);
                for (PlayerEntity player : blockEntity.oldlist) {
                    if (list.contains(player) || blockEntity.getEffect() == null) continue;
                    player.removeStatusEffect(Registries.STATUS_EFFECT.getEntry(blockEntity.getEffect()));
                }
                blockEntity.oldlist = world.getNonSpectatingEntities(PlayerEntity.class, box);
                for (PlayerEntity player : list) {
                    player.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(blockEntity.getEffect()), 40));
                }
                break block4;
            }
            if (!((Boolean)bs.get(EmptyLantern.WITH_MILK)).booleanValue()) break block4;
            Box box = new Box(pos).expand(10.0, 10.0, 10.0);
            List<PlayerEntity> list3 = world.getNonSpectatingEntities(PlayerEntity.class, box);
            for (PlayerEntity player : list3) {
                player.clearStatusEffects();
            }
        }
    }

    @Nullable
    public Object getRenderAttachmentData() {
        return this.effect;
    }
}


package net.airone.gandb.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={MinecraftClient.class})
public class ExampleClientMixin {
    @Inject(at={@At(value="HEAD")}, method={"run"})
    private void run(CallbackInfo info) {
    }
}


package net.featherpojav.mixin.client;

import net.featherpojav.client.FeatherPojavModClient;
import net.featherpojav.client.config.FeatherConfig;
import net.featherpojav.client.gui.FeatherHomeScreen;
import net.featherpojav.client.gui.FeatherSettingsScreen;
import net.featherpojav.client.gui.FeatherGameMenuScreen;
import net.featherpojav.client.gui.FeatherHudEditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if (FeatherPojavModClient.isZooming) {
            FeatherPojavModClient.targetZoomLevel = FeatherConfig.INSTANCE.zoomMagnification;
        } else {
            FeatherPojavModClient.targetZoomLevel = 1.0f;
        }

        // Smoothly interpolate zoom transition (20% step per frame)
        FeatherPojavModClient.zoomLevel += (FeatherPojavModClient.targetZoomLevel - FeatherPojavModClient.zoomLevel) * 0.2f;

        if (FeatherPojavModClient.zoomLevel > 1.01f) {
            cir.setReturnValue(cir.getReturnValue() / FeatherPojavModClient.zoomLevel);
        }
    }

    @Inject(method = "renderBlur", at = @At("HEAD"), cancellable = true)
    private void onRenderBlur(float delta, CallbackInfo ci) {
        if (this.client != null && this.client.currentScreen != null) {
            Screen s = this.client.currentScreen;
            if (s instanceof FeatherHomeScreen ||
                s instanceof FeatherSettingsScreen ||
                s instanceof FeatherGameMenuScreen ||
                s instanceof FeatherHudEditorScreen ||
                s.getClass().getName().startsWith("net.featherpojav.client.gui.")) {
                ci.cancel();
            }
        }
    }
}

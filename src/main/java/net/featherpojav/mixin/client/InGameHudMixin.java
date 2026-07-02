package net.featherpojav.mixin.client;

import net.featherpojav.client.config.FeatherConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (FeatherConfig.INSTANCE.customCrosshair) {
            FeatherConfig cfg = FeatherConfig.INSTANCE;
            int cx = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
            int cy = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2;
            
            float gap = cfg.crosshairGap;
            float size = cfg.crosshairSize;
            float th = cfg.crosshairThickness;
            int color = cfg.crosshairColor;
            int preset = cfg.crosshairPreset;
            
            // Draw custom Crosshair based on preset
            switch (preset) {
                case 1: // Dot
                    context.fill((int) (cx - th / 2.0f), (int) (cy - th / 2.0f), (int) (cx + th / 2.0f + 0.5f), (int) (cy + th / 2.0f + 0.5f), color);
                    break;
                case 2: // Circle
                case 3: // Circle with Dot
                    int radius = (int)(gap + size);
                    for (int angle = 0; angle < 360; angle += 15) {
                        double rad = Math.toRadians(angle);
                        int px = (int)(cx + Math.cos(rad) * radius);
                        int py = (int)(cy + Math.sin(rad) * radius);
                        context.fill((int)(px - th/2.0f), (int)(py - th/2.0f), (int)(px + th/2.0f + 0.5f), (int)(py + th/2.0f + 0.5f), color);
                    }
                    if (preset == 3) {
                        context.fill((int) (cx - th / 2.0f), (int) (cy - th / 2.0f), (int) (cx + th / 2.0f + 0.5f), (int) (cy + th / 2.0f + 0.5f), color);
                    }
                    break;
                case 4: // T-Shape
                    context.fill((int) (cx - gap - size), (int) (cy - th / 2.0f), (int) (cx - gap), (int) (cy + th / 2.0f + 0.5f), color);
                    context.fill((int) (cx + gap), (int) (cy - th / 2.0f), (int) (cx + gap + size), (int) (cy + th / 2.0f + 0.5f), color);
                    context.fill((int) (cx - th / 2.0f), (int) (cy + gap), (int) (cx + th / 2.0f + 0.5f), (int) (cy + gap + size), color);
                    break;
                case 5: // X-Shape
                    float offset = gap;
                    for (int idx = 0; idx < size; idx++) {
                        float f = offset + idx;
                        context.fill((int)(cx - f - th/2), (int)(cy - f - th/2), (int)(cx - f + th/2 + 0.5f), (int)(cy - f + th/2 + 0.5f), color);
                        context.fill((int)(cx + f - th/2), (int)(cy - f - th/2), (int)(cx + f + th/2 + 0.5f), (int)(cy - f + th/2 + 0.5f), color);
                        context.fill((int)(cx - f - th/2), (int)(cy + f - th/2), (int)(cx - f + th/2 + 0.5f), (int)(cy + f + th/2 + 0.5f), color);
                        context.fill((int)(cx + f - th/2), (int)(cy + f - th/2), (int)(cx + f + th/2 + 0.5f), (int)(cy + f + th/2 + 0.5f), color);
                    }
                    break;
                case 6: // Square
                case 9: // Box with Dot
                    int r = (int)(gap + size);
                    context.fill((int)(cx - r), (int)(cy - r - th/2), (int)(cx + r), (int)(cy - r + th/2 + 0.5f), color);
                    context.fill((int)(cx - r), (int)(cy + r - th/2), (int)(cx + r), (int)(cy + r + th/2 + 0.5f), color);
                    context.fill((int)(cx - r - th/2), (int)(cy - r), (int)(cx - r + th/2 + 0.5f), (int)(cy + r), color);
                    context.fill((int)(cx + r - th/2), (int)(cy - r), (int)(cx + r + th/2 + 0.5f), (int)(cy + r), color);
                    if (preset == 9) {
                        context.fill((int) (cx - th / 2.0f), (int) (cy - th / 2.0f), (int) (cx + th / 2.0f + 0.5f), (int) (cy + th / 2.0f + 0.5f), color);
                    }
                    break;
                case 7: // Arrow / Chevron
                    for (int idx = 0; idx < size; idx++) {
                        context.fill((int)(cx - idx - th/2), (int)(cy - gap + idx - th/2), (int)(cx - idx + th/2 + 0.5f), (int)(cy - gap + idx + th/2 + 0.5f), color);
                        context.fill((int)(cx + idx - th/2), (int)(cy - gap + idx - th/2), (int)(cx + idx + th/2 + 0.5f), (int)(cy - gap + idx + th/2 + 0.5f), color);
                    }
                    break;
                case 8: // Tri-Bar
                    context.fill((int) (cx - gap - size), (int) (cy - th / 2.0f), (int) (cx - gap), (int) (cy + th / 2.0f + 0.5f), color);
                    context.fill((int) (cx + gap), (int) (cy - th / 2.0f), (int) (cx + gap + size), (int) (cy + th / 2.0f + 0.5f), color);
                    context.fill((int) (cx - th / 2.0f), (int) (cy - gap - size), (int) (cx + th / 2.0f + 0.5f), (int) (cy - gap), color);
                    break;
                default: // 0: Classic Cross
                    context.fill((int) (cx - gap - size), (int) (cy - th / 2.0f), (int) (cx - gap), (int) (cy + th / 2.0f + 0.5f), color);
                    context.fill((int) (cx + gap), (int) (cy - th / 2.0f), (int) (cx + gap + size), (int) (cy + th / 2.0f + 0.5f), color);
                    context.fill((int) (cx - th / 2.0f), (int) (cy - gap - size), (int) (cx + th / 2.0f + 0.5f), (int) (cy - gap), color);
                    context.fill((int) (cx - th / 2.0f), (int) (cy + gap), (int) (cx + th / 2.0f + 0.5f), (int) (cy + gap + size), color);
                    break;
            }
            
            ci.cancel();
        }
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void onRenderScoreboardSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!FeatherConfig.INSTANCE.scoreboard) {
            ci.cancel();
        }
    }
}

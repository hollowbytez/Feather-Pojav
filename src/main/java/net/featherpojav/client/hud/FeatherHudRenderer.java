package net.featherpojav.client.hud;

import net.featherpojav.client.config.FeatherConfig;
import net.featherpojav.client.config.HollowHudConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FeatherHudRenderer {
    private static final List<Long> leftClicks = new ArrayList<>();
    private static final List<Long> rightClicks = new ArrayList<>();
    
    private static boolean lastLMB = false;
    private static boolean lastRMB = false;

    // PvP / Stats Tracking fields
    public static int comboCount = 0;
    public static long lastHitTime = 0;
    public static double lastReach = 0.0;
    
    private static final long sessionStartTime = System.currentTimeMillis();

    // Stopwatch states
    public static long stopwatchStart = 0;
    public static boolean stopwatchRunning = false;
    public static long stopwatchElapsed = 0;

    public static void addLeftClick() {
        leftClicks.add(System.currentTimeMillis());
    }

    public static void addRightClick() {
        rightClicks.add(System.currentTimeMillis());
    }

    public static int getLeftCPS() {
        long now = System.currentTimeMillis();
        leftClicks.removeIf(time -> now - time > 1000);
        return leftClicks.size();
    }

    public static int getRightCPS() {
        long now = System.currentTimeMillis();
        rightClicks.removeIf(time -> now - time > 1000);
        return rightClicks.size();
    }

    public static void onHit() {
        long now = System.currentTimeMillis();
        if (now - lastHitTime < 2000) {
            comboCount++;
        } else {
            comboCount = 1;
        }
        lastHitTime = now;
    }

    public static void onAttackEntity(net.minecraft.entity.Entity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && entity != null) {
            lastReach = client.player.distanceTo(entity);
        }
    }

    public static String getPlaytime() {
        long duration = System.currentTimeMillis() - sessionStartTime;
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        long hours = (duration / (1000 * 60 * 60));
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void toggleStopwatch() {
        if (stopwatchRunning) {
            stopwatchElapsed += System.currentTimeMillis() - stopwatchStart;
            stopwatchRunning = false;
        } else {
            stopwatchStart = System.currentTimeMillis();
            stopwatchRunning = true;
        }
    }

    public static String getStopwatchTime() {
        long elapsed = stopwatchElapsed;
        if (stopwatchRunning) {
            elapsed += System.currentTimeMillis() - stopwatchStart;
        }
        long seconds = (elapsed / 1000) % 60;
        long minutes = (elapsed / (1000 * 60)) % 60;
        long millis = (elapsed % 1000) / 10;
        return String.format("%02d:%02d.%02d", minutes, seconds, millis);
    }

    public static void updateCPS(MinecraftClient client) {
        boolean currentLMB = client.options.attackKey.isPressed();
        boolean currentRMB = client.options.useKey.isPressed();
        
        if (currentLMB && !lastLMB) {
            addLeftClick();
        }
        if (currentRMB && !lastRMB) {
            addRightClick();
        }
        
        lastLMB = currentLMB;
        lastRMB = currentRMB;
    }

    public static void renderHUD(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || client.currentScreen != null) return;
        
        TextRenderer tr = client.textRenderer;
        FeatherConfig cfg = FeatherConfig.INSTANCE;
        
        updateCPS(client);

        // 1. Keystrokes & CPS
        if (HollowHudConfig.get("Keystrokes").enabled) {
            int kx = (int) HollowHudConfig.get("Keystrokes").x;
            int ky = (int) HollowHudConfig.get("Keystrokes").y;
            float scale = HollowHudConfig.get("Keystrokes").scale;
            context.getMatrices().push();
            context.getMatrices().translate(kx, ky, 0);
            context.getMatrices().scale(scale, scale, 1f);
            kx = 0; ky = 0;
            
            boolean w = client.options.forwardKey.isPressed();
            boolean a = client.options.leftKey.isPressed();
            boolean s = client.options.backKey.isPressed();
            boolean d = client.options.rightKey.isPressed();
            boolean space = client.options.jumpKey.isPressed();
            boolean lmb = client.options.attackKey.isPressed();
            boolean rmb = client.options.useKey.isPressed();

            // WASD
            drawKey(context, tr, "W", kx + 20, ky, 18, 18, w);
            drawKey(context, tr, "A", kx, ky + 20, 18, 18, a);
            drawKey(context, tr, "S", kx + 20, ky + 20, 18, 18, s);
            drawKey(context, tr, "D", kx + 40, ky + 20, 18, 18, d);
            
            // Mouse Clicks / CPS
            drawKey(context, tr, getLeftCPS() + " CPS", kx, ky + 40, 29, 18, lmb);
            drawKey(context, tr, getRightCPS() + " CPS", kx + 31, ky + 40, 29, 18, rmb);
            
            // Spacebar
            drawKey(context, tr, "------", kx, ky + 60, 60, 10, space);
            context.getMatrices().pop();
        }

        // 2. Coordinates HUD
        if (HollowHudConfig.get("Coordinates").enabled) {
            int cx = (int) HollowHudConfig.get("Coordinates").x;
            int cy = (int) HollowHudConfig.get("Coordinates").y;
            float scale = HollowHudConfig.get("Coordinates").scale;
            context.getMatrices().push();
            context.getMatrices().translate(cx, cy, 0);
            context.getMatrices().scale(scale, scale, 1f);
            cx = 0; cy = 0;
            
            context.fill(cx, cy, cx + 120, cy + 26, 0x80000000);
            context.fill(cx, cy, cx + 2, cy + 26, cfg.themeColor);
            
            String coords = String.format("XYZ: %.1f / %.0f / %.1f", client.player.getX(), client.player.getY(), client.player.getZ());
            context.drawText(tr, coords, cx + 6, cy + 3, 0xFFFFFFFF, false);
            
            // Direction facing
            float yaw = client.player.getYaw();
            yaw = (yaw % 360 + 360) % 360;
            String direction = "S";
            if (yaw >= 315 || yaw < 45) direction = "S (+Z)";
            else if (yaw >= 45 && yaw < 135) direction = "W (-X)";
            else if (yaw >= 135 && yaw < 225) direction = "N (-Z)";
            else if (yaw >= 225 && yaw < 315) direction = "E (+X)";
            
            context.drawText(tr, "Facing: " + direction, cx + 6, cy + 14, 0xFF888888, false);
            context.getMatrices().pop();
        }

        // 3. FPS HUD
        if (HollowHudConfig.get("FPS HUD").enabled) {
            int fx = (int) HollowHudConfig.get("FPS HUD").x;
            int fy = (int) HollowHudConfig.get("FPS HUD").y;
            float scale = HollowHudConfig.get("FPS HUD").scale;
            context.getMatrices().push();
            context.getMatrices().translate(fx, fy, 0);
            context.getMatrices().scale(scale, scale, 1f);
            fx = 0; fy = 0;
            
            context.fill(fx, fy, fx + 50, fy + 14, 0x80000000);
            context.fill(fx, fy, fx + 2, fy + 14, cfg.themeColor);
            context.drawText(tr, client.getCurrentFps() + " FPS", fx + 6, fy + 3, 0xFFFFFFFF, false);
            context.getMatrices().pop();
        }

        // 4. Direction HUD / Compass Scale
        if (HollowHudConfig.get("Direction HUD").enabled) {
            int dx = (int) HollowHudConfig.get("Direction HUD").x;
            int dy = (int) HollowHudConfig.get("Direction HUD").y;
            float scale = HollowHudConfig.get("Direction HUD").scale;
            context.getMatrices().push();
            context.getMatrices().translate(dx, dy, 0);
            context.getMatrices().scale(scale, scale, 1f);
            dx = 0; dy = 0;
            int width = 160;
            int height = 18;
            
            context.fill(dx, dy, dx + width, dy + height, 0x80000000);
            context.drawBorder(dx, dy, width, height, 0x20FFFFFF);
            
            // Draw center marker line
            context.fill(dx + width / 2 - 1, dy, dx + width / 2 + 1, dy + height, cfg.themeColor);
            
            float playerYaw = client.player.getYaw();
            
            // Draw N, S, E, W
            String[] dirs = {"S", "W", "N", "E"};
            for (int j = 0; j < 4; j++) {
                float angle = j * 90;
                float diff = angle - playerYaw;
                diff = ((diff + 180) % 360 + 360) % 360 - 180;
                
                if (Math.abs(diff) <= 60) {
                    int posX = dx + width / 2 + (int) (diff * 1.3f);
                    context.drawCenteredTextWithShadow(tr, dirs[j], posX, dy + 2, cfg.themeColor);
                }
            }
            
            // Draw degree markings every 15 degrees
            for (int angle = 0; angle < 360; angle += 15) {
                if (angle % 90 == 0) continue;
                float diff = angle - playerYaw;
                diff = ((diff + 180) % 360 + 360) % 360 - 180;
                
                if (Math.abs(diff) <= 60) {
                    int posX = dx + width / 2 + (int) (diff * 1.3f);
                    context.drawCenteredTextWithShadow(tr, String.valueOf(angle), posX, dy + 4, 0xFF888888);
                }
            }
            context.getMatrices().pop();
        }

        // 5. Armor HUD & Armor Status
        if (HollowHudConfig.get("Armor HUD").enabled) {
            int ax = (int) HollowHudConfig.get("Armor HUD").x;
            int ay = (int) HollowHudConfig.get("Armor HUD").y;
            float scale = HollowHudConfig.get("Armor HUD").scale;
            context.getMatrices().push();
            context.getMatrices().translate(ax, ay, 0);
            context.getMatrices().scale(scale, scale, 1f);
            ax = 0; ay = 0;
            
            List<ItemStack> items = new ArrayList<>();
            items.add(client.player.getMainHandStack());
            for (int i = 3; i >= 0; i--) {
                items.add(client.player.getInventory().armor.get(i));
            }
            items.add(client.player.getOffHandStack());
            
            int offset = 0;
            for (ItemStack stack : items) {
                if (stack == null || stack.isEmpty()) continue;
                
                int itemX = ax + (cfg.armorHUDVertical ? 0 : offset);
                int itemY = ay + (cfg.armorHUDVertical ? offset : 0);
                
                context.drawItem(stack, itemX, itemY);
                context.drawItemInSlot(tr, stack, itemX, itemY);
                
                if (cfg.armorStatus && stack.isDamageable()) {
                    int maxDmg = stack.getMaxDamage();
                    int curDmg = maxDmg - stack.getDamage();
                    String durText = curDmg + "/" + maxDmg;
                    context.drawText(tr, durText, itemX + 18, itemY + 4, 0xFF88FF88, true);
                }
                
                offset += cfg.armorHUDVertical ? 18 : 36;
            }
            context.getMatrices().pop();
        }

        // 6. Potion HUD
        if (HollowHudConfig.get("Potion HUD").enabled) {
            int px = (int) HollowHudConfig.get("Potion HUD").x;
            int py = (int) HollowHudConfig.get("Potion HUD").y;
            float scale = HollowHudConfig.get("Potion HUD").scale;
            context.getMatrices().push();
            context.getMatrices().translate(px, py, 0);
            context.getMatrices().scale(scale, scale, 1f);
            px = 0; py = 0;
            
            Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
            int offsetY = 0;
            
            for (StatusEffectInstance effect : effects) {
                String effectName = Text.translatable(effect.getEffectType().value().getTranslationKey()).getString();
                int amplifier = effect.getAmplifier();
                if (amplifier > 0) {
                    effectName += " " + (amplifier + 1);
                }
                
                int duration = effect.getDuration();
                String durationStr = "Permanent";
                if (!effect.isInfinite()) {
                    int seconds = (duration / 20) % 60;
                    int minutes = (duration / 1200);
                    durationStr = String.format("%d:%02d", minutes, seconds);
                }
                
                String fullText = effectName + " (" + durationStr + ")";
                
                context.fill(px, py + offsetY, px + 110, py + offsetY + 14, 0x80000000);
                context.fill(px, py + offsetY, px + 2, py + offsetY + 14, cfg.themeColor);
                
                context.drawText(tr, fullText, px + 6, py + offsetY + 3, 0xFFFFFFFF, false);
                offsetY += 16;
            }
            context.getMatrices().pop();
        }

        // 6a. Armor Bar
        if (HollowHudConfig.get("Armor Bar").enabled) {
            int armorValue = client.player.getArmor();
            if (armorValue > 0) {
                int barX = client.getWindow().getScaledWidth() / 2 - 91;
                int barY = client.getWindow().getScaledHeight() - 49;
                context.drawText(tr, "🛡 " + armorValue, barX, barY, 0xFF5C6BC0, true);
            }
        }

        // 6b. Hearts Multiplier
        if (cfg.hearts) {
            int hp = (int) client.player.getHealth();
            int maxHp = (int) client.player.getMaxHealth();
            int abs = (int) client.player.getAbsorptionAmount();
            String hpText = "❤ " + hp + "/" + maxHp;
            if (abs > 0) hpText += " + " + abs + "★";
            int barX = client.getWindow().getScaledWidth() / 2 - 91;
            int barY = client.getWindow().getScaledHeight() - 40;
            context.drawText(tr, hpText, barX, barY, 0xFFE57373, true);
        }

        // 6c. Pack Display
        if (cfg.packDisplay) {
            int px = cfg.packDisplayX;
            int py = cfg.packDisplayY;
            String packName = "Default";
            if (client.getResourcePackManager() != null) {
                var enabledPacks = client.getResourcePackManager().getEnabledProfiles();
                if (!enabledPacks.isEmpty()) {
                    packName = enabledPacks.iterator().next().getDisplayName().getString();
                }
            }
            context.fill(px, py, px + 110, py + 14, 0x80000000);
            context.fill(px, py, px + 2, py + 14, cfg.themeColor);
            context.drawText(tr, "Pack: " + packName, px + 6, py + 3, 0xFFFFFFFF, false);
        }

        // 7. Combo Display
        if (cfg.comboDisplay) {
            int cx = cfg.comboDisplayX;
            int cy = cfg.comboDisplayY;
            context.fill(cx, cy, cx + 65, cy + 14, 0x80000000);
            context.fill(cx, cy, cx + 2, cy + 14, cfg.themeColor);
            context.drawText(tr, "Combo: " + comboCount, cx + 6, cy + 3, 0xFFFFFFFF, false);
        }

        // 8. Ping Display
        if (cfg.pingDisplay) {
            int px = cfg.pingDisplayX;
            int py = cfg.pingDisplayY;
            int ping = 0;
            if (client.getNetworkHandler() != null && client.player != null) {
                var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
                if (entry != null) {
                    ping = entry.getLatency();
                }
            }
            context.fill(px, py, px + 60, py + 14, 0x80000000);
            context.fill(px, py, px + 2, py + 14, cfg.themeColor);
            context.drawText(tr, "Ping: " + ping + "ms", px + 6, py + 3, 0xFFFFFFFF, false);
        }

        // 9. Playtime
        if (cfg.playtime) {
            int px = cfg.playtimeX;
            int py = cfg.playtimeY;
            context.fill(px, py, px + 100, py + 14, 0x80000000);
            context.fill(px, py, px + 2, py + 14, cfg.themeColor);
            context.drawText(tr, "Time: " + getPlaytime(), px + 6, py + 3, 0xFFFFFFFF, false);
        }

        // 10. Reach Display
        if (cfg.reachDisplay) {
            int rx = cfg.reachDisplayX;
            int ry = cfg.reachDisplayY;
            context.fill(rx, ry, rx + 70, ry + 14, 0x80000000);
            context.fill(rx, ry, rx + 2, ry + 14, cfg.themeColor);
            context.drawText(tr, String.format("Reach: %.2fm", lastReach), rx + 6, ry + 3, 0xFFFFFFFF, false);
        }

        // 11. Server Address
        if (cfg.serverAddress) {
            int sx = cfg.serverAddressX;
            int sy = cfg.serverAddressY;
            String addr = "Singleplayer";
            if (client.getCurrentServerEntry() != null) {
                addr = client.getCurrentServerEntry().address;
            }
            context.fill(sx, sy, sx + 110, sy + 14, 0x80000000);
            context.fill(sx, sy, sx + 2, sy + 14, cfg.themeColor);
            context.drawText(tr, "IP: " + addr, sx + 6, sy + 3, 0xFFFFFFFF, false);
        }

        // 12. Speed Meter
        if (cfg.speedMeter) {
            int sx = cfg.speedMeterX;
            int sy = cfg.speedMeterY;
            double xDiff = client.player.getX() - client.player.prevX;
            double zDiff = client.player.getZ() - client.player.prevZ;
            double speed = Math.sqrt(xDiff * xDiff + zDiff * zDiff) * 20.0;
            context.fill(sx, sy, sx + 85, sy + 14, 0x80000000);
            context.fill(sx, sy, sx + 2, sy + 14, cfg.themeColor);
            context.drawText(tr, String.format("Speed: %.2f m/s", speed), sx + 6, sy + 3, 0xFFFFFFFF, false);
        }

        // 13. Stopwatch
        if (cfg.stopwatch) {
            int sx = cfg.stopwatchX;
            int sy = cfg.stopwatchY;
            context.fill(sx, sy, sx + 95, sy + 14, 0x80000000);
            context.fill(sx, sy, sx + 2, sy + 14, cfg.themeColor);
            context.drawText(tr, "Timer: " + getStopwatchTime(), sx + 6, sy + 3, 0xFFFFFFFF, false);
        }

        // 14. Item Counter
        if (cfg.itemCounter) {
            int ix = cfg.itemCounterX;
            int iy = cfg.itemCounterY;
            int count = 0;
            ItemStack held = client.player.getMainHandStack();
            if (!held.isEmpty()) {
                for (int i = 0; i < client.player.getInventory().size(); i++) {
                    ItemStack stack = client.player.getInventory().getStack(i);
                    if (stack.getItem() == held.getItem()) {
                        count += stack.getCount();
                    }
                }
            }
            context.fill(ix, iy, ix + 65, iy + 14, 0x80000000);
            context.fill(ix, iy, ix + 2, iy + 14, cfg.themeColor);
            context.drawText(tr, "Held: " + count, ix + 6, iy + 3, 0xFFFFFFFF, false);
        }

        // 15. Target HUD (Damage Indicator)
        if (cfg.damageIndicator) {
            net.minecraft.entity.Entity targeted = client.targetedEntity;
            boolean isPreview = client.currentScreen instanceof net.featherpojav.client.gui.FeatherHudEditorScreen;
            
            if (targeted instanceof net.minecraft.entity.LivingEntity || isPreview) {
                int tx = cfg.targetHudX;
                int ty = cfg.targetHudY;
                int tw = 120;
                int th = 36;
                
                String name = isPreview ? "Target Preview" : targeted.getName().getString();
                float health = isPreview ? 14.5f : ((net.minecraft.entity.LivingEntity) targeted).getHealth();
                float maxHealth = isPreview ? 20.0f : ((net.minecraft.entity.LivingEntity) targeted).getMaxHealth();
                
                // Draw translucent target card container
                context.fill(tx, ty, tx + tw, ty + th, 0x80000000);
                context.fill(tx, ty, tx + 2, ty + th, cfg.themeColor);
                
                // Entity name text
                context.drawText(tr, name, tx + 6, ty + 4, 0xFFFFFFFF, false);
                
                // Red/dark health bar background
                int barWidth = tw - 12;
                int barHeight = 4;
                int fillWidth = (int) (barWidth * (health / maxHealth));
                fillWidth = Math.max(0, Math.min(barWidth, fillWidth));
                
                context.fill(tx + 6, ty + 16, tx + 6 + barWidth, ty + 16 + barHeight, 0xFF444444);
                context.fill(tx + 6, ty + 16, tx + 6 + fillWidth, ty + 16 + barHeight, 0xFFFF2222);
                
                // Health value text
                String hpText = String.format("HP: %.1f / %.0f", health, maxHealth);
                context.drawText(tr, hpText, tx + 6, ty + 23, 0xFFBA68C8, false);
            }
        }

        // 16. Totem Counter
        if (cfg.totemCounter) {
            int tx = cfg.totemCounterX;
            int ty = cfg.totemCounterY;
            int totems = 0;
            if (client.player != null) {
                for (int i = 0; i < client.player.getInventory().size(); i++) {
                    ItemStack stack = client.player.getInventory().getStack(i);
                    if (stack.getItem() == net.minecraft.item.Items.TOTEM_OF_UNDYING) {
                        totems += stack.getCount();
                    }
                }
                if (client.player.getOffHandStack().getItem() == net.minecraft.item.Items.TOTEM_OF_UNDYING) {
                    totems += client.player.getOffHandStack().getCount();
                }
            }
            context.fill(tx, ty, tx + 65, ty + 14, 0x80000000);
            context.fill(tx, ty, tx + 2, ty + 14, cfg.themeColor);
            context.drawText(tr, "Totems: " + totems, tx + 6, ty + 3, 0xFFFFFFFF, false);
        }

        // 17. Saturation HUD
        if (cfg.saturationHUD) {
            int sx = cfg.saturationHUDX;
            int sy = cfg.saturationHUDY;
            float saturation = 0.0f;
            if (client.player != null) {
                saturation = client.player.getHungerManager().getSaturationLevel();
            }
            context.fill(sx, sy, sx + 80, sy + 14, 0x80000000);
            context.fill(sx, sy, sx + 2, sy + 14, cfg.themeColor);
            context.drawText(tr, String.format("Sat: %.1f", saturation), sx + 6, sy + 3, 0xFFFFFFFF, false);
        }
    }

    private static void drawKey(DrawContext context, TextRenderer tr, String label, int x, int y, int w, int h, boolean pressed) {
        int bg = pressed ? FeatherConfig.INSTANCE.themeColor : 0x80000000;
        int textCol = pressed ? 0xFFFFFFFF : 0xFFCCCCCC;
        
        context.fill(x, y, x + w, y + h, bg);
        context.drawBorder(x, y, w, h, pressed ? 0xFFBA68C8 : 0x40FFFFFF);
        context.drawCenteredTextWithShadow(tr, label, x + w / 2, y + (h - 8) / 2, textCol);
    }
}

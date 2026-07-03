package net.featherpojav.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FeatherHomeScreen extends Screen {
    public static final CubeMapRenderer CUSTOM_PANORAMA = new CubeMapRenderer(Identifier.of("featherpojav", "textures/background/panorama"));
    private final RotatingCubeMapRenderer panoramaRenderer = new RotatingCubeMapRenderer(CUSTOM_PANORAMA);

    private final List<MenuButton> centerButtons = new ArrayList<>();
    private final List<IconWidget> topIcons = new ArrayList<>();
    private final List<ServerShortcut> serverShortcuts = new ArrayList<>();

    public FeatherHomeScreen() {
        super(Text.of("Hollow Home Screen"));
    }

    private static class MenuButton {
        String label;
        boolean isAccent;
        Runnable onClick;
        MenuButton(String label, boolean isAccent, Runnable onClick) {
            this.label = label;
            this.isAccent = isAccent;
            this.onClick = onClick;
        }
    }

    private static class IconWidget {
        String icon;
        Runnable onClick;
        IconWidget(String icon, Runnable onClick) { this.icon = icon; this.onClick = onClick; }
    }

    private static class ServerShortcut {
        String ip;
        String name;
        String initial;
        ServerShortcut(String name, String ip, String initial) { this.name = name; this.ip = ip; this.initial = initial; }
    }

    // Helper for rounded corners
    private void fillRounded(DrawContext ctx, int x, int y, int w, int h, int color, int r) {
        if (r < 2) { ctx.fill(x, y, x + w, y + h, color); return; }
        ctx.fill(x + r, y, x + w - r, y + h, color);
        ctx.fill(x, y + r, x + r, y + h - r, color);
        ctx.fill(x + w - r, y + r, x + w, y + h - r, color);
        ctx.fill(x + 1, y + 1, x + 2, y + 2, color);
        ctx.fill(x + w - 2, y + 1, x + w - 1, y + 2, color);
        ctx.fill(x + 1, y + h - 2, x + 2, y + h - 1, color);
        ctx.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, color);
    }

    @Override
    protected void init() {
        centerButtons.clear();
        topIcons.clear();
        serverShortcuts.clear();

        // Central Buttons
        centerButtons.add(new MenuButton("Singleplayer", false, () -> { if (this.client != null) this.client.setScreen(new SelectWorldScreen(this)); }));
        centerButtons.add(new MenuButton("Multiplayer", false, () -> { if (this.client != null) this.client.setScreen(new MultiplayerScreen(this)); }));
        centerButtons.add(new MenuButton("Cosmetics", false, () -> { Util.getOperatingSystem().open("https://github.com/hollowbytez"); }));
        centerButtons.add(new MenuButton("Screenshots", false, () -> { 
            if (this.client != null) {
                File dir = new File(this.client.runDirectory, "screenshots");
                if (!dir.exists()) dir.mkdirs();
                Util.getOperatingSystem().open(dir);
            }
        }));
        centerButtons.add(new MenuButton("Hollow Settings", true, () -> { if (this.client != null) this.client.setScreen(new FeatherSettingsScreen(this)); }));

        // Top Right Actions
        topIcons.add(new IconWidget("⚙", () -> { if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options)); }));
        topIcons.add(new IconWidget("👕", () -> { if (this.client != null) this.client.setScreen(new SkinOptionsScreen(this, this.client.options)); }));
        topIcons.add(new IconWidget("🌐", () -> { if (this.client != null) this.client.setScreen(new LanguageOptionsScreen(this, this.client.options, this.client.getLanguageManager())); }));
        
        // Left Sidebar Servers
        serverShortcuts.add(new ServerShortcut("PikaNetwork", "play.pikanetwork.net", "P"));
        serverShortcuts.add(new ServerShortcut("Jartex", "play.jartexnetwork.com", "J"));
        serverShortcuts.add(new ServerShortcut("BlocksMC", "blocksmc.com", "B"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        
        this.panoramaRenderer.render(context, this.width, this.height, 1.0f, delta);
        
        // Deep modern dark fade to remove white wash and make it look clean
        context.fillGradient(0, 0, this.width, this.height, 0x90000000, 0xD0000000);

        // --- Render Top Right Profile Box ---
        if (this.client != null && this.client.getSession() != null) {
            int rightX = this.width - 200;
            int topY = 20;

            // Profile rounded container
            fillRounded(context, rightX, topY, 110, 36, 0xD9141416, 6);
            
            try {
                SkinTextures skinTextures = this.client.getSkinProvider().getSkinTextures(this.client.getGameProfile());
                Identifier skinId = skinTextures.texture();
                context.drawTexture(skinId, rightX + 8, topY + 6, 24, 24, 8.0f, 8.0f, 8, 8, 64, 64);
                context.drawTexture(skinId, rightX + 8, topY + 6, 24, 24, 40.0f, 8.0f, 8, 8, 64, 64);
            } catch (Exception ignored) {}

            String username = this.client.getSession().getUsername();
            if (username.length() > 10) username = username.substring(0, 9) + "..";
            context.drawText(this.textRenderer, username.toUpperCase(), rightX + 40, topY + 14, 0xFFFFFFFF, false);

            // Action Icons
            int iconX = rightX + 118;
            for (IconWidget iconBtn : topIcons) {
                boolean hovered = mouseX >= iconX && mouseX <= iconX + 36 && mouseY >= topY && mouseY <= topY + 36;
                fillRounded(context, iconX, topY, 36, 36, hovered ? 0xD92A2A2E : 0xD9141416, 6);
                context.drawCenteredTextWithShadow(this.textRenderer, iconBtn.icon, iconX + 18, topY + 14, 0xFFFFFFFF);
                iconX += 42;
            }
        }

        // --- Render Left Server Sidebar ---
        int sideY = this.height / 2 - (serverShortcuts.size() * 50) / 2;
        int sideX = 20;
        for (ServerShortcut sc : serverShortcuts) {
            boolean hov = mouseX >= sideX && mouseX <= sideX + 40 && mouseY >= sideY && mouseY <= sideY + 40;
            fillRounded(context, sideX, sideY, 40, 40, hov ? 0xFF2A2A2E : 0xD9141416, 8); // Rounded square
            context.drawCenteredTextWithShadow(this.textRenderer, sc.initial, sideX + 20, sideY + 16, 0xFFFFFFFF);
            
            // Green online dot
            fillRounded(context, sideX + 32, sideY + 32, 8, 8, 0xFF22C55E, 4);
            
            if (hov) {
                context.drawText(this.textRenderer, sc.name, sideX + 50, sideY + 16, 0xFFFFFFFF, true);
            }
            sideY += 50;
        }

        // --- Render Center Custom Font Title ---
        int centerY = this.height / 2 - 80;
        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2f, centerY - 60, 0);
        context.getMatrices().scale(3.0f, 3.0f, 1.0f);
        Text title = Text.literal("HOLLOW CLIENT").setStyle(Style.EMPTY.withFont(Identifier.of("featherpojav", "eternalo")));
        int tw = this.textRenderer.getWidth(title);
        context.drawText(this.textRenderer, title, -tw / 2, 0, 0xFFFFFFFF, true);
        context.getMatrices().pop();

        // --- Render Main Center Buttons ---
        int buttonY = centerY;
        int buttonWidth = 200;
        int buttonHeight = 30;
        int leftX = this.width / 2 - buttonWidth / 2;

        for (MenuButton btn : centerButtons) {
            boolean hovered = mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
            
            int bg = btn.isAccent ? (hovered ? 0xFFE53935 : 0xFFEB4040) : (hovered ? 0xD92A2A2E : 0xD9141416);
            fillRounded(context, leftX, buttonY, buttonWidth, buttonHeight, bg, 6);
            context.drawCenteredTextWithShadow(this.textRenderer, btn.label, this.width / 2, buttonY + 11, 0xFFFFFFFF);
            
            buttonY += 36;
        }

        // Quit Button (Clean text only at bottom)
        int quitY = this.height - 30;
        boolean quitHovered = mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= quitY && mouseY <= quitY + 20;
        context.drawCenteredTextWithShadow(this.textRenderer, "Quit Game", this.width / 2, quitY, quitHovered ? 0xFFFF5555 : 0xFFAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Quit Button
        int buttonWidth = 200;
        int leftX = this.width / 2 - buttonWidth / 2;
        int quitY = this.height - 30;
        if (mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= quitY && mouseY <= quitY + 20) {
            if (this.client != null) this.client.scheduleStop();
            return true;
        }

        // Center Buttons
        int centerY = this.height / 2 - 80;
        int buttonY = centerY;
        for (MenuButton btn : centerButtons) {
            if (mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + 30) {
                btn.onClick.run();
                return true;
            }
            buttonY += 36;
        }

        // Top Right Icons
        if (this.client != null && this.client.getSession() != null) {
            int rightX = this.width - 200;
            int topY = 20;
            int iconX = rightX + 118;
            for (IconWidget iconBtn : topIcons) {
                if (mouseX >= iconX && mouseX <= iconX + 36 && mouseY >= topY && mouseY <= topY + 36) {
                    iconBtn.onClick.run();
                    return true;
                }
                iconX += 42;
            }
        }

        // Left Sidebar Servers
        int sideY = this.height / 2 - (serverShortcuts.size() * 50) / 2;
        int sideX = 20;
        for (ServerShortcut sc : serverShortcuts) {
            if (mouseX >= sideX && mouseX <= sideX + 40 && mouseY >= sideY && mouseY <= sideY + 40) {
                ServerInfo info = new ServerInfo(sc.name, sc.ip, ServerInfo.ServerType.OTHER);
                ConnectScreen.connect(this, this.client, ServerAddress.parse(info.address), info, false, null);
                return true;
            }
            sideY += 50;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}

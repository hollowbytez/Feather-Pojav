package net.featherpojav.client.gui;

import net.featherpojav.client.config.FeatherConfig;
import net.featherpojav.client.FeatherPojavModClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class FeatherSettingsScreen extends Screen {
    private final Screen parent;
    private Category currentCategory = Category.ALL;
    private final List<ModCard> cards = new ArrayList<>();
    private TextFieldWidget searchField;
    
    // Geometry
    private int boxWidth = 340;
    private int boxHeight = 240;
    private int boxX = 0;
    private int boxY = 0;
    
    // Scroll state
    private double scrollY = 0;
    
    public FeatherSettingsScreen(Screen parent) {
        super(Text.of("Feather Mod Menu"));
        this.parent = parent;
    }
    
    enum Category {
        ALL("All"),
        PVP("PvP"),
        HUD("HUD"),
        NEW("New");
        
        final String name;
        Category(String name) { this.name = name; }
    }
    
    private static class ModCard {
        String name;
        String icon;
        java.util.function.BooleanSupplier getter;
        java.util.function.Consumer<Boolean> setter;
        Category category;
        Runnable onConfigure;

        ModCard(String name, String icon, Category category, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
            this.name = name;
            this.icon = icon;
            this.category = category;
            this.getter = getter;
            this.setter = setter;
        }

        ModCard withConfig(Runnable onConfigure) {
            this.onConfigure = onConfigure;
            return this;
        }
    }
    
    @Override
    protected void init() {
        boxWidth = Math.min(340, this.width - 20);
        boxHeight = Math.min(240, this.height - 20);
        boxX = this.width / 2 - boxWidth / 2;
        boxY = this.height / 2 - boxHeight / 2;

        int searchX = boxX + boxWidth - 120;
        int searchY = boxY + 8;
        int searchW = 100;
        int searchH = 12;
        this.searchField = new TextFieldWidget(this.textRenderer, searchX, searchY, searchW, searchH, Text.of("Search"));
        this.searchField.setMaxLength(30);
        this.searchField.setDrawsBackground(false);
        this.searchField.setPlaceholder(Text.of("Search..."));
        this.searchField.setEditableColor(0xFFFFFFFF);
        this.searchField.setUneditableColor(0xFF888888);
        this.addSelectableChild(this.searchField);

        cards.clear();
        FeatherConfig cfg = FeatherConfig.INSTANCE;
        
        // --- HUD Category ---
        cards.add(new ModCard("Armor Bar", "🛡", Category.HUD, () -> cfg.armorBar, (v) -> cfg.armorBar = v));
        cards.add(new ModCard("Armor HUD", "🛡", Category.HUD, () -> cfg.armorHUD, (v) -> cfg.armorHUD = v));
        cards.add(new ModCard("Armor Status", "🛡", Category.HUD, () -> cfg.armorStatus, (v) -> cfg.armorStatus = v));
        cards.add(new ModCard("Boss Bar", "👿", Category.HUD, () -> cfg.bossBar, (v) -> cfg.bossBar = v));
        cards.add(new ModCard("Combo Display", "⚔", Category.HUD, () -> cfg.comboDisplay, (v) -> cfg.comboDisplay = v));
        cards.add(new ModCard("Coordinates", "📍", Category.HUD, () -> cfg.coordHUD, (v) -> cfg.coordHUD = v));
        cards.add(new ModCard("Damage Indicator", "💔", Category.HUD, () -> cfg.damageIndicator, (v) -> cfg.damageIndicator = v));
        cards.add(new ModCard("Direction HUD", "🧭", Category.HUD, () -> cfg.directionHUD, (v) -> cfg.directionHUD = v));
        cards.add(new ModCard("FPS HUD", "📊", Category.HUD, () -> cfg.fpsHUD, (v) -> cfg.fpsHUD = v));
        cards.add(new ModCard("Hearts", "❤", Category.HUD, () -> cfg.hearts, (v) -> cfg.hearts = v));
        cards.add(new ModCard("Item Counter", "📦", Category.HUD, () -> cfg.itemCounter, (v) -> cfg.itemCounter = v));
        cards.add(new ModCard("Keystrokes", "⌨", Category.HUD, () -> cfg.keystrokes, (v) -> cfg.keystrokes = v));
        cards.add(new ModCard("Pack Display", "🗂", Category.HUD, () -> cfg.packDisplay, (v) -> cfg.packDisplay = v));
        cards.add(new ModCard("Ping Display", "📶", Category.HUD, () -> cfg.pingDisplay, (v) -> cfg.pingDisplay = v));
        cards.add(new ModCard("Playtime", "⏳", Category.HUD, () -> cfg.playtime, (v) -> cfg.playtime = v));
        cards.add(new ModCard("Potion HUD", "🧪", Category.HUD, () -> cfg.potionHUD, (v) -> cfg.potionHUD = v));
        cards.add(new ModCard("Reach Display", "📏", Category.HUD, () -> cfg.reachDisplay, (v) -> cfg.reachDisplay = v));
        cards.add(new ModCard("Saturation HUD", "🥩", Category.HUD, () -> cfg.saturationHUD, (v) -> cfg.saturationHUD = v));
        cards.add(new ModCard("Scoreboard", "📋", Category.HUD, () -> cfg.scoreboard, (v) -> cfg.scoreboard = v));
        cards.add(new ModCard("Server IP", "🌐", Category.HUD, () -> cfg.serverAddress, (v) -> cfg.serverAddress = v));
        cards.add(new ModCard("Speed Meter", "👟", Category.HUD, () -> cfg.speedMeter, (v) -> cfg.speedMeter = v));
        cards.add(new ModCard("Stopwatch", "⏱", Category.HUD, () -> cfg.stopwatch, (v) -> cfg.stopwatch = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new FeatherKeybindSettingScreen(this, "Stopwatch", FeatherPojavModClient.stopwatchKey));
            }));
        cards.add(new ModCard("Totem Counter", "🪶", Category.HUD, () -> cfg.totemCounter, (v) -> cfg.totemCounter = v));

        // --- PvP Category ---
        cards.add(new ModCard("AutoGG", "🗣", Category.PVP, () -> cfg.autoGG, (v) -> cfg.autoGG = v));
        cards.add(new ModCard("Crystal Optimizer", "💎", Category.PVP, () -> cfg.crystalOptimizer, (v) -> cfg.crystalOptimizer = v));
        cards.add(new ModCard("Freelook", "👁", Category.PVP, () -> cfg.freelook, (v) -> cfg.freelook = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new FeatherKeybindSettingScreen(this, "Freelook", FeatherPojavModClient.freelookKey));
            }));
        cards.add(new ModCard("Hitbox Outlines", "📦", Category.PVP, () -> cfg.hitbox, (v) -> cfg.hitbox = v));
        cards.add(new ModCard("Hurt Cam", "🤕", Category.PVP, () -> cfg.hurtCam, (v) -> cfg.hurtCam = v));
        cards.add(new ModCard("Low Fire", "🔥", Category.PVP, () -> cfg.lowFire, (v) -> cfg.lowFire = v));
        cards.add(new ModCard("ToggleSprint", "🏃", Category.PVP, () -> cfg.toggleSprint, (v) -> cfg.toggleSprint = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new FeatherKeybindSettingScreen(this, "ToggleSprint", FeatherPojavModClient.toggleSprintKey));
            }));
        cards.add(new ModCard("Zoom", "🔍", Category.PVP, () -> cfg.zoom, (v) -> cfg.zoom = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new FeatherZoomScreen(this));
            }));

        // --- New/Gameplay Category ---
        cards.add(new ModCard("Auto Text", "✍", Category.NEW, () -> cfg.autoText, (v) -> cfg.autoText = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new FeatherAutoTextScreen(this));
            }));
        cards.add(new ModCard("Cull Logs", "🧹", Category.NEW, () -> cfg.cullLogs, (v) -> cfg.cullLogs = v));
        cards.add(new ModCard("Custom Crosshair", "⌖", Category.NEW, () -> cfg.customCrosshair, (v) -> cfg.customCrosshair = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new FeatherCrosshairScreen(this));
            }));
        cards.add(new ModCard("Drop Prevention", "🔒", Category.NEW, () -> cfg.dropPrevention, (v) -> cfg.dropPrevention = v));
        cards.add(new ModCard("Fullbright", "💡", Category.NEW, () -> cfg.fullbright, (v) -> cfg.fullbright = v));
        cards.add(new ModCard("Item Physics", "🌍", Category.NEW, () -> cfg.itemPhysics, (v) -> cfg.itemPhysics = v));
        cards.add(new ModCard("Nick Hider", "👤", Category.NEW, () -> cfg.nickHider, (v) -> cfg.nickHider = v));
        cards.add(new ModCard("TimeChanger", "☀️", Category.NEW, () -> cfg.timeChanger, (v) -> cfg.timeChanger = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new FeatherTimeChangerScreen(this));
            }));
    }

    private List<ModCard> getFilteredCards() {
        String query = this.searchField != null ? this.searchField.getText().toLowerCase().trim() : "";
        List<ModCard> list = new ArrayList<>();
        for (ModCard c : cards) {
            boolean matchesCategory = (currentCategory == Category.ALL || c.category == currentCategory);
            boolean matchesSearch = query.isEmpty() || c.name.toLowerCase().contains(query);
            if (matchesCategory && matchesSearch) {
                list.add(c);
            }
        }
        list.sort(java.util.Comparator.comparing(c -> c.name));
        return list;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= boxX && mouseX <= boxX + boxWidth && mouseY >= boxY + 52 && mouseY <= boxY + boxHeight) {
            int itemsCount = getFilteredCards().size();
            int rowsCount = (itemsCount + 1) / 2;
            int totalHeight = rowsCount * 30;
            double maxScroll = Math.max(0, totalHeight - (boxHeight - 60));
            scrollY -= verticalAmount * 18;
            if (scrollY < 0) scrollY = 0;
            if (scrollY > maxScroll) scrollY = maxScroll;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Subtle darkened background
        context.fill(0, 0, this.width, this.height, 0x70000000);

        // --- Main container with subtle border ---
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xF0111113);
        // Top accent line
        context.fill(boxX, boxY, boxX + boxWidth, boxY + 1, 0xFF9C27B0);

        // --- Header Row: Title + Search + Close ---
        int headerY = boxY + 6;
        context.drawText(this.textRenderer, "⚙ Mods", boxX + 10, headerY + 2, 0xFFE0E0E0, false);

        // Search field
        if (this.searchField != null) {
            int sX = this.searchField.getX();
            int sY = this.searchField.getY();
            int sW = this.searchField.getWidth();
            int sH = this.searchField.getHeight();
            context.fill(sX - 2, sY - 2, sX + sW + 2, sY + sH + 2, 0xFF1A1A1C);
            context.drawBorder(sX - 2, sY - 2, sW + 4, sH + 4, this.searchField.isFocused() ? 0xFF9C27B0 : 0xFF2A2A2E);
            this.searchField.render(context, mouseX, mouseY, delta);
        }

        // Close button
        boolean closeHovered = mouseX >= boxX + boxWidth - 18 && mouseX <= boxX + boxWidth - 4 && mouseY >= headerY && mouseY <= headerY + 14;
        context.drawText(this.textRenderer, "✕", boxX + boxWidth - 14, headerY + 2, closeHovered ? 0xFFE53935 : 0xFF666666, false);

        // --- Separator line ---
        context.fill(boxX + 8, boxY + 22, boxX + boxWidth - 8, boxY + 23, 0xFF222224);

        // --- Category Tabs (minimalist underline style) ---
        int tabY = boxY + 27;
        int tabX = boxX + 10;
        for (Category cat : Category.values()) {
            boolean active = cat == currentCategory;
            int tw = this.textRenderer.getWidth(cat.name) + 10;
            boolean hovered = mouseX >= tabX && mouseX <= tabX + tw && mouseY >= tabY && mouseY <= tabY + 16;

            int textColor = active ? 0xFFE1BEE7 : (hovered ? 0xFFCCCCCC : 0xFF888888);
            context.drawCenteredTextWithShadow(this.textRenderer, cat.name, tabX + tw / 2, tabY + 3, textColor);
            if (active) {
                context.fill(tabX + 2, tabY + 14, tabX + tw - 2, tabY + 16, 0xFF9C27B0);
            }
            tabX += tw + 4;
        }

        // HUD Layout button (right-aligned)
        int hudBtnW = 70;
        int hudBtnX = boxX + boxWidth - hudBtnW - 10;
        boolean hudHovered = mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= tabY && mouseY <= tabY + 16;
        context.fill(hudBtnX, tabY, hudBtnX + hudBtnW, tabY + 16, hudHovered ? 0xFF1E1E22 : 0x00000000);
        context.drawCenteredTextWithShadow(this.textRenderer, "📐 Layout", hudBtnX + hudBtnW / 2, tabY + 3, hudHovered ? 0xFFE1BEE7 : 0xFF9C27B0);

        // --- Separator ---
        context.fill(boxX + 8, boxY + 47, boxX + boxWidth - 8, boxY + 48, 0xFF1A1A1C);

        // --- Card Grid (2 columns, clean rows) ---
        int cardAreaY = boxY + 52;
        int cardAreaH = boxHeight - 58;
        int cardW = (boxWidth - 28) / 2;
        int cardH = 26;
        int cardGap = 3;

        context.enableScissor(boxX + 4, cardAreaY, boxX + boxWidth - 4, cardAreaY + cardAreaH);

        List<ModCard> filtered = getFilteredCards();
        int index = 0;
        for (ModCard card : filtered) {
            int col = index % 2;
            int row = index / 2;

            int cX = boxX + 8 + col * (cardW + cardGap + 4);
            int cY = cardAreaY + 4 + row * (cardH + cardGap) - (int) scrollY;

            if (cY + cardH >= cardAreaY && cY <= cardAreaY + cardAreaH) {
                boolean enabled = card.getter.getAsBoolean();
                boolean cardHovered = mouseX >= cX && mouseX <= cX + cardW && mouseY >= cY && mouseY <= cY + cardH;

                // Card background
                int cardBg = cardHovered ? 0xFF1C1C20 : 0xFF151517;
                context.fill(cX, cY, cX + cardW, cY + cardH, cardBg);
                // Left accent strip
                context.fill(cX, cY, cX + 2, cY + cardH, enabled ? 0xFF9C27B0 : 0xFF333336);

                // Icon
                context.drawText(this.textRenderer, card.icon, cX + 6, cY + 9, 0xFFCCCCCC, false);

                // Name
                String displayName = card.name;
                if (displayName.length() > 15) displayName = displayName.substring(0, 13) + "..";
                context.drawText(this.textRenderer, displayName, cX + 22, cY + 9, enabled ? 0xFFE0E0E0 : 0xFF888888, false);

                // Gear icon (if configurable)
                if (card.onConfigure != null) {
                    boolean gearHovered = mouseX >= cX + cardW - 28 && mouseX <= cX + cardW - 16 && mouseY >= cY + 4 && mouseY <= cY + cardH - 4;
                    context.drawText(this.textRenderer, "⚙", cX + cardW - 26, cY + 9, gearHovered ? 0xFFE1BEE7 : 0xFF555555, false);
                }

                // Toggle pill switch
                int pillW = 18;
                int pillH = 10;
                int pillX = cX + cardW - pillW - 4;
                int pillY = cY + (cardH - pillH) / 2;
                boolean pillHovered = mouseX >= pillX && mouseX <= pillX + pillW && mouseY >= pillY && mouseY <= pillY + pillH;

                int pillBg = enabled ? 0xFF7B1FA2 : 0xFF333336;
                if (pillHovered) pillBg = enabled ? 0xFF9C27B0 : 0xFF444448;
                context.fill(pillX, pillY, pillX + pillW, pillY + pillH, pillBg);
                // Knob
                int knobX = enabled ? pillX + pillW - 6 : pillX + 2;
                context.fill(knobX, pillY + 2, knobX + 4, pillY + pillH - 2, 0xFFE0E0E0);
            }
            index++;
        }

        context.disableScissor();

        // --- Scrollbar ---
        int rowsCount = (filtered.size() + 1) / 2;
        int totalHeight = rowsCount * (cardH + cardGap);
        if (totalHeight > cardAreaH) {
            int scrollBarH = Math.max(12, (int) (((double) cardAreaH / totalHeight) * cardAreaH));
            int scrollBarY = cardAreaY + (int) ((scrollY / (totalHeight - cardAreaH)) * (cardAreaH - scrollBarH));
            context.fill(boxX + boxWidth - 5, scrollBarY, boxX + boxWidth - 2, scrollBarY + scrollBarH, 0xFF333336);
        }

        // --- Bottom status bar ---
        int statusY = boxY + boxHeight - 3;
        context.fill(boxX, statusY, boxX + boxWidth, statusY + 3, 0xFF111113);
        int enabledCount = 0;
        for (ModCard c : filtered) {
            if (c.getter.getAsBoolean()) enabledCount++;
        }

        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int headerY = boxY + 6;

        // Close button
        if (mouseX >= boxX + boxWidth - 18 && mouseX <= boxX + boxWidth - 4 && mouseY >= headerY && mouseY <= headerY + 14) {
            this.close();
            return true;
        }

        // Search field focus
        if (this.searchField != null) {
            boolean clicked = mouseX >= this.searchField.getX() - 2 && mouseX <= this.searchField.getX() + this.searchField.getWidth() + 2
                           && mouseY >= this.searchField.getY() - 2 && mouseY <= this.searchField.getY() + this.searchField.getHeight() + 2;
            this.searchField.setFocused(clicked);
            if (clicked && button == 1) {
                this.searchField.setText("");
            }
        }

        // Category tabs
        int tabY = boxY + 27;
        int tabX = boxX + 10;
        for (Category cat : Category.values()) {
            int tw = this.textRenderer.getWidth(cat.name) + 10;
            if (mouseX >= tabX && mouseX <= tabX + tw && mouseY >= tabY && mouseY <= tabY + 16) {
                currentCategory = cat;
                scrollY = 0;
                return true;
            }
            tabX += tw + 4;
        }

        // HUD Layout button
        int hudBtnW = 70;
        int hudBtnX = boxX + boxWidth - hudBtnW - 10;
        if (mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= tabY && mouseY <= tabY + 16) {
            if (this.client != null) {
                this.client.setScreen(new net.featherpojav.client.gui.FeatherHudEditorScreen(this));
            }
            return true;
        }

        // Card interactions
        int cardAreaY = boxY + 52;
        int cardAreaH = boxHeight - 58;
        int cardW = (boxWidth - 28) / 2;
        int cardH = 26;
        int cardGap = 3;

        List<ModCard> filtered = getFilteredCards();
        int index = 0;
        for (ModCard card : filtered) {
            int col = index % 2;
            int row = index / 2;

            int cX = boxX + 8 + col * (cardW + cardGap + 4);
            int cY = cardAreaY + 4 + row * (cardH + cardGap) - (int) scrollY;

            if (cY + cardH >= cardAreaY && cY <= cardAreaY + cardAreaH) {
                boolean cardClicked = mouseX >= cX && mouseX <= cX + cardW && mouseY >= cY && mouseY <= cY + cardH;

                // Right-click anywhere on card opens config
                if (cardClicked && button == 1 && card.onConfigure != null) {
                    card.onConfigure.run();
                    return true;
                }

                // Gear icon click
                if (card.onConfigure != null) {
                    if (mouseX >= cX + cardW - 28 && mouseX <= cX + cardW - 16 && mouseY >= cY + 4 && mouseY <= cY + cardH - 4) {
                        card.onConfigure.run();
                        return true;
                    }
                }

                // Toggle pill click
                int pillW = 18;
                int pillH = 10;
                int pillX = cX + cardW - pillW - 4;
                int pillY = cY + (cardH - pillH) / 2;

                if (mouseX >= pillX && mouseX <= pillX + pillW && mouseY >= pillY && mouseY <= pillY + pillH) {
                    card.setter.accept(!card.getter.getAsBoolean());
                    FeatherConfig.save();
                    return true;
                }

                // Click anywhere on card to toggle
                if (cardClicked && button == 0) {
                    card.setter.accept(!card.getter.getAsBoolean());
                    FeatherConfig.save();
                    return true;
                }
            }
            index++;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Do nothing to prevent background blur
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchField != null && this.searchField.keyPressed(keyCode, scanCode, modifiers)) {
            scrollY = 0;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.searchField != null && this.searchField.charTyped(chr, modifiers)) {
            scrollY = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

}

// ==========================================
// Sub-Settings Configuration Screen: Auto Text
// ==========================================
class FeatherAutoTextScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget inputField;
    private boolean listening = false;
    private ButtonWidget bindButton;

    protected FeatherAutoTextScreen(Screen parent) {
        super(Text.of("Auto Text Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        // Centered input field for macro
        inputField = new TextFieldWidget(this.textRenderer, cx - 100, cy - 35, 200, 20, Text.of("Edit Macro Command"));
        inputField.setMaxLength(128);
        inputField.setText(FeatherConfig.INSTANCE.autoTextCommand);
        this.addSelectableChild(inputField);

        // Keybind configuration button
        bindButton = ButtonWidget.builder(Text.of(getKeyNameText()), button -> {
            listening = true;
            button.setMessage(Text.of("Press any key..."));
        }).dimensions(cx - 100, cy - 5, 200, 20).build();
        this.addDrawableChild(bindButton);

        // Save & Back button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Save & Back"), button -> {
            FeatherConfig.INSTANCE.autoTextCommand = inputField.getText();
            FeatherConfig.save();
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(cx - 50, cy + 25, 100, 20).build());
    }

    private String getKeyNameText() {
        return "Keybind: " + FeatherPojavModClient.autoTextKey.getBoundKeyTranslationKey().replace("key.keyboard.", "").toUpperCase();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening) {
            listening = false;
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                FeatherPojavModClient.autoTextKey.setBoundKey(InputUtil.fromKeyCode(keyCode, scanCode));
                if (this.client != null && this.client.options != null) {
                    this.client.options.write();
                }
            }
            bindButton.setMessage(Text.of(getKeyNameText()));
            return true;
        }
        if (inputField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (inputField.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xD0141416);
        context.drawCenteredTextWithShadow(this.textRenderer, "AUTO TEXT MACRO CONFIG", this.width / 2, this.height / 2 - 65, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Edit the command macro and click the keybind button to customize.", this.width / 2, this.height / 2 - 50, 0xFFBA68C8);
        inputField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
}

// ==========================================
// Sub-Settings Configuration Screen: TimeChanger
// ==========================================
class FeatherTimeChangerScreen extends Screen {
    private final Screen parent;

    protected FeatherTimeChangerScreen(Screen parent) {
        super(Text.of("TimeChanger Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int leftX = this.width / 2 - 95;
        int startY = this.height / 2 - 35;

        // Presets: Morning (0), Day (6000), Sunset (12000), Night (18000)
        this.addDrawableChild(ButtonWidget.builder(Text.of("Morning"), button -> setTime(0, 0)).dimensions(leftX, startY, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Day"), button -> setTime(6000, 1)).dimensions(leftX + 100, startY, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Sunset"), button -> setTime(12000, 2)).dimensions(leftX, startY + 25, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Night"), button -> setTime(18000, 3)).dimensions(leftX + 100, startY + 25, 90, 20).build());

        // Back button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 45, startY + 60, 90, 20).build());
    }

    private void setTime(long ticks, int mode) {
        FeatherConfig.INSTANCE.timeChangerTicks = ticks;
        FeatherConfig.INSTANCE.timeChangerMode = mode;
        FeatherConfig.save();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xD0141416);
        context.drawCenteredTextWithShadow(this.textRenderer, "TIMECHANGER PRESETS", this.width / 2, this.height / 2 - 60, 0xFFFFFFFF);
        
        String modeName = "Day";
        if (FeatherConfig.INSTANCE.timeChangerMode == 0) modeName = "Morning";
        else if (FeatherConfig.INSTANCE.timeChangerMode == 2) modeName = "Sunset";
        else if (FeatherConfig.INSTANCE.timeChangerMode == 3) modeName = "Night";
        context.drawCenteredTextWithShadow(this.textRenderer, "Active: " + modeName + " (" + FeatherConfig.INSTANCE.timeChangerTicks + " ticks)", this.width / 2, this.height / 2 - 48, 0xFFBA68C8);
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
}

// ==========================================
// Sub-Settings Configuration Screen: Crosshair
// ==========================================
class FeatherCrosshairScreen extends Screen {
    private final Screen parent;

    protected FeatherCrosshairScreen(Screen parent) {
        super(Text.of("Crosshair Settings"));
        this.parent = parent;
    }

    private static final int[] COLORS = {0xFF00FF00, 0xFFFF0000, 0xFF00BFFF, 0xFFFFFF00, 0xFF00FFFF, 0xFFFF00FF, 0xFFFFFFFF, 0xFFFF8C00, 0xFFFF69B4};
    private static final String[] COLOR_NAMES = {"Green", "Red", "Blue", "Yellow", "Cyan", "Magenta", "White", "Orange", "Pink"};

    private String getPresetButtonText() {
        String[] presets = {"Cross", "Dot", "Circle", "Circle/Dot", "T-Shape", "X-Shape", "Square", "Chevron", "Tri-Bar", "Box/Dot"};
        int p = FeatherConfig.INSTANCE.crosshairPreset;
        if (p < 0 || p >= presets.length) p = 0;
        return "Style: " + presets[p];
    }

    private void cyclePreset(ButtonWidget button) {
        FeatherConfig.INSTANCE.crosshairPreset = (FeatherConfig.INSTANCE.crosshairPreset + 1) % 10;
        FeatherConfig.save();
        button.setMessage(Text.of(getPresetButtonText()));
    }

    private String getColorButtonText() {
        int color = FeatherConfig.INSTANCE.crosshairColor;
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == color) {
                return "Color: " + COLOR_NAMES[i];
            }
        }
        return "Color: Custom";
    }

    private void cycleColor(ButtonWidget button) {
        int color = FeatherConfig.INSTANCE.crosshairColor;
        int nextIndex = 0;
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == color) {
                nextIndex = (i + 1) % COLORS.length;
                break;
            }
        }
        FeatherConfig.INSTANCE.crosshairColor = COLORS[nextIndex];
        FeatherConfig.save();
        button.setMessage(Text.of(getColorButtonText()));
    }

    @Override
    protected void init() {
        int leftX = this.width / 2 - 95;
        int startY = this.height / 2 - 40;

        // Size adjustment
        this.addDrawableChild(ButtonWidget.builder(Text.of("Size +"), button -> adjustSize(0.5f)).dimensions(leftX, startY, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Size -"), button -> adjustSize(-0.5f)).dimensions(leftX + 100, startY, 90, 20).build());

        // Gap adjustment
        this.addDrawableChild(ButtonWidget.builder(Text.of("Gap +"), button -> adjustGap(0.5f)).dimensions(leftX, startY + 25, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Gap -"), button -> adjustGap(-0.5f)).dimensions(leftX + 100, startY + 25, 90, 20).build());

        // Thickness adjustment
        this.addDrawableChild(ButtonWidget.builder(Text.of("Thickness +"), button -> adjustTh(0.5f)).dimensions(leftX, startY + 50, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Thickness -"), button -> adjustTh(-0.5f)).dimensions(leftX + 100, startY + 50, 90, 20).build());

        // Preset and Color cycling
        this.addDrawableChild(ButtonWidget.builder(Text.of(getPresetButtonText()), button -> cyclePreset(button)).dimensions(leftX, startY + 75, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of(getColorButtonText()), button -> cycleColor(button)).dimensions(leftX + 100, startY + 75, 90, 20).build());

        // Back button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 45, startY + 105, 90, 20).build());
    }

    private void adjustSize(float val) {
        FeatherConfig.INSTANCE.crosshairSize = Math.max(1.0f, FeatherConfig.INSTANCE.crosshairSize + val);
        FeatherConfig.save();
    }

    private void adjustGap(float val) {
        FeatherConfig.INSTANCE.crosshairGap = Math.max(0.0f, FeatherConfig.INSTANCE.crosshairGap + val);
        FeatherConfig.save();
    }

    private void adjustTh(float val) {
        FeatherConfig.INSTANCE.crosshairThickness = Math.max(0.5f, FeatherConfig.INSTANCE.crosshairThickness + val);
        FeatherConfig.save();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xD0141416);
        context.drawCenteredTextWithShadow(this.textRenderer, "CUSTOM CROSSHAIR CONFIGURATION", this.width / 2, this.height / 2 - 75, 0xFFFFFFFF);
        
        FeatherConfig cfg = FeatherConfig.INSTANCE;
        String desc = String.format("Size: %.1f | Gap: %.1f | Thickness: %.1f", cfg.crosshairSize, cfg.crosshairGap, cfg.crosshairThickness);
        context.drawCenteredTextWithShadow(this.textRenderer, desc, this.width / 2, this.height / 2 - 60, 0xFFBA68C8);
        
        // Render a preview of the crosshair in the center top
        int cx = this.width / 2;
        int cy = this.height / 2 - 100;
        float gap = cfg.crosshairGap;
        float size = cfg.crosshairSize;
        float th = cfg.crosshairThickness;
        int color = cfg.crosshairColor;
        int preset = cfg.crosshairPreset;

        int t = Math.max(1, (int) th);
        int h1 = t / 2;
        int h2 = t / 2 + (t % 2);

        switch (preset) {
            case 1: // Dot
                context.fill(cx - h1, cy - h1, cx + h2, cy + h2, color);
                break;
            case 2: // Circle
            case 3: // Circle with Dot
                int radius = (int)(gap + size);
                for (int angle = 0; angle < 360; angle += 10) {
                    double rad = Math.toRadians(angle);
                    int px = (int) Math.round(cx + Math.cos(rad) * radius);
                    int py = (int) Math.round(cy + Math.sin(rad) * radius);
                    context.fill(px - h1, py - h1, px + h2, py + h2, color);
                }
                if (preset == 3) {
                    context.fill(cx - h1, cy - h1, cx + h2, cy + h2, color);
                }
                break;
            case 4: // T-Shape
                context.fill((int)(cx - gap - size), cy - h1, (int)(cx - gap), cy + h2, color);
                context.fill((int)(cx + gap), cy - h1, (int)(cx + gap + size), cy + h2, color);
                context.fill(cx - h1, (int)(cy + gap), cx + h2, (int)(cy + gap + size), color);
                break;
            case 5: // X-Shape
                for (int idx = 0; idx < (int) size; idx++) {
                    int f = (int)(gap + idx);
                    context.fill(cx - f - h1, cy - f - h1, cx - f + h2, cy - f + h2, color);
                    context.fill(cx + f - h1, cy - f - h1, cx + f + h2, cy - f + h2, color);
                    context.fill(cx - f - h1, cy + f - h1, cx - f + h2, cy + f + h2, color);
                    context.fill(cx + f - h1, cy + f - h1, cx + f + h2, cy + f + h2, color);
                }
                break;
            case 6: // Square
            case 9: // Box with Dot
                int r = (int)(gap + size);
                context.fill(cx - r, cy - r - h1, cx + r, cy - r + h2, color);
                context.fill(cx - r, cy + r - h1, cx + r, cy + r + h2, color);
                context.fill(cx - r - h1, cy - r, cx - r + h2, cy + r, color);
                context.fill(cx + r - h1, cy - r, cx + r + h2, cy + r, color);
                if (preset == 9) {
                    context.fill(cx - h1, cy - h1, cx + h2, cy + h2, color);
                }
                break;
            case 7: // Arrow / Chevron
                for (int idx = 0; idx < (int) size; idx++) {
                    context.fill(cx - idx - h1, (int)(cy - gap + idx) - h1, cx - idx + h2, (int)(cy - gap + idx) + h2, color);
                    context.fill(cx + idx - h1, (int)(cy - gap + idx) - h1, cx + idx + h2, (int)(cy - gap + idx) + h2, color);
                }
                break;
            case 8: // Tri-Bar
                context.fill((int)(cx - gap - size), cy - h1, (int)(cx - gap), cy + h2, color);
                context.fill((int)(cx + gap), cy - h1, (int)(cx + gap + size), cy + h2, color);
                context.fill(cx - h1, (int)(cy - gap - size), cx + h2, (int)(cy - gap), color);
                break;
            default: // 0: Classic Cross
                context.fill((int)(cx - gap - size), cy - h1, (int)(cx - gap), cy + h2, color);
                context.fill((int)(cx + gap), cy - h1, (int)(cx + gap + size), cy + h2, color);
                context.fill(cx - h1, (int)(cy - gap - size), cx + h2, (int)(cy - gap), color);
                context.fill(cx - h1, (int)(cy + gap), cx + h2, (int)(cy + gap + size), color);
                break;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
}

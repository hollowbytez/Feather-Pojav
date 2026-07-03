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
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class FeatherSettingsScreen extends Screen {
    private final Screen parent;
    private Category currentCategory = Category.ALL;
    private final List<ModCard> cards = new ArrayList<>();
    private TextFieldWidget searchField;

    // ===== Exact color palette from blueprint =====
    private static final int BG_PANEL       = 0xFF16161A;  // Main panel background
    private static final int BG_CARD        = 0xFF1F2026;  // Card background
    private static final int ACCENT_RED     = 0xFFEB4040;  // Primary accent / selected tab
    private static final int ENABLED_GREEN  = 0xFF226422;  // Enabled toggle
    private static final int DISABLED_GRAY  = 0xFF2A2B36;  // Disabled toggle
    private static final int TEXT_PRIMARY   = 0xFFFFFFFF;  // White text
    private static final int TEXT_SECONDARY = 0xFF8A8C96;  // Slate gray text
    private static final int CLOSE_RED      = 0xFFBA2D2D;  // Close button
    private static final int HEADER_BG      = 0xFF121214;  // Header bg
    private static final int CARD_HOVER     = 0xFF282830;  // Card hover

    // Layout geometry (calculated in init)
    private int panelX, panelY, panelW, panelH;
    private int navBarH;  // External top navigation dock height
    private int filterBarH; // Internal sub-header filter bar
    private double scrollY = 0;

    public FeatherSettingsScreen(Screen parent) {
        super(Text.of("Feather Mod Menu"));
        this.parent = parent;
    }

    enum Category {
        ALL("All"),
        NEW("New"),
        HUD("HUD"),
        PVP("PvP");

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
        // === Blueprint: 68% screen width, 65% screen height, centered ===
        panelW = (int)(this.width * 0.68);
        panelH = (int)(this.height * 0.65);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        // Nav bar is ~6% of panel height, sits ABOVE the main panel
        navBarH = Math.max(20, (int)(panelH * 0.06));
        // Filter bar inside main panel top
        filterBarH = Math.max(18, (int)(panelH * 0.06));

        // Search field inside the filter bar, right side
        int searchW = Math.min(100, panelW / 4);
        int searchX = panelX + panelW - searchW - 40;
        int searchY = panelY + navBarH + (filterBarH - 10) / 2;
        this.searchField = new TextFieldWidget(this.textRenderer, searchX, searchY, searchW, 10, Text.of("Search"));
        this.searchField.setMaxLength(30);
        this.searchField.setDrawsBackground(false);
        this.searchField.setPlaceholder(Text.of("Search..."));
        this.searchField.setEditableColor(TEXT_PRIMARY);
        this.searchField.setUneditableColor(TEXT_SECONDARY);
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

    private int getCols() {
        if (panelW >= 400) return 4;
        if (panelW >= 280) return 3;
        return 2;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int gridTop = panelY + navBarH + filterBarH;
        int gridBot = panelY + panelH;
        if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= gridTop && mouseY <= gridBot) {
            int cols = getCols();
            int pad = 8;
            int cardH = getCardH();
            int items = getFilteredCards().size();
            int rows = (items + cols - 1) / cols;
            int totalH = rows * (cardH + pad) + pad;
            int gridH = gridBot - gridTop;
            double maxScroll = Math.max(0, totalH - gridH);
            scrollY -= verticalAmount * 22;
            if (scrollY < 0) scrollY = 0;
            if (scrollY > maxScroll) scrollY = maxScroll;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private int getCardH() {
        // Card height: roughly 3 rows fill ~82% of the panel grid area
        int gridH = panelH - navBarH - filterBarH;
        int h = (gridH - 8 * 4) / 3;  // 3 visible rows, 4 gaps of 8px
        return Math.max(50, Math.min(h, 90));
    }

    // ============================================================
    // RENDER
    // ============================================================
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Darken game background
        context.fill(0, 0, this.width, this.height, 0x88000000);

        int cols = getCols();
        int pad = 8;  // Uniform 8px gap from blueprint
        int cardH = getCardH();
        int cardW = (panelW - pad * (cols + 1)) / cols;

        // ===============================================
        // A. TOP NAVIGATION DOCK (External Header)
        // ===============================================
        int navY = panelY;
        int navBottom = navY + navBarH;

        // Nav bar background
        context.fill(panelX, navY, panelX + panelW, navBottom, HEADER_BG);

        // Feather icon (left side)
        Identifier iconId = Identifier.of("featherpojav", "icon.png");
        int iconSize = navBarH - 4;
        context.drawTexture(iconId, panelX + 4, navY + 2, 0.0f, 0.0f, iconSize, iconSize, 500, 500);

        // Red "MOD MENU" tab (left side, next to icon)
        int mmTabX = panelX + iconSize + 8;
        int mmTabW = 62;
        int mmTabH = navBarH - 4;
        int mmTabY = navY + 2;
        context.fill(mmTabX, mmTabY, mmTabX + mmTabW, mmTabY + mmTabH, ACCENT_RED);
        context.drawText(this.textRenderer, "🪶 MOD MENU", mmTabX + 4, mmTabY + (mmTabH - 8) / 2, TEXT_PRIMARY, false);

        // Small dark icon-only square buttons (section icons) after MOD MENU tab
        int secIconX = mmTabX + mmTabW + 6;
        String[] sectionIcons = {"⚙", "👕", "📺", "🎮", "👥"};
        for (String si : sectionIcons) {
            int btnSize = navBarH - 6;
            boolean siHover = mouseX >= secIconX && mouseX <= secIconX + btnSize && mouseY >= navY + 3 && mouseY <= navY + 3 + btnSize;
            context.fill(secIconX, navY + 3, secIconX + btnSize, navY + 3 + btnSize, siHover ? 0xFF2A2B36 : 0xFF1A1A1E);
            context.drawCenteredTextWithShadow(this.textRenderer, si, secIconX + btnSize / 2, navY + 3 + (btnSize - 8) / 2, TEXT_SECONDARY);
            secIconX += btnSize + 3;
        }

        // "F E A T H E R C L I E N T" branding text centered in header
        String brand = "F E A T H E R C L I E N T";
        int brandW = this.textRenderer.getWidth(brand);
        context.drawText(this.textRenderer, brand, panelX + panelW / 2 - brandW / 2, navY + (navBarH - 8) / 2, ACCENT_RED, true);

        // Profile username (right side)
        if (this.client != null && this.client.getSession() != null) {
            String uname = this.client.getSession().getUsername();
            if (uname.length() > 10) uname = uname.substring(0, 8) + "..";
            int unameW = this.textRenderer.getWidth(uname);
            context.drawText(this.textRenderer, uname, panelX + panelW - unameW - 24, navY + (navBarH - 8) / 2, TEXT_PRIMARY, true);
        }

        // Close X button (right edge, dark red)
        int closeSize = navBarH - 6;
        int closeX = panelX + panelW - closeSize - 3;
        int closeY = navY + 3;
        boolean closeHover = mouseX >= closeX && mouseX <= closeX + closeSize && mouseY >= closeY && mouseY <= closeY + closeSize;
        context.fill(closeX, closeY, closeX + closeSize, closeY + closeSize, closeHover ? 0xFFE53935 : CLOSE_RED);
        context.drawCenteredTextWithShadow(this.textRenderer, "✕", closeX + closeSize / 2, closeY + (closeSize - 8) / 2, TEXT_PRIMARY);

        // ===============================================
        // MAIN PANEL BODY (below nav bar)
        // ===============================================
        int bodyY = navBottom;
        int bodyH = panelH - navBarH;
        context.fill(panelX, bodyY, panelX + panelW, bodyY + bodyH, BG_PANEL);

        // ===============================================
        // B. INTERNAL SUB-HEADER (Filter Bar)
        // ===============================================
        int filterY = bodyY + 2;

        // Category pill tabs (left side)
        int catX = panelX + pad;
        for (Category cat : Category.values()) {
            boolean active = cat == currentCategory;
            int tw = this.textRenderer.getWidth(cat.name) + 12;
            int pillH = filterBarH - 6;
            int pillY = filterY + 2;
            boolean catHover = mouseX >= catX && mouseX <= catX + tw && mouseY >= pillY && mouseY <= pillY + pillH;

            if (active) {
                context.fill(catX, pillY, catX + tw, pillY + pillH, ACCENT_RED);
            } else if (catHover) {
                context.fill(catX, pillY, catX + tw, pillY + pillH, 0xFF2A2B36);
            }
            context.drawCenteredTextWithShadow(this.textRenderer, cat.name, catX + tw / 2, pillY + (pillH - 8) / 2, active ? TEXT_PRIMARY : TEXT_SECONDARY);
            catX += tw + 4;
        }

        // Search field (right side of filter bar)
        if (this.searchField != null) {
            int sX = this.searchField.getX();
            int sY = this.searchField.getY();
            int sW = this.searchField.getWidth();
            int sH = this.searchField.getHeight();
            context.fill(sX - 4, sY - 3, sX + sW + 4, sY + sH + 3, 0xFF1A1A1E);
            this.searchField.render(context, mouseX, mouseY, delta);
        }

        // Heart + Grid layout utility icons (far right of filter bar)
        int utilX = panelX + panelW - 28;
        int utilY = filterY + 3;
        int utilS = filterBarH - 8;
        context.fill(utilX, utilY, utilX + utilS, utilY + utilS, 0xFF1A1A1E);
        context.drawCenteredTextWithShadow(this.textRenderer, "♥", utilX + utilS / 2, utilY + (utilS - 8) / 2, TEXT_SECONDARY);
        utilX -= utilS + 3;
        context.fill(utilX, utilY, utilX + utilS, utilY + utilS, 0xFF1A1A1E);
        context.drawCenteredTextWithShadow(this.textRenderer, "▦", utilX + utilS / 2, utilY + (utilS - 8) / 2, TEXT_SECONDARY);

        // HUD Layout button (positioned left of utility icons)
        int hudBtnW = 60;
        int hudBtnH = filterBarH - 6;
        int hudBtnX = utilX - hudBtnW - 6;
        int hudBtnY = filterY + 2;
        boolean hudHover = mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= hudBtnY && mouseY <= hudBtnY + hudBtnH;
        context.fill(hudBtnX, hudBtnY, hudBtnX + hudBtnW, hudBtnY + hudBtnH, hudHover ? 0xFF2A2B36 : 0xFF1A1A1E);
        context.drawCenteredTextWithShadow(this.textRenderer, "📐 Layout", hudBtnX + hudBtnW / 2, hudBtnY + (hudBtnH - 8) / 2, hudHover ? TEXT_PRIMARY : TEXT_SECONDARY);

        // Separator line below filter bar
        int sepY = filterY + filterBarH - 1;
        context.fill(panelX + pad, sepY, panelX + panelW - pad, sepY + 1, 0xFF222226);

        // ===============================================
        // C. MOD GRID (4-column, ~3 visible rows)
        // ===============================================
        int gridTop = bodyY + filterBarH + 2;
        int gridH = bodyY + bodyH - gridTop;

        context.enableScissor(panelX, gridTop, panelX + panelW, gridTop + gridH);

        List<ModCard> filtered = getFilteredCards();
        int idx = 0;
        for (ModCard card : filtered) {
            int col = idx % cols;
            int row = idx / cols;

            int cX = panelX + pad + col * (cardW + pad);
            int cY = gridTop + pad + row * (cardH + pad) - (int) scrollY;

            if (cY + cardH >= gridTop && cY <= gridTop + gridH) {
                boolean enabled = card.getter.getAsBoolean();
                boolean hover = mouseX >= cX && mouseX <= cX + cardW && mouseY >= cY && mouseY <= cY + cardH;

                // === Card Background ===
                context.fill(cX, cY, cX + cardW, cY + cardH, hover ? CARD_HOVER : BG_CARD);

                // === "NEW" green badge (top-left, above name) ===
                int textStartY = cY + 4;
                if (card.category == Category.NEW) {
                    context.fill(cX + 4, textStartY, cX + 26, textStartY + 9, 0xFF2E7D32);
                    // Scale down the NEW text
                    context.drawText(this.textRenderer, "NEW", cX + 5, textStartY + 1, TEXT_PRIMARY, false);
                    textStartY += 10;
                }

                // === Mod name (top-left, left-aligned) ===
                String displayName = card.name;
                int maxNameW = cardW - 20;
                if (this.textRenderer.getWidth(displayName) > maxNameW) {
                    while (this.textRenderer.getWidth(displayName + "..") > maxNameW && displayName.length() > 2) {
                        displayName = displayName.substring(0, displayName.length() - 1);
                    }
                    displayName += "..";
                }
                context.drawText(this.textRenderer, displayName, cX + 5, textStartY, TEXT_PRIMARY, false);

                // === Heart icon (top-right, for favoriting) ===
                boolean heartHover = mouseX >= cX + cardW - 14 && mouseX <= cX + cardW - 2 && mouseY >= cY + 3 && mouseY <= cY + 15;
                context.drawText(this.textRenderer, "♥", cX + cardW - 12, cY + 4, heartHover ? ACCENT_RED : 0xFF444450, false);

                // === Center icon (vertically & horizontally centered in asset area) ===
                int iconAreaTop = textStartY + 12;
                int iconAreaBot = cY + cardH - 20;
                int iconCenterY = (iconAreaTop + iconAreaBot) / 2 - 4;
                context.drawCenteredTextWithShadow(this.textRenderer, card.icon, cX + cardW / 2, iconCenterY, 0xFFCCCCCC);

                // === Bottom Control Row ===
                int botRowY = cY + cardH - 16;

                // Gear icon button (bottom-left, small square)
                if (card.onConfigure != null) {
                    int gearS = 12;
                    int gearX = cX + 4;
                    int gearY = botRowY + 1;
                    boolean gearHover = mouseX >= gearX && mouseX <= gearX + gearS && mouseY >= gearY && mouseY <= gearY + gearS;
                    context.fill(gearX, gearY, gearX + gearS, gearY + gearS, gearHover ? 0xFF3A3A44 : 0xFF222228);
                    context.drawCenteredTextWithShadow(this.textRenderer, "⚙", gearX + gearS / 2, gearY + 2, gearHover ? TEXT_PRIMARY : TEXT_SECONDARY);
                }

                // Toggle pill button (spans rest of bottom, right side)
                int toggleX = card.onConfigure != null ? cX + 20 : cX + 4;
                int toggleW = cX + cardW - 4 - toggleX;
                int toggleH = 13;
                int toggleY = botRowY + 1;
                boolean toggleHover = mouseX >= toggleX && mouseX <= toggleX + toggleW && mouseY >= toggleY && mouseY <= toggleY + toggleH;

                if (enabled) {
                    int bg = toggleHover ? 0xFF2E8E2E : ENABLED_GREEN;
                    context.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, bg);
                    context.drawCenteredTextWithShadow(this.textRenderer, "Enabled", toggleX + toggleW / 2, toggleY + 3, TEXT_PRIMARY);
                } else {
                    int bg = toggleHover ? 0xFF3A3B46 : DISABLED_GRAY;
                    context.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, bg);
                    context.drawCenteredTextWithShadow(this.textRenderer, "Disabled", toggleX + toggleW / 2, toggleY + 3, TEXT_SECONDARY);
                }
            }
            idx++;
        }

        context.disableScissor();

        // === Scrollbar ===
        int rows = (filtered.size() + cols - 1) / cols;
        int totalH = rows * (cardH + pad) + pad;
        if (totalH > gridH) {
            int barH = Math.max(12, (int)((double) gridH / totalH * gridH));
            int barY = gridTop + (int)((scrollY / (totalH - gridH)) * (gridH - barH));
            context.fill(panelX + panelW - 4, barY, panelX + panelW - 1, barY + barH, 0xFF444450);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    // ============================================================
    // MOUSE CLICK
    // ============================================================
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int navY = panelY;

        // === Close X button ===
        int closeSize = navBarH - 6;
        int closeX = panelX + panelW - closeSize - 3;
        int closeY2 = navY + 3;
        if (mouseX >= closeX && mouseX <= closeX + closeSize && mouseY >= closeY2 && mouseY <= closeY2 + closeSize) {
            this.close();
            return true;
        }

        // === Search field focus ===
        if (this.searchField != null) {
            boolean clicked = mouseX >= this.searchField.getX() - 4 && mouseX <= this.searchField.getX() + this.searchField.getWidth() + 4
                           && mouseY >= this.searchField.getY() - 3 && mouseY <= this.searchField.getY() + this.searchField.getHeight() + 3;
            this.searchField.setFocused(clicked);
            if (clicked && button == 1) {
                this.searchField.setText("");
            }
        }

        // === Category tabs ===
        int bodyY = navY + navBarH;
        int filterY = bodyY + 2;
        int catX = panelX + 8;
        for (Category cat : Category.values()) {
            int tw = this.textRenderer.getWidth(cat.name) + 12;
            int pillH = filterBarH - 6;
            int pillY = filterY + 2;
            if (mouseX >= catX && mouseX <= catX + tw && mouseY >= pillY && mouseY <= pillY + pillH) {
                currentCategory = cat;
                scrollY = 0;
                return true;
            }
            catX += tw + 4;
        }

        // === HUD Layout button ===
        int utilX = panelX + panelW - 28;
        int utilS = filterBarH - 8;
        int hudBtnW = 60;
        int hudBtnH = filterBarH - 6;
        int hudBtnX = utilX - utilS - 3 - hudBtnW - 6;
        int hudBtnY = filterY + 2;
        if (mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= hudBtnY && mouseY <= hudBtnY + hudBtnH) {
            if (this.client != null) {
                this.client.setScreen(new net.featherpojav.client.gui.FeatherHudEditorScreen(this));
            }
            return true;
        }

        // === Card interactions ===
        int cols = getCols();
        int pad = 8;
        int cardH = getCardH();
        int cardW = (panelW - pad * (cols + 1)) / cols;
        int gridTop = bodyY + filterBarH + 2;
        int gridH = bodyY + (panelH - navBarH) - gridTop;

        List<ModCard> filtered = getFilteredCards();
        int idx = 0;
        for (ModCard card : filtered) {
            int col = idx % cols;
            int row = idx / cols;

            int cX = panelX + pad + col * (cardW + pad);
            int cY = gridTop + pad + row * (cardH + pad) - (int) scrollY;

            if (cY + cardH >= gridTop && cY <= gridTop + gridH) {
                boolean cardClicked = mouseX >= cX && mouseX <= cX + cardW && mouseY >= cY && mouseY <= cY + cardH;

                // Right-click opens config
                if (cardClicked && button == 1 && card.onConfigure != null) {
                    card.onConfigure.run();
                    return true;
                }

                // Gear icon click
                if (card.onConfigure != null) {
                    int gearS = 12;
                    int gearX = cX + 4;
                    int gearY = cY + cardH - 16 + 1;
                    if (mouseX >= gearX && mouseX <= gearX + gearS && mouseY >= gearY && mouseY <= gearY + gearS) {
                        card.onConfigure.run();
                        return true;
                    }
                }

                // Toggle button click
                int toggleX = card.onConfigure != null ? cX + 20 : cX + 4;
                int toggleW = cX + cardW - 4 - toggleX;
                int toggleH = 13;
                int toggleY = cY + cardH - 16 + 1;
                if (mouseX >= toggleX && mouseX <= toggleX + toggleW && mouseY >= toggleY && mouseY <= toggleY + toggleH) {
                    card.setter.accept(!card.getter.getAsBoolean());
                    FeatherConfig.save();
                    return true;
                }

                // Clicking card body also toggles
                if (cardClicked && button == 0) {
                    card.setter.accept(!card.getter.getAsBoolean());
                    FeatherConfig.save();
                    return true;
                }
            }
            idx++;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Do nothing - render game behind
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

        inputField = new TextFieldWidget(this.textRenderer, cx - 100, cy - 35, 200, 20, Text.of("Edit Macro Command"));
        inputField.setMaxLength(128);
        inputField.setText(FeatherConfig.INSTANCE.autoTextCommand);
        this.addSelectableChild(inputField);

        bindButton = ButtonWidget.builder(Text.of(getKeyNameText()), button -> {
            listening = true;
            button.setMessage(Text.of("Press any key..."));
        }).dimensions(cx - 100, cy - 5, 200, 20).build();
        this.addDrawableChild(bindButton);

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
        context.fill(0, 0, this.width, this.height, 0xD016161A);
        context.drawCenteredTextWithShadow(this.textRenderer, "AUTO TEXT MACRO CONFIG", this.width / 2, this.height / 2 - 65, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Edit the command macro and click the keybind button to customize.", this.width / 2, this.height / 2 - 50, 0xFFEB4040);
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

        this.addDrawableChild(ButtonWidget.builder(Text.of("Morning"), button -> setTime(0, 0)).dimensions(leftX, startY, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Day"), button -> setTime(6000, 1)).dimensions(leftX + 100, startY, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Sunset"), button -> setTime(12000, 2)).dimensions(leftX, startY + 25, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Night"), button -> setTime(18000, 3)).dimensions(leftX + 100, startY + 25, 90, 20).build());

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
        context.fill(0, 0, this.width, this.height, 0xD016161A);
        context.drawCenteredTextWithShadow(this.textRenderer, "TIMECHANGER PRESETS", this.width / 2, this.height / 2 - 60, 0xFFFFFFFF);

        String modeName = "Day";
        if (FeatherConfig.INSTANCE.timeChangerMode == 0) modeName = "Morning";
        else if (FeatherConfig.INSTANCE.timeChangerMode == 2) modeName = "Sunset";
        else if (FeatherConfig.INSTANCE.timeChangerMode == 3) modeName = "Night";
        context.drawCenteredTextWithShadow(this.textRenderer, "Active: " + modeName + " (" + FeatherConfig.INSTANCE.timeChangerTicks + " ticks)", this.width / 2, this.height / 2 - 48, 0xFFEB4040);

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
            if (COLORS[i] == color) return "Color: " + COLOR_NAMES[i];
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

        this.addDrawableChild(ButtonWidget.builder(Text.of("Size +"), button -> adjustSize(0.5f)).dimensions(leftX, startY, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Size -"), button -> adjustSize(-0.5f)).dimensions(leftX + 100, startY, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Gap +"), button -> adjustGap(0.5f)).dimensions(leftX, startY + 25, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Gap -"), button -> adjustGap(-0.5f)).dimensions(leftX + 100, startY + 25, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Thickness +"), button -> adjustTh(0.5f)).dimensions(leftX, startY + 50, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Thickness -"), button -> adjustTh(-0.5f)).dimensions(leftX + 100, startY + 50, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of(getPresetButtonText()), button -> cyclePreset(button)).dimensions(leftX, startY + 75, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of(getColorButtonText()), button -> cycleColor(button)).dimensions(leftX + 100, startY + 75, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 45, startY + 105, 90, 20).build());
    }

    private void adjustSize(float val) { FeatherConfig.INSTANCE.crosshairSize = Math.max(1.0f, FeatherConfig.INSTANCE.crosshairSize + val); FeatherConfig.save(); }
    private void adjustGap(float val) { FeatherConfig.INSTANCE.crosshairGap = Math.max(0.0f, FeatherConfig.INSTANCE.crosshairGap + val); FeatherConfig.save(); }
    private void adjustTh(float val) { FeatherConfig.INSTANCE.crosshairThickness = Math.max(0.5f, FeatherConfig.INSTANCE.crosshairThickness + val); FeatherConfig.save(); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xD016161A);
        context.drawCenteredTextWithShadow(this.textRenderer, "CUSTOM CROSSHAIR CONFIGURATION", this.width / 2, this.height / 2 - 75, 0xFFFFFFFF);

        FeatherConfig cfg = FeatherConfig.INSTANCE;
        String desc = String.format("Size: %.1f | Gap: %.1f | Thickness: %.1f", cfg.crosshairSize, cfg.crosshairGap, cfg.crosshairThickness);
        context.drawCenteredTextWithShadow(this.textRenderer, desc, this.width / 2, this.height / 2 - 60, 0xFFEB4040);

        int cx = this.width / 2;
        int cy = this.height / 2 - 100;
        float gap = cfg.crosshairGap, size = cfg.crosshairSize, th = cfg.crosshairThickness;
        int color = cfg.crosshairColor;
        int preset = cfg.crosshairPreset;
        int t = Math.max(1, (int) th);
        int h1 = t / 2, h2 = t / 2 + (t % 2);

        switch (preset) {
            case 1: context.fill(cx - h1, cy - h1, cx + h2, cy + h2, color); break;
            case 2: case 3:
                int radius = (int)(gap + size);
                for (int a = 0; a < 360; a += 10) { double r = Math.toRadians(a); int px = (int)Math.round(cx+Math.cos(r)*radius); int py = (int)Math.round(cy+Math.sin(r)*radius); context.fill(px-h1,py-h1,px+h2,py+h2,color); }
                if (preset == 3) context.fill(cx-h1,cy-h1,cx+h2,cy+h2,color);
                break;
            case 4:
                context.fill((int)(cx-gap-size),cy-h1,(int)(cx-gap),cy+h2,color); context.fill((int)(cx+gap),cy-h1,(int)(cx+gap+size),cy+h2,color); context.fill(cx-h1,(int)(cy+gap),cx+h2,(int)(cy+gap+size),color); break;
            case 5:
                for (int i=0;i<(int)size;i++){int f=(int)(gap+i);context.fill(cx-f-h1,cy-f-h1,cx-f+h2,cy-f+h2,color);context.fill(cx+f-h1,cy-f-h1,cx+f+h2,cy-f+h2,color);context.fill(cx-f-h1,cy+f-h1,cx-f+h2,cy+f+h2,color);context.fill(cx+f-h1,cy+f-h1,cx+f+h2,cy+f+h2,color);} break;
            case 6: case 9:
                int rr=(int)(gap+size);context.fill(cx-rr,cy-rr-h1,cx+rr,cy-rr+h2,color);context.fill(cx-rr,cy+rr-h1,cx+rr,cy+rr+h2,color);context.fill(cx-rr-h1,cy-rr,cx-rr+h2,cy+rr,color);context.fill(cx+rr-h1,cy-rr,cx+rr+h2,cy+rr,color);
                if(preset==9)context.fill(cx-h1,cy-h1,cx+h2,cy+h2,color); break;
            case 7:
                for(int i=0;i<(int)size;i++){context.fill(cx-i-h1,(int)(cy-gap+i)-h1,cx-i+h2,(int)(cy-gap+i)+h2,color);context.fill(cx+i-h1,(int)(cy-gap+i)-h1,cx+i+h2,(int)(cy-gap+i)+h2,color);} break;
            case 8:
                context.fill((int)(cx-gap-size),cy-h1,(int)(cx-gap),cy+h2,color);context.fill((int)(cx+gap),cy-h1,(int)(cx+gap+size),cy+h2,color);context.fill(cx-h1,(int)(cy-gap-size),cx+h2,(int)(cy-gap),color); break;
            default:
                context.fill((int)(cx-gap-size),cy-h1,(int)(cx-gap),cy+h2,color);context.fill((int)(cx+gap),cy-h1,(int)(cx+gap+size),cy+h2,color);context.fill(cx-h1,(int)(cy-gap-size),cx+h2,(int)(cy-gap),color);context.fill(cx-h1,(int)(cy+gap),cx+h2,(int)(cy+gap+size),color); break;
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() { if (this.client != null) this.client.setScreen(parent); }
}

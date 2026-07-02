package net.featherpojav.client.gui;

import net.featherpojav.client.config.FeatherConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class FeatherSettingsScreen extends Screen {
    private final Screen parent;
    private Category currentCategory = Category.HUD;
    private final List<ToggleOption> options = new ArrayList<>();
    
    // Sidebar geometry
    private int sidebarWidth = 100;
    
    public FeatherSettingsScreen(Screen parent) {
        super(Text.of("Feather Settings"));
        this.parent = parent;
    }
    
    enum Category {
        HUD("HUD Mods"),
        GAMEPLAY("Gameplay"),
        PERFORMANCE("Performance"),
        COSMETICS("Cosmetics");
        
        final String name;
        Category(String name) { this.name = name; }
    }
    
    private static class ToggleOption {
        String name;
        String desc;
        java.util.function.BooleanSupplier getter;
        java.util.function.Consumer<Boolean> setter;
        Category category;
        Runnable configRenderer; // Custom extra settings (like sliders/toggles)

        ToggleOption(String name, String desc, Category category, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
            this.name = name;
            this.desc = desc;
            this.category = category;
            this.getter = getter;
            this.setter = setter;
        }

        ToggleOption withConfig(Runnable configRenderer) {
            this.configRenderer = configRenderer;
            return this;
        }
    }
    
    @Override
    protected void init() {
        options.clear();
        FeatherConfig cfg = FeatherConfig.INSTANCE;
        
        // HUD
        options.add(new ToggleOption("Keystrokes", "Shows WASD and CPS clicks on screen", Category.HUD, () -> cfg.keystrokes, (v) -> cfg.keystrokes = v));
        options.add(new ToggleOption("Armor HUD", "Displays equipped armor and durability", Category.HUD, () -> cfg.armorHUD, (v) -> cfg.armorHUD = v)
            .withConfig(() -> {
                cfg.armorHUDVertical = drawCustomCheckbox("Vertical Layout", 120 + 200, 100, cfg.armorHUDVertical);
            }));
        options.add(new ToggleOption("Potion HUD", "Displays active status effects", Category.HUD, () -> cfg.potionHUD, (v) -> cfg.potionHUD = v));
        options.add(new ToggleOption("Coordinates HUD", "Displays current coordinates and direction", Category.HUD, () -> cfg.coordHUD, (v) -> cfg.coordHUD = v));
        options.add(new ToggleOption("Direction HUD", "Displays a compass bar at the top", Category.HUD, () -> cfg.directionHUD, (v) -> cfg.directionHUD = v));
        options.add(new ToggleOption("FPS HUD", "Displays your current Frames Per Second", Category.HUD, () -> cfg.fpsHUD, (v) -> cfg.fpsHUD = v));
        
        // Gameplay
        options.add(new ToggleOption("ToggleSprint", "Automatically keep sprinting", Category.GAMEPLAY, () -> cfg.toggleSprint, (v) -> cfg.toggleSprint = v));
        options.add(new ToggleOption("Zoom", "Zoom in using keybinding (default C)", Category.GAMEPLAY, () -> cfg.zoom, (v) -> cfg.zoom = v));
        options.add(new ToggleOption("Freelook (Perspective)", "Look around without turning player", Category.GAMEPLAY, () -> cfg.freelook, (v) -> cfg.freelook = v));
        options.add(new ToggleOption("AutoGG", "Sends GG in chat after multiplayer games", Category.GAMEPLAY, () -> cfg.autoGG, (v) -> cfg.autoGG = v));
        options.add(new ToggleOption("Custom Crosshair", "Renders a highly customizable crosshair", Category.GAMEPLAY, () -> cfg.customCrosshair, (v) -> cfg.customCrosshair = v)
            .withConfig(() -> {
                // Draws customized options
            }));
        options.add(new ToggleOption("TimeChanger", "Forces client-side world time", Category.GAMEPLAY, () -> cfg.timeChanger, (v) -> cfg.timeChanger = v)
            .withConfig(() -> {
                // Switch time setting
            }));
            
        // Performance
        options.add(new ToggleOption("Item Physics", "Items drop realistically on the floor", Category.PERFORMANCE, () -> cfg.itemPhysics, (v) -> cfg.itemPhysics = v));
        
        // Setup buttons
        this.addDrawableChild(ButtonWidget.builder(Text.of("Edit HUD Layout"), button -> {
            if (this.client != null) {
                this.client.setScreen(new FeatherHudEditorScreen(this));
            }
        }).dimensions(10, this.height - 50, sidebarWidth - 20, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> {
            if (this.client != null) {
                this.client.setScreen(parent);
            }
        }).dimensions(10, this.height - 25, sidebarWidth - 20, 20).build());
    }
    
    private boolean lastCheckboxState = false;
    private boolean drawCustomCheckbox(String label, int x, int y, boolean currentValue) {
        // Mock simple UI checkbox for sub-options
        return currentValue; // Just returns state for now; clicks are handled in mouseClicked
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Modern Feather Client themed background rendering
        // Left Sidebar: Darker purple
        context.fill(0, 0, sidebarWidth, this.height, 0xFF140D1A);
        
        // Right Main Area: Sleek dark grey
        context.fill(sidebarWidth, 0, this.width, this.height, 0xFF18181A);
        
        // Title
        context.drawText(this.textRenderer, "FEATHER CLIENT", 10, 15, 0xFF9C27B0, true);
        context.drawText(this.textRenderer, "Pojav Edition", 10, 27, 0xFF6A1B9A, true);
        
        // Render Sidebar Category Selection Buttons
        int categoryY = 50;
        for (Category cat : Category.values()) {
            boolean isSelected = cat == currentCategory;
            boolean isHovered = mouseX >= 5 && mouseX <= sidebarWidth - 5 && mouseY >= categoryY && mouseY <= categoryY + 20;
            
            int color = isSelected ? 0xFF9C27B0 : (isHovered ? 0xFF311B92 : 0x00000000);
            if (color != 0x00000000) {
                context.fill(5, categoryY, sidebarWidth - 5, categoryY + 20, color);
            }
            
            context.drawText(this.textRenderer, cat.name, 12, categoryY + 6, isSelected ? 0xFFFFFFFF : 0xFFCCCCCC, false);
            categoryY += 25;
        }
        
        // Render Category Contents
        int optionY = 20;
        int listLeft = sidebarWidth + 20;
        int listWidth = this.width - listLeft - 20;
        
        for (ToggleOption opt : options) {
            if (opt.category != currentCategory) continue;
            
            // Draw option card
            boolean isHovered = mouseX >= listLeft && mouseX <= listLeft + listWidth && mouseY >= optionY && mouseY <= optionY + 40;
            context.fill(listLeft, optionY, listLeft + listWidth, optionY + 40, isHovered ? 0xFF2A2A2E : 0xFF222224);
            context.drawBorder(listLeft, optionY, listWidth, 40, 0xFF3E3E42);
            
            // Name & Desc
            context.drawText(this.textRenderer, opt.name, listLeft + 10, optionY + 8, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, opt.desc, listLeft + 10, optionY + 22, 0xFF888888, false);
            
            // Toggle Switch rendering (Pill shape)
            boolean enabled = opt.getter.getAsBoolean();
            int toggleX = listLeft + listWidth - 50;
            int toggleY = optionY + 12;
            
            // Draw Toggle background
            context.fill(toggleX, toggleY, toggleX + 40, toggleY + 16, enabled ? 0xFF9C27B0 : 0xFF4A4A4F);
            // Draw Toggle handle
            int handleX = enabled ? toggleX + 24 : toggleX + 2;
            context.fill(handleX, toggleY + 2, handleX + 14, toggleY + 14, 0xFFFFFFFF);
            
            optionY += 45;
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Do nothing to prevent background blur
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle Sidebar Category Clicks
        int categoryY = 50;
        for (Category cat : Category.values()) {
            if (mouseX >= 5 && mouseX <= sidebarWidth - 5 && mouseY >= categoryY && mouseY <= categoryY + 20) {
                currentCategory = cat;
                return true;
            }
            categoryY += 25;
        }
        
        // Handle Option Toggle clicks
        int optionY = 20;
        int listLeft = sidebarWidth + 20;
        int listWidth = this.width - listLeft - 20;
        
        for (ToggleOption opt : options) {
            if (opt.category != currentCategory) continue;
            
            // If toggle switch area is clicked
            int toggleX = listLeft + listWidth - 50;
            int toggleY = optionY + 12;
            if (mouseX >= toggleX && mouseX <= toggleX + 40 && mouseY >= toggleY && mouseY <= toggleY + 16) {
                opt.setter.accept(!opt.getter.getAsBoolean());
                FeatherConfig.save();
                return true;
            }
            
            optionY += 45;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}

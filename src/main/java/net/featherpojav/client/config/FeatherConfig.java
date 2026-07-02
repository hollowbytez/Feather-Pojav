package net.featherpojav.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FeatherConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "featherpojav.json");
    
    public static FeatherConfig INSTANCE = new FeatherConfig();

    // General Client Mod Toggles
    // Category: HUD
    public boolean keystrokes = true;
    public boolean armorHUD = true;
    public boolean potionHUD = true;
    public boolean directionHUD = true;
    public boolean coordHUD = true;
    public boolean fpsHUD = true;
    public boolean comboDisplay = true;
    public boolean pingDisplay = true;
    public boolean playtime = true;
    public boolean reachDisplay = true;
    public boolean serverAddress = true;
    public boolean speedMeter = true;
    public boolean stopwatch = true;
    public boolean itemCounter = true;
    public boolean armorBar = true;
    public boolean armorStatus = true;
    public boolean bossBar = true;
    public boolean hearts = true;
    public boolean packDisplay = true;
    public boolean scoreboard = true;

    // Category: Gameplay
    public boolean autoText = false;
    public boolean autoPerspective = false;
    public boolean blockIndicator = false;
    public boolean customAdvancements = false;
    public boolean customChat = false;
    public boolean deathInfo = false;
    public boolean dropPrevention = false;
    public boolean elytras = false;
    public boolean fovChanger = false;
    public boolean hitIndicator = false;
    public boolean horses = false;
    public boolean hypixelUtilities = false;
    public boolean inventoryManagement = false;
    public boolean itemInfo = false;
    public boolean jumpReset = false;
    public boolean reconnect = false;
    public boolean saturation = false;
    public boolean screenshotUtility = false;
    public boolean searchKeybind = false;
    public boolean snaplook = false;
    public boolean toggleSprint = true;
    public boolean zoom = true;
    public boolean freelook = true;
    public boolean autoGG = true;
    public boolean timeChanger = false;

    // Category: Performance
    public boolean backups = false;
    public boolean cullLogs = false;
    public boolean customFog = false;
    public boolean itemDespawn = false;

    // Category: Cosmetics
    public boolean animations = false;
    public boolean blockOverlay = false;
    public boolean fullbright = false;
    public boolean camera = false;
    public boolean colorSaturation = false;
    public boolean customCrosshair = false;
    public boolean customF3 = false;
    public boolean damageIndicator = false;
    public boolean darkMode = false;
    public boolean discordRPC = false;
    public boolean glint = false;
    public boolean hitbox = false;
    public boolean lightLevel = false;
    public boolean lootBeams = false;
    public boolean mobOverlay = false;
    public boolean motionBlur = false;
    public boolean mousestrokes = false;
    public boolean nametags = false;
    public boolean nickHider = false;
    public boolean packOrganizer = false;
    public boolean soundFilters = false;
    public boolean itemPhysics = true;

    // Custom mod details
    public String autoTextCommand = "/lobby";
    public int timeChangerMode = 1; // 0 = Morning, 1 = Day, 2 = Sunset, 3 = Night

    // HUD Positions
    public int keystrokesX = 10;
    public int keystrokesY = 100;
    
    public int armorHUDX = 10;
    public int armorHUDY = 240;
    public boolean armorHUDVertical = true;
    
    public int potionHUDX = 10;
    public int potionHUDY = 320;
    
    public int directionHUDX = 200;
    public int directionHUDY = 10;
    
    public int coordHUDX = 10;
    public int coordHUDY = 10;
    
    public int fpsHUDX = 10;
    public int fpsHUDY = 50;

    public int comboDisplayX = 10;
    public int comboDisplayY = 70;

    public int pingDisplayX = 10;
    public int pingDisplayY = 150;

    public int playtimeX = 10;
    public int playtimeY = 170;

    public int reachDisplayX = 10;
    public int reachDisplayY = 190;

    public int serverAddressX = 10;
    public int serverAddressY = 210;

    public int speedMeterX = 10;
    public int speedMeterY = 230;

    public int stopwatchX = 10;
    public int stopwatchY = 250;

    public int itemCounterX = 10;
    public int itemCounterY = 270;

    public int packDisplayX = 10;
    public int packDisplayY = 290;
    
    // Crosshair Customize
    public int crosshairColor = 0xFF00FF00;
    public float crosshairSize = 5.0f;
    public float crosshairThickness = 1.0f;
    public float crosshairGap = 2.0f;
    public int crosshairPreset = 0;
    public float zoomMagnification = 4.0f;
    
    public long timeChangerTicks = 6000;
    public float particleMultiplier = 1.0f;
    
    public int themeColor = 0xFF9C27B0;
    public int backgroundColor = 0x80000000;
    public int textColor = 0xFFFFFFFF;
    
    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, FeatherConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new FeatherConfig();
                }
            } catch (Exception e) {
                System.err.println("[FeatherPojav] Failed to load config: " + e.getMessage());
                INSTANCE = new FeatherConfig();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            System.err.println("[FeatherPojav] Failed to save config: " + e.getMessage());
        }
    }
}

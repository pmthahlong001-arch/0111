package com.automod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoMod implements ModInitializer {
    public static final String MOD_ID = "automod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // ===== AUTO TOTEM =====
    public static boolean autoTotemEnabled = false;
    public static int totemDelay = 5;           // 1-100 ticks
    public static boolean totemOpenInv = false; // mở inventory khi thay totem

    // ===== AUTO CART =====
    public static boolean autoCartEnabled = false;
    public static String cartWeapon = "bow";    // "bow" hoặc "crossbow"
    public static int cartDelay = 5;            // delay sau khi bắn mũi ra
    public static boolean cartReclaim = false;  // lấy lại minecart
    public static int cartReclaimSlot = 0;      // slot lấy lại (0-8)
    public static boolean cartCrossbowFlame = false; // crossbow có flame arrow

    @Override
    public void onInitialize() {
        LOGGER.info("AutoMod loaded!");
    }
}

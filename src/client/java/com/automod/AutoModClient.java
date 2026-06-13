package com.automod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AutoModClient implements ClientModInitializer {

    // RShift → mở menu
    private static KeyBinding menuKey;
    // B → kích hoạt AutoCart (crossbow manual trigger)
    private static KeyBinding cartTriggerKey;

    @Override
    public void onInitializeClient() {

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.automod.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.automod"
        ));

        cartTriggerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.automod.cart_trigger",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.automod"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Menu key
            while (menuKey.wasPressed()) {
                if (client.currentScreen instanceof AutoModScreen) {
                    client.currentScreen.close();
                } else {
                    client.setScreen(new AutoModScreen());
                }
            }

            // Cart manual trigger (crossbow)
            while (cartTriggerKey.wasPressed()) {
                if (AutoMod.autoCartEnabled && AutoMod.cartWeapon.equals("crossbow")) {
                    AutoCartHandler.manualTrigger = true;
                }
            }

            // Tick handlers
            AutoTotemHandler.tick(client);
            AutoCartHandler.tick(client);
        });
    }
}

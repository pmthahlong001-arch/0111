package com.automod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotemHandler {

    private static int cooldown = 0;
    private static boolean wasInventoryOpen = false;

    public static void tick(MinecraftClient client) {
        if (!AutoMod.autoTotemEnabled || client.player == null) return;
        if (cooldown > 0) { cooldown--; return; }

        ItemStack offhand = client.player.getOffHandStack();
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) return; // already has totem

        PlayerInventory inv = client.player.getInventory();
        int totemSlot = -1;

        // Search inventory for totem
        for (int i = 0; i < 36; i++) {
            if (inv.getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }

        if (totemSlot < 0) return; // no totem found

        if (AutoMod.totemOpenInv) {
            // Open inventory screen to allow slot interaction
            if (client.currentScreen == null) {
                client.player.openHandledScreen(client.player.playerScreenHandler);
                wasInventoryOpen = false;
            }
        }

        // Map inventory slot to screen handler slot index
        // Hotbar: inv 0-8 → screen 36-44
        // Inventory: inv 9-35 → screen 9-35
        int screenSlot = totemSlot < 9 ? totemSlot + 36 : totemSlot;
        int offhandScreenSlot = 45;

        var handler = client.player.currentScreenHandler;

        // Pick up totem
        client.interactionManager.clickSlot(handler.syncId, screenSlot, 0, SlotActionType.PICKUP, client.player);
        // Place into offhand
        client.interactionManager.clickSlot(handler.syncId, offhandScreenSlot, 0, SlotActionType.PICKUP, client.player);
        // If cursor still has item (offhand wasn't empty), put it back
        if (!handler.getCursorStack().isEmpty()) {
            client.interactionManager.clickSlot(handler.syncId, screenSlot, 0, SlotActionType.PICKUP, client.player);
        }

        // Close inventory if we opened it and option is set
        if (AutoMod.totemOpenInv && !wasInventoryOpen && client.currentScreen != null) {
            client.currentScreen.close();
        }

        cooldown = AutoMod.totemDelay;
    }
}

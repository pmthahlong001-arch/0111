package com.automod;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoCartHandler {

    public enum CartState {
        IDLE,
        WAIT_ARROW_FIRED,   // đã bắn, đang chờ delay
        PLACE_RAIL,         // đặt đường ray
        PLACE_FIRE,         // đặt bật lửa (crossbow only, no flame)
        PLACE_CART,         // đặt minecart lên ray
        RECLAIM_WAIT        // chờ lấy lại minecart
    }

    public static CartState state = CartState.IDLE;
    public static boolean manualTrigger = false; // kích hoạt từ phím bind (crossbow)

    private static int stateTimer = 0;
    private static BlockPos targetRailPos = null;
    private static Direction arrowDirection = null;

    public static void tick(MinecraftClient client) {
        if (!AutoMod.autoCartEnabled || client.player == null) return;

        stateTimer--;
        if (stateTimer > 0) return;

        switch (state) {
            case IDLE -> handleIdle(client);
            case WAIT_ARROW_FIRED -> handleWaitArrow(client);
            case PLACE_RAIL -> handlePlaceRail(client);
            case PLACE_FIRE -> handlePlaceFire(client);
            case PLACE_CART -> handlePlaceCart(client);
            case RECLAIM_WAIT -> handleReclaim(client);
        }
    }

    // ─── IDLE: kiểm tra xem có trigger bắn không ────────────────────────────
    private static void handleIdle(MinecraftClient client) {
        if (AutoMod.cartWeapon.equals("crossbow") && manualTrigger) {
            manualTrigger = false;
            fireWeapon(client);
            stateTimer = AutoMod.cartDelay;
            state = CartState.WAIT_ARROW_FIRED;
        } else if (AutoMod.cartWeapon.equals("bow")) {
            // Bow: auto-fire khi người dùng đang giữ phím bắn
            if (client.options.useKey.isPressed()) {
                state = CartState.WAIT_ARROW_FIRED;
                stateTimer = AutoMod.cartDelay;
            }
        }
    }

    // ─── Sau khi bắn: tính vị trí đặt ray ──────────────────────────────────
    private static void handleWaitArrow(MinecraftClient client) {
        // Lấy hướng nhìn của player → tính block phía trước
        Vec3d look = client.player.getRotationVec(1.0f);
        arrowDirection = Direction.getFacing(look.x, look.y, look.z);

        // Đặt rail ở block phía trước mặt player, cùng độ cao
        BlockPos playerPos = client.player.getBlockPos();
        targetRailPos = playerPos.offset(arrowDirection);

        state = CartState.PLACE_RAIL;
        stateTimer = 2;
    }

    // ─── Đặt đường ray ───────────────────────────────────────────────────────
    private static void handlePlaceRail(MinecraftClient client) {
        int railSlot = findItem(client, Items.RAIL);
        if (railSlot < 0) { reset(); return; }

        selectHotbarSlot(client, railSlot);
        placeBlock(client, targetRailPos.down(), Direction.UP);

        if (AutoMod.cartWeapon.equals("crossbow") && !AutoMod.cartCrossbowFlame) {
            // crossbow không có flame → đặt lửa bên cạnh
            state = CartState.PLACE_FIRE;
        } else {
            // bow hoặc crossbow có flame → đặt cart luôn
            state = CartState.PLACE_CART;
        }
        stateTimer = 3;
    }

    // ─── Đặt bật lửa (crossbow, no flame) ───────────────────────────────────
    private static void handlePlaceFire(MinecraftClient client) {
        int flintSlot = findItem(client, Items.FLINT_AND_STEEL);
        if (flintSlot < 0) { reset(); return; }

        selectHotbarSlot(client, flintSlot);

        // Đặt lửa bên cạnh đường ray (tính toán để mũi tên đi qua)
        // Lửa đặt ở block kề cạnh đường ray, theo hướng mũi tên tới
        Direction perpendicular = arrowDirection.rotateYClockwise();
        BlockPos firePos = targetRailPos.offset(perpendicular);
        placeBlock(client, firePos, Direction.UP);

        state = CartState.PLACE_CART;
        stateTimer = 2;
    }

    // ─── Đặt minecart TNT ────────────────────────────────────────────────────
    private static void handlePlaceCart(MinecraftClient client) {
        int cartSlot = findItem(client, Items.TNT_MINECART);
        if (cartSlot < 0) {
            // thử minecart thường
            cartSlot = findItem(client, Items.MINECART);
        }
        if (cartSlot < 0) { reset(); return; }

        selectHotbarSlot(client, cartSlot);
        placeBlock(client, targetRailPos, Direction.UP);

        if (AutoMod.cartReclaim) {
            state = CartState.RECLAIM_WAIT;
            stateTimer = 20; // chờ 1 giây trước khi nhặt lại
        } else {
            reset();
        }
    }

    // ─── Lấy lại minecart ────────────────────────────────────────────────────
    private static void handleReclaim(MinecraftClient client) {
        // Swap item ở slot mục tiêu về slot cartReclaimSlot
        PlayerInventory inv = client.player.getInventory();
        inv.selectedSlot = AutoMod.cartReclaimSlot;
        // Nhặt bằng cách tương tác (thực tế cần entity pickup — đây là gợi ý slot)
        reset();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private static void fireWeapon(MinecraftClient client) {
        client.options.useKey.setPressed(true);
        // release sau 1 tick để bắn
        client.options.useKey.setPressed(false);
        client.interactionManager.stopUsingItem(client.player);
    }

    private static void placeBlock(MinecraftClient client, BlockPos pos, Direction face) {
        Vec3d hitVec = Vec3d.ofCenter(pos).add(Vec3d.of(face.getVector()).multiply(0.5));
        BlockHitResult hit = new BlockHitResult(hitVec, face, pos, false);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
    }

    private static void selectHotbarSlot(MinecraftClient client, int slot) {
        if (slot < 9) {
            client.player.getInventory().selectedSlot = slot;
        }
    }

    private static int findItem(MinecraftClient client, Item target) {
        PlayerInventory inv = client.player.getInventory();
        // hotbar first
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).getItem() == target) return i;
        }
        // then inventory
        for (int i = 9; i < 36; i++) {
            if (inv.getStack(i).getItem() == target) {
                // swap to hotbar slot 7
                client.interactionManager.clickSlot(
                        client.player.currentScreenHandler.syncId,
                        i, 7, SlotActionType.SWAP, client.player
                );
                return 7;
            }
        }
        return -1;
    }

    public static void reset() {
        state = CartState.IDLE;
        stateTimer = 0;
        targetRailPos = null;
        arrowDirection = null;
    }
}

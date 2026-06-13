package com.automod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AutoModScreen extends Screen {

    private static final int W = 240;
    private static final int H = 220;

    // Tabs
    private int currentTab = 0; // 0 = totem, 1 = cart

    public AutoModScreen() {
        super(Text.literal("§6✦ AutoMod Menu ✦"));
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearChildren();
        int cx = this.width / 2;
        int panelX = cx - W / 2;
        int panelY = this.height / 2 - H / 2;

        // Tab buttons
        addDrawableChild(ButtonWidget.builder(
                Text.literal(currentTab == 0 ? "§a[AutoTotem]" : "§7AutoTotem"),
                b -> { currentTab = 0; rebuildWidgets(); }
        ).dimensions(panelX + 5, panelY + 18, 110, 18).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal(currentTab == 1 ? "§a[AutoCart]" : "§7AutoCart"),
                b -> { currentTab = 1; rebuildWidgets(); }
        ).dimensions(panelX + 120, panelY + 18, 110, 18).build());

        if (currentTab == 0) buildTotemTab(panelX, panelY);
        else buildCartTab(panelX, panelY);

        // Close
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§cĐóng"),
                b -> close()
        ).dimensions(cx - 30, panelY + H - 22, 60, 16).build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TOTEM TAB
    // ─────────────────────────────────────────────────────────────────────────
    private void buildTotemTab(int px, int py) {
        int y = py + 45;

        // Toggle AutoTotem
        addDrawableChild(ButtonWidget.builder(
                Text.literal(AutoMod.autoTotemEnabled ? "AutoTotem: §aBẬT" : "AutoTotem: §cTẮT"),
                b -> { AutoMod.autoTotemEnabled = !AutoMod.autoTotemEnabled; rebuildWidgets(); }
        ).dimensions(px + 10, y, 220, 18).build());

        y += 26;

        // Delay controls
        addDrawableChild(ButtonWidget.builder(Text.literal("§c◀"),
                b -> { if (AutoMod.totemDelay > 1) AutoMod.totemDelay--; rebuildWidgets(); }
        ).dimensions(px + 10, y, 22, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§a▶"),
                b -> { if (AutoMod.totemDelay < 100) AutoMod.totemDelay++; rebuildWidgets(); }
        ).dimensions(px + 208, y, 22, 18).build());

        y += 26;

        // Open inventory toggle
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Mở inv khi thay: " + (AutoMod.totemOpenInv ? "§a✔" : "§c✘")),
                b -> { AutoMod.totemOpenInv = !AutoMod.totemOpenInv; rebuildWidgets(); }
        ).dimensions(px + 10, y, 220, 18).build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CART TAB
    // ─────────────────────────────────────────────────────────────────────────
    private void buildCartTab(int px, int py) {
        int y = py + 45;

        // Toggle AutoCart
        addDrawableChild(ButtonWidget.builder(
                Text.literal(AutoMod.autoCartEnabled ? "AutoCart: §aBẬT" : "AutoCart: §cTẮT"),
                b -> { AutoMod.autoCartEnabled = !AutoMod.autoCartEnabled; rebuildWidgets(); }
        ).dimensions(px + 10, y, 220, 18).build());

        y += 26;

        // Weapon selection: Bow / Crossbow
        addDrawableChild(ButtonWidget.builder(
                Text.literal(AutoMod.cartWeapon.equals("bow") ? "§a[Bow] §7Crossbow" : "§7Bow §a[Crossbow]"),
                b -> {
                    AutoMod.cartWeapon = AutoMod.cartWeapon.equals("bow") ? "crossbow" : "bow";
                    rebuildWidgets();
                }
        ).dimensions(px + 10, y, 220, 18).build());

        y += 26;

        // Delay controls
        addDrawableChild(ButtonWidget.builder(Text.literal("§c◀"),
                b -> { if (AutoMod.cartDelay > 1) AutoMod.cartDelay--; rebuildWidgets(); }
        ).dimensions(px + 10, y, 22, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§a▶"),
                b -> { if (AutoMod.cartDelay < 100) AutoMod.cartDelay++; rebuildWidgets(); }
        ).dimensions(px + 208, y, 22, 18).build());

        y += 26;

        // If crossbow: show flame toggle
        if (AutoMod.cartWeapon.equals("crossbow")) {
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Mũi tên lửa (Flame): " + (AutoMod.cartCrossbowFlame ? "§a✔" : "§c✘")),
                    b -> { AutoMod.cartCrossbowFlame = !AutoMod.cartCrossbowFlame; rebuildWidgets(); }
            ).dimensions(px + 10, y, 220, 18).build());
            y += 26;
        }

        // Reclaim minecart toggle
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Lấy lại minecart: " + (AutoMod.cartReclaim ? "§a✔" : "§c✘")),
                b -> { AutoMod.cartReclaim = !AutoMod.cartReclaim; rebuildWidgets(); }
        ).dimensions(px + 10, y, 220, 18).build());

        y += 26;

        // Reclaim slot (only shown if reclaim is on)
        if (AutoMod.cartReclaim) {
            addDrawableChild(ButtonWidget.builder(Text.literal("§c◀"),
                    b -> { if (AutoMod.cartReclaimSlot > 0) AutoMod.cartReclaimSlot--; rebuildWidgets(); }
            ).dimensions(px + 10, y, 22, 18).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("§a▶"),
                    b -> { if (AutoMod.cartReclaimSlot < 8) AutoMod.cartReclaimSlot++; rebuildWidgets(); }
            ).dimensions(px + 208, y, 22, 18).build());
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int cx = this.width / 2;
        int px = cx - W / 2;
        int py = this.height / 2 - H / 2;

        // Panel background
        ctx.fill(px, py, px + W, py + H, 0xCC111111);
        // Border
        ctx.fill(px, py, px + W, py + 2, 0xFFFFAA00);
        ctx.fill(px, py + H - 2, px + W, py + H, 0xFFFFAA00);
        ctx.fill(px, py, px + 2, py + H, 0xFFFFAA00);
        ctx.fill(px + W - 2, py, px + W, py + H, 0xFFFFAA00);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer, title, cx, py + 6, 0xFFFFAA);

        // Totem tab content labels
        if (currentTab == 0) {
            int y = this.height / 2 - H / 2 + 45 + 26;
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§eDelay đổi totem: §f" + AutoMod.totemDelay + " §7ticks"),
                    cx, y + 4, 0xFFFFFF);
        }

        // Cart tab content labels
        if (currentTab == 1) {
            int baseY = this.height / 2 - H / 2 + 45;
            int y = baseY + 26 + 26;
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§eDelay sau khi bắn: §f" + AutoMod.cartDelay + " §7ticks"),
                    cx, y + 4, 0xFFFFFF);

            if (AutoMod.cartReclaim) {
                int reclaimLabelY = baseY + 26 * (AutoMod.cartWeapon.equals("crossbow") ? 6 : 5);
                ctx.drawCenteredTextWithShadow(textRenderer,
                        Text.literal("§eSlot lấy lại: §f" + (AutoMod.cartReclaimSlot + 1)),
                        cx, reclaimLabelY + 4, 0xFFFFFF);
            }
        }

        // Footer hint
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7[RShift] Mở/đóng  |  [B] Kích hoạt Cart"),
                cx, py + H - 10, 0x888888);

        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}

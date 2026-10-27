package com.example.donutauction.ui;

import com.example.donutauction.DonutAuctionClient;
import com.example.donutauction.config.ConfigManager;
import com.example.donutauction.config.ModConfig;
import com.example.donutauction.notify.Notifier;
import com.example.donutauction.timer.AuctionTimer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import java.util.List;

/** Non-interactive overlay. Draws only; never captures input, so it cannot interfere with gameplay. */
public final class HudRenderer {
    private HudRenderer() {}

    public static void register() {
        HudElementRegistry.addLast(Identifier.of(DonutAuctionClient.MOD_ID, "hud"), HudRenderer::render);
    }

    private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        ModConfig c = ConfigManager.get();

        // Per-frame tick gives alerts better than 50 ms resolution; the END_CLIENT_TICK call is a fallback.
        DonutAuctionClient.TIMER.tick(c, DonutAuctionClient.NOTIFIER);

        long now = System.currentTimeMillis();
        if (c.screenFlash && now < Notifier.flashUntilMs) {
            int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight(), t = 5, col = Notifier.flashColor;
            ctx.fill(0, 0, w, t, col);
            ctx.fill(0, h - t, w, h, col);
            ctx.fill(0, t, t, h - t, col);
            ctx.fill(w - t, t, w, h - t, col);
        }

        if (!c.hudEnabled || mc.currentScreen instanceof MainScreen) return;
        if (DonutAuctionClient.TIMER.state() == AuctionTimer.State.IDLE
                && !c.hudShowWhenIdle && DonutAuctionClient.TRACKER.latestBid() == null) return;

        List<InfoLines.Line> lines = InfoLines.build();
        int width = 0;
        for (InfoLines.Line l : lines) width = Math.max(width, mc.textRenderer.getWidth(l.text()));
        int x = c.hudX, y = c.hudY, lh = 10;
        ctx.fill(x - 3, y - 3, x + width + 3, y + lines.size() * lh + 1, 0x90000000);
        for (InfoLines.Line l : lines) {
            ctx.drawTextWithShadow(mc.textRenderer, l.text(), x, y, l.color());
            y += lh;
        }
    }
}

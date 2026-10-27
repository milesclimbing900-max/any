package com.example.donutauction.ui;

import com.example.donutauction.DonutAuctionClient;
import com.example.donutauction.calc.BidCalculator;
import com.example.donutauction.chat.ChatTracker;
import com.example.donutauction.config.ConfigManager;
import com.example.donutauction.config.ModConfig;
import com.example.donutauction.config.ParseRule;
import com.example.donutauction.config.StreamerProfile;
import com.example.donutauction.timer.AuctionTimer;
import com.example.donutauction.util.Fmt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.util.ArrayList;
import java.util.List;

/** Builds the status text shared by the HUD and the main screen. */
public final class InfoLines {
    public record Line(String text, int color) {}

    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA, GREEN = 0xFF55FF55,
            YELLOW = 0xFFFFFF55, RED = 0xFFFF5555, AQUA = 0xFF55FFFF;

    private InfoLines() {}

    public static List<Line> build() {
        ModConfig c = ConfigManager.get();
        AuctionTimer t = DonutAuctionClient.TIMER;
        ChatTracker tr = DonutAuctionClient.TRACKER;
        List<Line> out = new ArrayList<>();

        StreamerProfile sp = c.selected();
        long delay = sp == null ? 0 : sp.delayMs;
        out.add(new Line(sp == null ? "Streamer: (none selected)"
                : "Streamer: " + sp.name + " | delay " + Fmt.delay(delay) + " (your estimate)", WHITE));

        switch (t.state()) {
            case IDLE -> out.add(new Line("Timer: idle", GRAY));
            case RUNNING, PAUSED -> {
                long rem = t.remainingMs();
                int col = rem <= 3000 ? RED : rem <= 10_000 ? YELLOW : GREEN;
                String paused = t.state() == AuctionTimer.State.PAUSED ? " [PAUSED]" : "";
                out.add(new Line("[EST] Server end in: " + Fmt.time(rem, c.preciseBelowSec) + paused, col));
                out.add(new Line("[EST] Stream shows end in: " + Fmt.time(t.streamRemainingMs(delay), c.preciseBelowSec), AQUA));
                out.add(new Line("Timer source: " + (t.origin() == AuctionTimer.Origin.CHAT_DETECTED
                        ? "start seen in chat (end still estimated)" : "manual start (estimate)"), GRAY));
            }
            case FINISHED -> {
                if (t.isVerifiedEnd()) {
                    long err = t.verifiedErrorMs();
                    out.add(new Line("[VERIFIED] End seen in chat; estimate was off by " + (err >= 0 ? "+" : "") + err + " ms", GREEN));
                } else {
                    out.add(new Line("[EST] End reached; no server end message seen", YELLOW));
                }
                long sr = t.streamRemainingMs(delay);
                if (sr > 0) out.add(new Line("Stream shows end in: " + Fmt.time(sr, c.preciseBelowSec), AQUA));
            }
        }

        ChatTracker.AmountRecord b = tr.latestBid();
        if (b == null) {
            out.add(new Line("Latest bid: none observed", GRAY));
        } else {
            ChatTracker.AmountRecord pb = tr.previousBid();
            out.add(new Line("Latest bid: " + Fmt.amount(b.amount()) + " by " + b.user()
                    + (pb != null ? " (prev " + Fmt.amount(pb.amount()) + ")" : ""), WHITE));
        }
        ChatTracker.AmountRecord p = tr.latestPayment();
        if (p != null) {
            out.add(new Line("Latest payment: " + Fmt.amount(p.amount()) + " from " + p.user() + " (not necessarily a bid)", GRAY));
        }

        BidCalculator.Suggestion s = tr.suggestion();
        if (s.hasBase()) {
            out.add(new Line("Suggested next bid: " + Fmt.amount(s.suggested())
                    + (c.maxBudget > 0 ? (s.overBudget() ? "  OVER BUDGET (max " + Fmt.amount(c.maxBudget) + ")"
                    : "  within budget") : ""), s.overBudget() ? RED : YELLOW));
        } else {
            out.add(new Line("Suggested next bid: waiting for an observed bid", GRAY));
        }
        if (!tr.lastResult().isEmpty()) out.add(new Line("Result: " + tr.lastResult(), WHITE));

        // Data / connection status
        MinecraftClient mc = MinecraftClient.getInstance();
        ServerInfo si = mc.getCurrentServerEntry();
        String where = !tr.connected() ? "offline" : si != null ? si.address : "singleplayer";
        int active = 0;
        for (ParseRule r : c.rules) if (r.enabled && r.pattern() != null) active++;
        long now = System.currentTimeMillis();
        out.add(new Line("Data: " + where + " | rules " + active + " | last chat "
                + Fmt.age(tr.lastChatAtMs() < 0 ? -1 : now - tr.lastChatAtMs())
                + " | last match " + Fmt.age(tr.lastMatchAtMs() < 0 ? -1 : now - tr.lastMatchAtMs()),
                tr.connected() ? GRAY : RED));
        if (active == 0 || !c.parsingEnabled) {
            out.add(new Line("No parsing rules active: chat is not being read for bids", YELLOW));
        }
        return out;
    }
}

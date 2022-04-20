package me.ayunami2000.ayunextras;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AyunExtras extends JavaPlugin implements Listener {
    private static final Pattern transMatch1 = Pattern.compile("[\"']translate[\"']:\"(?:(?:[^\"\\\\])|\\\\.)*\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern transMatch2 = Pattern.compile("[\"']translate[\"']:'(?:(?:[^'\\\\])|\\\\.)*'", Pattern.CASE_INSENSITIVE);

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {}

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (isIllegal(event.getMessage())) event.setCancelled(true);
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent event) {
        if (isIllegal(event.getCommand())) event.setCancelled(true);
    }

    private boolean isIllegal(String cmd) {
        String trimMsg = UnicodeEscaper.unescapeJavaString(cmd.replace(" ",""));
        Matcher matcher1 = transMatch1.matcher(trimMsg);
        Matcher matcher2 = transMatch2.matcher(trimMsg);
        boolean illegal = false;
        while (matcher1.find()) {
            String match = matcher1.group();
            if (match.toLowerCase().contains("translation.test.invalid") || match.contains("%")) {
                illegal = true;
                break;
            }
        }
        if (!illegal) {
            while (matcher2.find()) {
                String match = matcher2.group();
                if (match.toLowerCase().contains("translation.test.invalid") || match.contains("%")) {
                    illegal = true;
                    break;
                }
            }
        }
        return illegal;
    }
}

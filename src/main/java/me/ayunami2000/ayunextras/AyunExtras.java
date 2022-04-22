package me.ayunami2000.ayunextras;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AyunExtras extends JavaPlugin implements Listener {
    private static final Pattern transMatch1 = Pattern.compile("[\"']translate[\"']:\"(?:(?:[^\"\\\\])|\\\\.)*\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern transMatch2 = Pattern.compile("[\"']translate[\"']:'(?:(?:[^'\\\\])|\\\\.)*'", Pattern.CASE_INSENSITIVE);

    private Discord discord = null;

    public static AyunExtras INSTANCE;

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        if (this.getConfig().getBoolean("discord.enabled")) {
            discord = new Discord(this.getConfig().getString("discord.token"), this.getConfig().getString("discord.chat"), this.getConfig().getString("discord.console"), this.getConfig().getString("discord.status"));
            if (discord.api == null) discord = null;
        }
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("boost").setExecutor(this);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("&d&lServer will restart in &9&l&n15&d&l minutes!"), (3 * 60 + 45) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("&d&lServer will restart in &9&l&n10&d&l minutes!"), (3 * 60 + 50) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("&d&lServer will restart in &9&l&n5&d&l minutes!"), (3 * 60 + 55) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("&d&lServer will restart in &9&l&n2&d&l minutes!"), (3 * 60 + 58) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("&d&lServer will restart in &9&l&n1&d&l minute!"), (3 * 60 + 59) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("&d&lServer will restart &9&l&nany second now&d&l!"), 4 * 60 * 60 * 20);
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        if (discord != null) {
            discord.end();
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (discord != null) {
            discord.sendChat(event.getPlayer().getName(), event.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName();
        if (cmdName.equals("boost")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.setVelocity(player.getVelocity().clone().add(player.getEyeLocation().getDirection()));
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (isIllegal(event.getMessage())) event.setCancelled(true);
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent event) {
        if (isIllegal(event.getCommand())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().clear();
    }

    private boolean isIllegal(String cmd) {
        String trimMsg = parseCharCodes(cmd.replace(" ",""));
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

    // from kaboom extras
    private String parseCharCodes(final String input) {
        if (input.contains("\\u")) {
            StringBuilder output = new StringBuilder();
            String[] split = input.split("\\\\u");
            int index = 0;
            for (String item:split) {
                if (index == 0) {
                    output.append(item);
                } else {
                    String charCode = item.substring(0, 4);
                    output.append((char) Integer.parseInt(charCode, 16));
                    output.append(item.substring(4));
                }
                index++;
            }
            return output.toString();
        } else {
            return input;
        }
    }
}

package me.ayunami2000.ayunextras;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AyunExtras extends JavaPlugin implements Listener {
    private static final Pattern transMatch1 = Pattern.compile("[\"']translate[\"']:\"(?:(?:[^\"\\\\])|\\\\.)*\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern transMatch2 = Pattern.compile("[\"']translate[\"']:'(?:(?:[^'\\\\])|\\\\.)*'", Pattern.CASE_INSENSITIVE);

    private Discord discord = null;

    private final Set<Pattern> kickChats = new HashSet<>();
    private final Set<Pattern> blockChats = new HashSet<>();
    private final Set<Pattern> blockNames = new HashSet<>();

    private boolean captcha = true;
    private Set<Captcha> captchas = new HashSet<>();

    public static AyunExtras INSTANCE;

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        loadConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("boost").setExecutor(this);
        this.getCommand("ayunkick").setExecutor(this);
        this.getCommand("ayunrl").setExecutor(this);
        this.getCommand("ayuncap").setExecutor(this);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("§d§lServer will restart in §9§l§n15§d§l minutes!"), (3 * 60 + 45) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("§d§lServer will restart in §9§l§n10§d§l minutes!"), (3 * 60 + 50) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("§d§lServer will restart in §9§l§n5§d§l minutes!"), (3 * 60 + 55) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("§d§lServer will restart in §9§l§n2§d§l minutes!"), (3 * 60 + 58) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("§d§lServer will restart in §9§l§n1§d§l minute!"), (3 * 60 + 59) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("§d§lServer will restart §9§l§nany time now§d§l!"), 4 * 60 * 60 * 20);
    }

    private void kickPlayer(Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(AyunExtras.INSTANCE, () -> player.kickPlayer(""));
    }

    private void loadConfig() {
        captcha = this.getConfig().getBoolean("captcha");
        if (!captcha) captchas.clear();
        kickChats.clear();
        List<String> kickChatsRaw = this.getConfig().getStringList("kickChats");
        for (String kickChatRaw : kickChatsRaw) {
            kickChats.add(Pattern.compile(kickChatRaw, Pattern.CASE_INSENSITIVE));
        }
        blockChats.clear();
        List<String> blockChatsRaw = this.getConfig().getStringList("blockChats");
        for (String blockChatRaw : blockChatsRaw) {
            blockChats.add(Pattern.compile(blockChatRaw, Pattern.CASE_INSENSITIVE));
        }
        blockNames.clear();
        List<String> blockNamesRaw = this.getConfig().getStringList("blockUsernames");
        for (String blockNameRaw : blockNamesRaw) {
            blockNames.add(Pattern.compile(blockNameRaw, Pattern.CASE_INSENSITIVE));
        }
        if (discord != null) discord.end();
        if (this.getConfig().getBoolean("discord.enabled")) {
            discord = new Discord(this.getConfig().getString("discord.token"), this.getConfig().getString("discord.chat"), this.getConfig().getString("discord.console"), this.getConfig().getString("discord.status"));
            if (discord.api == null) discord = null;
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerName = event.getPlayer().getName();
        for (Pattern blockName : blockNames) {
            if (blockName.matcher(playerName).matches()) {
                kickPlayer(event.getPlayer());
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "");
            }
        }
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        if (discord != null) {
            discord.end();
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (!event.getReason().isEmpty()) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!captchaChecker(player, event.getMessage())) {
            event.setCancelled(true);
            return;
        }
        if (kickChatMatches(event.getMessage())) {
            kickPlayer(player);
            event.setCancelled(true);
            return;
        }
        if (blockChatMatches(event.getMessage())) {
            event.setCancelled(true);
            return;
        }
        if (discord != null) {
            discord.sendChat(player.getName(), event.getMessage());
        }
    }

    private boolean kickChatMatches(String in) {
        in = parseCharCodes(in);
        for (Pattern kickChat : kickChats) {
            if (kickChat.matcher(in).find()) return true;
        }
        return false;
    }

    private boolean blockChatMatches(String in) {
        in = parseCharCodes(in);
        for (Pattern blockChat : blockChats) {
            if (blockChat.matcher(in).find()) return true;
        }
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName();
        if (cmdName.equals("boost")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.setVelocity(player.getVelocity().clone().add(player.getEyeLocation().getDirection()));
            }
        } else if (cmdName.equals("ayunkick") && sender instanceof ConsoleCommandSender) {
            if (args.length == 0) {
                for (Player player : this.getServer().getOnlinePlayers()) {
                    kickPlayer(player);
                }
            } else {
                for (String playerName : args) {
                    Player player = this.getServer().getPlayer(playerName);
                    if (player != null) kickPlayer(player);
                }
            }
        } else if (cmdName.equals("ayunrl") && sender instanceof ConsoleCommandSender) {
            this.reloadConfig();
            loadConfig();
        } else if (cmdName.equals("ayuncap") && sender instanceof ConsoleCommandSender) {
            captcha = !captcha;
            this.getConfig().set("captcha", captcha);
            this.saveConfig();
        }
        return true;
    }

    private boolean captchaChecker(Player player, String msg) {
        if (captcha) {
            Captcha cap = Captcha.get(captchas, player);
            if (cap != null) {
                if (cap.check(msg)) {
                    if (!cap.passed1) {
                        cap.passed1 = true;
                        player.sendMessage("1/2 cap done");
                    } else {
                        captchas.remove(cap);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> player.setGameMode(GameMode.CREATIVE));
                        player.sendMessage("you may now play on the server!");
                    }
                } else {
                    captchas.remove(cap); // might need to be run after the kick which is delayed
                    kickPlayer(player);
                }
                return false;
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!captchaChecker(player, event.getMessage())) {
            event.setCancelled(true);
            return;
        }
        if (kickChatMatches(event.getMessage())) {
            kickPlayer(player);
            event.setCancelled(true);
            return;
        }
        if (blockChatMatches(event.getMessage())) {
            event.setCancelled(true);
            return;
        }
        if (isIllegal(event.getMessage())) event.setCancelled(true);
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent event) {
        if (kickChatMatches(event.getCommand()) || blockChatMatches(event.getCommand())) {
            event.setCancelled(true);
            return;
        }
        if (isIllegal(event.getCommand())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        if (captcha && Captcha.get(captchas, event.getPlayer()) != null) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (captcha) {
            player.setGameMode(GameMode.SPECTATOR);
            captchas.add(new Captcha(player));
        } else {
            player.setGameMode(GameMode.CREATIVE);
        }
        player.getInventory().clear();
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

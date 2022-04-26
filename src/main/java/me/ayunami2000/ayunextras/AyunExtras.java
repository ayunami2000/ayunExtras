package me.ayunami2000.ayunextras;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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
    private String captchaSecret = "";
    private String captchaHostname = "";
    private String captchaSiteKey = "";
    private int captchaPort = 8765;
    public Set<String> captchas = new HashSet<>();

    private boolean whitelist = false;
    private List<String> whitelisted = new ArrayList<>();

    public static AyunExtras INSTANCE;

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getConfig().addDefault("captcha.port", 8765);
        loadConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("boost").setExecutor(this);
        this.getCommand("ayunkick").setExecutor(this);
        this.getCommand("ayunrl").setExecutor(this);
        this.getCommand("ayuncap").setExecutor(this);
        this.getCommand("ayunwl").setExecutor(this);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("§d§lServer will restart in §9§l§n15§d§l minutes!"), (3 * 60 + 45) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("§d§lServer will restart in §9§l§n10§d§l minutes!"), (3 * 60 + 50) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("§d§lServer will restart in §9§l§n5§d§l minutes!"), (3 * 60 + 55) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("§d§lServer will restart in §9§l§n2§d§l minutes!"), (3 * 60 + 58) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("§d§lServer will restart in §9§l§n1§d§l minute!"), (3 * 60 + 59) * 60 * 20);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.getServer().broadcastMessage("§d§lServer will restart §9§l§nany time now§d§l!"), 4 * 60 * 60 * 20);
    }

    private void kickPlayer(Player player) {
        if (this.getServer().isPrimaryThread()) {
            player.kickPlayer("");
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(AyunExtras.INSTANCE, () -> player.kickPlayer(""));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (captcha && captchas.contains(player.getName())) {
            sendCaptchaMsg(player);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (captcha && captchas.contains(player.getName())) {
            sendCaptchaMsg(player);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (captcha && captchas.contains(player.getName())) {
            sendCaptchaMsg(player);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (captcha && captchas.contains(player.getName())) {
            sendCaptchaMsg(player);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (captcha && captchas.contains(player.getName())) {
            sendCaptchaMsg(player);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            if (captcha && captchas.contains(player.getName())) {
                sendCaptchaMsg(player);
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            if (captcha && captchas.contains(player.getName())) {
                sendCaptchaMsg(player);
                event.setCancelled(true);
                return;
            }
        }
    }

    private void loadConfig() {
        captcha = this.getConfig().getBoolean("captcha.enabled");
        captchaSecret = this.getConfig().getString("captcha.secret");
        captchaHostname = this.getConfig().getString("captcha.hostname");
        captchaSiteKey = this.getConfig().getString("captcha.sitekey");
        captchaPort = this.getConfig().getInt("captcha.port");
        if (!captcha) captchas.clear();
        handleCaptchaToggle();
        whitelist = this.getConfig().getBoolean("whitelist.enabled");
        whitelisted.clear();
        whitelisted = this.getConfig().getStringList("whitelist.users");
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
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        String playerName = event.getName();
        if (whitelist && !whitelisted.contains(playerName)) {
            event.setKickMessage("");
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "");
            return;
        }
        for (Pattern blockName : blockNames) {
            if (blockName.matcher(playerName).matches()) {
                event.setKickMessage("");
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "");
            }
        }
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        HCaptchaHandler.stop();
        if (discord != null) {
            discord.end();
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (!event.getReason().isEmpty()) event.setCancelled(true);
        if (whitelist) event.setReason("Server is currently whitelisted.");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (whitelist && !whitelisted.contains(player.getName())) {
            event.setCancelled(true);
            return;
        }
        if (captcha && captchas.contains(player.getName())) {
            sendCaptchaMsg(player);
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
        boolean senderIsConsole = sender instanceof ConsoleCommandSender;
        if (cmdName.equals("boost")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.setVelocity(player.getVelocity().clone().add(player.getEyeLocation().getDirection()));
            }
        } else if (cmdName.equals("ayunkick") && senderIsConsole) {
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
        } else if (cmdName.equals("ayunrl") && senderIsConsole) {
            this.reloadConfig();
            loadConfig();
        } else if (cmdName.equals("ayuncap") && senderIsConsole) {
            captcha = !captcha;
            handleCaptchaToggle();
            this.getConfig().set("captcha.enabled", captcha);
            this.saveConfig();
        } else if (cmdName.equals("ayunwl") && senderIsConsole) {
            whitelist = !whitelist;
            this.getConfig().set("whitelist.enabled", whitelist);
            this.saveConfig();
        }
        return true;
    }

    public void handleCaptchaToggle() {
        if (captcha) {
            try {
                HCaptchaHandler.create(captchaSecret, captchaHostname, captchaSiteKey, captchaPort);
            } catch (Exception e) {
                e.printStackTrace();
                captcha = false;
                return;
            }
        } else {
            HCaptchaHandler.stop();
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        CommandSender sender = event.getSender();
        if (sender instanceof Player) {
            if (captcha && captchas.contains(sender.getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (whitelist && !whitelisted.contains(player.getName())) {
            event.setCancelled(true);
            return;
        }
        if (captcha && captchas.contains(player.getName())) {
            sendCaptchaMsg(player);
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
        if (captcha && captchas.contains(event.getPlayer().getName())) event.setCancelled(true);
    }

    private void sendCaptchaMsg(Player player) {
        StringBuilder builder = new StringBuilder();
        builder.append("Please verify! https://").append(captchaHostname).append("/captcha");
        try {
            builder.append("#").append(URLEncoder.encode(player.getName(), "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {}
        player.sendMessage(builder.toString());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (captcha) {
            player.setGameMode(GameMode.SPECTATOR);
            sendCaptchaMsg(player);
            captchas.add(player.getName());
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

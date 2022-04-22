package me.ayunami2000.ayunextras;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class Discord {
    public DiscordApi api;
    private ServerTextChannel chatChannel = null;
    private ServerTextChannel consoleChannel = null;
    private ServerTextChannel statusChannel = null;
    private Message statusMessage = null;

    public Discord(String token, String chatId, String consoleId, String statusId) {
        try {
            api = new DiscordApiBuilder().setToken(token).login().join();

            chatChannel = api.getServerTextChannelById(chatId).orElse(null);
            if (chatChannel != null) {
                chatChannel.addMessageCreateListener(messageCreateEvent -> {
                    MessageAuthor messageAuthor = messageCreateEvent.getMessageAuthor();
                    if (!messageAuthor.isYourself()) {
                        StringBuilder out = new StringBuilder("§9§l[§3§lDiscord§9§l] §c");
                        out.append(messageAuthor.getDisplayName()).append("§r: ");
                        out.append(messageCreateEvent.getMessageContent());

                        for (MessageAttachment attachment : messageCreateEvent.getMessageAttachments()) {
                            out.append(" §e§n").append(attachment.getUrl()).append("§r");
                        }

                        Bukkit.broadcastMessage(out.toString());
                    }
                });
                Bukkit.getScheduler().scheduleSyncRepeatingTask(AyunExtras.INSTANCE, this::sendChatQueue, 0, 20);
            }
            consoleChannel = api.getServerTextChannelById(consoleId).orElse(null);
            if (consoleChannel != null) {
                consoleChannel.addMessageCreateListener(messageCreateEvent -> {
                    MessageAuthor messageAuthor = messageCreateEvent.getMessageAuthor();
                    if (!messageAuthor.isYourself()) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(AyunExtras.INSTANCE, () -> {
                            for (String cmd : messageCreateEvent.getMessageContent().split("\n")) {
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
                            }
                        });
                        messageCreateEvent.deleteMessage();
                    }
                });
            }
            statusChannel = api.getServerTextChannelById(statusId).orElse(null);
            if (statusChannel != null) {
                statusChannel.getMessages(1).join().deleteAll();
                statusMessage = statusChannel.sendMessage("Starting server...").join();
                if (statusMessage != null) Bukkit.getScheduler().scheduleSyncRepeatingTask(AyunExtras.INSTANCE, this::sendPlayerListUpdate, 0, 5 * 20);
            }
        } catch (Exception e) {
            e.printStackTrace();
            api = null;
        }
        if (api != null && (chatChannel == null && (statusChannel == null || statusMessage == null))) {
            api.disconnect();
            api = null;
        }
    }

    private String filterMsg(String in) {
        //from https://github.com/Swiiz/discord-escape/blob/master/index.js
        return in.replaceAll("(\\_|\\*|\\~|\\`|\\||\\\\|\\<|\\>|\\:|\\!)", "\\\\$1").replaceAll("@(everyone|here|[!&]?[0-9]{17,21})", "@\u200b\\\\$1");
    }

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)&[0-9A-FK-ORX]");

    private List<String> msgQueue = new ArrayList<>();

    public void sendChat(String username, String msg) {
        if (chatChannel == null) return;
        msg = STRIP_COLOR_PATTERN.matcher(msg).replaceAll("");
        msgQueue.add(filterMsg(username + " » " + msg));
    }

    private void sendChatQueue() {
        if (msgQueue.size() > 0) {
            String fullMsg = String.join("\n", msgQueue);
            if (fullMsg.length() > 1997) fullMsg = fullMsg.substring(0, 1997) + "...";
            chatChannel.sendMessage(fullMsg);
            msgQueue.clear();
        }
    }

    public void sendPlayerListUpdate() {
        if (chatChannel != null) {
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            StringBuilder topic = new StringBuilder();
            topic.append(" (").append(players.size()).append("):\n");
            for (Player player : players) {
                topic.append(player.getName()).append(", ");
            }
            boolean weirdCase = topic.length() == 1997;
            topic.setLength(Math.min(1997, topic.length() - 2));
            if (topic.length() == 1997 && !weirdCase) {
                topic.append("...");
            }
            statusMessage.edit("**Players**" + filterMsg(topic.toString()));
        }
    }

    public void end() {
        api.disconnect();
    }
}

package com.yourname.bugreport.listeners;

import com.yourname.bugreport.BugReporterPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import com.yourname.bugreport.sessions.BugData;
import com.yourname.bugreport.sessions.BugSession;
import net.dv8tion.jda.api.EmbedBuilder;
import java.awt.Color;
import java.util.List;


public class BugReportListener implements Listener {
    private final BugReporterPlugin plugin;


    public BugReportListener(BugReporterPlugin plugin) {
        this.plugin = plugin;
    }

    // Add event handlers here as needed
    @EventHandler
    public void onChat(AsyncPlayerChatEvent ev) {
        Player p = ev.getPlayer();
        var plugin = BugReporterPlugin.INSTANCE;
        var sess   = plugin.sessions.get(p.getName());
        if (sess == null) return;  // not in a bug session

        ev.setCancelled(true);      // consume the chat as answer
        sess.answers.add(ev.getMessage());

        if (sess.hasNext()) {
            // Ask next question in red
            p.sendMessage(ChatColor.RED + sess.nextQuestion());
        } else {
            // All questions answered
            plugin.sessions.remove(p.getName());

            // Build and store BugData
            BugData data = new BugData();
            data.bugId        = sess.bugId;
            data.playerName   = sess.playerName;
            data.questions    = sess.questions;
            data.answers      = sess.answers;
            plugin.localBugs.put(data.bugId, data);
            plugin.saveLocalBugs();

            // Build and send Discord embed as before
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("🐞 New Bug Report: " + data.bugId)
                    .setColor(Color.RED)
                    .addField("Reporter", data.playerName, false);
            for (int i = 0; i < data.questions.size(); i++) {
                eb.addField("Q" + (i+1) + ": " + data.questions.get(i),
                        data.answers.get(i), false);
            }
            plugin.jda.getTextChannelById(
                            plugin.getConfig().getString("reportChannelId"))
                    .sendMessageEmbeds(eb.build())
                    .queue(msg -> {
                        data.discordChannelId = msg.getChannel().getId();
                        data.discordMessageId = msg.getId();
                        plugin.saveLocalBugs();
                    });

            // Thank the player
            p.sendMessage(ChatColor.GREEN + "Thank you! Your bug has been submitted.");
        }
    }
}

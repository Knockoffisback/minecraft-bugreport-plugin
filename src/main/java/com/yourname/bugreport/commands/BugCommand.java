package com.yourname.bugreport.commands;

import com.yourname.bugreport.BugReporterPlugin;
import com.yourname.bugreport.session.BugData;
import com.yourname.bugreport.session.BugSession;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.awt.Color;
import java.time.Instant;

public class BugCommand implements CommandExecutor, Listener {
    public BugCommand() {
        // Register this class to listen for chat events
        BugReporterPlugin.INSTANCE.getServer().getPluginManager()
            .registerEvents(this, BugReporterPlugin.INSTANCE);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players can report bugs!");
            return true;
        }

        // Create a new bug session with unique ID
        String bugId = "BUG-" + Instant.now().toEpochMilli();
        BugSession sess = new BugSession(p.getName(), bugId);
        BugReporterPlugin.INSTANCE.sessions.put(p.getName(), sess);

        // Notify and ask first question (in red)
        p.sendMessage(ChatColor.RED + "Starting bug report. ID: " + bugId);
        p.sendMessage(ChatColor.RED + sess.nextQuestion());
        return true;
    }

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

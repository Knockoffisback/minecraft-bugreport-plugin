package com.yourname.bugreport.commands;

import com.yourname.bugreport.BugReporterPlugin;
import com.yourname.bugreport.sessions.BugSession;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


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


}

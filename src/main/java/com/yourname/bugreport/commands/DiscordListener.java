package com.yourname.bugreport.commands;

import com.yourname.bugreport.BugReporterPlugin;
import com.yourname.bugreport.session.BugData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.Color;

public class DiscordListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent ev) {
        String cmd   = ev.getName();
        String bugId = ev.getOption("id").getAsString();

        BugReporterPlugin plugin = BugReporterPlugin.INSTANCE;
        BugData data = plugin.localBugs.get(bugId);
        if (data == null || data.discordChannelId == null) {
            ev.reply("❌ Bug ID not found.").setEphemeral(true).queue();
            return;
        }

        var channel = plugin.jda.getTextChannelById(data.discordChannelId);
        if (channel == null) {
            ev.reply("❌ Could not find the original report channel.").setEphemeral(true).queue();
            return;
        }

        switch (cmd) {
            case "fix" -> {
                data.fixed = true;
                plugin.saveLocalBugs();

                channel.retrieveMessageById(data.discordMessageId).queue(msg -> {
                    EmbedBuilder eb = new EmbedBuilder(msg.getEmbeds().get(0))
                        .setColor(Color.BLUE)
                        .setFooter("Status: Fixed");
                    msg.editMessageEmbeds(eb.build()).queue();
                });

                ev.reply("✅ Marked " + bugId + " as fixed.").setEphemeral(true).queue();
            }

            case "reward" -> {
                String tier = ev.getOption("tier").getAsString();
                data.rewardTier = tier;
                plugin.saveLocalBugs();

                // 1) Edit the embed to green + footer
                channel.retrieveMessageById(data.discordMessageId).queue(msg -> {
                    EmbedBuilder eb = new EmbedBuilder(msg.getEmbeds().get(0))
                        .setColor(Color.GREEN)
                        .setFooter("Reward: " + tier);
                    msg.editMessageEmbeds(eb.build()).queue();
                });

                // 2) Give in-game items
                Player player = Bukkit.getPlayerExact(data.playerName);
                if (player != null) {
                    player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 16));
                    player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1));

                    // 3) Dispatch the /eco give command on the main thread
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            String consoleCmd = "eco give " + data.playerName + " 5000";
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCmd);
                        }
                    }.runTask(plugin);

                    player.sendMessage("🎁 You’ve been rewarded with 16 Iron, 1 Golden Apple, and $5,000!");
                }

                ev.reply("🎉 Reward applied to `" + bugId + "`: Iron×16, Golden Apple×1, $5,000")
                  .setEphemeral(true)
                  .queue();
            }

            default -> ev.reply("Unknown command.").setEphemeral(true).queue();
        }
    }
}

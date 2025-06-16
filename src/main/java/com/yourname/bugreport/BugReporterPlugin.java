package com.yourname.bugreport;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yourname.bugreport.listeners.DiscordListener;
import com.yourname.bugreport.commands.BugCommand;
import com.yourname.bugreport.sessions.BugData;
import com.yourname.bugreport.sessions.BugSession;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.bukkit.plugin.java.JavaPlugin;
import com.yourname.bugreport.utils.MenuUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class BugReporterPlugin extends JavaPlugin {
    public static BugReporterPlugin INSTANCE;


    public JDA jda;
    public Map<String, BugSession> sessions = new HashMap<>();
    public Map<String, BugData> localBugs = new HashMap<>();

    private File dataFile;
    private final Gson gson = new Gson();

    @Override
    public void onEnable() {
        MenuUtil.initialize(this);
        INSTANCE = this;

        // Load config
        saveDefaultConfig();

        // Prepare local JSON storage
        dataFile = new File(getDataFolder(), getConfig().getString("localStorage"));
        loadLocalBugs();

        // Build the Discord bot
        String token = getConfig().getString("discordToken");
        jda = JDABuilder.createDefault(token)
                .addEventListeners(new DiscordListener())
                .build();
        try {
            jda.awaitReady();  // wait until JDA is fully loaded

            // Replace with your own test-server's ID
            String guildId = "1176946631690686474";
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                getLogger().severe("Guild ID not found: " + guildId + ". Slash commands not registered.");
            } else {
                guild.updateCommands().addCommands(
                    Commands.slash("fix", "Mark a bug as fixed")
                        .addOption(OptionType.STRING, "id", "Bug ID to mark fixed", true),
                    Commands.slash("reward", "Reward a user for a bug fix")
                        .addOption(OptionType.STRING, "id", "Bug ID to reward", true)
                        .addOption(OptionType.STRING, "tier", "Reward tier (e.g. gold, silver)", true)
                ).queue(
                    success -> getLogger().info("Slash commands registered in guild " + guild.getName()),
                    error   -> getLogger().severe("Failed to register slash commands: " + error.getMessage())
                );
            }
        } catch (InterruptedException e) {
            getLogger().severe("Interrupted while waiting for JDA ready: " + e.getMessage());
        }
        // -------------------------------------------------------------------------------

        // Register the in-game /bug command
        getCommand("bug").setExecutor(new BugCommand());

        getLogger().info("BugReporterPlugin enabled!");
    }

    @Override
    public void onDisable() {
        saveLocalBugs();
        if (jda != null) jda.shutdown();
    }

    private void loadLocalBugs() {
        try {
            if (!dataFile.exists()) return;
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }

            try (Reader reader = new FileReader(dataFile)) {
                Type type = new TypeToken<Map<String, BugData>>() {}.getType();
                localBugs = gson.fromJson(reader, type);
                if (localBugs == null) localBugs = new HashMap<>();
            }
        } catch (IOException e) {
            getLogger().severe("Failed to load bugs.json: " + e.getMessage());
        }
    }

    public void saveLocalBugs() {
        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(localBugs, writer);
            }
        } catch (IOException e) {
            getLogger().severe("Failed to save bugs.json: " + e.getMessage());
        }
    }
}
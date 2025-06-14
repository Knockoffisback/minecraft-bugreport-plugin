package com.yourname.bugreport.session;

import java.util.List;

public class BugData {
    public String bugId;
    public String playerName;
    public List<String> questions;
    public List<String> answers;

    // NEW: where the initial embed lives
    public String discordChannelId;
    public String discordMessageId;

    public boolean fixed = false;
    public String rewardTier = "";
}

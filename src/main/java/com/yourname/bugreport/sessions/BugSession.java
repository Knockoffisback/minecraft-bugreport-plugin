package com.yourname.bugreport.sessions;

import java.util.ArrayList;
import java.util.List;

public class BugSession {
    public final String playerName;
    public final String bugId;
    public final List<String> questions = List.of(
        "Please describe the bug in one sentence.",
        "What steps reproduce it?",
        "What did you expect to happen?",
        "What actually happened?",
        "What version are you on?",
        "Any screenshots or logs? (paste URL)",
        "Anything else?"
    );
    public List<String> answers = new ArrayList<>();
    public int current = 0;

    public BugSession(String playerName, String bugId) {
        this.playerName = playerName;
        this.bugId = bugId;
    }

    public boolean hasNext() {
        return current < questions.size();
    }

    public String nextQuestion() {
        return questions.get(current++);
    }
}

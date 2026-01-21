package com.example.RealMatch.chat.application.tx;

public interface AfterCommitExecutor {
    
    void execute(Runnable task);
}

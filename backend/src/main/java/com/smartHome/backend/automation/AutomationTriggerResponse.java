package com.smartHome.backend.automation;

import com.smartHome.backend.automation.AutomationTriggerType;

public class AutomationTriggerResponse {

    private AutomationTriggerType triggerType;
    private int matchedCount;
    private int executedCount;

    public AutomationTriggerResponse(
            AutomationTriggerType triggerType,
            int matchedCount,
            int executedCount
    ) {
        this.triggerType = triggerType;
        this.matchedCount = matchedCount;
        this.executedCount = executedCount;
    }

    public AutomationTriggerType getTriggerType() {
        return triggerType;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public int getExecutedCount() {
        return executedCount;
    }
}
package com.romio.locationtest.data;

/**
 * Created by roman on 3/16/17
 */

public enum AreaAction {
    ENTER("enter"), TRACK("track"), LEAVE("leave");
    private String actionName;

    AreaAction(String actionName) {
        this.actionName = actionName;
    }

    public String getActionName() {
        return actionName;
    }
}

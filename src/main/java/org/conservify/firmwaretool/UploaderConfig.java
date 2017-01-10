package org.conservify.firmwaretool;

public class UploaderConfig {
    private boolean use1200bpsTouch;
    private String commandLine;

    public boolean isUse1200bpsTouch() {
        return use1200bpsTouch;
    }

    public void setUse1200bpsTouch(boolean use1200bpsTouch) {
        this.use1200bpsTouch = use1200bpsTouch;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }
}

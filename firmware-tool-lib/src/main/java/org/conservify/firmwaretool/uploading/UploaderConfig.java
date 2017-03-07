package org.conservify.firmwaretool.uploading;

import java.io.File;

public class UploaderConfig {
    private boolean use1200bpsTouch;
    private String commandLine;
    private String port;
    private File toolsPath;

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

    public File getToolsPath() {
        return toolsPath;
    }

    public void setToolsPath(File toolsPath) {
        this.toolsPath = toolsPath;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}

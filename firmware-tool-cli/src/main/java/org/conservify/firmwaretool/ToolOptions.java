package org.conservify.firmwaretool;

import org.apache.commons.cli.CommandLine;

import java.io.File;

public class ToolOptions {
    private final CommandLine cmd;

    public ToolOptions(CommandLine cmd) {
        this.cmd = cmd;
    }

    public void requireDeviceName() {
        if (!hasDeviceName()) {
            throw new RuntimeException("Missing command line argument: --device|-d");
        }
    }

    public boolean hasDeviceName() {
        return cmd.hasOption("device");
    }

    public boolean hasPort() {
        return cmd.hasOption("port");
    }

    public String getPort() {
        return cmd.getOptionValue("port");
    }

    public String getDeviceName() {
        return cmd.getOptionValue("device");
    }

    public boolean shouldTouch() {
        return cmd.hasOption("touch");
    }

    public boolean disableSsl() {
        return cmd.hasOption("disable-ssl");
    }

    public boolean shouldListOldVersions() { return cmd.hasOption("all"); }

    public File getToolsPath() {
        if (cmd.hasOption("tools-path")) {
            return new File(cmd.getOptionValue("tools-path"));
        }
        return findToolsPath();
    }

    public String getDistributionServerUrl() {
        if (disableSsl()) {
            return "http://code.conservify.org/distribution";
        }
        return "https://code.conservify.org/distribution";
    }

    private File findToolsPath() {
        File[] candidates = {
                new File("../tools"),
                new File("tools")
        };

        for (File path : candidates) {
            if (path.isDirectory())  {
                return path;
            }
        }

        throw new RuntimeException("Unable to find tools directory:\n" + System.getProperty("user.dir"));
    }
}

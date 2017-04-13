package org.conservify.firmwaretool;

import org.apache.commons.cli.CommandLine;

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

    public String getDistributionServerUrl() {
        if (disableSsl()) {
            return "http://conservify.page5of4.com/distribution";
        }
        return "https://conservify.page5of4.com/distribution";
    }
}

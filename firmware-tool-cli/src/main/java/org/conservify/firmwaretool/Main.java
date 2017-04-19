package org.conservify.firmwaretool;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.Tool;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws UnirestException, ParseException {
        Options options = new Options();
        options.addOption("d", "device", true, "the kind of device to flash, source of the binary images");
        options.addOption("p", "port", true, "the port to use");
        options.addOption(null, "touch", false, "force a 1200baud touch");
        options.addOption(null, "disable-ssl", false, "disable ssl");
        options.addOption(null, "upload", false, "perform upload");
        options.addOption(null, "monitor", false, "open serial monitor");
        options.addOption(null, "download", false, "locally cache binaries");
        options.addOption(null, "tools-path", true, "path to flashing tools directory");
        options.addOption(null, "all", false, "list old versions of binaries");
        options.addOption(null, "help", false, "display this message");

        org.apache.commons.cli.CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        ToolOptions toolOptions = new ToolOptions(cmd);

        if (cmd.hasOption("help")) {
            System.out.println();
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("firmwaretool", options);
            return;
        }

        if (cmd.hasOption("upload")) {
            UploadTask task = new UploadTask();
            task.run(toolOptions);

            if (cmd.hasOption("monitor")) {
                MonitorTask monitor = new MonitorTask();
                monitor.setPorts(task.getPorts());
                monitor.run(toolOptions);
            }
        }
        else if (cmd.hasOption("download")) {
            new DownloadTask().run(toolOptions);
        }
        else {
            new ListingTask().run(toolOptions);
        }
    }
}

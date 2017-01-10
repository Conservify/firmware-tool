package org.conservify.firmwaretool;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws UnirestException, ParseException {
        Options options = new Options();
        options.addOption("d", "device", true, "the kind of device to flash, source of the binary images");
        options.addOption(null, "upload", false, "perform upload");
        options.addOption(null, "monitor", false, "open serial monitor");
        options.addOption(null, "download", false, "locally cache binaries");
        options.addOption(null, "help", false, "display this message");

        org.apache.commons.cli.CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("help")) {
            System.out.println();
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("firmwaretool", options);
            return;
        }

        if (cmd.hasOption("upload")) {
            new UploadTask().run(cmd);
        }
        else if (cmd.hasOption("download")) {
            new DownloadAllTask().run(cmd);
        }
        else {
            new ListingTask().run(cmd);
        }
    }
}

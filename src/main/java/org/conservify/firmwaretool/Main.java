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
        options.addOption("u", false, "perform upload");
        options.addOption("s", false, "open serial monitor");
        options.addOption(null, "download-all", false, "locally cache all the binaries");

        org.apache.commons.cli.CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        DistributionService service = new DistributionService();
        BinaryCache binaryCache = new BinaryCache();
        for (DeviceFirmware device : service.getDeviceFirmwares()) {
            if (!cmd.hasOption("device") || cmd.getOptionValue("device").equals(device.getName())) {
                logger.info(device.toString());
                for (DeviceFirmwareBinary binary : service.getFirmwareBinaries(device)) {
                    logger.info(binary.toString());
                    CachedBinary localPath = binaryCache.cache(binary);

                    SettingsCache settings = SettingsCache.get();
                    DiscoveredPort port = getPort(settings);
                    if (port != null) {
                        UploaderConfig config = new UploaderConfig();
                        config.setCommandLine("\"{bossac}\" -i -d --port={port} -U true -i -e -w -v \"{binary}\" -R");
                        config.setUse1200bpsTouch(true);

                        Uploader uploader = new Uploader();
                        if (!port.isDiscovered()) {
                            PortChooser portChooser = new PortChooser();
                            logger.info("Performing 1200bps trick on {} to get {}...", port.getTouchPort(), port.getUploadPort());
                            port = portChooser.perform1200bpsTouch(port.getTouchPort());
                        }

                        if (uploader.upload(localPath.getBinary(), port.getUploadPort(), config)) {
                            settings.setLastUploadPort(port.getUploadPort());
                            settings.setLastTouchPort(port.getTouchPort());
                            settings.save();
                        }
                    }

                    return;
                }
            }
        }
    }

    static DiscoveredPort getPort(SettingsCache settings) {
        PortChooser portChooser = new PortChooser();

        if (settings.getLastUploadPort() != null) {
            if (portChooser.exists(settings.getLastTouchPort()) && !portChooser.exists(settings.getLastUploadPort())) {
                logger.info("Using {}", settings.getLastUploadPort());
                return new DiscoveredPort(settings.getLastUploadPort(), settings.getLastTouchPort(), false);
            }

            if (!portChooser.exists(settings.getLastTouchPort()) && portChooser.exists(settings.getLastUploadPort())) {
                logger.info("Using {}", settings.getLastUploadPort());
                return new DiscoveredPort(settings.getLastUploadPort(), settings.getLastTouchPort(), true);
            }

            logger.info("No such port {}", settings.getLastUploadPort());
        }

        return portChooser.discoverPort(null, false);
    }
}

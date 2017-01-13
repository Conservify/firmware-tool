package org.conservify.firmwaretool;

import org.apache.commons.cli.CommandLine;
import org.conservify.firmwaretool.distribution.*;
import org.conservify.firmwaretool.uploading.DiscoveredPort;
import org.conservify.firmwaretool.uploading.PortChooser;
import org.conservify.firmwaretool.uploading.Uploader;
import org.conservify.firmwaretool.uploading.UploaderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadTask extends Task {
    private static final Logger logger = LoggerFactory.getLogger(UploadTask.class);

    @Override
    void run(CommandLine cmd) {
        if (!cmd.hasOption("device")) {
            throw new RuntimeException("Missing command line argument: --device|-d");
        }
        String deviceName = cmd.getOptionValue("device");
        CachedBinary binary = findBinary(deviceName);
        if (binary == null) {
            throw new RuntimeException("Unable to find binary for " + deviceName);
        }
        upload(binary);
    }

    private CachedBinary findBinary(String deviceName) {
        DistributionService service = new DistributionService();
        BinaryCache binaryCache = new BinaryCache();
        for (DeviceFirmware device : service.getDeviceFirmwares()) {
            if (deviceName.equals(device.getName())) {
                logger.info(device.toString());
                for (DeviceFirmwareBinary binary : service.getFirmwareBinaries(device)) {
                    logger.info(binary.toString());
                    return binaryCache.cache(binary);
                }
            }
        }
        return null;
    }

    private void upload(CachedBinary localPath) {
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

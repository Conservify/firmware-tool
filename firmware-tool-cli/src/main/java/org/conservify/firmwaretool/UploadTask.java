package org.conservify.firmwaretool;

import org.apache.commons.cli.CommandLine;
import org.conservify.firmwaretool.distribution.*;
import org.conservify.firmwaretool.uploading.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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

        UploaderConfig config = new UploaderConfig();
        config.setToolsPath(findToolsPath());
        config.setCommandLine("\"{path}/{cmd}\" -i -d --port={port} -U true -i -e -w -v \"{binary}\" -R");

        if (cmd.hasOption("port")) {
            config.setPort(cmd.getOptionValue("port"));
            if (cmd.hasOption("touch")) {
                config.setUse1200bpsTouch(true);
            }
        }
        else {
            config.setUse1200bpsTouch(true);
        }

        Uploader uploader = new Uploader(new Slf4jPortDiscovery());
        uploader.upload(binary.getBinary(), config);
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

        throw new RuntimeException("Unable to find Tools directory.");
    }
}

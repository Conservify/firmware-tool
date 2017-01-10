package org.conservify.firmwaretool;

import org.apache.commons.cli.CommandLine;
import org.conservify.firmwaretool.distribution.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadAllTask extends Task {
    private static final Logger logger = LoggerFactory.getLogger(DownloadAllTask.class);

    @Override
    void run(CommandLine cmd) {
        DistributionService service = new DistributionService();
        BinaryCache binaryCache = new BinaryCache();
        for (DeviceFirmware device : service.getDeviceFirmwares()) {
            logger.info(device.toString());
            for (DeviceFirmwareBinary binary : service.getFirmwareBinaries(device)) {
                logger.info(binary.toString());
                binaryCache.cache(binary);
            }
        }
    }
}

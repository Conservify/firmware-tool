package org.conservify.firmwaretool;

import org.conservify.firmwaretool.distribution.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadTask extends Task {
    private static final Logger logger = LoggerFactory.getLogger(DownloadTask.class);

    @Override
    void run(ToolOptions options) {
        DistributionService service = new DistributionService(options.getDistributionServerUrl());
        BinaryCache binaryCache = new BinaryCache();
        for (DeviceFirmware device : service.getDeviceFirmwares()) {
            if (!options.hasDeviceName() || options.getDeviceName().equals(device.getName())) {
                logger.info(device.toString());
                for (DeviceFirmwareBinary binary : service.getFirmwareBinaries(device)) {
                    logger.info(binary.toString());
                    binaryCache.cache(binary);
                }
            }
        }
    }
}

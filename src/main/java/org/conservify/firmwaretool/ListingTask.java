package org.conservify.firmwaretool;

import org.apache.commons.cli.CommandLine;
import org.conservify.firmwaretool.distribution.DeviceFirmware;
import org.conservify.firmwaretool.distribution.DeviceFirmwareBinary;
import org.conservify.firmwaretool.distribution.DistributionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListingTask extends Task {
    private static final Logger logger = LoggerFactory.getLogger(ListingTask.class);

    @Override
    void run(CommandLine cmd) {
        DistributionService service = new DistributionService();
        for (DeviceFirmware device : service.getDeviceFirmwares()) {
            logger.info(device.toString());
            for (DeviceFirmwareBinary binary : service.getFirmwareBinaries(device)) {
                logger.info(binary.toString());
            }
        }
    }
}
package org.conservify.firmwaretool.uploading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jPortDiscovery implements PortDiscoveryInteraction {
    private static final Logger logger = LoggerFactory.getLogger(PortChooser.class);

    @Override
    public void onBeginning() {
        logger.info("---------------------------------------------------------------------");
        logger.info("ERROR: Unable to find the specified port, try resetting while I look.");
        logger.info("ERROR: Press RESET and cross your fingers.");
        logger.info("---------------------------------------------------------------------");
    }

    @Override
    public void onPortStatus(String[] portNamesBefore, String[] portNamesNow, String[] missingPorts, String[] newPorts) {
        logger.info("{} -> {}: {} / {}", portNamesBefore, portNamesNow, missingPorts, newPorts);
    }

    @Override
    public void onProgress(String info) {
        logger.info(info);
    }
}

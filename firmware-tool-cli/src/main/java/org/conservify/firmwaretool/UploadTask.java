package org.conservify.firmwaretool;

import org.conservify.firmwaretool.distribution.*;
import org.conservify.firmwaretool.uploading.*;
import org.conservify.firmwaretool.util.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class UploadTask extends Task {
    private static final Logger logger = LoggerFactory.getLogger(UploadTask.class);
    private DevicePorts ports;

    public DevicePorts getPorts() {
        return ports;
    }

    public void setPorts(DevicePorts ports) {
        this.ports = ports;
    }

    @Override
    void run(ToolOptions options) {
        options.requireDeviceName();
        String deviceName = options.getDeviceName();
        CachedBinary binary = findBinary(deviceName);
        if (binary == null) {
            throw new RuntimeException("Unable to find binary for " + deviceName);
        }

        UploaderConfig config = new UploaderConfig();
        config.setToolsPath(findToolsPath());

        // Eventually clean this up.
        if (Platform.isArm()) {
            config.setCommandLine("\"{path}/{cmd}\" -i -d -p {port.name} -U true -e -w -v \"{binary}\" -R");
        }
        else {
            config.setCommandLine("\"{path}/{cmd}\" -i -d -p {port.file} -U true -e -w -v \"{binary}\" -R");
        }

        if (options.hasPort()) {
            config.setPort(options.getPort());
            if (options.shouldTouch()) {
                config.setUse1200bpsTouch(true);
            }
        }
        else {
            config.setUse1200bpsTouch(true);
        }

        Uploader uploader = new Uploader(new Slf4jPortDiscovery());
        ports = uploader.upload(binary.getBinary(), config);
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

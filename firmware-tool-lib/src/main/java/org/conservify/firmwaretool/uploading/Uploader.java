package org.conservify.firmwaretool.uploading;

import org.apache.commons.io.FilenameUtils;
import org.conservify.firmwaretool.util.RunCommand;
import org.conservify.firmwaretool.util.SettingsCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Uploader {
    private static final Logger logger = LoggerFactory.getLogger(Uploader.class);
    private final PortDiscoveryInteraction portDiscoveryInteraction;

    public Uploader(PortDiscoveryInteraction portDiscoveryInteraction) {
        this.portDiscoveryInteraction = portDiscoveryInteraction;
    }

    public boolean upload(File binary, String port, UploaderConfig config) {
        String os = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");

        logger.info(String.format("%s-%s", os, arch));

        String command = config.getCommandLine();
        Properties properties = new Properties();
        properties.put("path", config.getToolsPath().toString().replace("\\", "/"));
        if (os.toLowerCase().contains("win")) {
            properties.put("cmd", "bossac.exe");
        }
        else {
            if (os.toLowerCase().contains("linux")) {
                if (arch.toLowerCase().contains("arm")) {
                    properties.put("cmd", "bossac_linux_arm");
                }
                else {
                    properties.put("cmd", "bossac_linux");
                }
            }
            else {
                properties.put("cmd", "bossac_osx");
            }
        }
        properties.put("upload.verbose", "-i -d");
        properties.put("binary", binary.toString().replace("\\", "/"));
        properties.put("build.path", binary.getParent().toString().replace("\\", "/"));
        properties.put("build.project_name", FilenameUtils.removeExtension(binary.getName()));
        properties.put("serial.port.file", port);

        properties.put("cmd.path", config.getToolsPath().toString().replace("\\", "/"));
        properties.put("serial.port", port);
        properties.put("port", port);
        properties.put("upload.verify", "-v");

        String populated = replace(properties, command);

        logger.info(populated);
        RunCommand.run(populated, new File("."), line -> portDiscoveryInteraction.onProgress(line));
        return true;
    }

    private String getKey(Properties props, String key) {
        if (!props.containsKey(key)) {
            return "{missing: " + key + "}";
        }

        Properties newProperties = new Properties();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            if (!entry.getKey().equals(key)) {
                newProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return this.replace(newProperties, (String)props.get(key));
    }

    private String replace(Properties props, String value) {
        Pattern p = Pattern.compile("\\{([\\w\\.-]+)\\}");
        Matcher m = p.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, getKey(props, m.group(1)));
        }
        m.appendTail(sb);

        return sb.toString();
    }

    public DevicePorts upload(File binary, UploaderConfig config) {
        SettingsCache settings = SettingsCache.get();
        DevicePorts port = getPort(settings, config);
        if (port != null) {
            Uploader uploader = new Uploader(portDiscoveryInteraction);
            if (!port.isDiscovered() && port.getTouchPort() != null) {
                PortChooser portChooser = new PortChooser(portDiscoveryInteraction);
                portDiscoveryInteraction.onProgress(String.format("Performing 1200bps trick on %s...", port.getTouchPort()));
                port = portChooser.perform1200bpsTouch(port.getTouchPort());
                if (port == null) {
                    if (config.getPort() != null) {
                        port = new DevicePorts(config.getPort(), null, false);
                    }
                    else {
                        throw new RuntimeException("Oh no, the 1200bps trick failed. Is there a permissions problem or is the device open elsewhere?");
                    }
                }
            }

            if (uploader.upload(binary, port.getUploadPort(), config)) {
                settings.setLastUploadPort(port.getUploadPort());
                settings.setLastTouchPort(port.getTouchPort());
                settings.save();
            }
        }
        return port;
    }

    DevicePorts getPort(SettingsCache settings, UploaderConfig config) {
        PortChooser portChooser = new PortChooser(portDiscoveryInteraction);

        if (config.getPort() != null) {
            if (config.isUse1200bpsTouch()) {
                return new DevicePorts(null, config.getPort(), false);
            }
            return new DevicePorts(config.getPort(), null, false);
        }

        if (settings.getLastUploadPort() != null) {
            if (portChooser.exists(settings.getLastTouchPort()) && !portChooser.exists(settings.getLastUploadPort())) {
                portDiscoveryInteraction.onProgress(String.format("Will try touching %s to get %s", settings.getLastUploadPort(), settings.getLastUploadPort()));
                return new DevicePorts(settings.getLastUploadPort(), settings.getLastTouchPort(), false);
            }

            if (!portChooser.exists(settings.getLastTouchPort()) && portChooser.exists(settings.getLastUploadPort())) {
                portDiscoveryInteraction.onProgress(String.format("Using %s", settings.getLastUploadPort()));
                return new DevicePorts(settings.getLastUploadPort(), settings.getLastTouchPort(), true);
            }

            logger.info("No such port {}", settings.getLastUploadPort());
        }

        return portChooser.discoverPort(null, false);
    }
}

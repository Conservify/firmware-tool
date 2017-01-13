package org.conservify.firmwaretool.uploading;

import org.apache.commons.io.FilenameUtils;
import org.conservify.firmwaretool.util.RunCommand;
import org.conservify.firmwaretool.util.SettingsCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.Port;
import java.io.File;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Uploader {
    private static final Logger logger = LoggerFactory.getLogger(Uploader.class);
    private final PortDiscoveryInteraction portDiscoveryInteraction;

    public Uploader(PortDiscoveryInteraction portDiscoveryInteraction) {
        this.portDiscoveryInteraction = portDiscoveryInteraction;
    }

    public boolean upload(File binary, String port, UploaderConfig config) {
        String command = config.getCommandLine();
        Properties properties = new Properties();
        properties.put("path", config.getToolsPath().toString().replace("\\", "/"));
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            properties.put("cmd", "bossac.exe");
        }
        else {
            properties.put("cmd", "bossac_osx");
        }
        properties.put("upload.verbose", "-i -d");
        properties.put("build.path", binary.getParent().toString().replace("\\", "/"));
        properties.put("build.project_name", FilenameUtils.removeExtension(binary.getName()));
        properties.put("serial.port.file", port);

        String populated = replace(properties, command);
        logger.info(populated);
        RunCommand.run(populated, new File("."), line -> portDiscoveryInteraction.onProgress(line));
        return true;
    }


    private String getKey(Properties props, String key) {
        if (!props.containsKey(key)) {
            return "{" + key + "}";
        }
        return this.replace(props, (String)props.get(key));
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

    public DiscoveredPort upload(File binary, UploaderConfig config) {
        SettingsCache settings = SettingsCache.get();
        DiscoveredPort port = getPort(settings);
        if (port != null) {
            Uploader uploader = new Uploader(portDiscoveryInteraction);
            if (!port.isDiscovered()) {
                PortChooser portChooser = new PortChooser(portDiscoveryInteraction);
                portDiscoveryInteraction.onProgress(String.format("Performing 1200bps trick on %s to get %s...", port.getTouchPort(), port.getUploadPort()));
                port = portChooser.perform1200bpsTouch(port.getTouchPort());
            }

            if (uploader.upload(binary, port.getUploadPort(), config)) {
                settings.setLastUploadPort(port.getUploadPort());
                settings.setLastTouchPort(port.getTouchPort());
                settings.save();
            }
        }
        return port;
    }

    DiscoveredPort getPort(SettingsCache settings) {
        PortChooser portChooser = new PortChooser(portDiscoveryInteraction);

        if (settings.getLastUploadPort() != null) {
            if (portChooser.exists(settings.getLastTouchPort()) && !portChooser.exists(settings.getLastUploadPort())) {
                portDiscoveryInteraction.onProgress(String.format("Using %s", settings.getLastUploadPort()));
                return new DiscoveredPort(settings.getLastUploadPort(), settings.getLastTouchPort(), false);
            }

            if (!portChooser.exists(settings.getLastTouchPort()) && portChooser.exists(settings.getLastUploadPort())) {
                portDiscoveryInteraction.onProgress(String.format("Using %s", settings.getLastUploadPort()));
                return new DiscoveredPort(settings.getLastUploadPort(), settings.getLastTouchPort(), true);
            }

            logger.info("No such port {}", settings.getLastUploadPort());
        }

        return portChooser.discoverPort(null, false);
    }
}

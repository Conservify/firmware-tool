package org.conservify.firmwaretool.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SettingsCache {
    private String lastUploadPort;
    private String lastTouchPort;

    public String getLastUploadPort() {
        return lastUploadPort;
    }

    public void setLastUploadPort(String lastUploadPort) {
        this.lastUploadPort = lastUploadPort;
    }

    public String getLastTouchPort() {
        return lastTouchPort;
    }

    public void setLastTouchPort(String lastTouchPort) {
        this.lastTouchPort = lastTouchPort;
    }

    private static String LAST_UPLOAD_PORT = "last.upload.port";
    private static String LAST_TOUCH_PORT = "last.touch.port";

    public static SettingsCache get() {
        SettingsCache settings = new SettingsCache();
        try {
            if (new File("firmwaretool.properties").isFile()) {
                FileInputStream stream = new FileInputStream("project.properties");
                try {
                    Properties properties = new Properties();
                    properties.load(stream);
                    settings.setLastUploadPort((String)properties.get(LAST_UPLOAD_PORT));
                    settings.setLastTouchPort((String)properties.get(LAST_TOUCH_PORT));
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return settings;
    }

    public void save() {
        try {
            FileOutputStream stream = new FileOutputStream("firmwaretool.properties");
            try {
                Properties properties = new Properties();
                if (lastUploadPort != null) {
                    properties.put(LAST_UPLOAD_PORT, lastUploadPort);
                }
                if (lastTouchPort != null) {
                    properties.put(LAST_TOUCH_PORT, this.lastTouchPort);
                }
                properties.store(stream, "");
                stream.close();
            } finally {
                IOUtils.closeQuietly(stream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

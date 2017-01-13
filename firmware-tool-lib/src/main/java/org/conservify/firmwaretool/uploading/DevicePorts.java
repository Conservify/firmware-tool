package org.conservify.firmwaretool.uploading;

public class DevicePorts {
    private String uploadPort;
    private String touchPort;
    private boolean discovered;

    public String getUploadPort() {
        return uploadPort;
    }

    public String getTouchPort() {
        return touchPort;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public DevicePorts(String likelyPort, String touchPort, boolean discovered) {
        this.uploadPort = likelyPort;
        this.touchPort = touchPort;
        this.discovered = discovered;
    }

    public String getMonitorPort() {
        if (touchPort != null) {
            return touchPort;
        }
        return uploadPort;
    }
}

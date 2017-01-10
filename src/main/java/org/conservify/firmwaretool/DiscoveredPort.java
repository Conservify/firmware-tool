package org.conservify.firmwaretool;

public class DiscoveredPort {
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

    public DiscoveredPort(String likelyPort, String touchPort, boolean discovered) {
        this.uploadPort = likelyPort;
        this.touchPort = touchPort;
        this.discovered = discovered;
    }
}

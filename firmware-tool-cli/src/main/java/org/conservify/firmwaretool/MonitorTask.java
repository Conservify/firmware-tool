package org.conservify.firmwaretool;

import org.conservify.firmwaretool.monitoring.SerialMonitor;
import org.conservify.firmwaretool.uploading.DevicePorts;
import org.conservify.firmwaretool.uploading.Slf4jPortDiscovery;

public class MonitorTask extends Task {
    private DevicePorts ports;

    public DevicePorts getPorts() {
        return ports;
    }

    public void setPorts(DevicePorts ports) {
        this.ports = ports;
    }

    @Override
    void run(ToolOptions options) {
        SerialMonitor serialMonitor = new SerialMonitor(new Slf4jPortDiscovery());
        serialMonitor.stream(ports, 115200);
    }
}

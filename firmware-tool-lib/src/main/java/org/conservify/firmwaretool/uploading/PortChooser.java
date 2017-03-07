package org.conservify.firmwaretool.uploading;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PortChooser {
    private static final Logger logger = LoggerFactory.getLogger(PortChooser.class);
    private final PortDiscoveryInteraction portDiscoveryInteraction;

    public PortChooser(PortDiscoveryInteraction portDiscoveryInteraction) {
        this.portDiscoveryInteraction = portDiscoveryInteraction;
    }

    public DevicePorts perform1200bpsTouch(String portName){
        try {
            SerialPort serialPort = new SerialPort(portName);
            try {
                serialPort.openPort();
                serialPort.setParams(1200, 8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                serialPort.setDTR(false);
                serialPort.closePort();
                return lookForNewPort(getPortNames(), 5);
            } catch (SerialPortException e) {
                throw new RuntimeException(String.format("Error touching serial port ''%s''.", portName));
            } finally {
                if (serialPort.isOpened()) {
                    try {
                        serialPort.closePort();
                    } catch (SerialPortException e) {
                        // Ignore
                    }
                }
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean waitForPort(String portName, int tries) throws InterruptedException {
        while (tries-- > 0) {
            Thread.sleep(500);
            if (exists(portName)) {
                return true;
            }
        }

        return false;
    }

    public DevicePorts discoverPort(String specifiedPort, boolean perform1200bpsTouch) {
        try {
            String newPort = specifiedPort;
            DevicePorts ports = null;
            if (perform1200bpsTouch && specifiedPort != null) {
                ports = perform1200bpsTouch(specifiedPort);
                if (ports == null) {
                    return null;
                }
            }

            if (ports == null) {
                portDiscoveryInteraction.onBeginning();
                DevicePorts found = lookForNewPort(getPortNames(), 20);
                if (found != null) {
                    return found;
                }
                return null;
            }

            return new DevicePorts(newPort, null, true);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getPortNames() {
        return SerialPortList.getPortNames();
    }

    public boolean exists(String portName) {
        List<String> portNames = Arrays.asList(getPortNames());
        return portNames.contains(portName);
    }

    private DevicePorts lookForNewPort(String[] portNamesBefore, int tries) throws InterruptedException {
        String candidatePort = null;

        while (tries-- > 0) {
            Thread.sleep(500);
            String[] portNamesNow = getPortNames();
            String[] missingPorts = difference(portNamesBefore, portNamesNow);
            String[] newPorts = difference(portNamesNow, portNamesBefore);

            portDiscoveryInteraction.onPortStatus(portNamesBefore, portNamesNow, missingPorts, newPorts);

            if (newPorts.length > 0) {
                if (missingPorts.length > 0) {
                    return new DevicePorts(newPorts[0], missingPorts[0], true);
                }
                return new DevicePorts(newPorts[0], null, true);
            }
            else if (missingPorts.length > 0) {
                candidatePort = missingPorts[0];
            }
            else if (candidatePort != null) {
                return new DevicePorts(candidatePort, null, true);
            }
        }

        return null;
    }

    private String[] difference(String[] before, String[] after) {
        Set<String> a = new HashSet<String>();
        a.addAll(Arrays.asList(before));
        a.removeAll(Arrays.asList(after));
        return a.toArray(new String[0]);
    }
}

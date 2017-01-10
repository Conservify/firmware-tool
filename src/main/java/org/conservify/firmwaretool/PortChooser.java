package org.conservify.firmwaretool;

import com.fazecast.jSerialComm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PortChooser {
    private static final Logger logger = LoggerFactory.getLogger(PortChooser.class);

    public DiscoveredPort perform1200bpsTouch(String portName){
        try {
            SerialPort serialPort = SerialPort.getCommPort(portName);
            serialPort.setBaudRate(1200);
            boolean opened = serialPort.openPort();
            if (opened) {
                serialPort.closePort();
                return lookForNewPort(getPortNames(), 5);
            }

            return null;
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean waitForPort(String portName, int tries) throws InterruptedException {
        while (tries-- > 0) {
            Thread.sleep(500);
            if (exists(portName)) {
                return true;
            }
        }

        return false;
    }

    private DiscoveredPort lookForNewPort(String[] portNamesBefore, int tries) throws InterruptedException {
        while (tries-- > 0) {
            Thread.sleep(500);
            String[] portNamesNow = getPortNames();
            String[] missingPorts = difference(portNamesBefore, portNamesNow);
            String[] newPorts = difference(portNamesNow, portNamesBefore);

            logger.info("{} -> {}: {} / {}", portNamesBefore, portNamesNow, missingPorts, newPorts);

            if (newPorts.length > 0) {
                if (missingPorts.length > 0) {
                    return new DiscoveredPort(newPorts[0], missingPorts[0], true);
                }
                return new DiscoveredPort(newPorts[0], null, true);
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

    public DiscoveredPort discoverPort(String specifiedPort, boolean perform1200bpsTouch) {
        try {
            String newPort = specifiedPort;
            DiscoveredPort ports = null;
            String serialPort = specifiedPort != null ? SerialPort.getCommPort(specifiedPort).getSystemPortName() : null;
            if (perform1200bpsTouch && specifiedPort != null) {
                ports = perform1200bpsTouch(specifiedPort);
                if (ports == null) {
                    return null;
                }
            }

            if (ports == null) {
                logger.info("ERROR: Unable to find the specified port, try resetting while I look.");
                logger.info("ERROR: Press RESET and cross your fingers.");
                DiscoveredPort found = lookForNewPort(getPortNames(), 20);
                if (found != null) {
                    return found;
                }
                return null;
            }

            return new DiscoveredPort(newPort, null, true);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getPortNames() {
        SerialPort[] currentPorts = SerialPort.getCommPorts();
        ArrayList<String> names = new ArrayList<String>();
        for (SerialPort port : currentPorts) {
            names.add(port.getSystemPortName());
        }
        return names.toArray(new String[0]);
    }

    public boolean exists(String portName) {
        List<String> portNames = Arrays.asList(getPortNames());
        return portNames.contains(portName);
    }
}

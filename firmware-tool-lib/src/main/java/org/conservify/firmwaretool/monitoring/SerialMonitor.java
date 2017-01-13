package org.conservify.firmwaretool.monitoring;

import com.fazecast.jSerialComm.SerialPort;
import org.conservify.firmwaretool.uploading.DevicePorts;
import org.conservify.firmwaretool.uploading.PortChooser;
import org.conservify.firmwaretool.uploading.PortDiscoveryInteraction;
import org.conservify.firmwaretool.util.CommandLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SerialMonitor {
    private static final Logger logger = LoggerFactory.getLogger(SerialMonitor.class);
    private final PortChooser portChooser;
    private final PortDiscoveryInteraction portDiscoveryInteraction;

    public SerialMonitor(PortDiscoveryInteraction portDiscoveryInteraction) {
        this.portChooser = new PortChooser(portDiscoveryInteraction);
        this.portDiscoveryInteraction = portDiscoveryInteraction;
    }

    public void open(DevicePorts devicePorts, int baudRate, File log) {
        try {
            if (!portChooser.waitForPort(devicePorts.getMonitorPort(), 10)) {
                throw new RuntimeException(String.format("Port %s never showed up.", devicePorts.getMonitorPort()));
            }

            File puttyPath = findPuttyPath();
            String commandLine = String.format("%s -serial %s -sercfg %s -sessionlog %s", puttyPath, devicePorts.getMonitorPort(), baudRate, log.getName());
            logger.info(commandLine);

            String[] parsed = CommandLineParser.translateCommandLine(commandLine);

            ProcessBuilder processBuilder = new ProcessBuilder(parsed);
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(log.getParentFile());

            Process process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stream(DevicePorts devicePorts, int baudRate) {
        try {
            if (!portChooser.waitForPort(devicePorts.getMonitorPort(), 10)) {
                throw new RuntimeException(String.format("Port %s never showed up.", devicePorts.getMonitorPort()));
            }

            final SerialPort serialPort = SerialPort.getCommPort(devicePorts.getMonitorPort());
            serialPort.setBaudRate(baudRate);
            if (serialPort.openPort()) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> serialPort.closePort()));
                serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
                try {
                    InputStream inputStream = serialPort.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    while (true) {
                        char c = (char)inputStream.read();
                        buffer.append(c);
                        if (c == '\r') {
                            portDiscoveryInteraction.onProgress(buffer.toString());
                            buffer = new StringBuffer();
                        }
                    }
                }
                finally {
                    serialPort.closePort();

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // This needs work.
    private File findPuttyPath() {
        String[] puttyPaths = new String[] {
            "./",
            "C:/Windows/System32"
        };

        List<File> found = Arrays.stream(puttyPaths)
                .map(s -> new File(s, "putty.exe"))
                .filter(f -> f.isFile())
                .collect(Collectors.toList());

        if (found.size() == 0) {
            throw new RuntimeException("Unable to find putty.exe");
        }

        return found.get(0);
    }
}

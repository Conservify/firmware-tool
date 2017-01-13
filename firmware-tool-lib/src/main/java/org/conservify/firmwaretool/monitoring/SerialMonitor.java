package org.conservify.firmwaretool.monitoring;

import org.conservify.firmwaretool.uploading.DiscoveredPort;
import org.conservify.firmwaretool.uploading.PortChooser;
import org.conservify.firmwaretool.uploading.PortDiscoveryInteraction;
import org.conservify.firmwaretool.util.CommandLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SerialMonitor {
    private static final Logger logger = LoggerFactory.getLogger(SerialMonitor.class);
    private final PortChooser portChooser;

    public SerialMonitor(PortDiscoveryInteraction portDiscoveryInteraction) {
        this.portChooser = new PortChooser(portDiscoveryInteraction);
    }

    public void open(DiscoveredPort discoveredPort, long baud, File log) {
        try {
            if (!portChooser.waitForPort(discoveredPort.getMonitorPort(), 10)) {
                throw new RuntimeException(String.format("Port %s never showed up.", discoveredPort.getMonitorPort()));
            }

            File puttyPath = findPuttyPath();
            String commandLine = String.format("%s -serial %s -sercfg %s -sessionlog %s", puttyPath, discoveredPort.getMonitorPort(), baud, log.getName());
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

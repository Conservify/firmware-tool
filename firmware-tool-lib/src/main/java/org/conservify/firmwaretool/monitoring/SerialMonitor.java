package org.conservify.firmwaretool.monitoring;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.conservify.firmwaretool.uploading.DevicePorts;
import org.conservify.firmwaretool.uploading.PortChooser;
import org.conservify.firmwaretool.uploading.PortDiscoveryInteraction;
import org.conservify.firmwaretool.util.CommandLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
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

            final SerialPort serialPort = new SerialPort(devicePorts.getMonitorPort());
            try {
                if (serialPort.openPort()) {
                    serialPort.setParams(baudRate, 8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    serialPort.addEventListener(new SerialStreamer(serialPort, portDiscoveryInteraction));
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            serialPort.closePort();
                        }
                        catch (SerialPortException e) {
                            // Ignore
                        }
                    }));
                    while (true) {
                        Thread.sleep(1000);
                    }
                }
            }
            catch (SerialPortException e) {
                throw new RuntimeException(e);
            }
            finally {
                if (serialPort.isOpened()) {
                    try {
                        serialPort.closePort();
                    }
                    catch (SerialPortException e) {
                        // Ignore
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    class SerialStreamer implements SerialPortEventListener {
        private SerialPort port;
        private CharsetDecoder bytesToStrings;
        private static final int IN_BUFFER_CAPACITY = 128;
        private static final int OUT_BUFFER_CAPACITY = 128;
        private ByteBuffer inFromSerial = ByteBuffer.allocate(IN_BUFFER_CAPACITY);
        private CharBuffer outToMessage = CharBuffer.allocate(OUT_BUFFER_CAPACITY);
        private PortDiscoveryInteraction portDiscoveryInteraction;

        public SerialStreamer(SerialPort port, PortDiscoveryInteraction portDiscoveryInteraction) {
            this.portDiscoveryInteraction = portDiscoveryInteraction;
            Charset charset = Charset.forName("UTF-8");
            this.port = port;
            this.bytesToStrings = charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE)
                    .replaceWith("\u2e2e");
        }

        @Override
        public void serialEvent(SerialPortEvent serialEvent) {
            if (serialEvent.isRXCHAR()) {
                try {
                    byte[] buf = port.readBytes(serialEvent.getEventValue());
                    int next = 0;
                    while (next < buf.length) {
                        while (next < buf.length && outToMessage.hasRemaining()) {
                            int spaceInIn = inFromSerial.remaining();
                            int copyNow = buf.length - next < spaceInIn ? buf.length - next : spaceInIn;
                            inFromSerial.put(buf, next, copyNow);
                            next += copyNow;
                            inFromSerial.flip();
                            bytesToStrings.decode(inFromSerial, outToMessage, false);
                            inFromSerial.compact();
                        }
                        outToMessage.flip();
                        if (outToMessage.hasRemaining()) {
                            char[] chars = new char[outToMessage.remaining()];
                            outToMessage.get(chars);
                            // portDiscoveryInteraction.onProgress(new String(chars));
                        }
                        outToMessage.clear();
                    }
                } catch (SerialPortException e) {
                    logger.error("Error", e);
                }
            }
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

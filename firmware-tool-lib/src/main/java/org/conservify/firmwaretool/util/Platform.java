package org.conservify.firmwaretool.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Platform {
    private static final Logger logger = LoggerFactory.getLogger(Platform.class);

    static {
        String os = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");
        logger.info(String.format("%s-%s", os, arch));
    }

    public static boolean isLinux() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("linux");
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("win");
    }

    public static boolean isArm() {
        String os = System.getProperty("os.arch");
        return os.toLowerCase().contains("arm");
    }
}

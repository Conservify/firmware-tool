package org.conservify.firmwaretool.distribution;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceFirmwareBinary {
    private DeviceFirmware device;
    private Date buildDate;
    private String board;
    private String binaryUrl;
    private String manifestUrl;

    public Date getBuildDate() {
        return buildDate;
    }

    public String getBoard() {
        return board;
    }

    public String getBinaryUrl() {
        return binaryUrl;
    }

    public String getManifestUrl() {
        return manifestUrl;
    }

    public DeviceFirmwareBinary(DeviceFirmware device, Date buildDate, String board, String binaryUrl, String manifestUrl) {
        this.device = device;
        this.buildDate = buildDate;
        this.board = board;
        this.binaryUrl = binaryUrl;
        this.manifestUrl = manifestUrl;
    }

    @Override
    public String toString() {
        return "Binary<" + buildDate + " " + board + ">";
    }

    public String getCacheKey() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String fileNameUnfriendly = device.getName() + "_" + formatter.format(buildDate) + "_" + board;
        return fileNameUnfriendly.replaceAll("[/:]", "_");
    }
}

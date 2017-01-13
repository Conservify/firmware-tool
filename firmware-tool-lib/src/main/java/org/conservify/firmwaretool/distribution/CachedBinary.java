package org.conservify.firmwaretool.distribution;

import java.io.File;

public class CachedBinary {
    private File binary;
    private File manifest;

    public File getBinary() {
        return binary;
    }

    public File getManifest() {
        return manifest;
    }

    public CachedBinary(File binary, File manifest) {
        this.binary = binary;
        this.manifest = manifest;
    }
}

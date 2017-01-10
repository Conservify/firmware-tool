package org.conservify.firmwaretool.distribution;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class BinaryCache {
    private static final Logger logger = LoggerFactory.getLogger(BinaryCache.class);

    private static String cacheDirectoryName = "binaries";

    public CachedBinary cache(DeviceFirmwareBinary binary) {
        try {
            File cacheDirectory = new File(cacheDirectoryName);
            File localBinary = new File(cacheDirectory, binary.getCacheKey());
            File localManifest = new File(cacheDirectory, binary.getCacheKey() + ".json");
            cacheDirectory.mkdirs();

            if (!localBinary.isFile()) {
                cache(binary.getBinaryUrl(), localBinary);
            }
            if (!localManifest.isFile()) {
                cache(binary.getManifestUrl(), localManifest);
            }

            return new CachedBinary(localBinary, localManifest);
        }
        catch (Exception e) {
            logger.error("Error caching binary", e);
            throw new RuntimeException("Error caching binary", e);
        }
    }

    void cache(String remoteUrl, File localCopy) throws IOException {
        logger.info("Downloading {}", remoteUrl);

        URL url = new URL(remoteUrl);
        URLConnection connection = url.openConnection();
        FileOutputStream outputStream = new FileOutputStream(localCopy);
        try {
            IOUtils.copy(connection.getInputStream(), outputStream);
        }
        finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

}

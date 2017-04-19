package org.conservify.firmwaretool.distribution;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import java.io.IOException;

public class DistributionService {
    private static final Logger logger = LoggerFactory.getLogger(DistributionService.class);
    private String distributionServerUrl;

    public DistributionService(String distributionServerUrl) {
        this.distributionServerUrl = distributionServerUrl;
        configure();
    }

    public ArrayList<DeviceFirmware> getDeviceFirmwares() {
        try {
            ArrayList<String> searchUrls = new ArrayList<String>();
            ArrayList<DeviceFirmware> devices = new ArrayList<DeviceFirmware>();
            searchUrls.add(distributionServerUrl);

            while (!searchUrls.isEmpty()) {
                ArrayList<String> newUrls = new ArrayList<String>();
                String url = searchUrls.remove(0);

                HttpResponse<DirectoryListingEntry[]> response = Unirest.get(url)
                        .header("accept", "application/json")
                        .asObject(DirectoryListingEntry[].class);

                boolean anyTimestampedEntries = false;
                for (DirectoryListingEntry entry : response.getBody()) {
                    if (shouldWalk(entry)) {
                        newUrls.add(url + "/" + entry.getName());
                    }
                    anyTimestampedEntries = anyTimestampedEntries || isTimestampedDirectory(entry);
                }
                if (!anyTimestampedEntries) {
                    searchUrls.addAll(newUrls);
                }
                else {
                    String name = url.replace(distributionServerUrl + "/", "");
                    devices.add(new DeviceFirmware(name, url));
                }
            }
            return devices;
        }
        catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean shouldWalk(DirectoryListingEntry entry) {
        return entry.isDirectory();
    }

    static boolean isTimestampedDirectory(DirectoryListingEntry entry) {
        return Pattern.matches("\\d{8}+_\\d{6}+", entry.getName());
    }

    public ArrayList<DeviceFirmwareBinary> getFirmwareBinaries(DeviceFirmware deviceFirmware, boolean onlyLatest) {
        try {
            ArrayList<String> searchUrls = new ArrayList<String>();
            ArrayList<DeviceFirmwareBinary> binaries = new ArrayList<DeviceFirmwareBinary>();

            searchUrls.add(deviceFirmware.getUrl());

            while (!searchUrls.isEmpty()) {
                String url = searchUrls.remove(0);

                HttpResponse<DirectoryListingEntry[]> response = Unirest.get(url)
                        .header("accept", "application/json")
                        .asObject(DirectoryListingEntry[].class);

                ArtifactDirectory artifactDirectory = isArtifactDirectory(url, response.getBody());
                if (artifactDirectory != null) {
                    binaries.add(createBinary(deviceFirmware, artifactDirectory));
                }
                else {
                    for (DirectoryListingEntry entry : getBinariesToScan(response.getBody(), onlyLatest)) {
                        searchUrls.add(url + "/" + entry.getName());
                    }
                }
            }

            binaries.sort((o1, o2) -> o2.getBuildDate().compareTo(o1.getBuildDate()));

            return binaries;
        }
        catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    static SimpleDateFormat DIRECTORY_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd_hhmmss");

    List<DirectoryListingEntry> getBinariesToScan(DirectoryListingEntry[] entries, boolean onlyLatest) {
        List<DirectoryListingEntry> filtered = new ArrayList<DirectoryListingEntry>();
        for (DirectoryListingEntry entry : entries) {
            if (shouldWalk(entry)) {
                filtered.add(entry);
            }
        }
        Collections.sort(filtered, new Comparator<DirectoryListingEntry>() {
            @Override
            public int compare(DirectoryListingEntry o1, DirectoryListingEntry o2) {
                try {
                    Date d1 = DIRECTORY_TIME_FORMAT.parse(o1.getName());
                    Date d2 = DIRECTORY_TIME_FORMAT.parse(o2.getName());
                    return d1.compareTo(d2);
                } catch (ParseException e) {
                    return 0;
                }
            }
        });

        if (onlyLatest) {
            while (filtered.size() > 1) {
                filtered.remove(0);
            }
        }

        return filtered;
    }

    ArtifactDirectory isArtifactDirectory(String directoryUrl, DirectoryListingEntry[] entries) {
        Optional<DirectoryListingEntry> binary = Arrays.stream(entries)
                .filter(x -> Pattern.matches(".+\\.(hex|bin)$", x.getName()))
                .findFirst();
        Optional<DirectoryListingEntry> manifest = Arrays.stream(entries)
                .filter(x -> Pattern.matches(".+\\.json$", x.getName()))
                .findFirst();

        if (!binary.isPresent() || !manifest.isPresent()) {
            return null;
        }

        String manifestUrl = directoryUrl + "/" + manifest.get().getName();
        String binaryUrl = directoryUrl + "/" + binary.get().getName();
        return new ArtifactDirectory(directoryUrl, manifestUrl, binaryUrl);
    }

    DeviceFirmwareBinary createBinary(DeviceFirmware device, ArtifactDirectory artifactDirectory) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(artifactDirectory.getManifestUrl())
                .header("accept", "application/json")
                .asJson();

        JSONObject meta = (JSONObject)response.getBody().getObject().get("meta");
        Date buildDate = new Date(new Long(meta.get("time").toString()) * 1000);
        return new DeviceFirmwareBinary(device, buildDate, meta.get("board").toString(), artifactDirectory.getBinaryUrl(), artifactDirectory.getManifestUrl());
    }

    class ArtifactDirectory {
        private String url;
        private String manifestUrl;
        private String binaryUrl;

        public String getUrl() {
            return url;
        }

        public String getManifestUrl() {
            return manifestUrl;
        }

        public String getBinaryUrl() {
            return binaryUrl;
        }

        public ArtifactDirectory(String url, String manifestUrl, String binaryUrl) {
            this.url = url;
            this.manifestUrl = manifestUrl;
            this.binaryUrl = binaryUrl;
        }

        @Override
        public String toString() {
            return "Artifact<" + manifestUrl + " " + binaryUrl + ">";
        }
    }

    static void configure() {
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}

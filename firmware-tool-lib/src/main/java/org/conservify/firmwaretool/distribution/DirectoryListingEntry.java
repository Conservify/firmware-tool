package org.conservify.firmwaretool.distribution;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class DirectoryListingEntry {
    String name;
    String type;
    Date modifiedTime;
    long size;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("mtime")
    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public DirectoryListingEntry() {

    }

    public boolean isDirectory() {
        return type.equalsIgnoreCase("directory");
    }
}

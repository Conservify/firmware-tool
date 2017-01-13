package org.conservify.firmwaretool.uploading;

public interface PortDiscoveryInteraction {
    void onBeginning();

    void onPortStatus(String[] portNamesBefore, String[] portNamesNow, String[] missingPorts, String[] newPorts);

    void onProgress(String info);
}

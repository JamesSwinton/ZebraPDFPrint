package com.zebra.jamesswinton.zebrapdfprintv2.interfaces;

import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

public interface OnSelectPrinterCallback {
    void onDiscoveredPrinterSelected(DiscoveredPrinter discoveredPrinter);
    void onDiscoveryFailed(String error);
}

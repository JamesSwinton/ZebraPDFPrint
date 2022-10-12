package com.zebra.jamesswinton.zebrapdfprintv2.interfaces;

import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;

public interface OnSelectPrinterCallback {
    void onDiscoveredPrinterSelected(DiscoveredPrinter discoveredPrinter);

    void onDiscoveredPrinterUSBSelected(DiscoveredPrinterUsb discoveredPrinterUsb);

    void onDiscoveryFailed(String error);
}

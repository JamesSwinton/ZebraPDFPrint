package com.zebra.jamesswinton.zebrapdfprintv2.interfaces;

import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;

public interface OnDiscoveryUsbPrintersListener {
    void onFinished(DiscoveredPrinterUsb discoveredPrinterUsb);

    void onError(String error);
}

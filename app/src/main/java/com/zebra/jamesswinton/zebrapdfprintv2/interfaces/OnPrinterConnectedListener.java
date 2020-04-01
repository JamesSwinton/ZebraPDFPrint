package com.zebra.jamesswinton.zebrapdfprintv2.interfaces;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

public interface OnPrinterConnectedListener {
    void onConnected(Connection printerConnection);
    void onError(String error);
}

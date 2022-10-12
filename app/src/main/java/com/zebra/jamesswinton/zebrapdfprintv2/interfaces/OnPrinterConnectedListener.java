package com.zebra.jamesswinton.zebrapdfprintv2.interfaces;

import com.zebra.sdk.comm.Connection;

public interface OnPrinterConnectedListener {
    void onConnected(Connection printerConnection, String printerAddress);
    void onError(String error);
}

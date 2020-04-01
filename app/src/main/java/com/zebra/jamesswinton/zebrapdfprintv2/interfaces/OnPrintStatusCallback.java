package com.zebra.jamesswinton.zebrapdfprintv2.interfaces;

public interface OnPrintStatusCallback {
    void onPrintComplete();
    void onPrintProgress(int fileNumber, int totalFiles, int progress,  int bytesWritten, int totalBytes);
    void onPrintError(String error);
}

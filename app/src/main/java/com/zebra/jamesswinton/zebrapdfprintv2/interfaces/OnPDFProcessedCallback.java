package com.zebra.jamesswinton.zebrapdfprintv2.interfaces;

import android.graphics.Bitmap;

import java.io.File;
import java.util.List;

public interface OnPDFProcessedCallback {
    void onProcessed(File pdfFile, List<Bitmap> bitmaps);
    void onError(String error);
}

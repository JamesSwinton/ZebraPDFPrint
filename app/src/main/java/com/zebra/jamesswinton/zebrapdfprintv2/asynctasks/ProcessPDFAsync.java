package com.zebra.jamesswinton.zebrapdfprintv2.asynctasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnPDFProcessedCallback;
import com.zebra.jamesswinton.zebrapdfprintv2.utilities.FileHelper;
import com.zebra.jamesswinton.zebrapdfprintv2.utilities.PDFHelper;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class ProcessPDFAsync extends AsyncTask<Void, Void, Void> {

    // Debugging
    private static final String TAG = "ProcessPDFAsync";

    // Constants
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    // Private Variables
    private Uri mPdfUri = null;
    private WeakReference<Context> mContext = null;
    private OnPDFProcessedCallback mOnPDFProcessedCallback = null;

    // Public Variables


    public ProcessPDFAsync(WeakReference<Context> context, Uri pdfUri, OnPDFProcessedCallback onPDFProcessedCallback) {
        this.mPdfUri = pdfUri;
        this.mContext = context;
        this.mOnPDFProcessedCallback = onPDFProcessedCallback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            // Get PDF File from URI
            File pdfFile = FileHelper.getFileFromUri(mContext.get(), mPdfUri);

            // Get Bitmaps from PDF Pages
            List<Bitmap> pdfBitmaps = PDFHelper.getBitmapsFromPDFFile(mContext.get(), pdfFile);

            // Return
            mHandler.post(() -> mOnPDFProcessedCallback.onProcessed(pdfFile, pdfBitmaps));

        } catch (IOException e) {
            // Log Results
            e.printStackTrace();
            Log.e(TAG, "IOException: " + e.getMessage());

            // Return Error
            mHandler.post(() -> mOnPDFProcessedCallback.onError(e.getMessage()));
        }

        // Empty Return
        return null;
    }
}

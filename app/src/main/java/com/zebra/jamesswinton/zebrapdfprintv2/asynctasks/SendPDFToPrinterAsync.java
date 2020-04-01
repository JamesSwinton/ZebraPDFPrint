package com.zebra.jamesswinton.zebrapdfprintv2.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnPrintStatusCallback;
import com.zebra.jamesswinton.zebrapdfprintv2.utilities.CustomDialog;
import com.zebra.jamesswinton.zebrapdfprintv2.utilities.PDFHelper;
import com.zebra.sdk.comm.BluetoothConnectionInsecure;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.device.ProgressMonitor;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.security.auth.login.LoginException;

public class SendPDFToPrinterAsync extends AsyncTask<Void, Void, Void> {

    // Debugging
    private static final String TAG = "SendPDFToPrinterAsync";

    // Constants
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    // Private Variables
    private int mQuantity;
    private File mPDFFile;
    private Connection mPrinterConnection;
    private OnPrintStatusCallback mOnPrintStatusCallback;

    // Public Variables


    public SendPDFToPrinterAsync(Connection printerConnection, File pdfFile, int quantity,
                                 OnPrintStatusCallback onPrintStatusCallback) {
        this.mPDFFile = pdfFile;
        this.mQuantity = quantity;
        this.mPrinterConnection = printerConnection;
        this.mOnPrintStatusCallback = onPrintStatusCallback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            // Open the connection - physical connection is established here.
            if (!mPrinterConnection.isConnected()) {
                mPrinterConnection.open();
            }

            // Get Instance of Printer
            ZebraPrinterLinkOs printer = ZebraPrinterFactory.getLinkOsPrinter(mPrinterConnection);

            // Verify Printer Status is Ready
            PrinterStatus printerStatus = printer.getCurrentStatus();
            if (printerStatus.isReadyToPrint) {
                // Scale Printer for First Print
                String scaleCommand = getPrinterScaleCommand(mPrinterConnection);
                SGD.SET("apl.settings", scaleCommand, mPrinterConnection);

                // Loop Quantity
                for (int i = 0; i < mQuantity; i++) {
                    final int fileNumber = i + 1;
                    // Send the data to printer as a byte array.
                    printer.sendFileContents(mPDFFile.getAbsolutePath(), (bytesWritten, totalBytes) -> {
                        // Calc Progress
                        double rawProgress = bytesWritten * 100 / totalBytes;
                        int progress = (int) Math.round(rawProgress);

                        // Notify Calling Class
                        mHandler.post(() -> mOnPrintStatusCallback.onPrintProgress(fileNumber,
                                mQuantity, progress, bytesWritten, totalBytes));
                    });

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(500);
                }

                // Mark Complete
                mHandler.post(() -> mOnPrintStatusCallback.onPrintComplete());
            } else {
                mHandler.post(() -> {
                    if (printerStatus.isPaused) {
                        mOnPrintStatusCallback.onPrintError("Printer paused");
                    } else if (printerStatus.isHeadOpen) {
                        mOnPrintStatusCallback.onPrintError("Printer head open");
                    } else if (printerStatus.isPaperOut) {
                        mOnPrintStatusCallback.onPrintError("Printer is out of paper");
                    } else {
                        mOnPrintStatusCallback.onPrintError("Unknown error occurred");
                    }
                });
            }
        } catch (ConnectionException | InterruptedException | IOException e) {
            // Pass Error Up
            mHandler.post(() -> mOnPrintStatusCallback.onPrintError(e.getMessage()));
        } finally {
            try {
                // Close Connections
                mPrinterConnection.close();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }

        // Empty Return
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    /**
     * Utility Methods
     */

    // Takes the size of the pdf and the printer's maximum size and scales the file down
    private String getPrinterScaleCommand(Connection connection) throws ConnectionException, IOException {
        // Init Scale Command & Factor Holder
        double scaleFactor;
        String scale = "dither scale-to-fit";

//        // Get PDF File Width
//        int pdfFileWidth = PDFHelper.getPageWidth(mPDFFile);
//
//        // Verify Width
//        if (pdfFileWidth != 0) {
//            // Get Printer Model
//            String printerModel = SGD.GET("device.host_identification", connection).substring(0, 5);
//
//            // Set Scale based on Model
//            if (printerModel.equals("iMZ22")||printerModel.equals("QLn22")||printerModel.equals("ZD410")) {
//                scaleFactor = 2.0 / pdfFileWidth * 100;
//            } else if (printerModel.equals("iMZ32")||printerModel.equals("QLn32")||printerModel.equals("ZQ510")) {
//                scaleFactor = 3.0 / pdfFileWidth * 100;
//            } else if (printerModel.equals("QLn42")||printerModel.equals("ZQ520")|| printerModel.equals("ZD420")||printerModel.equals("ZD500")|| printerModel.equals("ZT220")||printerModel.equals("ZT230")|| printerModel.equals("ZT410")) {
//                scaleFactor = 4.0 / pdfFileWidth * 100;
//            } else if (printerModel.equals("ZT420")) {
//                scaleFactor = 6.5 / pdfFileWidth * 100;
//            } else {
//                scaleFactor = 100;
//            }
//
//            // Set Scale Command
//            scale = "dither scale=" + (int) scaleFactor + "x" + (int) scaleFactor;
//        }

        // Return Scale Command
        return scale;
    }
}

package com.zebra.jamesswinton.zebrapdfprintv2.asynctasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnPrinterConnectedListener;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;

public class ConnectToPrinterAsync extends AsyncTask<Void, Void, Void> {

    // Debugging
    private static final String TAG = "ConnectToPrinterAsync";

    // Constants
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    // Private Variables
    private String mPrinterToConnectAddress = null;
    private DiscoveredPrinterUsb mDiscoveredPrinterUsb = null;
    private OnPrinterConnectedListener mOnPrinterConnectedListener = null;


    public ConnectToPrinterAsync(String selectedPrinterMacAddress,
                                 OnPrinterConnectedListener onPrinterConnectedListener) {
        this.mPrinterToConnectAddress = selectedPrinterMacAddress;
        this.mOnPrinterConnectedListener = onPrinterConnectedListener;
    }

    public ConnectToPrinterAsync(DiscoveredPrinterUsb discoveredPrinterUsb,
                                 OnPrinterConnectedListener onPrinterConnectedListener) {
        this.mDiscoveredPrinterUsb = discoveredPrinterUsb;
        this.mPrinterToConnectAddress = discoveredPrinterUsb.address;
        this.mOnPrinterConnectedListener = onPrinterConnectedListener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        // Init Connection
        final Connection connection;
        if (mDiscoveredPrinterUsb != null) {
            connection = mDiscoveredPrinterUsb.getConnection();
        } else {
            connection = new BluetoothConnection(mPrinterToConnectAddress);
        }

        try {
            // Open Connection
            connection.open();

            // Verify Printer Supports PDF
            if (zebraPrinterSupportsPDF(connection)) {
                mHandler.post(() -> mOnPrinterConnectedListener.onConnected(connection, mPrinterToConnectAddress));
            } else {
                mHandler.post(() -> mOnPrinterConnectedListener.onError(
                        "Printer does not support PDF Printing"));

                // Close Connection
                connection.close();
            }
        } catch (ConnectionException e) {
            e.printStackTrace();
            Log.e(TAG, "Connection Failed: " + e.getMessage());

            // Notify Calling Class
            mHandler.post(() -> mOnPrinterConnectedListener.onError(e.getMessage()));
        }

        // Empty Return
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    // Checks the selected printer to see if it has the pdf virtual device installed.
    private boolean zebraPrinterSupportsPDF(Connection connection) throws ConnectionException {
        // Use SGD command to check if apl.enable returns "pdf"
        String printerInfo = SGD.GET("apl.enable", connection);
        return printerInfo.equals("pdf");
    }
}

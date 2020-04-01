package com.zebra.jamesswinton.zebrapdfprintv2.asynctasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnPrinterConnectedListener;
import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnPrinterDisconnectedListener;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.SGD;

public class DisconnectFromPrinterAsync extends AsyncTask<Void, Void, Void> {

    // Debugging
    private static final String TAG = "ConnectToPrinterAsync";

    // Constants
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    // Private Variables
    private Connection mPrinterConnection = null;
    private OnPrinterDisconnectedListener mOnPrinterDisconnectedListener = null;

    // Public Variables


    public DisconnectFromPrinterAsync(Connection printerConnection,
                                      OnPrinterDisconnectedListener onPrinterDisconnectedListener) {
        this.mPrinterConnection = printerConnection;
        this.mOnPrinterDisconnectedListener = onPrinterDisconnectedListener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            // Close Connection
            mPrinterConnection.close();

            // Notify
            mHandler.post(() -> mOnPrinterDisconnectedListener.onDisconnected());
        } catch (ConnectionException e) {
            e.printStackTrace();
            Log.e(TAG, "Disconnection Failed: " + e.getMessage());

            // Notify Calling Class
            mHandler.post(() -> mOnPrinterDisconnectedListener.onError(e.getMessage()));
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

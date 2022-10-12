package com.zebra.jamesswinton.zebrapdfprintv2.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnDiscoveryUsbPrintersListener;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public class PrinterUSBDiscoveryAsync extends AsyncTask<String, Void, Void> {

    // Debugging
    private static final String TAG = "PrintersUSBDiscoveryAsync";

    // Private Variables
    private DiscoveredPrinterUsb discoveredPrinterUsb;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private OnDiscoveryUsbPrintersListener onDiscoveryUsbPrintersListener;
    private WeakReference<Context> mContextWeakRef;

    public PrinterUSBDiscoveryAsync(Context cx, OnDiscoveryUsbPrintersListener onDiscoveryUsbPrintersListener) {
        this.onDiscoveryUsbPrintersListener = onDiscoveryUsbPrintersListener;
        this.mContextWeakRef = new WeakReference<>(cx);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... printerAddress) {
        // Find connected printers
        UsbDiscoveryHandler handler = new UsbDiscoveryHandler();
        UsbDiscoverer.findPrinters(mContextWeakRef.get(), handler);

        try {
            while (!handler.discoveryComplete) {
                Thread.sleep(100);
            }

            if (handler.printers != null && handler.printers.size() > 0) {
                discoveredPrinterUsb = handler.printers.get(0);
                    mHandler.post(() -> onDiscoveryUsbPrintersListener.onFinished(discoveredPrinterUsb));
            } else {
                mHandler.post(() -> onDiscoveryUsbPrintersListener.onError("No Printer Found"));
            }
        } catch (Exception e) {
            mHandler.post(() -> onDiscoveryUsbPrintersListener.onError(e.getMessage()));
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
    }

    // Handles USB device discovery
    static class UsbDiscoveryHandler implements DiscoveryHandler {
        public List<DiscoveredPrinterUsb> printers;
        public boolean discoveryComplete = false;

        public UsbDiscoveryHandler() {
            printers = new LinkedList<DiscoveredPrinterUsb>();
        }

        public void foundPrinter(final DiscoveredPrinter printer) {
            printers.add((DiscoveredPrinterUsb) printer);
        }

        public void discoveryFinished() {
            discoveryComplete = true;
        }

        public void discoveryError(String message) {
            discoveryComplete = true;
        }
    }
}
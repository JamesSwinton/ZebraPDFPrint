package com.zebra.jamesswinton.zebrapdfprintv2.dialogfragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.zebra.jamesswinton.zebrapdfprintv2.R;
import com.zebra.jamesswinton.zebrapdfprintv2.adapter.DiscoveredPrinterAdapter;
import com.zebra.jamesswinton.zebrapdfprintv2.databinding.DialogFragmentSelectPrinterBinding;
import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnSelectPrinterCallback;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import java.util.ArrayList;
import java.util.List;

public class SelectPrinterDialogFragment extends DialogFragment {

    // Debugging
    private static final String TAG = "SelectPrinterFragment";

    // Constants

    // Private Variables
    private DialogFragmentSelectPrinterBinding mDataBinding = null;

    private OnSelectPrinterCallback mOnDiscoveredPrintClickListener = null;
    private DiscoveredPrinterAdapter mDiscoveredPrinterAdapter = null;
    private List<DiscoveredPrinter> mDiscoveredPrinters = null;

    // Public Variables


    public SelectPrinterDialogFragment(OnSelectPrinterCallback onDiscoveredPrinterClickListner) {
        this.mOnDiscoveredPrintClickListener = onDiscoveredPrinterClickListner;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Init DataBinding
        mDataBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.dialog_fragment_select_printer, null, false);

        // Build Dialog
        AlertDialog.Builder dialogBuilder =
                new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        dialogBuilder.setView(mDataBinding.getRoot());

        // Build Adapter
        mDiscoveredPrinters = new ArrayList<>();
        mDiscoveredPrinterAdapter = new DiscoveredPrinterAdapter(mOnDiscoveredPrintClickListener, mDiscoveredPrinters);
        mDataBinding.discoveredPrintersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDataBinding.discoveredPrintersRecyclerView.setAdapter(mDiscoveredPrinterAdapter);

        final AlertDialog dialog = dialogBuilder.create();

        // Init Bluetooth Discovery
        try {
            Log.i(TAG, "Searching for printers...");
            BluetoothDiscoverer.findPrinters(getActivity(), new DiscoveryHandler() {
                @Override
                public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
                    Log.i(TAG, "Discovered Printer: " + discoveredPrinter.address);
                    mDiscoveredPrinters.add(discoveredPrinter);
                    mDiscoveredPrinterAdapter.loadDiscoveredPrinters(mDiscoveredPrinters);
                }

                @Override
                public void discoveryFinished() {
                    Log.i(TAG, "Discovery finished");

                    // Verify If Printer Was Found
                    if (mDiscoveredPrinters.isEmpty()) {
                        mOnDiscoveredPrintClickListener.onDiscoveryFailed("No Printers Found");
                    } else {
                        // Notify User
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Bluetooth Discovery Complete",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void discoveryError(String e) {
                    Log.i(TAG, "Discovery error");

                    // Pass Error Up
                    mOnDiscoveredPrintClickListener.onDiscoveryFailed(e);
                }
            });
        } catch (ConnectionException e) {
            Log.i(TAG, "Printer connection error");
        }

        // Build & Return Dialog
        return dialog;
    }
}

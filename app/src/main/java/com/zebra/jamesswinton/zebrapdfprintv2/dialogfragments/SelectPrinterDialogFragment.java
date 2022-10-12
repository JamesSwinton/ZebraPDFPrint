package com.zebra.jamesswinton.zebrapdfprintv2.dialogfragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zebra.jamesswinton.zebrapdfprintv2.R;
import com.zebra.jamesswinton.zebrapdfprintv2.adapter.DiscoveredPrinterAdapter;
import com.zebra.jamesswinton.zebrapdfprintv2.asynctasks.PrinterUSBDiscoveryAsync;
import com.zebra.jamesswinton.zebrapdfprintv2.databinding.DialogFragmentSelectPrinterBinding;
import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnDiscoveryUsbPrintersListener;
import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnSelectPrinterCallback;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import java.util.ArrayList;
import java.util.List;

public class SelectPrinterDialogFragment extends DialogFragment {

    // Debugging
    private static final String TAG = "SelectPrinterFragment";

    // Private Variables
    private DialogFragmentSelectPrinterBinding mDataBinding = null;

    private OnSelectPrinterCallback mOnDiscoveredPrintClickListener = null;
    private DiscoveredPrinterAdapter mDiscoveredPrinterAdapter = null;
    private List<DiscoveredPrinter> mDiscoveredPrinters = null;

    private Context mContext;

    // Public Variables
    public SelectPrinterDialogFragment(OnSelectPrinterCallback onDiscoveredPrinterClickListener) {
        this.mOnDiscoveredPrintClickListener = onDiscoveredPrinterClickListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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
        mDiscoveredPrinterAdapter = new DiscoveredPrinterAdapter(mOnDiscoveredPrintClickListener);
        mDataBinding.discoveredPrintersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDataBinding.discoveredPrintersRecyclerView.setAdapter(mDiscoveredPrinterAdapter);

        mDataBinding.printersTypeRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.bluetooth_radio) {
                    mDiscoveredPrinters = new ArrayList<>();
                    scanBluetoothPrinters();
                } else if (checkedId == R.id.usb_otg_radio) {
                    mDiscoveredPrinters = new ArrayList<>();
                    scanUSBPrinters();
                }
            }
        });

        // Build & Return Dialog
        return dialogBuilder.create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void scanBluetoothPrinters() {
        updateUI(true);

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
                    updateUI(false);

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
                    updateUI(false);

                    // Pass Error Up
                    mOnDiscoveredPrintClickListener.onDiscoveryFailed(e);
                }
            });
        } catch (ConnectionException e) {
            Log.i(TAG, "Printer connection error");
        }
    }

    private void scanUSBPrinters() {
        updateUI(true);

        // Init USB Discovery
        new PrinterUSBDiscoveryAsync(getActivity(), new OnDiscoveryUsbPrintersListener() {
            @Override
            public void onFinished(DiscoveredPrinterUsb discoveredPrinterUsb) {
                Log.i(TAG, "Discovery finished");
                updateUI(false);

                // Verify If Printer was found
                if (discoveredPrinterUsb == null) {
                    mOnDiscoveredPrintClickListener.onDiscoveryFailed("No Printer Found");
                } else {
                    Log.i(TAG, "Discovered Printer: " + discoveredPrinterUsb.address);
                    mDiscoveredPrinters.add(discoveredPrinterUsb);
                    mDiscoveredPrinterAdapter.loadDiscoveredPrinters(mDiscoveredPrinters);

                    // Notify User
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "USB Discovery Complete",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.i(TAG, error);
                updateUI(false);

                mOnDiscoveredPrintClickListener.onDiscoveryFailed(error);
            }
        }).execute();
    }

    private void updateUI(Boolean isSearching) {
        mDataBinding.bluetoothRadio.setEnabled(!isSearching);
        mDataBinding.usbOtgRadio.setEnabled(!isSearching);

        if (isSearching && mDataBinding.bluetoothRadio.isChecked()) {
            mDataBinding.searchingPrintersContainer.printerType.setImageResource(R.drawable.ic_bluetooth);
            mDataBinding.searchingPrintersContainer.baseLayout.setVisibility(View.VISIBLE);
        } else if (isSearching && mDataBinding.usbOtgRadio.isChecked()) {
            mDataBinding.searchingPrintersContainer.printerType.setImageResource(R.drawable.ic_usb);
            mDataBinding.searchingPrintersContainer.baseLayout.setVisibility(View.VISIBLE);
        } else {
            mDataBinding.searchingPrintersContainer.baseLayout.setVisibility(View.GONE);
        }
    }
}

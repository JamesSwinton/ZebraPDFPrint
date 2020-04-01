package com.zebra.jamesswinton.zebrapdfprintv2.dialogfragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.zebra.jamesswinton.zebrapdfprintv2.R;
import com.zebra.jamesswinton.zebrapdfprintv2.databinding.DialogFragmentPrintSettingsBinding;
import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnPrintSettingsDefinedListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class PrintSettingsDialogFragment extends DialogFragment {

    // Debugging
    private static final String TAG = "PrintSettingDialogFrag";

    // Constants
    private static final String ARG_MIN_PAGE = "arg-min-page";
    private static final String ARG_MAX_PAGE = "arg-max-page";

    // Private Variables
    private DialogFragmentPrintSettingsBinding mDataBinding;
    private OnPrintSettingsDefinedListener mOnPrintSettingsDefinedListener;

    // Public Variables


    public PrintSettingsDialogFragment(OnPrintSettingsDefinedListener onPrintSettingsDefinedListener) {
        this.mOnPrintSettingsDefinedListener = onPrintSettingsDefinedListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Init DataBinding
        mDataBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.dialog_fragment_print_settings, null, false);

        // Build Dialog
        AlertDialog.Builder dialogBuilder =
                new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

        // Init Up / Down Listener
        mDataBinding.quantityUp.setOnClickListener(view -> {
            try {
                int currentQuantity = mDataBinding.quantity.getText() == null ? 1
                        : Integer.parseInt(mDataBinding.quantity.getText().toString().trim());
                mDataBinding.quantity.setText(String.valueOf(++currentQuantity));

            } catch(Exception e) {
                e.printStackTrace();
                Log.i(TAG, "Error Getting Quantity");
            }
        });

        mDataBinding.quantityDown.setOnClickListener(view -> {
            try {
                int currentQuantity = mDataBinding.quantity.getText() == null ? 1
                        : Integer.parseInt(mDataBinding.quantity.getText().toString().trim());
                if (currentQuantity > 1) {
                    mDataBinding.quantity.setText(String.valueOf(--currentQuantity));
                }
            } catch(Exception e) {
                e.printStackTrace();
                Log.i(TAG, "Error Getting Quantity");
            }
        });

        // Set Print Listener
        mDataBinding.printAsFileButton.setOnClickListener(view -> {
            // Notify Calling
            mOnPrintSettingsDefinedListener.print(
                    mDataBinding.quantity.getText() == null ? 1 :
                            Integer.valueOf(mDataBinding.quantity.getText().toString())
            );

            // Remove Dialog
            this.dismiss();
        });

        // Build & Return Dialog
        dialogBuilder.setView(mDataBinding.getRoot());
        return dialogBuilder.create();
    }
}

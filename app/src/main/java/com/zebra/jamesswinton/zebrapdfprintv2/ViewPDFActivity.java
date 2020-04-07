package com.zebra.jamesswinton.zebrapdfprintv2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zebra.jamesswinton.zebrapdfprintv2.adapter.PDFBitmapAdapter;
import com.zebra.jamesswinton.zebrapdfprintv2.asynctasks.ConnectToPrinterAsync;
import com.zebra.jamesswinton.zebrapdfprintv2.asynctasks.DisconnectFromPrinterAsync;
import com.zebra.jamesswinton.zebrapdfprintv2.asynctasks.ProcessPDFAsync;
import com.zebra.jamesswinton.zebrapdfprintv2.asynctasks.SendPDFToPrinterAsync;
import com.zebra.jamesswinton.zebrapdfprintv2.databinding.ActivityViewPdfBinding;
import com.zebra.jamesswinton.zebrapdfprintv2.dialogfragments.PrintSettingsDialogFragment;
import com.zebra.jamesswinton.zebrapdfprintv2.dialogfragments.SelectPrinterDialogFragment;
import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnPDFProcessedCallback;
import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnPrintStatusCallback;
import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnPrinterConnectedListener;
import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnPrinterDisconnectedListener;
import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnSelectPrinterCallback;
import com.zebra.jamesswinton.zebrapdfprintv2.utilities.CustomDialog;
import com.zebra.jamesswinton.zebrapdfprintv2.utilities.FileHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class ViewPDFActivity extends AppCompatActivity implements OnSelectPrinterCallback {

    // Debugging
    private static final String TAG = "ViewPDFActivity";

    // Constants
    private static final int SELECT_PDF_INTENT = 100;
    private static final String DEFAULT_SHARED_PREFS = "default-shared-prefs";
    private static final String PRINTER_MAC_PREF = "printer-mac-pref";

    // Private Variables
    private File mPDFFile = null;
    private List<Bitmap> mPDFBitmaps = null;
    private PDFBitmapAdapter mPDFBitmapAdapter = null;

    private AlertDialog mLoadingDialog = null;
    private SelectPrinterDialogFragment mSelectPrinterDialogFragment = null;

    private String mPrinterMacAddress = null;
    private Connection mPrinterConnection = null;
    private SharedPreferences mSharedPreferences = null;

    // Public Variables
    private ActivityViewPdfBinding mDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_view_pdf);
        mSharedPreferences = getSharedPreferences(DEFAULT_SHARED_PREFS, MODE_PRIVATE);
        mSelectPrinterDialogFragment = new SelectPrinterDialogFragment(this);

        // Init Toolbar
        setSupportActionBar(mDataBinding.toolbarLayout.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Zebra PDF Print Demo");
        }

        // Handle PDF Uri
        Intent viewPdfIntent = getIntent();
        if (viewPdfIntent != null && viewPdfIntent.getData() != null) {
            processPdf(viewPdfIntent.getData());
        } else {
            Log.e(TAG, "No PDF URI Provided, exiting");
            Toast.makeText(this, "Could not get PDF File", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
        }

        // Create RecyclerView Adapter
        mPDFBitmapAdapter = new PDFBitmapAdapter(mDataBinding.pdfRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mDataBinding.pdfRecyclerView.setLayoutManager(linearLayoutManager);
        mDataBinding.pdfRecyclerView.setAdapter(mPDFBitmapAdapter);

        // Init Select Printer Listener
        mDataBinding.printerStatusLayout.baseLayout.setOnClickListener(view -> {
            // Show Printer Connection Dialog
            mSelectPrinterDialogFragment.show(getSupportFragmentManager(), "select-printer");
        });

        // Init Print Listener
        mDataBinding.printButton.setOnClickListener(view -> printPdf());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verify Result
        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_PDF_INTENT && data != null
                && data.getData() != null) {
            Log.i(TAG, "PDF Selected: " + data.getData());
            // Start ViewPdfActivity (Pass Uri)
            processPdf(data.getData());
        } else {
            Log.e(TAG, "No PDF Selected");
            Toast.makeText(ViewPDFActivity.this, "No PDF Selected", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Show Confirm Dialog
        CustomDialog.showCustomDialog(this, CustomDialog.DialogType.INFO, "Confirm Exit",
            "Are you sure you wish to exit this application?",
            "EXIT", (dialogInterface, i) -> {
                // Disconnect Printer Gracefully
                if (mPrinterConnection != null && mPrinterConnection.isConnected()) {
                    AlertDialog loadingDialog = CustomDialog.createLoadingDialog(
                            ViewPDFActivity.this,
                            "Closing Printer Connection");
                    loadingDialog.show();

                    // Close Connection
                    new DisconnectFromPrinterAsync(mPrinterConnection, new OnPrinterDisconnectedListener() {
                        @Override
                        public void onDisconnected() {
                            // Clear Loading Dialog
                            loadingDialog.dismiss();
                            // Clear Connection
                            mPrinterConnection = null;
                            // Exit
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Disconnect Error: " + error);
                            // Clear Loading Dialog
                            loadingDialog.dismiss();
                            // Clear Connection
                            mPrinterConnection = null;
                            // Exit
                            finish();
                        }
                    }).execute();
                } else {
                    finish();
                }
            }, "CANCEL", null);
    }

    /**
     * Toolbar Menu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_pdf_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle Navigation Events
        switch(item.getItemId()) {
            case R.id.select_new_pdf:
                // Open File Selection
                Intent selectPdfIntent = new Intent();
                selectPdfIntent.setType("application/pdf");
                selectPdfIntent.setAction(Intent.ACTION_GET_CONTENT);
                selectPdfIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(selectPdfIntent, "Select PDF"),
                        SELECT_PDF_INTENT);
                break;
        } return true;
    }

    private void printPdf() {
        // Verify Printer Status
        if (mPrinterConnection != null) {
            if (mPDFFile != null) {
                // Show PrintSettingsDialogFragment
                new PrintSettingsDialogFragment((quantity) -> {
                    // Inflate View
                    View customDialogView = LayoutInflater.from(this).inflate(
                            R.layout.layout_determinate_loading_dialog, null);

                    // Get Progress Bar
                    ProgressBar progressBar = customDialogView.findViewById(R.id.progress_bar);
                    TextView progressText = customDialogView.findViewById(R.id.bytes_written);
                    TextView dialogTitle = customDialogView.findViewById(R.id.dialog_title);
                    dialogTitle.setText("Sending "  + (quantity > 1 ? quantity + " PDFs " : "PDF ")
                            + "to " + mPrinterConnection.getSimpleConnectionName());

                    // Create Dialog
                    AlertDialog progressDialog = new MaterialAlertDialogBuilder(this)
                            .setView(customDialogView)
                            .setCancelable(false)
                            .create();

                    // Show Dialog
                    progressDialog.show();

                    // Get Scale & Additional ZPL Shared Pref
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    String scaleCommand = sharedPreferences.getString("pdf_print_scale_pref", "");
                    String additionalZpl = sharedPreferences.getString("additional_zpl_pref", "");

                    // Init Print
                    new SendPDFToPrinterAsync(mPrinterConnection, mPDFFile, quantity, scaleCommand,
                            additionalZpl, new OnPrintStatusCallback() {
                        @Override
                        public void onPrintComplete() {
                            progressDialog.dismiss();
                            CustomDialog.showCustomDialog(ViewPDFActivity.this, CustomDialog.DialogType.SUCCESS,
                                "Print Complete", "PDF File sent to printer",
                                "OK", (dialogInterface, i) -> dialogInterface.dismiss(),
                                null, null,
                                dialogInterface -> {
                                    // Disconnect Printer Gracefully
                                    if (mPrinterConnection != null && mPrinterConnection.isConnected()) {
                                        AlertDialog loadingDialog = CustomDialog.createLoadingDialog(
                                                ViewPDFActivity.this,
                                                "Closing Printer Connection");
                                        loadingDialog.show();

                                        // Close Connection
                                        new DisconnectFromPrinterAsync(mPrinterConnection, new OnPrinterDisconnectedListener() {
                                            @Override
                                            public void onDisconnected() {
                                                // Clear Loading Dialog
                                                loadingDialog.dismiss();
                                                // Clear Connection
                                                mPrinterConnection = null;
                                                // Exit
                                                finish();
                                            }

                                            @Override
                                            public void onError(String error) {
                                                Log.e(TAG, "Disconnect Error: " + error);
                                                // Clear Loading Dialog
                                                loadingDialog.dismiss();
                                                // Clear Connection
                                                mPrinterConnection = null;
                                                // Exit
                                                finish();
                                            }
                                        }).execute();
                                    } else {
                                        finish();
                                    }
                                });
                        }

                        @Override
                        public void onPrintProgress(int fileNumber, int totalFiles, int progress,
                                                    int bytesWritten, int totalBytes) {
                            dialogTitle.setText("Sending PDF " + fileNumber + " of " + totalFiles);
                            progressBar.setProgress(progress);
                            progressText.setText(getResources().getString(R.string.bytes,
                                    bytesWritten, totalBytes));
                        }

                        @Override
                        public void onPrintError(String error) {
                            progressDialog.dismiss();
                            CustomDialog.showCustomDialog(ViewPDFActivity.this,
                                    CustomDialog.DialogType.ERROR, "Print Failed", error);
                        }
                    }).execute();
                }).show(getSupportFragmentManager(), "PRINT-SETTINGS-FRAGMENT");

            } else {
                CustomDialog.showCustomDialog(this, CustomDialog.DialogType.ERROR,
                        "no PDF Found", "Please select a PDF");
            }
        } else {
            CustomDialog.showCustomDialog(this, CustomDialog.DialogType.ERROR,
                    "no Printer Found", "Please select a printer");
        }
    }

    /**
     * Interfaces
     */

    @Override
    public void onDiscoveredPrinterSelected(DiscoveredPrinter discoveredPrinter) {
        // Init Connection
        connectToPrinter(discoveredPrinter.address);
    }

    @Override
    public void onDiscoveryFailed(String error) {
        // Dismiss Dialog
        if (mSelectPrinterDialogFragment != null) {
            mSelectPrinterDialogFragment.dismiss();
        }

        // Show Error Dialog
        CustomDialog.showCustomDialog(this, CustomDialog.DialogType.ERROR,
                "Bluetooth Discovery Error", error);
    }

    private void connectToPrinter(String printerMacAddress) {
        // Clear Connection
        mPrinterConnection = null;

        // Remove Connection Dialog
        if (mSelectPrinterDialogFragment.isAdded()) {
            mSelectPrinterDialogFragment.dismiss();
        }

        // Show Progress Dialog
        AlertDialog progressDialog = CustomDialog.createLoadingDialog(this,
                "Connecting to Printer: " + printerMacAddress);
        progressDialog.show();

        // Connect to selected printer
        new ConnectToPrinterAsync(printerMacAddress, new OnPrinterConnectedListener() {
            @Override
            public void onConnected(Connection printerConnection) {
                // Remove Dialog
                progressDialog.dismiss();

                // Update UI
                mDataBinding.printerStatusLayout.baseLayout.setBackgroundColor(
                        getResources().getColor(R.color.success));
                mDataBinding.printerStatusLayout.progressMessage.setText("Printer Connected: "
                        + printerConnection.getSimpleConnectionName());
                mDataBinding.printerStatusLayout.progressBar.setImageResource(R.drawable.ic_success);

                // Store Connection
                mPrinterConnection = printerConnection;

                // Store MAC Address in SharedPrefs
                if (mPrinterMacAddress == null || !mPrinterMacAddress.equals(printerMacAddress)) {
                    mSharedPreferences.edit().putString(PRINTER_MAC_PREF, printerMacAddress)
                            .apply();
                }
            }

            @Override
            public void onError(String error) {
                // Remove Progress dialog
                progressDialog.dismiss();

                // Show Error Dialog
                CustomDialog.showCustomDialog(ViewPDFActivity.this, CustomDialog.DialogType.ERROR,
                        "Connection Failed", error);
            }
        }).execute();
    }

    /**
     * PDF Handling
     */

    private void processPdf(Uri pdfUri) {
        // Show Loading Dialog
        mLoadingDialog = CustomDialog.createLoadingDialog(this, "Processing PDF...");
        mLoadingDialog.show();

        // Init PDF Processing Async
        new ProcessPDFAsync(new WeakReference<>(this), pdfUri, new OnPDFProcessedCallback() {
            @Override
            public void onProcessed(File pdfFile, List<Bitmap> bitmaps) {
                // Get PDF File from URI
                mPDFFile = pdfFile;

                // Get Bitmaps from PDF Pages
                mPDFBitmaps = bitmaps;

                // Update Adapter
                mPDFBitmapAdapter.loadPages(mPDFBitmaps);

                // Init Title
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle(FileHelper.getFileNameFromContentUriWithExtension(
                            ViewPDFActivity.this, pdfUri));
                }

                // Hide dialog
                if (mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }

                // Check for Existing Printer MAC Address
                mPrinterMacAddress = mSharedPreferences.getString(PRINTER_MAC_PREF, null);
                if (mPrinterMacAddress != null && (mPrinterConnection == null || !mPrinterConnection.isConnected())) {
                    Log.i(TAG, "Previous printer stored, attempting to connect");
                    connectToPrinter(mPrinterMacAddress);
                } else {
                    Log.i(TAG, "No default printer found");
                }
            }

            @Override
            public void onError(String error) {
                // Log Results
                Log.e(TAG, "IOException: " + error);
                Toast.makeText(ViewPDFActivity.this, "Could not process PDF", Toast.LENGTH_LONG).show();

                // Hide dialog
                if (mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
            }
        }).execute();
    }
}

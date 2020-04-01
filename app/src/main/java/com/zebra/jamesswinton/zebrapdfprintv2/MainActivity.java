package com.zebra.jamesswinton.zebrapdfprintv2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zebra.jamesswinton.zebrapdfprintv2.databinding.ActivityMainBinding;
import com.zebra.jamesswinton.zebrapdfprintv2.utilities.Constants;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "MainActivity";

    // Constants
    private static final int SELECT_PDF_INTENT = 100;
    private static final int PERMISSIONS_REQUEST = 0;

    // Private Variables
    ActivityMainBinding mDataBinding;

    // Public Variables


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Verify Permissions
        if (!permissionsGranted()) {
            // Request Permissions
            ActivityCompat.requestPermissions(this, Constants.PERMISSIONS, PERMISSIONS_REQUEST);

            // Exit Setup, Resume in onPermissionsResult Callback if Granted
            return;
        }

        // Init PDF Selection
        mDataBinding.baseLayout.setOnClickListener(view -> openFileSelectionDialog());
    }

    private void openFileSelectionDialog() {
        Log.i(TAG, "Opening PDF Selection Dialog");

        // Open File Selection
        Intent selectPdfIntent = new Intent();
        selectPdfIntent.setType("application/pdf");
        selectPdfIntent.setAction(Intent.ACTION_GET_CONTENT);
        selectPdfIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(selectPdfIntent, "Select PDF"),
                SELECT_PDF_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verify Result
        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_PDF_INTENT && data != null
                && data.getData() != null) {
            Log.i(TAG, "PDF Selected: " + data.getData());

            // Start ViewPdfActivity (Pass Uri)
            Intent viewPDFActivity = new Intent(this, ViewPDFActivity.class);
            viewPDFActivity.setData(data.getData());
            startActivity(viewPDFActivity);
        } else {
            Log.e(TAG, "No PDF Selected");
        }
    }

    /**
     * Permissions Methods
     */

    private boolean permissionsGranted() {
        boolean permissionsGranted = true;
        for (String permission : Constants.PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED) {
                permissionsGranted = false;
                break;
            }
        }

        return permissionsGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);

        // Handle Permissions Request
        if (requestCode == PERMISSIONS_REQUEST) {
            Log.i(TAG, "Permissions Request Complete - checking permissions granted...");

            // Validate Permissions State
            boolean permissionsGranted = true;
            if (results.length > 0) {
                for (int result : results) {
                    if (result != PERMISSION_GRANTED) {
                        permissionsGranted = false;
                    }
                }
            } else {
                permissionsGranted = false;
            }

            // Check Permissions were granted & Load slide images or exit
            if (permissionsGranted) {
                Log.i(TAG, "Permissions Granted, loading slide images...");

                // Init Selection Listener
                mDataBinding.baseLayout.setOnClickListener(view -> openFileSelectionDialog());
            } else {
                Log.e(TAG, "Permissions Denied - Exiting App");

                // Explain reason
                Toast.makeText(this, "Please enable all permissions to run this app",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}

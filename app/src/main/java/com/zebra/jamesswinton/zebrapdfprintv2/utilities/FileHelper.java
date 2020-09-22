package com.zebra.jamesswinton.zebrapdfprintv2.utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {

    // Debugging
    private static final String TAG = "FileHelper";

    // Constants
    private static final String[] mProjection = {MediaStore.MediaColumns.DISPLAY_NAME};

    // Private Variables


    // Public Variables


    public FileHelper() {    }

    @Nullable
    public static File getFileFromUri(Context context, Uri contentUri) throws IOException {
        // Get Input Stream && Init File
        File pdfFile = null;
        InputStream inputStream = context.getContentResolver().openInputStream(contentUri);
        if (inputStream != null) {
            try {
                pdfFile = File.createTempFile(getFileNameFromContentUri(context, contentUri), ".pdf", context.getCacheDir());
                try (OutputStream output = new FileOutputStream(pdfFile)) {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                }
            } finally {
                inputStream.close();
            }
        } return pdfFile;
    }

    @NonNull
    public static String getFileNameFromContentUri(Context context, Uri uri) {
        String path = "pdf-to-print";
        ContentResolver cr = context.getContentResolver();
        Cursor metaCursor = cr.query(uri, mProjection, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    path = metaCursor.getString(0);
                }
            } finally {
                metaCursor.close();
            }
        }

        if (path.indexOf(".") > 0) {
            return path.substring(0, path.lastIndexOf("."));
        } else {
            return path;
        }
    }

    @NonNull
    public static String getFileNameFromContentUriWithExtension(Context context, Uri uri) {
        String path = "pdf-to-print";
        ContentResolver cr = context.getContentResolver();
        Cursor metaCursor = cr.query(uri, mProjection, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    path = metaCursor.getString(0);
                }
            } finally {
                metaCursor.close();
            }
        }

        return path;
    }

    public static File getFileFromBase64(Context cx, String pdfBase64) throws IOException {
        byte[] imgBytesData = Base64.decode(pdfBase64, Base64.DEFAULT);
        File file = File.createTempFile("pdf-to-print" + System.currentTimeMillis(), ".pdf", cx.getCacheDir());
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            bufferedOutputStream.write(imgBytesData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return file;
    }

    public static Uri getUriFromBase64(Context cx, String base64) {
        try {
            File pdfFle = FileHelper.getFileFromBase64(cx, base64);
            if (pdfFle != null && pdfFle.exists()) {
                return Uri.fromFile(pdfFle);
            } else {
                Log.e(TAG, "No PDF File found at path: " + pdfFle.getAbsolutePath());
                Toast.makeText(cx, "Could not get PDF File at path: "
                        + pdfFle.getAbsolutePath(), Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}

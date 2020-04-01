package com.zebra.jamesswinton.zebrapdfprintv2.utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
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

}

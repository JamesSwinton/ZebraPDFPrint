package com.zebra.jamesswinton.zebrapdfprintv2.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFHelper {

    // Debugging
    private static final String TAG = "PDFHelper";

    // Constants


    // Private Variables


    // Public Variables


    public PDFHelper() { }

    public static List<Bitmap> getBitmapsFromPDFFile(Context context, File pdfFile) throws IOException {
        // Init Holder Array
        List<Bitmap> pdfBitmaps = new ArrayList<>();

        // Get ParcelFileDescriptor
        ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY);

        // Create PDF Renderer
        PdfRenderer pdfRenderer = new PdfRenderer(parcelFileDescriptor);

        // Loop Renderer -> Convert Pages to Bitmap
        for (int i = 0; i < pdfRenderer.getPageCount(); i++) {
            PdfRenderer.Page page = pdfRenderer.openPage(i);
            int width = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
            int height = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
            page.close();
            pdfBitmaps.add(bitmap);
        }

        // return Array of Bitmaps
        return pdfBitmaps;
    }

    public static Integer getPageWidth(File pdfFile) throws IOException {
        // Get ParcelFileDescriptor
        ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY);

        // Build PDFRender -> Return Width
        PdfRenderer pdf = new PdfRenderer(parcelFileDescriptor);
        PdfRenderer.Page page = pdf.openPage(0);
        int pixWidth = page.getWidth();
        return pixWidth / 72;
    }

}

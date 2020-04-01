package com.zebra.jamesswinton.zebrapdfprintv2.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.jamesswinton.zebrapdfprintv2.R;

import java.util.List;

public class PDFBitmapAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Debugging
    private static final String TAG = "PdfPageAdapter";

    // Constants


    // Private Variables
    private RecyclerView mRecyclerView;
    private List<Bitmap> mPagesBitmaps;

    // Public Variables


    public PDFBitmapAdapter(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Adapter View
        final View pdfPageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_pdf_page, parent, false);

        // Set Width (to show preview)
        if (!mPagesBitmaps.isEmpty() && mPagesBitmaps.size() > 1) {
            int height = mRecyclerView.getHeight();
            ViewGroup.LayoutParams params = pdfPageView.getLayoutParams();
            params.height = (int) (height * 0.9);
            pdfPageView.setLayoutParams(params);
        }

        // Return View
        return new PageViewHolder(pdfPageView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        // Cast ViewHolder to PopulatedViewHolder
        PageViewHolder vh = (PageViewHolder) viewHolder;

        // Set Page Image
        vh.pageImageView.setImageBitmap(mPagesBitmaps.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mPagesBitmaps == null ? 0 : mPagesBitmaps.size();
    }

    /**
     * Utility Methods
     */

    public void loadPages(List<Bitmap> pageBitmaps) {
        this.mPagesBitmaps = pageBitmaps;
        notifyDataSetChanged();
    }

    /**
     * View Holder
     */

    public class PageViewHolder extends RecyclerView.ViewHolder {

        // Views
        ImageView pageImageView;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            pageImageView = itemView.findViewById(R.id.pdf_image);
        }
    }
}
package com.zebra.jamesswinton.zebrapdfprintv2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.jamesswinton.zebrapdfprintv2.R;
import com.zebra.jamesswinton.zebrapdfprintv2.interfaces.OnSelectPrinterCallback;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.util.List;

public class DiscoveredPrinterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Debugging
    private static final String TAG = "DiscoveredPrinterAdapter";

    // Constants
    private static final int NO_PRINTERS_FOUND = 0;
    private static final int PRINTERS_FOUND = 1;

    // Private Variables
    private List<DiscoveredPrinter> mDiscoveredPrinters;
    private OnSelectPrinterCallback mOnDiscoveredPrinterClickListener;

    // Public Variables


    public DiscoveredPrinterAdapter(OnSelectPrinterCallback onDiscoveredPrinterClickListner,
                                    List<DiscoveredPrinter> discoveredPrinters) {
        this.mDiscoveredPrinters = discoveredPrinters;
        this.mOnDiscoveredPrinterClickListener = onDiscoveredPrinterClickListner;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NO_PRINTERS_FOUND) {
            return new NoPrintersFoundViewHolder(LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.adapter_no_printers_found, parent,
                    false));
        } else {
            return new PrintersFoundViewHolder(LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.adapter_printers_found, parent,
                    false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof PrintersFoundViewHolder) {

            // Show Max Address in View
            ((PrintersFoundViewHolder) viewHolder).printerName.setText(mDiscoveredPrinters
                    .get(position).address);

            // Set Listener
            ((PrintersFoundViewHolder) viewHolder).baseLayout.setOnClickListener(view -> {
                // Pass Click to Calling class
                mOnDiscoveredPrinterClickListener.onDiscoveredPrinterSelected(mDiscoveredPrinters.get(position));
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDiscoveredPrinters == null || mDiscoveredPrinters.isEmpty() ? NO_PRINTERS_FOUND : PRINTERS_FOUND;
    }

    @Override
    public int getItemCount() {
        return mDiscoveredPrinters == null || mDiscoveredPrinters.isEmpty() ? 1 :
                mDiscoveredPrinters.size();
    }

    /**
     * Utility Methods
     */

    public void loadDiscoveredPrinters(List<DiscoveredPrinter> discoveredPrinters) {
        this.mDiscoveredPrinters = discoveredPrinters;
        notifyDataSetChanged();
    }

    /**
     * View Holder
     */

    public class NoPrintersFoundViewHolder extends RecyclerView.ViewHolder {
        NoPrintersFoundViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class PrintersFoundViewHolder extends RecyclerView.ViewHolder {

        // Views
        LinearLayout baseLayout;
        TextView printerName;

        public PrintersFoundViewHolder(@NonNull View itemView) {
            super(itemView);
            baseLayout = itemView.findViewById(R.id.base_layout);
            printerName = itemView.findViewById(R.id.printer_name);
        }
    }
}

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
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;

import java.util.ArrayList;
import java.util.List;

public class DiscoveredPrinterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Debugging
    private static final String TAG = "DiscoveredPrinterAdapter";

    // Private Variables
    private List<DiscoveredPrinter> mDiscoveredPrinters = new ArrayList<>();
    private OnSelectPrinterCallback mOnDiscoveredPrinterClickListener;

    public DiscoveredPrinterAdapter(OnSelectPrinterCallback onDiscoveredPrinterClickListener) {
        this.mOnDiscoveredPrinterClickListener = onDiscoveredPrinterClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PrintersFoundViewHolder(LayoutInflater.from(
                parent.getContext()).inflate(R.layout.adapter_printers_found, parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        // Show Max Address in View
        ((PrintersFoundViewHolder) viewHolder).printerName.setText(mDiscoveredPrinters
                .get(position).address);

        // Set Listener
        ((PrintersFoundViewHolder) viewHolder).baseLayout.setOnClickListener(view -> {
            // Pass Click to Calling class
            if (mDiscoveredPrinters.get(0) instanceof DiscoveredPrinterUsb) {
                mOnDiscoveredPrinterClickListener.onDiscoveredPrinterUSBSelected((DiscoveredPrinterUsb) mDiscoveredPrinters.get(position));
            } else {
                mOnDiscoveredPrinterClickListener.onDiscoveredPrinterSelected(mDiscoveredPrinters.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDiscoveredPrinters.size();
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

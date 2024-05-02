package com.example.my_app.view_adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_app.R;
import com.example.my_app.models.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
    int selectedPosition = -1;
    private List<Address> addresses;
    private Context context;
    private OnAddressClickedListener listener;

    public AddressAdapter(List<Address> addresses, Context context, OnAddressClickedListener listener) {
        this.addresses = addresses;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.address_box, parent, false);

        return new AddressAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Address address = addresses.get(position);

        holder.radioButton.setText(address.getAddress());
        holder.radioButton.setChecked(position == selectedPosition);
        holder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    selectedPosition = holder.getBindingAdapterPosition();
                    listener.onAddressClick(address);
                }
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onAddressDelete(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private RadioButton radioButton;
        private ImageView deleteButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.address_box_active);
            deleteButton = itemView.findViewById(R.id.address_box_delete_btn);
        }
    }

    public interface OnAddressClickedListener {
        void onAddressClick(Address address);
        void onAddressDelete(Address address);
    };
}

package com.example.gronthomongol.ui.util.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.models.Order;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderItemViewHolder> {
    private Context context;
    private List<Order> orders;
    private OnOrderClickListener monOrderClickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class OrderItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView idTextView;
        public TextView timestampTextView;
        public TextView statusTextView;
        public TextView priceTextView;

        OnOrderClickListener onOrderClickListener;
        public OrderItemViewHolder(View view, OnOrderClickListener onOrderClickListener) {
            super(view);
            this.onOrderClickListener = onOrderClickListener;
            idTextView = view.findViewById(R.id.idTextViewItemOrder);
            timestampTextView = view.findViewById(R.id.timestampTextViewItemOrder);
            statusTextView = view.findViewById(R.id.statusTextViewItemOrder);
            priceTextView = view.findViewById(R.id.priceTextViewItemOrder);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onOrderClickListener.onOrderClick(getAdapterPosition());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public OrdersAdapter(List<Order> orders_para, Context context, OnOrderClickListener onOrderClickListener) {
        monOrderClickListener = onOrderClickListener;
        orders = orders_para;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public OrdersAdapter.OrderItemViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_orders, parent, false);
        OrderItemViewHolder viewHolder = new OrderItemViewHolder(view, monOrderClickListener);
        return viewHolder;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(OrderItemViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        DateFormat dateFormat = new SimpleDateFormat("d MMM, yyyy, EEE | hh:mm a");
        Order order = orders.get(position);
        String id = "OrderID# " + order.getOrderId();
        String timestamp = dateFormat.format(order.getCreated());
        String price = order.getTotal_Price() + " BDT";

        holder.idTextView.setText(id);
        holder.timestampTextView.setText(timestamp);
        holder.priceTextView.setText(price);

        if(order.isDelivered()){
            holder.statusTextView.setText("ডেলিভার করা হয়েছে");
            holder.statusTextView.setTextColor(Color.parseColor("#157015"));
        } else if(order.isPaid()){
            holder.statusTextView.setText("পেমেন্ট করা হয়েছে");
            holder.statusTextView.setTextColor(Color.parseColor("#918200"));
        } else{
            holder.statusTextView.setText("পেমেন্ট বাকি আছে");
            holder.statusTextView.setTextColor(Color.parseColor("#800A0A"));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return orders.size();
    }

    public interface OnOrderClickListener{
        void onOrderClick(int position);
    }
}
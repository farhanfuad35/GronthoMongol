package com.example.gronthomongol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class OrderlistAdapterRV_admin extends RecyclerView.Adapter<OrderlistAdapterRV_admin.MyViewHolder> {
    private Context context;
    private List<Order> orders;
    private OnOrderClickListener monOrderClickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView tvOrderId;
        public TextView tvOrderDate;
        public TextView tvPrice;
        public TextView tvStatus;
        OnOrderClickListener onOrderClickListener;
        public MyViewHolder(View v, OnOrderClickListener onOrderClickListener) {
            super(v);
            this.onOrderClickListener = onOrderClickListener;
            tvOrderId = v.findViewById(R.id.tvOrderRow_OrderId);
            tvOrderDate = v.findViewById(R.id.tvOrderRow_Date);
            tvPrice = v.findViewById(R.id.tvOrderList_price);
            tvStatus = v.findViewById(R.id.tvOrderRow_Status);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onOrderClickListener.onOrderClick(getAdapterPosition());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public OrderlistAdapterRV_admin(List<Order> orders_para, Context context, OnOrderClickListener onOrderClickListener) {
        monOrderClickListener = onOrderClickListener;
        orders = orders_para;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public OrderlistAdapterRV_admin.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                    int viewType) {
        // create a new view
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_order_list_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(view, monOrderClickListener);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        DateFormat dateFormat = new SimpleDateFormat("d MMM, yyyy, EEE");
        Order order = orders.get(position);

        holder.tvOrderId.setText("Order ID # " + order.getOrderId());
        holder.tvOrderDate.setText(dateFormat.format(order.getCreated()));
        holder.tvPrice.setText(order.getTotal_Price() + "/=");
        if(order.isDelivered()){
            holder.tvStatus.setText("Delivered");
            holder.tvStatus.setTextColor(Color.parseColor("#157015"));
        }
        else{
            if(order.isPaid()){
                holder.tvStatus.setText("Pending");
                holder.tvStatus.setTextColor(Color.parseColor("#918200"));
            }
            else{
                holder.tvStatus.setText("Payment Incomplete");
                holder.tvStatus.setTextColor(Color.parseColor("#800A0A"));
            }
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
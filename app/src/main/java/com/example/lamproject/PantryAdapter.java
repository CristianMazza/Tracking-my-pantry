package com.example.lamproject;

import android.content.Context;
import android.graphics.Color;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    private ViewHolder.OnViewClickListener listener;
    private List<Product> productsList;
    private Context context;
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    private String today;
    private Calendar calendar;
    private SimpleDateFormat format;



    public PantryAdapter(List<Product> productsList, Context context, ViewHolder.OnViewClickListener listener) {
        this.listener = listener;
        this.productsList = productsList;
        this.context = context;
        StrictMode.setThreadPolicy(policy);
        calendar = Calendar.getInstance();
         format = new SimpleDateFormat("dd/MM/yyyy");
        today= format.format(calendar.getTime());
    }




    public static class ViewHolder extends RecyclerView.ViewHolder{
        private OnViewClickListener listener;
        private TextView price;
        private TextView name;
        private TextView barcode;
        private TextView quantity;
        private ImageView img;
        private TextView category;
        private TextView data;


        public interface OnViewClickListener {
            void onViewClick(View v, int adapterPosition);
        }

        public ViewHolder(final View v, OnViewClickListener listener){
            super(v);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onViewClick(v, getAdapterPosition());
                }
            });

            name = (TextView)v.findViewById(R.id.nomeProductPantry);
            barcode = (TextView)v.findViewById(R.id.barcodeProductPantry);
            img = (ImageView) v.findViewById(R.id.imageViewPantry);
            quantity = (TextView) v.findViewById(R.id.quantitaProductPantry);
            price = (TextView) v.findViewById(R.id.prezzoProductPantry);
            category = (TextView) v.findViewById(R.id.categoryProductPantry);
            data = (TextView) v.findViewById(R.id.dataProductPantry);
        }
    }


    @Override
    public int getItemCount() {
        if(productsList == null) return 0;
        else return productsList.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View viewProduct = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.product_pantry, viewGroup, false);


        return new ViewHolder(viewProduct, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product pv = productsList.get(position);
        holder.quantity.setText(String.valueOf(pv.getQuantity()));
        holder.name.setText(pv.getName());
        holder.barcode.setText(pv.getBarcode());
        holder.price.setText(String.valueOf(pv.getPrice()));
        holder.category.setText(pv.getCategory());
        boolean scaduto = false;
        boolean inScadenza = false;
        try {
            Date tod = format.parse(today);
            Date prodDate = format.parse(pv.getDate());

            if(prodDate.before(tod)) scaduto = true;
            else if(prodDate.equals(tod)) inScadenza = true;
        }catch (Exception ex){}

        if(!scaduto && !inScadenza)holder.data.setText(pv.getDate());
        else if(inScadenza){
            holder.data.setText("Oggi");
            holder.data.setTextColor(Color.rgb(255, 165, 0));
        }
        else{
            holder.data.setText("Scaduto");
            holder.data.setTextColor(Color.RED);
        }

        if (pv.getImg() != null && pv.getImg().contains("https://")) {
            Glide.with(context).load(pv.getImg()).into(holder.img);
        } else {
            holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
        };
    }



    public void setProductsList(List<Product> productsList){
        this.productsList = productsList;
    }

    public Product getProductByPosition (int position){
        return productsList.get(position);
    }

    public List<Product> getProductsList(){
        return productsList;
    }

}

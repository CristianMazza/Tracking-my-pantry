package com.example.lamproject;

import android.content.Context;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private ViewHolder.OnViewClickListener listener;
    private List<ProductOnline> productsList;
    private Context context;
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();



    //private Dao product;

    public HomeAdapter(List<ProductOnline> productsList, Context context, ViewHolder.OnViewClickListener listener) {
        this.listener = listener;
        this.productsList = productsList;
        this.context = context;
        StrictMode.setThreadPolicy(policy);
    }




    public static class ViewHolder extends RecyclerView.ViewHolder{
        private OnViewClickListener listener;
        private TextView id;
        private TextView name;
        private TextView barcode;
        private TextView description;
        private ImageView img;
       // private ImageLoad imageLoad;

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

           id = (TextView)v.findViewById(R.id.idProductView);
           name = (TextView)v.findViewById(R.id.nomeProductPantry);
           barcode = (TextView)v.findViewById(R.id.quantitaProductPantry);
           description = (TextView)v.findViewById(R.id.descriptionProductView);
           img = (ImageView) v.findViewById(R.id.imageViewPantry);
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
                .inflate(R.layout.product_view, viewGroup, false);


        return new ViewHolder(viewProduct, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ProductOnline pv = productsList.get(position);
        holder.id.setText(pv.getId());
        holder.name.setText(pv.getName());
        holder.barcode.setText(pv.getBarcode());
        holder.description.setText(pv.getDescription());

        if (pv.getImg() != null && pv.getImg().contains("https://")) {
            Glide.with(context).load(pv.getImg()).into(holder.img);
            } else {
            holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
        };
        }



    public void setProductsList(List<ProductOnline> productsList){
        this.productsList = productsList;
    }

    public ProductOnline getProductByPosition (int position){
        return productsList.get(position);
    }

    public List<ProductOnline> getProductsList(){
        return productsList;
    }

}



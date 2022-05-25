package com.example.lamproject;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

public class ProductOnline {
   private String id;
   private String name;
   private String barcode;
   private String description;
   private String img;


    public ProductOnline(String id, String name, String barcode, String description, String img){
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.description = description;
        this.img = img;
    }

    public String getBarcode (){
        return barcode;
    }
    public String getName(){return name;}
    public String getDescription(){return description;}
    public String getImg(){return img;}

    public String getId() {return id; }
}

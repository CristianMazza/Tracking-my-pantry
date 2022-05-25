package com.example.lamproject;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

@Entity
public class Product {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NotNull
    private String id;
    @ColumnInfo(name = "onlineId")
    @NotNull
    String onlineId;
    @ColumnInfo(name = "name")
    String name;
    @ColumnInfo(name = "description")
    String description;
    @ColumnInfo(name = "barcode")
    String barcode;
    @ColumnInfo
    String img;
    @ColumnInfo(name = "category")
    @NotNull
    private String category;
    @ColumnInfo(name = "quantity")
    @NotNull
    private int quantity;
    @ColumnInfo(name = "price")
    private double price;
    @ColumnInfo(name = "date")
    private String date;




    @Override
    public String toString() {
        return "id: " + this.id +'\n' +
                "onlineId: " + this.onlineId + '\n' +
                "name: " + this.name + '\n' +
                "description: " + this.description + '\n' +
                "barcode: " + this.barcode + '\n' +
                "img: " + this.img + '\n' +
                "price: " + this.price + '\n' +
                "category: " + this.category + '\n' +
                "quantity: " + this.quantity + '\n'+
                "data: " + this.date + '\n';

    }

    public Product(String id, String onlineId, String name, String description, String barcode, String img, String category, int quantity, double price, String date){
        this.id = id;
        this.onlineId = onlineId;
        this.name = name;
        this.description = description;
        this.barcode = barcode;
        this.img = img;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.date = date;
    }

    public String getCategory(){ return category;}

    public int getQuantity(){ return quantity;}
    public double getPrice(){ return price;}

    //public Date getDate() { return date;}
    public String getBarcode() {
        return barcode;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImg() {
        return img;
    }

    public String getOnlineId() {return onlineId; }

    public String getDate() {return date;}
}

package com.example.lamproject;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import java.util.List;


@androidx.room.Dao
public interface ProductDao {
    @Insert
    void insert(Product... product);

    @Query("DELETE from product")
    void deleteData();

    @Query("delete from product where id = :id")
    void deleteProduct(String id);

    @Query("SELECT * FROM product")
    List<Product> getProducts();

    @Query("UPDATE product SET quantity = :quantity WHERE id = :id")
    void addProduct(String id,int quantity);

    @Query("UPDATE product set category = :category, quantity = :quantity, price = :price where id = :id")
    void updateProduct(String id, String category, int quantity, double price);

    @Query("SELECT quantity FROM product WHERE id = :id")
    int getQuantity(String id);

    @Query("SELECT * FROM product where id = :id")
    Product getProduct(String id);

    @Query("select * from product where barcode like :barcode  ")
    List<Product> getProductsByBarcode(String barcode);

    @Query("select * from product where name like :name  ")
    List<Product> getProductsByName(String name);

    @Query("select * from product where barcode like :barcode  and category = :category")
    List<Product> getProductsByBarcodeAndCategory(String barcode, String category);

    @Query("select * from product where name like :name and category = :category")
    List<Product> getProductsByNameAndCategory(String name, String category);

    @Query("select count(*) from product where date like :date")
    int getCountByDate(String date);


}

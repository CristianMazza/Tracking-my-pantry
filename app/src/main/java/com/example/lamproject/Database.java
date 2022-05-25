package com.example.lamproject;

import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {Product.class}, version = 1)
public abstract class Database extends RoomDatabase {
    public abstract ProductDao productDao();
}

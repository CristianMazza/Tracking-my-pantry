package com.example.lamproject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class ViewModel extends androidx.lifecycle.ViewModel {

    private MutableLiveData <List<ProductOnline>> productList;
    private MutableLiveData <List<Product>> productPantryList;
    private boolean isEmpty = true;


    public void setProductList(List<ProductOnline> productList){
        if(productList == null) this.productList = new MutableLiveData<List<ProductOnline>>();
        this.productList.setValue(productList);
    }

    public void setProductPantryList(List<Product> productPantryList){
        if(productPantryList == null) this.productPantryList = new MutableLiveData<List<Product>>();
        this.productPantryList.setValue(productPantryList);
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public LiveData<List<ProductOnline>> getProducts() {
        return productList;
    }
    public LiveData<List<Product>> getProductsPantry(){return productPantryList;}

}

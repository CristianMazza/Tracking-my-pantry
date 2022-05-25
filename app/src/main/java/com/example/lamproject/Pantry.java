package com.example.lamproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Pantry extends Fragment {
    private final int activeColor = Color.parseColor("#FF03DAC5");
    private final int normalColor = Color.parseColor("#C1C1C1");

    Context context;
    OnBackPressedCallback callback;
    Bundle b;
    SharedPreferences preferences;
    RecyclerView rv;
    ViewModel vm;
    List<Product> productsList;
    PantryAdapter adapter;
    LinearLayoutManager layoutManager;
    PantryAdapter.ViewHolder.OnViewClickListener listener;
    SharedPreferences.Editor ed;
    ProductDao productsDB;
    Database db;
    Thread requestThread;
   LinearLayout[] filters;
    LinearLayout carne;
    LinearLayout pesce;
    LinearLayout pasta;
    LinearLayout bevande;
    LinearLayout dolci;
    LinearLayout altro;
    LinearLayout cereali;
    LinearLayout frutta;
    LinearLayout filtroAttivo;
    LinearLayout tutti;
    Executor ex;
    RadioButton nomeRb;
    RadioButton barcodeRb;
    RadioGroup radioGroup;
    SearchView searchInput;
    TextView nProdotti;
    ToggleButton scadutiBtn;
    ToggleButton inScadenzaBtn;
    Date todayDate;
    Date tomorrowDate;
    String todayString;
    String tomorrowString;
    Button logoutBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();
        db = Room.databaseBuilder(this.getActivity().getApplicationContext(), Database.class, "pantryDB").build();
        productsDB = db.productDao();
        rv = view.findViewById(R.id.recyclerViewPantry);
        ex = Executors.newSingleThreadExecutor();

        initFilters(view);
        filters = new LinearLayout[]{tutti, carne, pesce, pasta,dolci, cereali, bevande, altro, frutta};
        nProdotti= view.findViewById(R.id.nProdotti);
        scadutiBtn = view.findViewById(R.id.scadutiBtn);
        inScadenzaBtn = view.findViewById(R.id.inScadenzaBtn);
        nomeRb = view.findViewById(R.id.nomeSelect);
        barcodeRb = view.findViewById(R.id.barcodeSelect);
        searchInput = view.findViewById(R.id.searchText);
        radioGroup = view.findViewById(R.id.radioGroup);
        logoutBtn = view.findViewById(R.id.logoutBtn);

        if(b != null) {
            searchInput.setQuery(b.getString("qrCode"), false);
        }

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        todayString = format.format(calendar.getTime());
        calendar.add(Calendar.DATE,1);
        tomorrowString = format.format(calendar.getTime());

        try {
            todayDate = format.parse(todayString);
            tomorrowDate =  format.parse(tomorrowString);
        } catch (Exception ex){}

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(Pantry.this).navigate(R.id.action_pantry_to_login);
            }
        });



        listener = new PantryAdapter.ViewHolder.OnViewClickListener() {
            @Override
            public void onViewClick(View v, int adapterPosition) {
                if(b == null) b = new Bundle();
                b.putString("id", adapter.getProductByPosition(adapterPosition).getId());
                b.putString("barcode", adapter.getProductByPosition(adapterPosition).getBarcode());
                b.putString("name", adapter.getProductByPosition(adapterPosition).getName());
                b.putString("description", adapter.getProductByPosition(adapterPosition).getDescription());
                b.putString("img", adapter.getProductByPosition(adapterPosition).getImg());
                b.putString("quantity", String.valueOf(adapter.getProductByPosition(adapterPosition).getQuantity()));
                b.putString("category", adapter.getProductByPosition(adapterPosition).getCategory());
                b.putString("price", String.valueOf(adapter.getProductByPosition(adapterPosition).getPrice()));
                b.putString("data", String.valueOf(adapter.getProductByPosition(adapterPosition).getDate()));


                NavHostFragment.findNavController(Pantry.this).navigate(R.id.action_pantry_to_productPantryDescription2, b);
            }
        };

        setAdapter();
        vm = new ViewModelProvider(requireActivity()).get(ViewModel.class);
        vm.setProductPantryList(productsList);
        vm.getProductsPantry().observe(getViewLifecycleOwner(), new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> productViews) {
                if(productsList != null && !productsList.isEmpty()){
                    view.findViewById(R.id.noProductText).setVisibility(View.GONE);
                    nProdotti.setText(String.valueOf(productsList.size()));
                }
                else {
                    view.findViewById(R.id.noProductText).setVisibility(View.VISIBLE);
                    nProdotti.setText(String.valueOf(0));
                }
                adapter.setProductsList(productsList);
                adapter.notifyDataSetChanged();
            }
        });


        scadutiBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    inScadenzaBtn.setEnabled(false);
                    List<Product> prodottiDaRimuovere = new ArrayList<Product>();
                    for(Product product: productsList) {
                        if (product.getDate() != null) {
                            try {
                                Date dateProduct = format.parse(product.getDate());
                                Log.e("prodotto", product.toString());
                                if (dateProduct.after(todayDate)) {
                                    prodottiDaRimuovere.add(product);
                                }
                            } catch (Exception ex) {
                            }
                        } else prodottiDaRimuovere.add(product);
                    }
                    for (Product product: prodottiDaRimuovere){
                        productsList.remove(product);
                    }
                }else {
                    filtroAttivo.performClick();
                    inScadenzaBtn.setEnabled(true);
                }
                new Handler(Looper.getMainLooper()).post(() -> vm.setProductPantryList(productsList));
            }
        });


        inScadenzaBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    scadutiBtn.setEnabled(false);
                    List<Product> prodottiDaRimuovere = new ArrayList<Product>();
                    for(Product product: productsList){
                            if (!(product.getDate().equals(todayString) || (product.getDate().equals(tomorrowString)))){
                                prodottiDaRimuovere.add(product);
                        }
                    }
                    for (Product product: prodottiDaRimuovere){
                        productsList.remove(product);
                    }
                }else {
                    filtroAttivo.performClick();
                    scadutiBtn.setEnabled(true);
                }
                 new Handler(Looper.getMainLooper()).post(() -> vm.setProductPantryList(productsList));
            }
        });


        requestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                productsList = productsDB.getProducts();
                if(productsList != null) view.findViewById(R.id.noProductText).setVisibility(View.GONE);
                adapter.setProductsList(productsList);
                adapter.notifyDataSetChanged();
                nProdotti.setText(String.valueOf(adapter.getItemCount()));
            }
        });
        requestThread.start();


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(nomeRb.isChecked()) searchInput.setQueryHint("Inserisci nome...");
                else searchInput.setQueryHint("Inserisci barcode...");
            }
        });

        searchInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                String finalText = "%" + text + "%";
                if(barcodeRb.isChecked()){
                    if (filtroAttivo != tutti) ex.execute(()-> productsList = productsDB.getProductsByBarcodeAndCategory(finalText, filtroAttivo.getTag().toString()));
                    else ex.execute(()-> productsList = productsDB.getProductsByBarcode(finalText));
                } else {
                    if (filtroAttivo != tutti) ex.execute(()-> productsList = productsDB.getProductsByNameAndCategory(finalText, filtroAttivo.getTag().toString()));
                    else ex.execute(()-> productsList = productsDB.getProductsByName(finalText));
                }
                new Handler(Looper.getMainLooper()).post(() -> vm.setProductPantryList(productsList));
                return false;
            }
        });

        for (LinearLayout filter : filters){
            filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        filtroAttivo.setBackgroundColor(normalColor);
                        filtroAttivo = filter;
                        filtroAttivo.setBackgroundColor(activeColor);
                        scadutiBtn.setChecked(false);
                        inScadenzaBtn.setChecked(false);

                        String textInput = "%%";
                        String filterTag = filtroAttivo.getTag().toString();
                        if(searchInput.getQuery().length() > 0) textInput = "%" + searchInput.getQuery().toString() + "%";

                        String finalTextInput = textInput;
                        ex.execute(() -> {
                            if (filtroAttivo != tutti) {
                                if (barcodeRb.isChecked()) productsList = productsDB.getProductsByBarcodeAndCategory(finalTextInput,filterTag);
                                else productsList = productsDB.getProductsByNameAndCategory(finalTextInput,filterTag);
                            } else {
                                if (barcodeRb.isChecked()) productsList = productsDB.getProductsByBarcode(finalTextInput);
                                else productsList = productsDB.getProductsByName(finalTextInput);
                            }
                            Log.e("productsList", productsList.toString());
                        });
                        new Handler(Looper.getMainLooper()).post(() -> vm.setProductPantryList(productsList));

                }
            });
            }
    }


    public void setAdapter(){
        adapter = new PantryAdapter(productsList, context, listener);
        rv.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
    }

    public void initFilters(View v){
        tutti = v.findViewById(R.id.tuttiOption);
        filtroAttivo = tutti;
        carne = v.findViewById(R.id.meatOption);
        pesce = v.findViewById(R.id.fishOption);
        bevande = v.findViewById(R.id.beverageOption);
        altro = v.findViewById(R.id.otherOption);
        cereali = v.findViewById(R.id.cerealOption);
        pasta = v.findViewById(R.id.pastaOption);
        dolci = v.findViewById(R.id.dessertOption);
        frutta = v.findViewById(R.id.fruitOption);
    }

    @Override
    public void onResume() {
        super.onResume();
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(Pantry.this).popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(callback);
    }
}
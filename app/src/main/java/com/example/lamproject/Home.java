package com.example.lamproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Home extends Fragment {

    OnBackPressedCallback callback;
    Context context;
    Bundle b;
    SharedPreferences preferences;
    RecyclerView rv;
    RequestQueue queueRequests;
    ViewModel vm;
    List<ProductOnline> productsList;
    HomeAdapter adapter;
    SearchView searchView;
    LinearLayoutManager  layoutManager;
    Button cercaBtn;
    Button aggiungiBtn;
    HomeAdapter.ViewHolder.OnViewClickListener listener;
    SharedPreferences.Editor ed;
    ProductDao localProducts;
    Database db;
    Thread requestThread;
    ImageButton toPantryBtn;
    TextView nProdotti;
    ProgressBar progressBar;
    LinearLayout progressBarLL;
    FloatingActionButton cameraBtn;
    Button logoutBtn;

    Executor ex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null)
            b = getArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = this.getContext();
        db = Room.databaseBuilder(context, Database.class, "pantryDB").build();
        ex = Executors.newSingleThreadExecutor();
        localProducts = db.productDao();

        toPantryBtn = view.findViewById(R.id.toPantryBtn);
        searchView = view.findViewById(R.id.searchText);
        cercaBtn = view.findViewById(R.id.cercaBtn);
        aggiungiBtn = view.findViewById(R.id.aggiungiBtn);
        nProdotti = view.findViewById(R.id.nProdotti);
        progressBar = view.findViewById(R.id.progressBar);
        progressBarLL = view.findViewById(R.id.progressBarLL);
        cameraBtn = view.findViewById(R.id.home_camera_button);
        logoutBtn = view.findViewById(R.id.logoutBtn);


        Log.e("CHIAMATO","SI");

        if(b != null) {
            searchView.setQuery(b.getString("qrCode"), false);
        }

        toPantryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(Home.this).navigate(R.id.action_home2_to_pantry);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(Home.this).navigate(R.id.action_home2_to_login);
            }
        });

        cercaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestThread.isAlive())
                    requestThread.interrupt();
                    requestThread.run();
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bun = new Bundle();
                bun.putString("sender", "home");
                NavHostFragment.findNavController(Home.this).navigate(R.id.action_home2_to_barcodeSearch, bun);
            }
        });

        aggiungiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(Home.this).navigate(R.id.action_home2_to_productToServer);
            }
        });



        preferences = context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        queueRequests = Volley.newRequestQueue(context);
        b = getArguments();

        rv = view.findViewById(R.id.recyclerView);

        listener = new HomeAdapter.ViewHolder.OnViewClickListener() {
            @Override
            public void onViewClick(View v, int adapterPosition) {
                if(b == null) b = new Bundle();
                b.putString("id", adapter.getProductByPosition(adapterPosition).getId());
                b.putString("barcode", adapter.getProductByPosition(adapterPosition).getBarcode());
                b.putString("name", adapter.getProductByPosition(adapterPosition).getName());
                b.putString("description", adapter.getProductByPosition(adapterPosition).getDescription());
                b.putString("img", adapter.getProductByPosition(adapterPosition).getImg());
                NavHostFragment.findNavController(Home.this).navigate(R.id.action_home2_to_productToPantry, b);
            }
        };

        setAdapter();



            vm = new ViewModelProvider(requireActivity()).get(ViewModel.class);
            vm.setProductList(productsList);
            vm.getProducts().observe(getViewLifecycleOwner(), new Observer<List<ProductOnline>>() {
                @Override
                public void onChanged(List<ProductOnline> productViews) {
                    adapter.setProductsList(productsList);
                    adapter.notifyDataSetChanged();
                }
            });


        requestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                getOnlineProducts(searchView.getQuery().toString());
            }
        });

        if (vm.isEmpty() || adapter.getItemCount() == 0) cercaBtn.performClick();

        else nProdotti.setText(String.valueOf(adapter.getItemCount()));




    }


    public void setAdapter(){
        adapter = new HomeAdapter(productsList, context, listener);
        rv.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
    }



    public void getOnlineProducts(String barcodeString){
        try{
            JSONObject body = new JSONObject();
            String url = "https://lam21.iot-prism-lab.cs.unibo.it/products";
            if (!barcodeString.equals("")) url += "?barcode=" + barcodeString;

            ableButtons(false);
            progressBarLL.setVisibility(View.VISIBLE);

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray products = response.getJSONArray("products");
                        productsList = new Gson().fromJson(products.toString(),new TypeToken<ArrayList<ProductOnline>>(){}.getType());
                        vm.setProductList(productsList);
                        nProdotti.setText(String.valueOf(adapter.getItemCount()));
                        String tokenResponse = response.getString("token");
                        ed = preferences.edit();
                        ed.putString("productToken", tokenResponse);
                        ed.commit();
                        progressBarLL.setVisibility(View.GONE);
                        ableButtons(true);
                        vm.setEmpty(false);
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBarLL.setVisibility(View.GONE);
                    ableButtons(true);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> authToken = new HashMap<>();

                    authToken.put("Authorization", "Bearer "+ preferences.getString("authToken", null));
                    return authToken;
                }
            };
            queueRequests.add(req);


        }catch (Exception ex){

        }

    }



    @Override
    public void onResume() {
        super.onResume();
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(callback);
    }

    public void ableButtons(boolean bool){
     aggiungiBtn.setEnabled(bool);
     cercaBtn.setEnabled(bool);
     searchView.setEnabled(bool);
     cameraBtn.setEnabled(bool);

    }
}
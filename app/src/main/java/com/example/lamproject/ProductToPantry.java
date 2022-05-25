package com.example.lamproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class ProductToPantry extends Fragment {
    Bundle b;
    OnBackPressedCallback callback;
    TextView nameInput;
    TextView descriptionInput;
    TextView barcodeInput;
    ImageView immagineInput;
    EditText quantitaInput;
    EditText prezzoInput;
    EditText dataInput;
    Spinner categoriaInput;
    private final String[] categorie = {"Carne", "Pesce", "Pasta", "Bevande", "Cereali", "Frutta", "Dolci", "Altro"};
    String categoriaAttuale;
    TextView compila;
    Button aggiungiBtn;
    SharedPreferences preferences;
    Context context;
    String authToken;
    String productToken;
    ImageButton checkAddToPantry;
    Executor ex;
    Database db;
    ProductDao dao;
    ImageButton preferenzaBtn;
    String date = null;



    public ProductToPantry() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            b = getArguments();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_to_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        init(view);
        aggiungiBtn = view.findViewById(R.id.addProduct);
        checkAddToPantry = view.findViewById(R.id.addBtn);
        preferenzaBtn = view.findViewById(R.id.preferenzaBtn);

        ex = Executors.newSingleThreadExecutor();
        db = Room.databaseBuilder(context, Database.class, "pantryDB").build();
        dao = db.productDao();


        checkAddToPantry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAddToPantry.setVisibility(View.GONE);
                aggiungiBtn.setEnabled(true);
                view.findViewById(R.id.verticalLL).setVisibility(View.VISIBLE);
            }
        });

        //rendo nulli i campi perchè il prodotto è già presente nel DB online
        if (b != null) setFields();

        preferences = context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        authToken = preferences.getString("authToken", null);
        productToken = preferences.getString("productToken", null);

        preferenzaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                    JSONObject body = new JSONObject();
                    String url = "https://lam21.iot-prism-lab.cs.unibo.it/votes";

                    body.put("token", productToken);
                    body.put("rating", 1);
                    body.put("productId", b.getString("id"));
                    RequestQueue requestQueue = Volley.newRequestQueue(context);

                    JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(context, "Preferenza prodotto inviata correttamente.", Toast.LENGTH_SHORT).show();

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context, "La preferenza è stata già inviata.", Toast.LENGTH_SHORT).show();
                        }
                    }) {
                        @Override
                        public byte[] getBody() {
                            return body.toString().getBytes(StandardCharsets.UTF_8);
                        }

                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> authToken = new HashMap<>();

                            authToken.put("Authorization", "Bearer "+ preferences.getString("authToken", null));
                            return authToken;
                        }
                    };
                    requestQueue.add(req);


                }catch (Exception ex){

                }

            }
        });


        aggiungiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean okData = true;
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                if(dataInput.getText().length() > 0){
                     try{
                    Date d = format.parse(dataInput.getText().toString());
                    date = format.format(d);
                    } catch(Exception ex) {
                         okData = false;
                         getActivity().runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 Toast.makeText(context, "Data con formato errato. Inserisci una data dd/mm/yyyy", Toast.LENGTH_LONG).show();
                             }
                         });
                     }
                }
                if(okData) {
                    try {

                        String uuid = UUID.randomUUID().toString();
                        Product product = new Product(
                                uuid,
                                b.getString("id"),
                                b.getString("name"),
                                b.getString("description"),
                                b.getString("barcode"),
                                b.getString("img"),
                                categoriaAttuale,
                                Integer.parseInt(quantitaInput.getText().toString()),
                                Double.parseDouble(prezzoInput.getText().toString()),
                                date
                        );

                        ex.execute(() -> dao.insert(product));
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Prodotto inserito correttamente nella dispensa.", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception ex) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Errore nell'inserimento del prodotto.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }






    public void setFields(){
        nameInput.setText(b.getString("name"));
        barcodeInput.setText(b.getString("barcode"));
        descriptionInput.setText(b.getString("description"));

        if (b.getString("img") != null && b.getString("img").contains("https://"))
            Glide.with(context).load(b.getString("img")).into(immagineInput);



    }


    public void init(View view){
        nameInput = view.findViewById(R.id.nomeInput);
        barcodeInput = view.findViewById(R.id.barcodeInput);
        descriptionInput = view.findViewById(R.id.descrizioneInput);
        immagineInput = view.findViewById(R.id.immagineInput);
        categoriaInput = view.findViewById(R.id.categoriaInput);
        ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item ,categorie);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriaInput.setAdapter(categoriaAdapter);

        categoriaInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoriaAttuale = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        quantitaInput = view.findViewById(R.id.quantitaInput);
        prezzoInput = view.findViewById(R.id.prezzoInput);
        dataInput = view.findViewById(R.id.dataInput);
        compila = view.findViewById(R.id.compilaText);
    }

    @Override
    public void onResume() {
        super.onResume();
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(ProductToPantry.this).popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(callback);
    }

}
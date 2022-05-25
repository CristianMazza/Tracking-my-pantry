package com.example.lamproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProductPantryDescription extends Fragment {
    OnBackPressedCallback callback;
    Bundle b;
    Context context;
    String id;
    TextView nameInput;
    TextView descriptionInput;
    TextView barcodeInput;
    EditText immagineInput;
    EditText quantitaInput;
    EditText prezzoInput;
    Spinner categoriaInput;
    ImageView imageProduct;
    ImageButton editBtn;
    ImageButton deleteBtn;
    private final String[] categorie = {"Carne", "Pesce", "Pasta", "Bevanda", "Cereali", "Frutta", "Dolci", "Altro"};
    private String categoriaAttuale;
    Button aggiornaProdotto;
    Executor ex;
    Database db;
    ProductDao dao;



    public ProductPantryDescription() {
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
        return inflater.inflate(R.layout.fragment_product_pantry_description, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        categoriaAttuale = b.getString("category");
        init(view);
        ex = Executors.newSingleThreadExecutor();
        db = Room.databaseBuilder(context, Database.class, "pantryDB").build();
        dao = db.productDao();

        editBtn = view.findViewById(R.id.editBtn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFields(true);
            }
        });

        deleteBtn = view.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new AlertDialog.Builder(context).setTitle("Cancellazione prodotto")
                       .setMessage("Sicuro di voler eliminare il prodotto?")
                       .setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               try {
                                   ex.execute(() -> dao.deleteProduct(id));
                                   mostraMessaggio("Prodotto cancellato correttamente");
                                   NavHostFragment.findNavController(ProductPantryDescription.this).popBackStack();
                               } catch(Exception ex){
                                   mostraMessaggio("Errore nella cancellazione del prodotto.");
                               }
                           }
                       }).setNegativeButton("Annulla", null).show();
            }
        });

        aggiornaProdotto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    ex.execute(() -> {
                        int quantitaProd = Integer.parseInt(quantitaInput.getText().toString());
                        double prezzoProd = Double.parseDouble(prezzoInput.getText().toString());

                        try {
                            dao.updateProduct(id, categoriaAttuale, quantitaProd, prezzoProd);
                            mostraMessaggio("Il prodotto è stato aggiornato correttamente.");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setFields(false);
                                }
                            });
                        } catch (Exception ex) {
                            mostraMessaggio("Errore nell'aggiornamento del prodotto.");
                        }
                    });
            }
        });

    }

    public void init(View view) {
        nameInput = view.findViewById(R.id.nomeInput);
        barcodeInput = view.findViewById(R.id.barcodeInput);
        descriptionInput = view.findViewById(R.id.descrizioneInput);
        immagineInput = view.findViewById(R.id.immagineInput);
        categoriaInput = view.findViewById(R.id.categoriaInput);
        quantitaInput = view.findViewById(R.id.quantitaInput);
        prezzoInput = view.findViewById(R.id.prezzoInput);
        imageProduct = view.findViewById(R.id.imageProduct);
        aggiornaProdotto = view.findViewById(R.id.updateProduct);

        id = b.getString("id");
        nameInput.setText(b.getString("name"));
        barcodeInput.setText(b.getString("barcode"));
        descriptionInput.setText(b.getString("description"));

        ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item ,categorie);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriaInput.setAdapter(categoriaAdapter);

        int position = categoriaAdapter.getPosition(categoriaAttuale);
        categoriaInput.setSelection(position);
        categoriaInput.setEnabled(false);

        categoriaInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoriaAttuale = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



        quantitaInput.setText(b.getString("quantity"));
        prezzoInput.setText(b.getString("price"));

        String imageSrc = b.getString("img");

        if (imageSrc != null && imageSrc.contains("https://")) {
            Glide.with(context).load(imageSrc).into(imageProduct);
        }
    }

    public void mostraMessaggio(String text){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text,Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setFields(boolean bool){
        aggiornaProdotto.setEnabled(bool);
        categoriaInput.setEnabled(bool);
        prezzoInput.setEnabled(bool);
        prezzoInput.setFocusable(bool);
        prezzoInput.setFocusableInTouchMode(bool);
        quantitaInput.setEnabled(bool);
        quantitaInput.setFocusableInTouchMode(bool);
        quantitaInput.setFocusable(bool);

        if(bool) {
            editBtn.setVisibility(View.GONE);
            prezzoInput.setTextColor(Color.BLACK);
            quantitaInput.setTextColor(Color.BLACK);
        } else{
            prezzoInput.setTextColor(Color.LTGRAY);
            quantitaInput.setTextColor(Color.LTGRAY);
            editBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        NavHostFragment.findNavController(ProductPantryDescription.this).popBackStack();
                    }
                });

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(callback);
    }
}
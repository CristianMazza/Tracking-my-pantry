package com.example.lamproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProductToServer extends Fragment {

    EditText nameInput;
    EditText descriptionInput;
    EditText barcodeInput;
    TextView nameTextView;
    TextView barcodeTextView;
    TextView descriptionTextView;
    FrameLayout addFrameLayout;
    FrameLayout checkFrameLayout;
    Button imageBtn;
    ImageView imageView;
    Button aggiungiBtn;
    SharedPreferences preferences;
    Context context;
    String authToken;
    String productToken;
    OnBackPressedCallback callback;
    Bundle b = new Bundle();
    boolean imageInsertedOnline = false; //può valere 0,1,2 -> 0: non è presente alcuna imm;
                                 // 1: immagine presente e caricata correttamente;
                                 // 2: immagine presente ma non caricata correttamente

    String uriOnlineImage;

    FirebaseStorage storage;
    StorageReference storageReference;

    ActivityResultLauncher getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if(result != null){
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("Caricamento dell'immagine...");
                progressDialog.show();
                String uuid = UUID.randomUUID().toString();
                storageReference = storageReference.child("picture/" + uuid);

                try {
                    storageReference.putFile(result).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageInsertedOnline = true;
                            imageView.setImageURI(result);
                            uriOnlineImage = "https://firebasestorage.googleapis.com/v0/b/lamstorageunibo.appspot.com/o/picture%2F" + uuid + "?alt=media";
                            progressDialog.dismiss();
                            Toast.makeText(context, "Immagine inserita correttamente.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(context, "Errore nel caricamento dell'immagine", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        }
                    });
                } catch (Exception ex){};
            }
        }
    });



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!= null){
            b = getArguments();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_to_server, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        init(view);

        aggiungiBtn = view.findViewById(R.id.updateProduct);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContent.launch("image/*");
            }
        });

        preferences = context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        authToken = preferences.getString("authToken", null);
        productToken = preferences.getString("productToken", null);

        imageView = view.findViewById(R.id.imageViewServer);

        aggiungiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //attendo che finisca di caricare l'immagine, se è da caricare
               postProduct(imageInsertedOnline);
            }
        });
    }

    public void init(View view){
        nameInput = view.findViewById(R.id.nomeInput);
        barcodeInput = view.findViewById(R.id.barcodeInput);
        descriptionInput = view.findViewById(R.id.descrizioneInput);
        nameTextView = view.findViewById(R.id.nomeTextView);
        barcodeTextView = view.findViewById(R.id.barcodeTextView);
        descriptionTextView = view.findViewById(R.id.descrizioneTextView);
        addFrameLayout = view.findViewById(R.id.addFrameLayout);
        checkFrameLayout = view.findViewById(R.id.checkFrameLayout);

        imageBtn = view.findViewById(R.id.imageBtn);
    }

    public void postProduct(boolean imageInsertedOnline) {
            JSONObject body = new JSONObject();
            String url = "https://lam21.iot-prism-lab.cs.unibo.it/products";
            try {
                body.put("token", productToken);
                body.put("name", nameInput.getText().toString());
                body.put("description", descriptionInput.getText().toString());
                body.put("barcode", barcodeInput.getText().toString());
                body.put("test", false);
                if(imageInsertedOnline) body.put("img", uriOnlineImage);


            } catch (Exception ex) {
            }

            String bodyToString = body.toString();
            RequestQueue queueRequests = Volley.newRequestQueue(context);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("andato", response.toString());
                    barcodeTextView.setText(barcodeInput.getText());
                    nameTextView.setText(nameInput.getText());
                    descriptionTextView.setText(descriptionInput.getText());
                    addFrameLayout.setVisibility(View.GONE);
                    checkFrameLayout.setVisibility(View.VISIBLE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("ErroreProdotto", error.toString());
                }
            }) {
                @Override
                public byte[] getBody() {
                    return bodyToString.getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> authTok = new HashMap<>();
                    authTok.put("Authorization", "Bearer " + authToken);
                    return authTok;
                }
            };
            queueRequests.add(req);

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
                        NavHostFragment.findNavController(ProductToServer.this).popBackStack();
                    }
                });

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(callback);
    }


    }








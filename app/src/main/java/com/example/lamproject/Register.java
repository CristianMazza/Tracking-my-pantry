package com.example.lamproject;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.nio.charset.StandardCharsets;


public class Register extends Fragment {
    OnBackPressedCallback callback;
    Context context;



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = this.getContext();

        view.findViewById(R.id.LoginButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText email = view.findViewById(R.id.emailLogin);
                EditText psw = view.findViewById(R.id.passwordLogin);
                EditText username = view.findViewById(R.id.usernameRegister);

                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    JSONObject bodyRequest = new JSONObject();
                    bodyRequest.put("username", username.getText().toString());
                    bodyRequest.put("email", email.getText().toString());
                    bodyRequest.put("password", psw.getText().toString());
                    String bodyRequestString = bodyRequest.toString();

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://lam21.iot-prism-lab.cs.unibo.it/users", null,
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i("Risposta", response.toString());
                                    Toast.makeText(context, "Utente registrato correttamente", Toast.LENGTH_LONG).show();
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("Errore", error.toString());
                            Toast.makeText(context, "Errore in fase di registrazione", Toast.LENGTH_LONG).show();
                        }
                    }) {
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public byte[] getBody() {
                            return bodyRequestString.getBytes(StandardCharsets.UTF_8);
                        }
                    };

                    requestQueue.add(request);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    }

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
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(Register.this).popBackStack();

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(callback);
    }
}
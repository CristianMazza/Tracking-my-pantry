package com.example.lamproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;


public class Login extends Fragment {

    Context context;
    SharedPreferences pr;
    SharedPreferences.Editor ed;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("ciao","si");
        pr = this.getActivity().getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        ed = pr.edit();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = this.getContext();




        view.findViewById(R.id.registratiButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(Login.this).navigate(R.id.action_login_to_register);
            }


        });

        view.findViewById(R.id.LoginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                EditText email = view.findViewById(R.id.emailLogin);
                EditText password = view.findViewById(R.id.passwordLogin);

                try {
                    RequestQueue queue = Volley.newRequestQueue(context);
                    JSONObject bodyRequest = new JSONObject();
                    bodyRequest.put("email", email.getText().toString());
                    bodyRequest.put("password", password.getText().toString());
                    String url = "https://lam21.iot-prism-lab.cs.unibo.it/auth/login";

                    String bodyReqToString = bodyRequest.toString();
                    JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.POST, url, null, new Response.Listener<JSONObject>() {


                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(context, "Login avvenuto con successo", Toast.LENGTH_LONG).show();
                            try {

                                String authToken = response.get("accessToken").toString();
                                ed.putString("authToken", authToken);
                                ed.commit();

                                NavHostFragment.findNavController(Login.this).navigate(R.id.action_login_to_home2);

                            } catch(Exception ex){
                                ex.printStackTrace();
                            }
                        }

                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context, "Credenziali errate", Toast.LENGTH_LONG).show();
                            Log.i("ErrorLOG", error.toString());
                        }
                    }) {
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }
                        @Override
                        public byte[] getBody() {
                            return bodyReqToString.getBytes(StandardCharsets.UTF_8);
                        }
                    };

                    queue.add(request);

                } catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

}
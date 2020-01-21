package com.example.museumguide;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

// AGGIUNTA MUSEO

public class addActivity extends AppCompatActivity {

    private static final String KEYUSER = "user";
    private static final String KEYMUSEUM = "museum";
    private static final String KEYCITY = "city";
    private static final String KEYSTREET = "street";
    private static final String KEYNUMBER = "number";

    private EditText museum;
    private EditText city;
    private EditText street;
    private EditText num;
    private Button addMuseum;

    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        museum = findViewById(R.id.museum);
        city = findViewById(R.id.city);
        street = findViewById(R.id.street);
        num = findViewById(R.id.number);
        addMuseum = findViewById(R.id.addMuseum);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Aggiungere il museo al db
        addMuseum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user = mAuth.getCurrentUser().getEmail();

                String name = museum.getText().toString();
                String citta = city.getText().toString();
                String via = street.getText().toString();
                String civico = num.getText().toString();

                //Controlla che tutti i campi siano non vuoti
                if(!name.equals("") && !citta.equals("") && !via.equals("") && !civico.equals("")) {
                    writeData(name, user, citta, via, civico);
                    finish();
                }
                else{
                    makeToast(getString(R.string.allDataError));
                }
            }
        });

    }

    //Funzione per scivere i dati su Firebase
    public void writeData(String name, String user, String citta, String via, String civico) {
        Map<String, Object> newMuseum = new HashMap<>();
        newMuseum.put(KEYMUSEUM, name);
        newMuseum.put(KEYCITY, citta);
        newMuseum.put(KEYSTREET, via);
        newMuseum.put(KEYNUMBER, civico);
        newMuseum.put(KEYUSER, user);

        //Usato per poter identificare musei omonimi di città differenti
        String museumName = name + "#" + citta;

        db.collection(KEYMUSEUM)
                .document(museumName)
                .set(newMuseum)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeToast(getString(R.string.musuemAdded));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast(getString(R.string.museumError));
                    }
                });

    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex) {
        Toast.makeText(getApplicationContext(), mex, Toast.LENGTH_SHORT).show();
    }
}

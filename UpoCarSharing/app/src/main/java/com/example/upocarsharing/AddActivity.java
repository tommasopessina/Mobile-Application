package com.example.upocarsharing;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity {

    private static final String KEYDATA = "data";
    private static final String KEYTIME = "time";
    private static final String KEYLOCATION = "location";
    private static final String KEYDESTINATION = "destination";
    private static final String KEYSEATS = "seat";
    private static final String KEYTRIP = "trip";
    private static final String KEYUSER = "user";
    private EditText dataEditText;
    private EditText timeEditText;
    private EditText locationEditText;
    private EditText destinationEditText;
    private EditText seatEditText;
    private Button addTrip;

    DocumentReference mDocRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        dataEditText = findViewById(R.id.data);
        timeEditText = findViewById(R.id.time);
        locationEditText = findViewById(R.id.location);
        destinationEditText = findViewById(R.id.destination);
        seatEditText = findViewById(R.id.seats);
        addTrip = findViewById(R.id.addTrip);

        mAuth = FirebaseAuth.getInstance();

        addTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String data = dataEditText.getText().toString();
                String time = timeEditText.getText().toString();
                String location = locationEditText.getText().toString();
                String destination = destinationEditText.getText().toString();
                String seats = seatEditText.getText().toString();

                if(!data.equals("") && !time.equals("") && !location.equals("") && !destination.equals("") && !seats.equals("")){

                    String dataTime = data + " " + time;
                    mDocRef = FirebaseFirestore.getInstance().collection(KEYTRIP).document(dataTime + " " + mAuth.getCurrentUser().getEmail());
                    addNewTrip(data,time,location,destination,seats);

                } else{
                    dataEditText.setText("");
                    timeEditText.setText("");
                    locationEditText.setText("");
                    destinationEditText.setText("");
                    seatEditText.setText("");
                    makeToast(getString(R.string.errorInsertAllData));
                }
            }
        });

    }

    //Funzione per l'aggiunta di un nuovo viaggio
    private void addNewTrip(String data, String time, String location, String destination, String seat) {
        Map<String, Object> newTrip = new HashMap<>();
        newTrip.put(KEYDATA, data);
        newTrip.put(KEYTIME, time);
        newTrip.put(KEYLOCATION, location);
        newTrip.put(KEYDESTINATION, destination);
        newTrip.put(KEYSEATS, seat);
        newTrip.put(KEYUSER,mAuth.getCurrentUser().getEmail());

        mDocRef.set(newTrip)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeToast(getString(R.string.tripAdded));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast(getString(R.string.errorAdd));
                        Log.d("TAG", e.toString());
                    }
                });
        finish();
    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex){
        Toast.makeText(getApplicationContext(),mex.toString(),Toast.LENGTH_SHORT).show();
    }


}

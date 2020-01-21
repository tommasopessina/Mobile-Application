package com.example.upocarsharing;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class driverModifyActivity extends AppCompatActivity {

    private static final String TAG = "test";
    private static final String KEYDATA = "data";
    private static final String KEYTIME = "time";
    private static final String KEYLOCATION = "location";
    private static final String KEYDESTINATION = "destination";
    private static final String KEYSEATS = "seat";
    private static final String KEYTRIP = "trip";
    private static final String KEYUSER = "user";
    private static final String KEYPASSENGER = "passengerList";
    private static final String KEYCOUNT= "count";

    private EditText dataEditText;
    private EditText timeEditText;
    private EditText locationEditText;
    private EditText destinationEditText;
    private EditText seatEditText;
    private Button addTrip;

    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    DocumentReference mDocRef;

    private String docName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_modify);

        dataEditText = findViewById(R.id.data);
        timeEditText = findViewById(R.id.time);
        locationEditText = findViewById(R.id.location);
        destinationEditText = findViewById(R.id.destination);
        seatEditText = findViewById(R.id.seats);
        addTrip = findViewById(R.id.addTrip);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        final String seat = getIntent().getStringExtra(KEYSEATS);
        String loc = getIntent().getStringExtra(KEYLOCATION);
        final String userTrip = getIntent().getStringExtra(KEYUSER);
        final String data = getIntent().getStringExtra(KEYDATA);
        final String time = getIntent().getStringExtra(KEYTIME);

        docName = data + " " + time + " " + userTrip;
        final String bookUser = mAuth.getCurrentUser().getEmail();

        String[] parts = loc.split(" ");
        String start = parts[1];
        String end = parts[3];

        dataEditText.setText(data);
        timeEditText.setText(time);
        locationEditText.setText(start);
        destinationEditText.setText(end);
        seatEditText.setText(seat);

        addTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String data = dataEditText.getText().toString();
                String time = timeEditText.getText().toString();
                String location = locationEditText.getText().toString();
                String destination = destinationEditText.getText().toString();
                final String seats = seatEditText.getText().toString();

                //Query per verificare che non ci siano inconsistenze sui posti disponibili
                db.collection(KEYTRIP)
                        .document(docName)
                        .collection(KEYPASSENGER)
                        .document(KEYCOUNT)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(Integer.valueOf(seats) < Integer.valueOf(documentSnapshot.getString(KEYCOUNT))){
                                    makeToast(getString(R.string.seatError));
                                    finish();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                makeToast(getString(R.string.seatError));
                                finish();
                            }
                        });

                if(!data.equals("") && !time.equals("") && !location.equals("") && !destination.equals("") && !seats.equals("")){
                    String dataTime = data + " " + time;
                    mDocRef = FirebaseFirestore.getInstance().collection(KEYTRIP).document(dataTime + " " + mAuth.getCurrentUser().getEmail());
                    updateNewTrip(data,time,location,destination,seats);

                } else{
                    dataEditText.setText("");
                    timeEditText.setText("");
                    locationEditText.setText("");
                    destinationEditText.setText("");
                    seatEditText.setText("");
                    makeToast(getString(R.string.errorInsertAllData));
                }
                finish();
            }
        });
    }

    //Funzione per aggiornare i dati del viaggio
    private void updateNewTrip(String data, String time, String location, String destination, String seat) {
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
        Toast.makeText(getApplicationContext(),mex,Toast.LENGTH_SHORT).show();
    }

}

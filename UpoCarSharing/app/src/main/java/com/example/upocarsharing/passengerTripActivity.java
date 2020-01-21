package com.example.upocarsharing;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class passengerTripActivity extends AppCompatActivity {

    private static final String KEYSEATS = "seat";
    private static final String KEYLOCATION = "location";
    private static final String KEYUSER = "user";
    private static final String KEYDATA = "data";
    private static final String KEYTIME = "time";
    private static final String KEYTRIP = "trip";
    private static final String KEYPASSENGER = "passengerList";
    private static final String KEYCOUNT= "count";
    private static final String KEYBOOK = "isBooked";

    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView destination;
    private TextView datatime;
    private TextView seats;
    private TextView seatsText;
    private TextView userText;
    private TextView user;
    private Button list;
    private Button book;
    private Button delete;

    private ArrayList<String> passengerList;
    private ArrayList<String> bookingList;
    private int count;
    private String docName;
    private  String userList;

    AlertDialog.Builder builder;
    AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_trip);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        passengerList = new ArrayList<>();
        bookingList = new ArrayList<>();

        destination = findViewById(R.id.dest);
        datatime = findViewById(R.id.datatime);
        seats = findViewById(R.id.freeSeat);
        seatsText = findViewById(R.id.seatText);
        userText = findViewById(R.id.userText);
        user = findViewById(R.id.user);
        list = findViewById(R.id.passengerList);
        book = findViewById(R.id.bookTrip);
        delete = findViewById(R.id.deleteTrip);

        final String seat = getIntent().getStringExtra(KEYSEATS);
        String loc = getIntent().getStringExtra(KEYLOCATION);
        final String userTrip = getIntent().getStringExtra(KEYUSER);
        final String data = getIntent().getStringExtra(KEYDATA);
        final String time = getIntent().getStringExtra(KEYTIME);

        destination.setText(loc);
        datatime.setText(data);
        seatsText.setText(getString(R.string.freeSeat) + " ");
        seats.setText(seat);
        userText.setText(getString(R.string.user) + " ");
        user.setText(userTrip);

        docName = data + " " + time + " " + userTrip;
        final String bookUser = mAuth.getCurrentUser().getEmail();

        builder = new AlertDialog.Builder(this);

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int seatTmp = Integer.valueOf(seat);

                if(seatTmp>0 && !userTrip.equals(bookUser)) {

                    if(!passengerList.contains(bookUser) && !bookingList.contains(bookUser)){

                        updateSeat(seatTmp,true); //aggiorna i posti disponibili

                        String userBookCount = KEYUSER + String.valueOf(count);
                        HashMap<String, Object> bookuser = new HashMap<>();
                        bookuser.put(KEYUSER,bookUser);
                        bookuser.put(KEYBOOK, true); //Utile per mantenere tutte le prenotazioni

                        //Aggiunge l'utente che ha prenotato il viaggio
                        db.collection(KEYTRIP)
                                .document(docName)
                                .collection(KEYPASSENGER)
                                .document(userBookCount)
                                .set(bookuser)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        makeToast(getString(R.string.bookOK));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        makeToast(getString(R.string.bookNO));
                                    }
                                });

                        //Aggiorna il contatore di tutte le prenotazioni (attive e cancellate)
                        HashMap<String, Object> countuser = new HashMap<>();
                        count = count+1;
                        countuser.put(KEYCOUNT,String.valueOf(count));
                        db.collection(KEYTRIP)
                                .document(docName)
                                .collection(KEYPASSENGER)
                                .document(KEYCOUNT)
                                .set(countuser)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });

                    }else{
                        makeToast(getString(R.string.bookAgain));
                    }
                }
                else{
                    if(seatTmp <= 0)
                        makeToast(getString(R.string.noSeatAvaible));
                    else
                        makeToast(getString(R.string.noBooking));
                }
                finish();

            }
        });

        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Mostra un Alert Dialog con la lista dei passeggeri prenotati
                userList = "";
                for(int i=0; i<passengerList.size();i++){
                    userList = userList + passengerList.get(i) + " ";
                }

                builder = new AlertDialog.Builder(passengerTripActivity.this);

                builder.setTitle(KEYPASSENGER);
                builder.setMessage(userList);
                builder.setCancelable(true);

                builder.setPositiveButton("OK",null);
                alert = builder.create();

                alert.show();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Collections.reverse(passengerList); //utile poichè l'array risulta in ordine inverso

                if(passengerList.contains(bookUser)){

                    builder = new AlertDialog.Builder(passengerTripActivity.this);

                    builder.setTitle(getString(R.string.Sure));
                    builder.setMessage(getString(R.string.SureDesc));
                    builder.setCancelable(false);

                    builder.setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String userBookCount = KEYUSER + String.valueOf(passengerList.indexOf(bookUser));
                            HashMap<String, Object> bookuser = new HashMap<>();
                            bookuser.put(KEYBOOK, false); //Indica che la prenotazione è cancellata (ma comunque mantenuta)

                            db.collection(KEYTRIP)
                                    .document(docName)
                                    .collection(KEYPASSENGER)
                                    .document(userBookCount)
                                    .set(bookuser, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            makeToast(getString(R.string.updated));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            makeToast(getString(R.string.noUpdated));
                                        }
                                    });

                            int seatTmp = Integer.valueOf(seat);
                            updateSeat(seatTmp,false); //aggiorna i posti disponibili
                            passengerList.remove(bookUser);

                            finish();
                        }
                    });

                    builder.setNegativeButton(getString(R.string.No), null);

                    alert = builder.create();

                    alert.show();

                }
                else{
                    makeToast(getString(R.string.notBookError));
                }
            }
        });
    }

    //Funzione per prendere la lista dei passeggeri prenotati
    private void getPassengerList(final String docName){
        if(passengerList.size()>0)
            passengerList.clear();
        db.collection(KEYTRIP)
                .document(docName)
                .collection(KEYPASSENGER)
                .document(KEYCOUNT)
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot document) {
                    if(document.getString(KEYCOUNT)!=null) {
                        count = Integer.valueOf(document.getString(KEYCOUNT));
                        for(int i=0; i<count; i++){
                            String userBookCount = KEYUSER + String.valueOf(i);
                            final int finalI = i;
                            db.collection(KEYTRIP)
                                .document(docName)
                                .collection(KEYPASSENGER)
                                .document(userBookCount)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot document) {
                                        if(document.getBoolean(KEYBOOK)) {
                                            passengerList.add(document.getString(KEYUSER));
                                        }
                                        bookingList.add(document.getString(KEYUSER));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        makeToast(getString(R.string.errorPassengerList));
                                    }
                            });
                        }
                    }
                    else
                        count = 0;
                }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getPassengerList(docName);
    }

    //Funzione per aggiornare il contatore dei posti disponibili
    private void updateSeat(int seatTmp, boolean isBook){

        if(isBook){
            //Eseguito se si tratta di una prenotazione, quindi occore decrementare il valore
            HashMap<String, Object> result = new HashMap<>();
            String seatUpdate = String.valueOf(seatTmp - 1);

            result.put(KEYSEATS, seatUpdate);

            db.collection(KEYTRIP)
                .document(docName)
                .set(result, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeToast(getString(R.string.updated));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast(getString(R.string.noUpdated));
                    }
                });
        }
        else{
            //In questo caso si vuole eliminare una prenotazione, quindi occorre incrementare il contatore.
            HashMap<String, Object> result = new HashMap<>();
            String seatUpdate = String.valueOf(seatTmp + 1);

            result.put(KEYSEATS, seatUpdate);

            db.collection(KEYTRIP)
                .document(docName)
                .set(result, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeToast(getString(R.string.updated));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast(getString(R.string.noUpdated));
                    }
                });
        }
    }

    private void makeToast(String mex){
        Toast.makeText(getApplicationContext(),mex,Toast.LENGTH_SHORT).show();
    }

}

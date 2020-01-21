package com.example.upocarsharing;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class deleteActivity extends AppCompatActivity {

    private static final String TAG = "test";
    private static final String KEYDATA = "data";
    private static final String KEYTIME = "time";
    private static final String KEYLOCATION = "location";
    private static final String KEYDESTINATION = "destination";
    private static final String KEYSEATS = "seat";
    private static final String KEYTRIP = "trip";
    private static final String KEYUSER = "user";

    private ArrayList<Dati> dataSet;
    private MyArrayAdapter adapter;
    private Dati d;
    private ListView tripList;

    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tripList = findViewById(R.id.tripList);
        dataSet = new ArrayList<>();
        adapter = new MyArrayAdapter(this, R.layout.list_row, dataSet);
        tripList.setAdapter(adapter);

        tripList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String user = dataSet.get(position).getUser();
                String data = dataSet.get(position).getDatatime();

                String[] parts = data.split(" ");
                String DATA = parts[0];
                String TIME = parts[2];
                String docName = DATA + " " + TIME + " " + user;
                makeToast(docName);
                db.collection(KEYTRIP)
                    .document(docName)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            makeToast(getString(R.string.removed));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            makeToast(getString(R.string.notRemoved));
                            finish();
                        }
                    });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String user = mAuth.getCurrentUser().getEmail();
        fetchData(user);
    }

    //Funzione per leggere da Firebase
    public void fetchData(final String user){
        dataSet.clear(); //Pulire l'array prima di inserire i nuovi dati letti
        final String finalUser = user;
        db.collection(KEYTRIP)
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    Dati d;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        String seat = "";
                        String locdest = "";
                        String datatime = "";
                        if(document.getString(KEYUSER).equals(finalUser)) {

                            Log.d(TAG, document.getId() + " => " + document.getData());

                            String data = document.getString(KEYDATA);
                            String time = document.getString(KEYTIME);
                            String destination = document.getString(KEYDESTINATION);
                            String location = document.getString(KEYLOCATION);
                            seat = document.getString(KEYSEATS);
                            String user = document.getString(KEYUSER);

                            locdest = getString(R.string.from) + " " + location + " " + getString(R.string.to) + " " + destination;
                            datatime = data + " " + getString(R.string.at) + " " + time;

                            d = new Dati(seat, locdest, datatime, user);
                            dataSet.add(d);
                        }

                    }
                    adapter.notifyDataSetChanged();
                }
            });
    }

    private void makeToast(String mex){
        Toast.makeText(getApplicationContext(),mex,Toast.LENGTH_SHORT).show();
    }
}

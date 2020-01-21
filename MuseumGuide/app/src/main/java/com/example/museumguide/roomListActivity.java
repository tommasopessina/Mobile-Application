package com.example.museumguide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

// LISTA STANZE MUSEO

public class roomListActivity extends AppCompatActivity {

    private static final String TAG = "TEST";

    private static final String KEYUSER = "user";
    private static final String KEYMUSEUM = "museum";
    private static final String KEYCITY = "city";
    private static final String KEYROOM = "room";

    private Button add;
    private ListView list;
    private TextView position;

    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ArrayList<Dati> dataSet;
    private MyArrayAdapter adapter;
    private Dati d;

    HashMap<String,String> data;

    private Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        add = findViewById(R.id.addRoom);
        list = findViewById(R.id.room_list);
        position = findViewById(R.id.roomPosition);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

       data = new HashMap<>();
        dataSet = new ArrayList<>();
        adapter = new MyArrayAdapter(this, R.layout.list_row, dataSet);
        list.setAdapter(adapter);

        final String museum = getIntent().getStringExtra(KEYMUSEUM);
        final String city = getIntent().getStringExtra(KEYCITY);

        //Solo scopo di mostrare all'utente dove si trova nell'app
        position.setText(museum + " > " + getString(R.string.roomList));

        //Lanciata l'activity per aggiungere una stanza
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(roomListActivity.this, roomAddActivity.class);
                i.putExtra(KEYMUSEUM, museum);
                i.putExtra(KEYCITY, city);
                startActivity(i);
            }
        });

        //Lancia l'activity per vedere gli oggetti della stanza selezionata
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               Intent y = new Intent(roomListActivity.this, objectListActivity.class);
               y.putExtra(KEYROOM, dataSet.get(position).getTitle());
               y.putExtra(KEYMUSEUM, museum);
               y.putExtra(KEYCITY, city);
               y.putExtra(KEYUSER, dataSet.get(position).getCity());
               startActivity(y);
            }
        });

    }

    //Funzione per leggere da Firebase
    public void fetchData(String museumName) {
        dataSet.clear(); //Pulire l'array prima di inserire i nuovi dati letti
        db.collection(KEYMUSEUM)
                .document(museumName)
                .collection(KEYROOM)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Dati d;
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d(TAG, document.getId() + " => " + document.getData());

                            String room = document.getString(KEYROOM);
                            String user = document.getString(KEYUSER);

                            d = new Dati(room, user,"","",false);
                            dataSet.add(d);

                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex) {
        Toast.makeText(getApplicationContext(), mex, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String museum = getIntent().getStringExtra(KEYMUSEUM);
        String city = getIntent().getStringExtra(KEYCITY);

        //Usato per poter identificare musei omonimi di città differenti
        String museumName = museum + "#" + city;

        fetchData(museumName);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                makeToast(getString(R.string.exit));
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();

                Intent i = new Intent(roomListActivity.this, MainActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

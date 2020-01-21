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

// LISTA MUSEI

public class listActivity extends AppCompatActivity {

    private static final String TAG = "TEST";

    private static final String KEYMUSEUM = "museum";
    private static final String KEYCITY = "city";
    private static final String KEYSTREET = "street";
    private static final String KEYNUMBER = "number";

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
        setContentView(R.layout.activity_list);

        add = findViewById(R.id.addBook);
        list = findViewById(R.id.book_list);
        position = findViewById(R.id.listPosition);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        data = new HashMap<>();
        dataSet = new ArrayList<>();
        adapter = new MyArrayAdapter(this, R.layout.list_row, dataSet);
        list.setAdapter(adapter);

        //Solo scopo di informare l'utente di dove si trova nell'applicazione
        position.setText(getString(R.string.museumList));

        //Aggiungere un museo
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(listActivity.this, addActivity.class);
                startActivity(i);
            }
        });

        //Lanciata l'activity che mostrerà la lista delle stanze
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(listActivity.this, roomListActivity.class);
                i.putExtra(KEYMUSEUM, dataSet.get(position).getTitle());
                i.putExtra(KEYCITY, dataSet.get(position).getCity());
                startActivity(i);
            }
        });

    }

    //Funzione per leggere da Firebase
    public void fetchData() {
        dataSet.clear(); //Pulire l'array prima di inserire i nuovi dati letti
        db.collection(KEYMUSEUM)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Dati d;
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d(TAG, document.getId() + " => " + document.getData());

                            String title = document.getString(KEYMUSEUM);
                            String city = document.getString(KEYCITY);
                            String street = document.getString(KEYSTREET);
                            String num = document.getString(KEYNUMBER);

                            d = new Dati(title,city,street,num,false);
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

        fetchData();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                makeToast(getString(R.string.exit));
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();

                Intent i = new Intent(listActivity.this, MainActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

package com.example.museumguide;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

//LISTA OGGETTI
public class objectListActivity extends AppCompatActivity {

    private static final String TAG = "TEST";

    private Button add;
    private ListView list;
    private TextView position;

    private static final String KEYROOM = "room";
    private static final String KEYMUSEUM = "museum";
    private static final String KEYCITY = "city";
    private static final String KEYUSER = "user";
    private static final String KEYTITLE= "title";
    private static final String KEYDESC = "description";
    private static final String KEYPHOTO = "photoBase64";
    private static final String KEYOBJ= "object";

    private ArrayList<Dati> dataSet;
    private MyArrayAdapter adapter;
    private Dati d;

    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menudelete,menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_list);

        add = findViewById(R.id.newObject);
        list = findViewById(R.id.object_list);
        position = findViewById(R.id.objPosition);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        final String room = getIntent().getStringExtra(KEYROOM);
        final String museum = getIntent().getStringExtra(KEYMUSEUM);
        final String city = getIntent().getStringExtra(KEYCITY);

        //Solo scopo di informare l'utente sulla posizione nell'app
        position.setText(museum + " > " + room + " > " + getString(R.string.objList));

        dataSet = new ArrayList<>();
        adapter = new MyArrayAdapter(this, R.layout.list_row, dataSet);
        list.setAdapter(adapter);

        //Lanciata l'activity per aggiungere un oggetto
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(objectListActivity.this, addObjectActivity.class);
                i.putExtra(KEYROOM,room);
                i.putExtra(KEYMUSEUM, museum);
                i.putExtra(KEYCITY, city);
                startActivity(i);
            }
        });

        //Lanciata l'activity per mostrare i dettagli dell'oggetto selezionato
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent y = new Intent(objectListActivity.this, showActivity.class);
                y.putExtra(KEYTITLE, dataSet.get(position).getTitle());
                y.putExtra(KEYDESC, dataSet.get(position).getStreet());
                y.putExtra(KEYUSER,dataSet.get(position).getCity());
                y.putExtra(KEYPHOTO,dataSet.get(position).getNum());
                y.putExtra(KEYROOM,room);
                y.putExtra(KEYMUSEUM,museum);
                y.putExtra(KEYCITY, city);
                startActivity(y);
            }
        });
    }

    //Funzione per leggere da Firebase
    public void fetchData(String museumName, String roomName) {
        dataSet.clear(); //Pulire l'array prima di inserire i nuovi dati letti
        db.collection(KEYMUSEUM)
                .document(museumName)
                .collection(KEYROOM)
                .document(roomName)
                .collection(KEYOBJ)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Dati d;
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d(TAG, document.getId() + " => " + document.getData());

                            String title = document.getString(KEYTITLE);
                            String user = document.getString(KEYUSER);
                            String desc = document.getString(KEYDESC);
                            String photo = document.getString(KEYPHOTO);

                            d = new Dati(title,user,desc,photo,true);
                            dataSet.add(d);

                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String museum = getIntent().getStringExtra(KEYMUSEUM);
        String city = getIntent().getStringExtra(KEYCITY);
        String room = getIntent().getStringExtra(KEYROOM);

        //Usato per poter identificare musei omonimi di città differenti
        String museumName = museum + "#" + city;

        fetchData(museumName,room);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String utente = getIntent().getStringExtra(KEYUSER);
        switch (item.getItemId()) {
            case R.id.delete:
                //Si impone che solo chi ha inserito un dato, possa cancellarlo
                if(mAuth.getCurrentUser().getEmail().equals(utente)){
                    String museumName = getIntent().getStringExtra(KEYMUSEUM) + "#" + getIntent().getStringExtra(KEYCITY);
                    deleteRoom(museumName, getIntent().getStringExtra(KEYROOM));
                }
                else
                    makeToast(getString(R.string.notPermitted));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Funzione per eliminare una stanza (anche se non vuota)
    private void deleteRoom(String museumName, String roomName){
        db.collection(KEYMUSEUM)
                .document(museumName)
                .collection(KEYROOM)
                .document(roomName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeToast(getString(R.string.deleted));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeToast(getString(R.string.notDeleted));
            }
        });
    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex) {
        Toast.makeText(getApplicationContext(), mex, Toast.LENGTH_SHORT).show();
    }

}

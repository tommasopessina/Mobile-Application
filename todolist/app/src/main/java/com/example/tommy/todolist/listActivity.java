package com.example.tommy.todolist;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class listActivity extends AppCompatActivity {

    public static final String KEY_EXTRA = "user";  //Chiave per il bundle
    private Button add;
    public static final String KEYTEXT = "text";
    public static final String KEYTITLE = "title";
    public static final String KEYAUTHOR = "author";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LONG = "long";
    public static final String KEYISCLOUD = "isCloud";
    public static final String TAG = "FireStoreLog";
    FirebaseFirestore db;
    private String value;
    private String user;
    private FirebaseAuth mAuth;
    private DbHandler dbHandler;
    private  MyArrayAdapter adapter;

    ArrayList<Dati> dataSet;
    ArrayList<Dati> noteList;
    private ListView listView;

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

        listView = findViewById(R.id.note_list);

        add = findViewById(R.id.addItem);

        if (getIntent().hasExtra(KEY_EXTRA)) { //se c'è qualcosa salvato nel bundle
            value = getIntent().getStringExtra(KEY_EXTRA);
        } else { //se non riesce a trovare qualcosa
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();
            value = mAuth.getCurrentUser().getEmail();
        }

        dataSet = new ArrayList<>();
        adapter = new MyArrayAdapter(this, R.layout.list_row, dataSet);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //lancia showActivity e mostra dettagli nota
                Intent i = new Intent(listActivity.this, showActivity.class);

                if(value.equals("anonimo")){
                    i.putExtra(KEYISCLOUD,false);
                   // i.putExtra(KEYTITLE, noteList.get(position).getTitolo());
                   // i.putExtra(KEYTEXT,noteList.get(position).getSottotitolo());
                    i.putExtra(KEYTITLE, dataSet.get(position).getTitolo());
                    i.putExtra(KEYTEXT,dataSet.get(position).getSottotitolo());

                }else{
                    i.putExtra(KEYISCLOUD,true);
                    i.putExtra(KEYTITLE,dataSet.get(position).getTitolo());
                    i.putExtra(KEYTEXT,dataSet.get(position).getSottotitolo());
                    i.putExtra(KEYAUTHOR,dataSet.get(position).getAutore());
                }
                startActivity(i);
            }
        });

        //activity per inserire nuove note
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent i = new Intent(listActivity.this,AddActivity.class);
            if(value.equals("anonimo")){
                i.putExtra(AddActivity.KEY_EXTRA,"anonimo");
            }
            startActivity(i);
            }
        });
    }

    //Funzione per leggere da Firebase
    public void fetchData(String user){
        dataSet.clear(); //Pulire l'array prima di inserire i nuovi dati letti
        final String finalUser = user;
        db.collection(user)
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Dati d;
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Log.d(TAG, document.getId() + " => " + document.getData());
                    String text = document.getString(KEYTEXT);
                    String title = document.getString(KEYTITLE);
                    String lat = document.getString(KEY_LAT);
                    String lng = document.getString(KEY_LONG);

                    d = new Dati(title, text, lat, lng, finalUser);
                    dataSet.add(d);

                }
                adapter.notifyDataSetChanged();
                }
            });
    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex){
        Toast.makeText(getApplicationContext(),mex.toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                makeToast(getString(R.string.exit));
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(listActivity.this);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", null);
                editor.commit();

                Intent i = new Intent(listActivity.this,MainActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Necessaria poichè se l'utente sceglie di non essere ricordato, e se non passa dal logout,
    // resterebbe salvata nella shared preferences il valore di username e rimarrebbe aperto il db.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(listActivity.this);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(sharedPref.getString("username","aa").equals("anonimo"))
            dbHandler.close();
        editor.putString("username", null);
        editor.commit();

    }

    public void dbFetchData(){
        dataSet.clear(); //Pulire l'array prima di inserire i nuovi dati letti
        dbHandler = new DbHandler(this);
        ArrayList<Dati> tmp = dbHandler.GetNote();
        for(Dati d : tmp){
            dataSet.add(d);
            makeToast(d.getTitolo());
        }
        adapter.notifyDataSetChanged();
    }

    // Qui vengono chiamate le funzioni per leggere i dati sul db locale o su firebase.
    // Il vantaggio è che l'onResume viene chiamata sia qiando viene lanciata l'applicazione e sia
    // quando l'activity "riparte" (si torna dall'AddActivity)
    @Override
    protected void onResume() {
        super.onResume();
        //Db locale
        if(value.equals("anonimo")){
            makeToast("a");
            /*dbHandler = new DbHandler(this);
            noteList = dbHandler.GetNote();
            listView = findViewById(R.id.note_list);
            MyArrayAdapter arrayAdapter = new MyArrayAdapter(this,R.layout.list_row,noteList);
            listView.setAdapter(arrayAdapter);*/
            dbFetchData();
        }
        //Firebase
        else {
            fetchData(value);
        }

    }

}

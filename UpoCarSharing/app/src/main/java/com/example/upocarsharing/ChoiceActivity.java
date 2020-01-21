package com.example.upocarsharing;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ChoiceActivity extends AppCompatActivity {

    private Button add;
    private Button show;
    private Button remove;
    private Button modify;

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
        setContentView(R.layout.activity_choice);

        add = findViewById(R.id.add);
        show = findViewById(R.id.show);
        modify = findViewById(R.id.modify);
        remove = findViewById(R.id.remove);

        //Bottone per l'aggiunta di un nuovo viaggio
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChoiceActivity.this,AddActivity.class);
                startActivity(i);
            }
        });

        //Bottone per visualizzare la lista di tutti i viaggi disponibili
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChoiceActivity.this,ShowActivity.class);
                startActivity(i);
            }
        });

        //Bottone per modificare un viaggio proposto
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChoiceActivity.this,modifyActivity.class);
                startActivity(i);
            }
        });

        //Bottone per rimuovere un viaggio proposto
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChoiceActivity.this,deleteActivity.class);
                startActivity(i);
            }
        });

    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex){
        Toast.makeText(getApplicationContext(),mex,Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ChoiceActivity.this);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", null);
                editor.commit();

                Intent i = new Intent(ChoiceActivity.this,MainActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

package com.example.museumguide;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class showActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String KEYUSER = "user";
    private static final String KEYTITLE= "title";
    private static final String KEYDESC = "description";
    private static final String KEYPHOTO = "photoBase64";
    private static final String KEYROOM = "room";
    private static final String KEYMUSEUM = "museum";
    private static final String KEYOBJ= "object";
    private static final String KEYCITY = "city";

    private ImageView photo;
    private TextView title;
    private TextView room;
    private TextView museum;
    private TextView desc;
    private TextView user;
    private TextView listen;

    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String museo, stanza, titolo, utente, city, descrizione, foto;

    private Menu menu;

    TextToSpeech textToSpeech;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menudeleteupdate,menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        photo = findViewById(R.id.objPhoto);
        title = findViewById(R.id.objTitle);
        room = findViewById(R.id.objRoom);
        museum = findViewById(R.id.objMuseum);
        desc = findViewById(R.id.descrizione);
        user = findViewById(R.id.user);
        listen = findViewById(R.id.listen);

        //Rende la TextView "scrollabile" in verticale
        desc.setMovementMethod(new ScrollingMovementMethod());

        titolo = getIntent().getStringExtra(KEYTITLE);
        descrizione = getIntent().getStringExtra(KEYDESC);
        utente = getIntent().getStringExtra(KEYUSER);
        foto = getIntent().getStringExtra(KEYPHOTO);
        stanza = getIntent().getStringExtra(KEYROOM);
        museo = getIntent().getStringExtra(KEYMUSEUM);
        city = getIntent().getStringExtra(KEYCITY);

        title.setText(titolo);
        desc.setText(descrizione);
        user.setText(utente);
        room.setText(stanza);
        museum.setText(museo);
        Bitmap bitmap = StringToBitMap(foto);
        photo.setImageBitmap(bitmap);

        //Viene letta la descrizione dell'oggetto
        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech = new TextToSpeech(showActivity.this, showActivity.this);
                texttoSpeak();
            }
        });
    }

    //Funzione per trasformare una stringa in un Bitmap
    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                //Solo chi ha inserito l'oggeto può rimuoverlo
                if(mAuth.getCurrentUser().getEmail().equals(utente)){
                    String museumName = museo + "#" + city;
                    String title = titolo + " " + utente;
                    deleteObj(museumName, stanza, title);
                }
                else
                    makeToast(getString(R.string.notPermitted));
                finish();
                return true;

            case R.id.update:
                //Solo chi ha creato l'ggetto può modificarlo
                if(mAuth.getCurrentUser().getEmail().equals(utente)){
                    //Si riutilizza la addObjectActivity
                    Intent i = new Intent(showActivity.this, addObjectActivity.class);
                    i.putExtra(KEYTITLE, titolo);
                    i.putExtra(KEYDESC, descrizione);
                    i.putExtra(KEYMUSEUM, museo);
                    i.putExtra(KEYCITY, city);
                    i.putExtra(KEYROOM, stanza);
                    i.putExtra(KEYPHOTO, foto);
                    startActivity(i);
                    finish();

                }
                else
                    makeToast(getString(R.string.notPermitted));
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Funzione per eliminare un oggetto dal DB
    private void deleteObj(String museumName, String roomName, String objName){
        db.collection(KEYMUSEUM)
                .document(museumName)
                .collection(KEYROOM)
                .document(roomName)
                .collection(KEYOBJ)
                .document(objName)
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

    //Inizializza il servizio di speechtotext
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported");
            } else {
                texttoSpeak();
            }
        } else {
            Log.e("error", "Failed to Initialize");
        }
    }

    //Funzione che legge il testo
    private void texttoSpeak() {
        //Crea il testo da leggere
        String text = getString(R.string.object) + " " + titolo + ". " + getString(R.string.description) + " " + descrizione + ". " +
              getString(R.string.insert) + " " + utente;
        if ("".equals(text)) {
            text = getString(R.string.errorDesc);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }


}

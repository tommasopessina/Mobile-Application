package com.example.museumguide;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class addObjectActivity extends AppCompatActivity {

    private TextView roomName;
    private EditText title;
    private EditText desc;
    private Button addObject;
    private ImageView photo;
    private ImageView speechTextTitle;
    private ImageView speechTextDesc;

    private Bitmap photobm;

    private static final String KEYROOM = "room";
    private static final String KEYUSER = "user";
    private static final String KEYMUSEUM = "museum";
    private static final String KEYFOTOBUNDLE = "photoBundle";
    private static final String KEYCITY = "city";
    private static final String KEYTITLE= "title";
    private static final String KEYDESC = "description";
    private static final String KEYPHOTO = "photoBase64";
    private static final String KEYOBJ= "object";

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQ_CODE_SPEECH_TITLE = 100;
    private static final int REQ_CODE_SPEECH_DESC = 101;

    private FirebaseAuth mAuth;
    FirebaseFirestore db;

    String museumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_object);

        photo = findViewById(R.id.photo);
        roomName = findViewById(R.id.roomName);
        title = findViewById(R.id.objectName);
        desc = findViewById(R.id.desc);
        addObject = findViewById(R.id.addObject);
        speechTextTitle = findViewById(R.id.speechTitle);
        speechTextDesc = findViewById(R.id.speechDesc);

        //Utile per recuperare la foto quando si cambia orientamento al device
        if(getIntent().getStringExtra(KEYFOTOBUNDLE)!=null){
            String b = getIntent().getStringExtra(KEYFOTOBUNDLE);
            photo.setImageBitmap(StringToBitMap(b));
            photo.setEnabled(false); //rendere la foto non più cliccabile
        }

        String museum = getIntent().getStringExtra(KEYMUSEUM);
        String city = getIntent().getStringExtra(KEYCITY);

        //Questo extra conterrà qualcosa solo nel caso in cui questa activity venga lanciata
        // quando l'utente vuole modoficare il suo oggetto.
        // In tal caso si recuperano i due campi e vengono inseriti nelle EditText
        if(getIntent().getStringExtra(KEYTITLE)!=null){
            title.setText(getIntent().getStringExtra(KEYTITLE));
            desc.setText(getIntent().getStringExtra(KEYDESC));
            photobm = StringToBitMap(getIntent().getStringExtra(KEYPHOTO));
            photo.setImageBitmap(photobm);
        }

        //Usato per poter identificare musei omonimi di città differenti
        this.museumName = museum + "#" + city;

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        final String room = getIntent().getStringExtra(KEYROOM);
        roomName.setText(room);

        //Scatta la foto
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        //Inserire la descrizione con lo SpeechToText
        speechTextDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput(false);
            }
        });

        //Inserire il titolo con lo SpeechToText
        speechTextTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput(true);
            }
        });

        addObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String obTitle = title.getText().toString();
                String obDesc = desc.getText().toString();

                //Se tutti i campi non sono vuoti, aggiunge l'oggetto al db
                if(!obTitle.equals("") && !obDesc.equals("") && photobm!=null){
                    String foto = BitMapToString(photobm);
                    writeData(room,obTitle,obDesc,foto,mAuth.getCurrentUser().getEmail());
                    finish();
                }
                else{
                    makeToast(getString(R.string.allDataError));
                }
            }
        });
    }

    //Funzione per convertite un Bitmap in una Stringa
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    //Funzione per convertire una Stringa in un Bitmap
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

    //Scrive i dati su Firebase
    public void writeData(String room, String title, String desc, String foto, String user) {
        Map<String, Object> newObject = new HashMap<>();
        newObject.put(KEYTITLE, title);
        newObject.put(KEYDESC, desc);
        newObject.put(KEYPHOTO, foto);
        newObject.put(KEYUSER, user);
        String objectName = title + " " + user;  //in modo da poter diversificare gli oggetti inseriti
        db.collection(KEYMUSEUM)
                .document(museumName)
                .collection(KEYROOM)
                .document(room)
                .collection(KEYOBJ)
                .document(objectName)
                .set(newObject)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeToast(getString(R.string.objAdded));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast(getString(R.string.objError));
                    }
                });

    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex) {
        Toast.makeText(getApplicationContext(), mex, Toast.LENGTH_SHORT).show();
    }

    //Lancia l'activity per scattare la foto
   private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //Inizia il servizion di Speech2Text
    private void startVoiceInput(boolean isTitle) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //Utilizza la lingua default del device
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speechHint));
        try {
            if(isTitle)
                startActivityForResult(intent, REQ_CODE_SPEECH_TITLE);
            else
                startActivityForResult(intent, REQ_CODE_SPEECH_DESC);
        } catch (ActivityNotFoundException a) {

        }
    }

    //Al termine dell'activity di Speech2Text, viene chiamata questa funzione per insire il testo e la foto nella TextView o nella ImageView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_TITLE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    title.setText(result.get(0));
                }
                break;
            }
            case REQ_CODE_SPEECH_DESC: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    desc.setText(result.get(0));
                }
                break;
            }
            case REQUEST_IMAGE_CAPTURE:{
                photobm = (Bitmap) data.getExtras().get("data");
                String b = BitMapToString(photobm);
                getIntent().putExtra(KEYFOTOBUNDLE,b);
                photo.setImageBitmap(photobm);
                photo.setEnabled(false); //rendere la foto non più cliccabile
            }
        }
    }

}

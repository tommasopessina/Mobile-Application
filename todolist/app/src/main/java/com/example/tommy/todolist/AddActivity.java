package com.example.tommy.todolist;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class AddActivity extends AppCompatActivity {

    public static final String KEY_EXTRA = "value";
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String ERROR_MSG = "Google Play services are unavailable.";
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private EditText title;
    private EditText text;
    private Button addButton;
    private Button text2speech;
    private Button desc2speech;
    public static final String KEYTEXT = "text";
    public static final String KEYTITLE = "title";
    public static final String KEYAUTHOR = "author";
    private static final String KEY_LAT = "lat";
    public static final String KEYISMODIFY= "isModify";
    private static final String KEY_LONG = "long";
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int REQ_CODE_SPEECH_INPUTDESC = 101;

    private String value;
    FirebaseFirestore db;
    DocumentReference mDocRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();

        // Check if we have permission to access high accuracy fine location.
        int permission = ActivityCompat.checkSelfPermission(this,
                ACCESS_FINE_LOCATION);

        // If permission is granted, fetch the last location.
        if (permission == PERMISSION_GRANTED) {

        } else {
            // If permission has not been granted, request permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        title = findViewById(R.id.titleAdd);
        text = findViewById(R.id.textAdd);
        addButton = findViewById(R.id.okAdd);
        text2speech = findViewById(R.id.speech2text);
        desc2speech = findViewById(R.id.desc2text);

        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();

        int result = availability.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (!availability.isUserResolvableError(result)) {
                Toast.makeText(this, ERROR_MSG, Toast.LENGTH_LONG).show();
            }
        }

        if (getIntent().hasExtra(KEY_EXTRA)) { //se c'è qualcosa salvato nel bundle
            value = getIntent().getStringExtra(KEY_EXTRA);
        } else { //se non riesce a trovare qualcosa
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();
            value = mAuth.getCurrentUser().getEmail();
        }

        if(getIntent().hasExtra(KEYTITLE)){
            title.setText(getIntent().getStringExtra(KEYTITLE));
            text.setText(getIntent().getStringExtra(KEYTEXT));
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titleT = title.getText().toString();
                String textT = text.getText().toString();

                if(!titleT.equals("") && !textT.equals("")) {
                    if(value.equals("anonimo")){
                        DbHandler dbHandler = new DbHandler(getApplicationContext());
                        if(getIntent().getStringExtra(KEYISMODIFY)!=null && dbHandler.deleteTitle(titleT)) {
                            mDocRef = FirebaseFirestore.getInstance().collection(value).document(titleT);
                            getLastLocation(false, titleT, textT, value, mDocRef);
                        }
                        else{
                            mDocRef = FirebaseFirestore.getInstance().collection(value).document(titleT);
                            getLastLocation(false, titleT, textT, value, mDocRef);
                        }
                    }
                    else {
                        mDocRef = FirebaseFirestore.getInstance().collection(value).document(titleT);
                        getLastLocation(true, titleT, textT, value, mDocRef);
                    }
                    finish();
                }
                else{
                    makeToast(getString(R.string.dataError));
                }
            }
        });

        //Bottone per inserire il testo via Speech2Text
        text2speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput(true);
            }
        });

        desc2speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput(false);
            }
        });
    }

    private void getLastLocation(final boolean isCloud, String title, String text, String user, final DocumentReference docRef) {
        final String fTitle = title;
        final String fText = text;
        final String fUser = user;
        final DocumentReference fDocRef = docRef;
        final Location loc;
        FusedLocationProviderClient fusedLocationClient;
        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                == PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
                        == PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            //Se si vuole scrivere su Firebase
                            if(isCloud){
                                addNewNote(fTitle,fText,location,fUser,fDocRef);
                            }
                            else{
                                //db interno
                                updateDb(location,fTitle,fText);
                            }
                        }
                    });
        }
        else{
            showMessageOKCancel(getString(R.string.askPermission),new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_CODE);
                    }
                }
            });
        }
    }

    //Db loale
    private void updateDb(final Location location, String title, String text){
        DbHandler dbHandler = new DbHandler(getApplicationContext());
        final double lat = location.getLatitude();
        final double lng = location.getLongitude();
        dbHandler.insertPosition(title,text,String.valueOf(lat), String.valueOf(lng));
        makeToast(title + " " + getString(R.string.added));
    }

    //Firebase
    void addNewNote(final String title, String text, Location loc, String user, DocumentReference docRef){
        Map < String, Object > newNote = new HashMap < > ();
        newNote.put(KEYAUTHOR, user);
        newNote.put(KEYTEXT, text);
        newNote.put(KEYTITLE, title);
        newNote.put(KEY_LAT, String.valueOf(loc.getLatitude()));
        newNote.put(KEY_LONG, String.valueOf(loc.getLongitude()));
        docRef.set(newNote)
                .addOnSuccessListener(new OnSuccessListener < Void > () {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeToast(title + " " + getString(R.string.added));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast(getString(R.string.errorAdd));
                        Log.d("TAG", e.toString());
                    }
                });
    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex){
        Toast.makeText(getApplicationContext(),mex.toString(),Toast.LENGTH_SHORT).show();
    }

    // Necessaria poichè se l'utente sceglie di non essere ricordato, non passa dal logout, quindi
    // resterebbe salvata nella shared preferences il valore di username
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(AddActivity.this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("username", null);
        editor.commit();
    }

    //Funzione per creare e mostrare un Alert Dialog
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(AddActivity.this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show();
    }

    //Inizia il servizion di Speech2Text
    private void startVoiceInput(boolean isTitle) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak));
        try {
            if(isTitle)
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            else
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUTDESC);
        } catch (ActivityNotFoundException a) {

        }
    }

    //Al termine dell'activity di Speech2Text, viene chiamata questa funzione per insire il testo nella TextView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    title.setText(result.get(0));
                }
                break;
            }
            case REQ_CODE_SPEECH_INPUTDESC:{
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    text.setText(result.get(0));
                }
                break;
            }
        }
    }

}

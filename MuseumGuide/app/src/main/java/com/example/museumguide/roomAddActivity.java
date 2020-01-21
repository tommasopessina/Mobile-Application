package com.example.museumguide;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// AGGIUNTA STANZA AL MUSEO

public class roomAddActivity extends AppCompatActivity {

    private static final String KEYROOM = "room";
    private static final String KEYUSER = "user";
    private static final String KEYMUSEUM = "museum";
    private static final String KEYCITY = "city";

    private EditText room;
    private Button addRoom;

    private FirebaseAuth mAuth;
    FirebaseFirestore db;

    String museumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_add);

        room = findViewById(R.id.room);
        addRoom = findViewById(R.id.addRoom);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String museum = getIntent().getStringExtra(KEYMUSEUM);
        String city = getIntent().getStringExtra(KEYCITY);

        //Usato per poter identificare musei omonimi di città differenti
        this.museumName = museum + "#" + city;

        addRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String roomName = room.getText().toString();
                String user = mAuth.getCurrentUser().getEmail();

                //Controlla che il nome della stanza non sia vuoto
                if(!roomName.equals("")){
                    writeData(roomName,user);
                    finish();
                }
                else{
                    makeToast(getString(R.string.allDataError));
                }

            }
        });
    }

    //Funzione per aggiungere la stanza a Firebase
    public void writeData(String room, String user) {
        Map<String, Object> newRoom = new HashMap<>();
        newRoom.put(KEYROOM, room);
        newRoom.put(KEYUSER, user);
        db.collection(KEYMUSEUM)
                .document(museumName)
                .collection(KEYROOM)
                .document(room)
                .set(newRoom)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeToast(getString(R.string.roomAdded));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast(getString(R.string.roomError));
                    }
                });

    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex) {
        Toast.makeText(getApplicationContext(), mex, Toast.LENGTH_SHORT).show();
    }

}

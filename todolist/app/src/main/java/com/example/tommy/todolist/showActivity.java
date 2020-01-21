package com.example.tommy.todolist;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class showActivity extends AppCompatActivity {

    public static final String KEYTEXT = "text";
    public static final String KEYTITLE = "title";
    public static final String KEYAUTHOR = "author";
    public static final String KEYISCLOUD = "isCloud";
    public static final String KEY_EXTRA = "value";
    public static final String KEYISMODIFY= "isModify";

    private Button back;
    private Button delete;
    private TextView title;
    private TextView text;
    private Button modify;

    FirebaseFirestore db;
    DocumentReference mDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        back = findViewById(R.id.back);
        delete = findViewById(R.id.delete);
        title = findViewById(R.id.titleShow);
        text = findViewById(R.id.textNote);
        modify = findViewById(R.id.modify);

        text.setMovementMethod(new ScrollingMovementMethod()); //Permette di effettuare lo scroll verticale

        db = FirebaseFirestore.getInstance();

        final String titleText = getIntent().getStringExtra(KEYTITLE);
        final String textText = getIntent().getStringExtra(KEYTEXT);
        final String autore = getIntent().getStringExtra(KEYAUTHOR);
        final Boolean isCloud = getIntent().getBooleanExtra(KEYISCLOUD,false);

        title.setText(titleText);
        text.setText(textText);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(showActivity.this, AddActivity.class);
                i.putExtra(KEYTEXT, textText);
                i.putExtra(KEYTITLE, titleText);
                i.putExtra(KEYISMODIFY, "modify");
                if(autore==null)
                    i.putExtra(KEY_EXTRA, "anonimo");
                else
                    i.putExtra(KEY_EXTRA, autore);
                startActivity(i);
                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isCloud) {
                    db.collection(autore)
                        .document(titleText)
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

                            }
                        });
                    finish();
                }
                else{
                    DbHandler dbHandler = new DbHandler(getApplicationContext());
                    if(dbHandler.deleteTitle(titleText))
                        makeToast(getString(R.string.removed));
                    else
                        makeToast(getString(R.string.notRemoved));
                    finish();
                }
            }

        });

    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex){
        Toast.makeText(getApplicationContext(),mex,Toast.LENGTH_SHORT).show();
    }

}

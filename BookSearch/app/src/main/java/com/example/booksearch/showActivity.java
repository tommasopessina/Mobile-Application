package com.example.booksearch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class showActivity extends AppCompatActivity {

    private static final String KEYUSER = "user";
    private static final String KEYBOOK = "book";
    private static final String KEYBOOKNAME = "bookName";
    private static final String KEYAUTHOR = "bookAuthor";
    private static final String KEYBOOKCODE = "bookISBN";
    private static final String KEYSUBTITLE = "subtitle";
    private static final String KEYDESCRIPTION = "description";
    private static final String KEYLAT = "lat";
    private static final String KEYLNG = "lng";
    private static final String KEYIMAGE = "image";

    private ImageView copertina;
    private TextView titolo;
    private TextView sottotitolo;
    private TextView autore;
    private TextView descrizione;
    private TextView userT;

    private String title = "";
    private String user = "";

    FirebaseFirestore db;

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
        setContentView(R.layout.activity_show);

        db = FirebaseFirestore.getInstance();

        copertina = findViewById(R.id.copertina);
        titolo = findViewById(R.id.titoloLibro);
        sottotitolo = findViewById(R.id.sottotitolo);
        autore = findViewById(R.id.autore);
        descrizione = findViewById(R.id.descrizione);
        userT = findViewById(R.id.user);

        descrizione.setMovementMethod(new ScrollingMovementMethod());

        title = getIntent().getStringExtra(KEYBOOKNAME);
        user = getIntent().getStringExtra(KEYUSER);
        String stringBitmap = getIntent().getStringExtra(KEYIMAGE);

        String bookName = title + " " + user;

        fetchData(bookName);
        titolo.setText(title);
        userT.setText(user);
        copertina.setImageBitmap(StringToBitMap(stringBitmap));

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

    //Funzione per leggere da Firebase
    public void fetchData(final String book) {
        final String finalBook = book;
        db.collection(KEYBOOK)
                .document(book)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        sottotitolo.setText(documentSnapshot.getString(KEYSUBTITLE));
                        autore.setText(documentSnapshot.getString(KEYAUTHOR));
                        String desc = documentSnapshot.getString(KEYDESCRIPTION);
                        if(desc==null){
                            descrizione.setText(getString(R.string.descNotFound));
                        }
                        else
                            descrizione.setText(desc);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                    }
                });
    }



    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex) {
        Toast.makeText(getApplicationContext(), mex, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:

                deleteBook(title,user);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteBook(String book, String user){
        String bookName = book + " " + user;
        db.collection(KEYBOOK)
                .document(bookName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeToast("DELETED");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast("NOT DELETED");
                    }
                });

    }

}

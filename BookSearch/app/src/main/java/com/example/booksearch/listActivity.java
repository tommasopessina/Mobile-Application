package com.example.booksearch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class listActivity extends AppCompatActivity {

    private static final String TAG = "TEST";

    private static final String KEYUSER = "user";
    private static final String KEYBOOK = "book";
    private static final String KEYBOOKNAME = "bookName";
    private static final String KEYIMAGE = "image";
    private static final String KEYLAT = "lat";
    private static final String KEYLNG = "lng";
    private static final String KEYPOS = "location";

    private Button add;
    private ListView list;
    private Button mapBook;

    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ArrayList<Dati> dataSet;
    private MyArrayAdapter adapter;
    private Dati d;

    HashMap<String,String> data;

    private Menu menu;

    private ProgressDialog mProgressDialog;

    Bitmap bitmapAll = null;

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
        mapBook = findViewById(R.id.showMapBook);
        list = findViewById(R.id.book_list);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        data = new HashMap<>();
        dataSet = new ArrayList<>();
        adapter = new MyArrayAdapter(this, R.layout.list_row, dataSet);
        list.setAdapter(adapter);

/*
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(listActivity.this);
        mProgressDialog.setIndeterminate(true);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Progress dialog title
        mProgressDialog.setTitle("Please wait");
        // Progress dialog message
        mProgressDialog.setMessage("Please wait, we are downloading your image file...");
*/

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent i = new Intent(listActivity.this, addActivity.class);
               i.putExtra(KEYUSER, mAuth.getCurrentUser().getEmail());
               startActivity(i);
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(listActivity.this, showActivity.class);
                i.putExtra(KEYBOOKNAME,dataSet.get(position).getTitle());
                i.putExtra(KEYUSER,dataSet.get(position).getUser());
                i.putExtra(KEYIMAGE,BitMapToString(dataSet.get(position).getImageResource()));
                startActivity(i);
            }
        });

        mapBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(listActivity.this,mapBookActivity.class);
                ArrayList<String> pos = new ArrayList<>();
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> user = new ArrayList<>();
                for(int y=0; y<dataSet.size(); y++){
                    String latlng = dataSet.get(y).getLat() + "," + dataSet.get(y).getLng();
                    pos.add(y,latlng);
                    title.add(y,dataSet.get(y).getTitle());
                    user.add(y,dataSet.get(y).getUser());
                }
                i.putExtra(KEYBOOK,title);
                i.putExtra(KEYPOS, pos);
                i.putExtra(KEYUSER, user);
                startActivity(i);
            }
        });

    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream ByteStream=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, ByteStream);
        byte [] b=ByteStream.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

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
    public void fetchData(String user) {
     //   mProgressDialog.show();
        dataSet.clear(); //Pulire l'array prima di inserire i nuovi dati letti
        final String finalUser = user;
        db.collection(KEYBOOK)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Dati d;
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d(TAG, document.getId() + " => " + document.getData());

                            String title = document.getString(KEYBOOKNAME);
                            String user = document.getString(KEYUSER);
                            String imm = document.getString(KEYIMAGE);
                            String lat = document.getString(KEYLAT);
                            String lng = document.getString(KEYLNG);

                            Bitmap res = StringToBitMap(imm);

                            d = new Dati(title,lng,lat,user,res);
                            dataSet.add(d);

                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }


    private class DownloadTask extends AsyncTask<URL,Void,Bitmap>{
        // Before the tasks execution
        protected void onPreExecute(){
            // Display the progress dialog on async task start

        }

        // Do the task in background/non UI thread
        protected Bitmap doInBackground(URL...urls){
            URL url = urls[0];
            HttpURLConnection connection = null;

            try{
                // Initialize a new http url connection
                connection = (HttpURLConnection) url.openConnection();

                // Connect the http url connection
                connection.connect();

                // Get the input stream from http url connection
                InputStream inputStream = connection.getInputStream();

                /*
                    BufferedInputStream
                        A BufferedInputStream adds functionality to another input stream-namely,
                        the ability to buffer the input and to support the mark and reset methods.
                */
                /*
                    BufferedInputStream(InputStream in)
                        Creates a BufferedInputStream and saves its argument,
                        the input stream in, for later use.
                */
                // Initialize a new BufferedInputStream from InputStream*/
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                /*
                    decodeStream
                        Bitmap decodeStream (InputStream is)
                            Decode an input stream into a bitmap. If the input stream is null, or
                            cannot be used to decode a bitmap, the function returns null. The stream's
                            position will be where ever it was after the encoded data was read.

                        Parameters
                            is InputStream : The input stream that holds the raw data
                                              to be decoded into a bitmap.
                        Returns
                            Bitmap : The decoded bitmap, or null if the image data could not be decoded.
                */
                // Convert BufferedInputStream to Bitmap object
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                // Return the downloaded bitmap
                return bmp;

            }catch(IOException e){
                e.printStackTrace();
            }finally{
                // Disconnect the http url connection
                connection.disconnect();
            }
            return null;
        }

        // When all async task done
        protected void onPostExecute(Bitmap result){
            // Hide the progress dialog
          //  mProgressDialog.dismiss();

            if(result!=null){
                // Display the downloaded image into ImageView
                //mImageView.setImageBitmap(result);

                bitmapAll = result;
                // Save bitmap to internal storage
                // Uri imageInternalUri = saveImageToInternalStorage(result);
                // Set the ImageView image from internal storage
                // mImageViewInternal.setImageURI(imageInternalUri);
            }else {
                // Notify user that an error occurred while downloading image
                makeToast("Error");
            }
        }
    }

    // Custom method to convert string to url
    protected URL stringToURL(String urlString){
        try{
            URL url = new URL(urlString);
            return url;
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex) {
        Toast.makeText(getApplicationContext(), mex, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String user = mAuth.getCurrentUser().getEmail();
        fetchData(user);
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

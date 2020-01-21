package com.example.booksearch;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class addActivity extends AppCompatActivity {

    private static final String TAG = "TEST";

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
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private static final String ERROR_MSG = "Google Play services are unavailable.";

    private EditText isbn;
    private Button addInput;
    private Button addScan;

    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    //private ArrayList<Dati> dataSet;
    //private MyArrayAdapter adapter;
    //private Dati d;

    HashMap<String,String> data;
    Bitmap bitmapAll;

    private AsyncTask<String,?,?> networkTask;

    private AsyncTask mMyTask;

    //private ProgressDialog mProgressDialog;

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

        isbn = findViewById(R.id.isbn);
        addInput = findViewById(R.id.addISBN);
        addScan = findViewById(R.id.addBOOK);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int result = availability.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (!availability.isUserResolvableError(result)) {
                Toast.makeText(this, ERROR_MSG, Toast.LENGTH_LONG).show();
            }
        }

       /* // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(addActivity.this);
        mProgressDialog.setIndeterminate(true);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Progress dialog title
        mProgressDialog.setTitle("AsyncTask");
        // Progress dialog message
        mProgressDialog.setMessage("Please wait, we are downloading your image file...");
*/

        data = new HashMap<>();
       // dataSet = new ArrayList<>();

        addInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String isbnCode = isbn.getText().toString();
                if(!isbnCode.equals("")){
                    launchSearch(isbnCode);
                }
            }
        });

        addScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator i = new IntentIntegrator(addActivity.this);
                i.initiateScan();
            }
        });
    }

    private void getLastLocation(final String title, final String user, final String autore, final String subTitle, final String descrizione, final String immagine) {

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
                            //Scrittura su firebase
                            writeData(title,user,autore,subTitle, descrizione, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), immagine);
                        }
                    });
        } else {
            showMessageOKCancel(getString(R.string.askPermission), new DialogInterface.OnClickListener() {
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

    //Funzione per creare e mostrare un Alert Dialog
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(addActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            // data.put(KEYUSER, mAuth.getCurrentUser().getEmail());
            launchSearch(scanResult.getContents());
        } else {
            makeToast("error");
        }

    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream ByteStream=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, ByteStream);
        byte [] b=ByteStream.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public void writeData(String title, String user, String author, String subTitle, String description, String lat, String lng, String image) {

        Map<String, Object> newBook = new HashMap<>();
        newBook.put(KEYAUTHOR, author);
        newBook.put(KEYBOOKNAME, title);
        newBook.put(KEYSUBTITLE, subTitle);
        newBook.put(KEYDESCRIPTION, description);
        newBook.put(KEYUSER, user);
        newBook.put(KEYLAT, lat);
        newBook.put(KEYLNG, lng);

        Bitmap res=null;
        try {
            res = new addActivity.DownloadTask().execute(stringToURL(image)).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String photo = BitMapToString(res);
        newBook.put(KEYIMAGE, photo);

        String bookName = title + " " + user;

        db.collection(KEYBOOK)
                .document(bookName)
                .set(newBook)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeToast("added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast("NOT added");
                    }
                });
        finish();

    }

    @Override
    protected void onPause() {
        AsyncTask<?,?,?> oldTask = networkTask;
        if (oldTask != null) {
            oldTask.cancel(true);
            networkTask = null;
        }
        super.onPause();
    }

    private void launchSearch(String isbn) {
        AsyncTask<?,?,?> oldTask = networkTask;
        if (oldTask != null) {
            oldTask.cancel(true);
        }
        networkTask = new addActivity.NetworkTask();
        networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, isbn, mAuth.getCurrentUser().getEmail());
    }

    private class DownloadTask extends AsyncTask<URL,Void,Bitmap>{
        // Before the tasks execution
        protected void onPreExecute(){
            // Display the progress dialog on async task start
            //mProgressDialog.show();
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
                // Initialize a new BufferedInputStream from InputStream
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

    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //fix outOfMemory problem
    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

    private class DownloadFile extends AsyncTask<String,Bitmap,Bitmap> {

        Bitmap b;

        @Override
        protected Bitmap doInBackground(String... url) {

            b = getBitmapFromURL(url[0]);
            publishProgress(b);
            return b;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            bitmapAll = bitmap;

        }
    }

    private final class NetworkTask extends AsyncTask<String,Object, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                // These return a JSON result which describes if and where the query was found. This API may
                // break or disappear at any time in the future. Since this is an API call rather than a
                // website, we don't use LocaleManager to change the TLD.
                String theIsbn = args[0];
                String user = args[1];
                String uri;

                uri = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + theIsbn;

                CharSequence content = HttpHelper.downloadViaHttp(uri, HttpHelper.ContentType.JSON);
                return new JSONObject(content.toString());
            } catch (IOException | JSONException ioe) {
                Log.w(TAG, "Error accessing book search", ioe);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result == null || result.get("items")==null) {
                    makeToast("NULL");
                } else {
                    //makeToast(result.toString());
                    data = getDataFromResult(result);

                    getLastLocation(data.get(KEYBOOKNAME),data.get(KEYUSER),data.get(KEYAUTHOR),data.get(KEYSUBTITLE), data.get(KEYDESCRIPTION), data.get(KEYIMAGE));
                }
            } catch (JSONException e) {
                makeToast(getString(R.string.isbnNotFound));
                e.printStackTrace();
            }
        }

        private HashMap<String,String> getDataFromResult(JSONObject result){
            HashMap<String,String> dati = new HashMap<>();
            try {
                JSONArray arr = (JSONArray) result.get("items");
                JSONObject obj = arr.getJSONObject(0);

                JSONObject info = obj.getJSONObject("volumeInfo");
                dati.put(KEYBOOKNAME, info.getString("title"));

                if(info.has("subtitle")){
                    dati.put(KEYSUBTITLE, info.getString("subtitle"));
                }
                if(info.has("authors")){
                    dati.put(KEYAUTHOR,info.getString("authors"));
                }

                if(info.has("description")){
                    dati.put(KEYDESCRIPTION, info.getString("description"));
                }

                JSONObject imm = info.getJSONObject("imageLinks");

                dati.put(KEYIMAGE, imm.getString("smallThumbnail"));

                dati.put(KEYUSER, mAuth.getCurrentUser().getEmail());

                //makeToast(info.getString("title") + " "+ imm.getString("smallThumbnail"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return dati;
        }


    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex) {
        Toast.makeText(getApplicationContext(), mex, Toast.LENGTH_SHORT).show();
    }


}

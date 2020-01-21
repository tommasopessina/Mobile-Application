package com.example.booksearch;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class mapBookActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final String TAG = "TEST";

    private static final String KEYLIST= "list";

    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ArrayList<Dati> dataSet;

    HashMap<String, String> data;

    private static final String KEYUSER = "user";
    private static final String KEYBOOK = "book";
    private static final String KEYBOOKNAME = "bookName";
    private static final String KEYIMAGE = "image";
    private static final String KEYPOS = "location";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_book);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        data = new HashMap<>();
        dataSet = new ArrayList<>();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        final LatLng upo = new LatLng(44.9237555, 8.6179071);
         CameraPosition cm = new CameraPosition.Builder()
                .target(upo).tilt(45).zoom(5).build();
        mMap.animateCamera(  // reach the new position in 3 seconds
                CameraUpdateFactory.newCameraPosition(cm), 3000, null);

        ArrayList<String> title = getIntent().getStringArrayListExtra(KEYBOOK);
        ArrayList<String> pos = getIntent().getStringArrayListExtra(KEYPOS);
        ArrayList<String> user = getIntent().getStringArrayListExtra(KEYUSER);

        for (int i = 0; i < title.size(); i++) {
            String key = title.get(i);
            String userSnippet = user.get(i);
            String[] latlong =  pos.get(i).split(",");
            double lat = Double.parseDouble(latlong[0]);
            double lng = Double.parseDouble(latlong[1]);
            LatLng value = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(value).title(key)
                    .snippet(userSnippet)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }


    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex) {
        Toast.makeText(getApplicationContext(), mex, Toast.LENGTH_SHORT).show();
    }

}

package com.example.museumguide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

public class MyArrayAdapter extends ArrayAdapter<Dati> {

    private List<Dati> dati;
    private Context context;
    private Bitmap bitmap;

    public MyArrayAdapter(@NonNull Context context, int resource, java.util.List<Dati> dati) {
        super(context, resource);
        this.dati = dati;
        this.context = context;
    }

    //Restituisce il numero di elementi della textview da disegnare
    @Override
    public int getCount() {
        return dati.size();
    }

    //getView sarà richiamata tante volte quanti sono gli elementi contenuti nella lista di dati
    //ogni volta con position incrementato e il riferimento convertView alla view da disegnare
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //è analogo alla setContentView della activity
        convertView = View.inflate(context,R.layout.list_row,null);

        //findViewById sull'oggetto contentView perchè array adapter non l'ha definito, ma view sì
        TextView name = convertView.findViewById(R.id.name);
        TextView city = convertView.findViewById(R.id.city);
        TextView address = convertView.findViewById(R.id.address);
        ImageView photo = convertView.findViewById(R.id.photo);

        name.setText(dati.get(position).getTitle());
        city.setText(dati.get(position).getCity());
        if(!dati.get(position).getStreet().equals("") || !dati.get(position).getNum().equals("")) {
            if (!dati.get(position).isPhoto()) {
                address.setText(dati.get(position).getStreet() + ", " + dati.get(position).getNum());
            }
        }
        if(dati.get(position).isPhoto()){
            Bitmap bitmap = StringToBitMap(dati.get(position).getNum());
            photo.setImageBitmap(bitmap);
            //address.setText(dati.get(position).getStreet());
        }

        return convertView;
    }

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

}

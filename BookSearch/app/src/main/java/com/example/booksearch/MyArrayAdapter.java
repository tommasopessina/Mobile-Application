package com.example.booksearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class MyArrayAdapter extends ArrayAdapter<Dati> {

    private List<Dati> dati;
    private Context context;
    ArrayList<Bitmap> bitmapAll;

    public MyArrayAdapter(@NonNull Context context, int resource, java.util.List<Dati> dati) {
        super(context, resource);
        this.dati = dati;
        this.context = context;
        bitmapAll = new ArrayList<>();
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
        ImageView imageView = convertView.findViewById(R.id.immagine);
        TextView titolo = convertView.findViewById(R.id.titolo);
        TextView user = convertView.findViewById(R.id.utente);

        titolo.setText(dati.get(position).getTitle());
        user.setText(dati.get(position).getUser());
       /* new DowloadFile().execute(dati.get(position).getImageResource());
        if(this.bitmapAll.size()>0) {
            Bitmap b = this.bitmapAll.get(position);
            imageView.setImageBitmap(b);
        }*/
        imageView.setImageBitmap(dati.get(position).getImageResource());
        return convertView;
    }
/*
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

    private class DowloadFile extends AsyncTask<String,Bitmap,Bitmap> {

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
            bitmapAll.add(bitmap);

        }
    }*/



}

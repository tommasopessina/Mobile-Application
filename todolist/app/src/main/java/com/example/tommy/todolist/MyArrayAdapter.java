package com.example.tommy.todolist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyArrayAdapter extends ArrayAdapter<Dati> {

    private List<Dati> dati;
    private Context context;

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
        TextView titolo = convertView.findViewById(R.id.titleNote);
        TextView lat = convertView.findViewById(R.id.latNote);
        TextView lng = convertView.findViewById(R.id.longNote);

       // im.setImageResource(dati.get(position).getRisorsaImmagine());
        titolo.setText(dati.get(position).getTitolo());
        lat.setText(dati.get(position).getLat());
        lng.setText(dati.get(position).getLng());

        return convertView;
    }

}

package com.example.matija077.autotrolej;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View myContentsView;
    private String[] ruteArray;

    MyInfoWindowAdapter(LayoutInflater infalInflater){

        myContentsView = infalInflater.inflate(R.layout.info_window, null);
    }

    @Override
    public View getInfoContents(Marker marker) {
        /*
        TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
        tvTitle.setText("Test");

        TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
        tvSnippet.setText("Drugi red");
        */
        TextView txtTitle = ((TextView)myContentsView.findViewById(R.id.txtTitle));
        txtTitle.setText(marker.getTitle());

        ArrayAdapter adapter = new ArrayAdapter<String>(myContentsView.getContext(),
                R.layout.info_window_item, R.id.txtListItem, ruteArray);

        ListView listView = (ListView) myContentsView.findViewById(R.id.lvRutes);
        listView.setAdapter(adapter);

        return myContentsView;
    }

    @Override
    public View getInfoWindow(Marker marker) {

        return null;
    }

    public void setRuteArray(String[] array){
        ruteArray = array;
    }
}

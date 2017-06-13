package com.example.matija077.autotrolej;

import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.j256.ormlite.stmt.query.In;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class TimeTableActivity extends AppCompatActivity {

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    OrmLiteDatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        ImageButton btnBack = (ImageButton) findViewById(R.id.btnBack);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.expList);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + listDataHeader.get(groupPosition),
                // Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();

            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
                Toast.makeText(
                        getApplicationContext(),
                        listDataHeader.get(groupPosition)
                                + " : "
                                + listDataChild.get(
                                listDataHeader.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT)
                        .show();
                Intent intent = new Intent(TimeTableActivity.this, LineTimeTableActivity.class);
                String route = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                intent.putExtra("route", route);
                startActivity(intent);
                return false;
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Gradske linije");
        listDataHeader.add("Prigradske linije");
        listDataHeader.add("Noćne linije");

        // Adding child data
        List<String> gradkse = new ArrayList<String>();

        List<String> prigradske = new ArrayList<String>();

        List<String> nocne = new ArrayList<String>();

        listDataChild.put(listDataHeader.get(0), gradkse); // Header, Child data
        listDataChild.put(listDataHeader.get(1), prigradske);
        listDataChild.put(listDataHeader.get(2), nocne);

        db = new OrmLiteDatabaseHelper(getApplicationContext());
        List<Route> routes2 = db.getAllRoutes();
        List<String> routes = db.queryRoot_routMark();

        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).split("-")[1].equals("city")) {
                gradkse.add(routes.get(i).split("-")[0]);
            } else if (routes.get(i).split("-")[1].equals("suburb")) {
                prigradske.add(routes.get(i).split("-")[0]);
            } else {
                nocne.add(routes.get(i).split("-")[0]);
            }
        }

        //  TODO add reverse order.
        Collections.sort(gradkse, new RouteComparator());
        Collections.sort(prigradske, new RouteComparator());
        Collections.sort(nocne, new RouteComparator());
        Log.i("tag", "tag");
    }

    class RouteComparator implements Comparator<String> {
        @Override
        public int compare(String route1, String route2) {
            Integer routeMark1 = null;
            Integer routeMark2 = null;
            String r1 = null;
            String r2 = null;
            //Boolean same = Boolean.FALSE;

            try {
                r1 = route1.replaceAll("[^\\d.]", "");
                r2 = route2.replaceAll("[^\\d.]", "");
            } catch (Exception e) {
                e.printStackTrace();
            }

                /*if (r1.equals(r2)) {
                    try {
                        r1 = route1.replaceAll("[^A-Za-z]+", "");
                        r2 = route2.replaceAll("[^A-Za-z]+", "");
                        routeMark1 = (int) r1.charAt(0);
                        routeMark2 = (int) r2.charAt(0);
                    } catch (Exception e) {

                    }
                    same = Boolean.TRUE;
                }*/

            //if (!same) {
                try {
                    if (r1 != "") {
                        routeMark1 = Integer.parseInt(r1);
                    } else {

                    }
                    if (r2 != "") {
                        routeMark2 = Integer.parseInt(r2);
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
           // }
            return routeMark1 < routeMark2 ? - 1 : routeMark1 == routeMark2 ? 0 : 1;
        }
    };
}


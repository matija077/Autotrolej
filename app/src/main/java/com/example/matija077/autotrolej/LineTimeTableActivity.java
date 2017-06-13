package com.example.matija077.autotrolej;

import android.opengl.Visibility;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


public class LineTimeTableActivity extends AppCompatActivity {
		private ExpandableListAdapter listAdapter;
		private ExpandableListView expListView;
		private List<String> listDataHeader;
		private HashMap<String, List<String>> listDataChild;
		private OrmLiteDatabaseHelper db;
		private String routeMark = null;
		private List<String> directionValues = null;
		private List<String> routeMarkValues = null;
		private List<Station_route> station_routes = null;
		private final int directionRoutePosition = 0;
		private final int stationsPosition = 1;
		private ListView radni_dan = null;
		private ListView subota = null;
		private ListView nedelja = null;
		private LinearLayout scheduleLayout = null;
		private String smjerKey = "Smjer";
		private String stanicaKey = "Stanica";

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_line_time_table);

			routeMark  = getIntent().getStringExtra("route");

			ImageButton btnBack = (ImageButton) findViewById(R.id.btnBack);
			TextView header = (TextView) findViewById(R.id.txtLineTimeTable);
			if (header != null){
				header.setText(routeMark);
			}
			radni_dan = (ListView) findViewById(R.id.radni_dan);
			subota = (ListView) findViewById(R.id.subota);
			nedelja = (ListView) findViewById(R.id.nedelja);
			// get the listview
			expListView = (ExpandableListView) findViewById(R.id.expList);
			scheduleLayout = (LinearLayout) findViewById(R.id.scheduleLayout);

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



					for (int i = 0; i < listDataHeader.size(); i++) {
						if ((expListView.isGroupExpanded(i)) &&(i != groupPosition)) {
							expListView.collapseGroup(i);
						}
					}

					if (scheduleLayout.getVisibility() == VISIBLE) {
						scheduleLayout.setVisibility(View.GONE);
					}
					return false;
				}
			});

			// Listview Group expanded listener
			expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

				@Override
				public void onGroupExpand(int groupPosition) {
					Log.i("ex", "ex");
					/*Toast.makeText(getApplicationContext(),
							listDataHeader.get(groupPosition) + " Expanded",
							Toast.LENGTH_SHORT).show();*/
				}
			});

			// Listview Group collasped listener
			expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

				@Override
				public void onGroupCollapse(int groupPosition) {
					Log.e("", "");
					/*Toast.makeText(getApplicationContext(),
							listDataHeader.get(groupPosition) + " Collapsed",
							Toast.LENGTH_SHORT).show();*/

				}
			});

			// Listview on child click listener
			expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
											int groupPosition, int childPosition, long id) {
					// TODO Auto-generated method stub
					/*Toast.makeText(
							getApplicationContext(),
							listDataHeader.get(groupPosition)
									+ " : "
									+ listDataChild.get(
									listDataHeader.get(groupPosition)).get(
									childPosition), Toast.LENGTH_SHORT)
							.show();*/
					if (groupPosition == directionRoutePosition) {
						populateStations(routeMarkValues.get(childPosition), directionValues.
								get(childPosition));
						expListView.collapseGroup(groupPosition);
						expListView.expandGroup(groupPosition + 1);
						String temp = "Smjer : " + listDataChild.
								get(listDataHeader.get(groupPosition)).get(childPosition);
						listDataHeader.set(groupPosition, temp);

						List<String> tempList = listDataChild.get(smjerKey);
						listDataChild.put(temp, tempList);
						if (listDataChild.size() > 2) {
							listDataChild.remove(smjerKey);
						}
						smjerKey = temp;
					}
					if (groupPosition == stationsPosition) {
						populateSchedule(station_routes.get(childPosition));
						expListView.collapseGroup(stationsPosition);
						String temp = "Stanica : " + listDataChild.
								get(listDataHeader.get(groupPosition)).get(childPosition);
						listDataHeader.set(groupPosition, temp);

						List<String> tempList = listDataChild.get(stanicaKey);
						listDataChild.put(temp, tempList);
						if (listDataChild.size() > 2) {
							listDataChild.remove(stanicaKey);
						}
						stanicaKey = temp;
						scheduleLayout.setVisibility(VISIBLE);
					}
					return false;
				}
			});

			btnBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

			expListView.expandGroup(directionRoutePosition);
		}

		/*
		 * Preparing the list data
		 */
		private void prepareListData() {
			listDataHeader = new ArrayList<String>();
			listDataChild = new HashMap<String, List<String>>();

			db = new OrmLiteDatabaseHelper(getApplicationContext());

			listDataHeader.add(smjerKey);
			listDataHeader.add(stanicaKey);
			populateRoutes();

			/*List<String> primjer = new ArrayList<String>();
			primjer.add("Udaljenost");

			listDataChild.put(listDataHeader.get(0), primjer); // Header, Child data*/
		}

		private void populateRoutes() {
			List<Route> routes = db.queryRoot_routMark(routeMark);
			List<String> routeNames = new ArrayList<>();
			routeMarkValues = new ArrayList<>();
			directionValues = new ArrayList<>();
			for (Route route : routes) {
				if ((!routeNames.contains(route.getRouteMark() + " : " + route.getDirectionA())) &&
						(route.getDirectionA() != null)) {
					routeNames.add(route.getDirectionA());
					routeMarkValues.add(route.getRouteMark());
					directionValues.add("A");
				}
				if ((!routeNames.contains(route.getRouteMark() + " : " + route.getDirectionB())) &&
						(route.getDirectionB() != null)) {
					routeNames.add(route.getDirectionB());
					routeMarkValues.add(route.getRouteMark());
					directionValues.add("B");
				}
			}
			//listDataChild.put(listDataHeader.get(directionRoutePosition), routeNames);
			listDataChild.put(smjerKey, routeNames);
		}

		private void populateStations(String routeMarkValue, String direction) {
			List<String> stationNames = new ArrayList<>();
			station_routes = db.queryStation_route_specific4(routeMarkValue, direction);
			Collections.sort(station_routes, new station_routeComparator());
			// Adding child data
			for (Station_route station_route : station_routes) {
				try {
					//stations.add(station_route.getStation());
					stationNames.add(station_route.getStation().getName());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//listDataChild.put(listDataHeader.get(stationsPosition), new ArrayList<String>(stationNames));
			listDataChild.put(stanicaKey, new ArrayList<String>(stationNames));
		}

		private void populateSchedule(Station_route station_route) {
			List<Schedule> schedules = null;
			schedules = db.querySchedule_timeTable(station_route);
			List<String> radni_danList = new ArrayList<>();
			List<String> subotaList = new ArrayList<>();
			List<String> nedeljaList = new ArrayList<>();
			for (Schedule schedule : schedules) {
				String tempSchedule = null;
				try {
					tempSchedule = schedule.getDate().split("\\.")[0];
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (schedule.getDay().equals("radni dan")) {
					radni_danList.add(tempSchedule);
				} else if (schedule.getDay().equals("subota")) {
					subotaList.add(tempSchedule);
				} else {
					nedeljaList.add(tempSchedule);
				}
			}

			Collections.sort(radni_danList, new scheduleComparator());
			Collections.sort(subotaList, new scheduleComparator());
			Collections.sort(nedeljaList, new scheduleComparator());

			try {
				ArrayAdapter<String> adapter;
				adapter = new ArrayAdapter<String>(this, R.layout.list_item_schedule, radni_danList);
				radni_dan.setAdapter(adapter);
				adapter = new ArrayAdapter<String>(this, R.layout.list_item_schedule, subotaList);
				subota.setAdapter(adapter);
				adapter = new ArrayAdapter<String>(this, R.layout.list_item_schedule, nedeljaList);
				nedelja.setAdapter(adapter);

			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.i("", "");
		}

		class station_routeComparator implements Comparator<Station_route> {

			@Override
			public int compare(Station_route o1, Station_route o2) {

				return o1.getStationNumber() < o2.getStationNumber() ? - 1 : o1.getStationNumber()
						== o2.getStationNumber() ? 0 : 1;
			}
		}

		class scheduleComparator implements Comparator<String> {

			@Override
			public int compare(String o1, String o2) {
				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
				int compare = 0;
				try {
					Date o1Date = format.parse(o1);
					Date o2Date = format.parse(o2);
					compare = o1Date.compareTo(o2Date);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				return compare;
			}
		}
}

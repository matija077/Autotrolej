package com.example.matija077.autotrolej;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class parseScheduleDataIntentService extends IntentService {

	OrmLiteDatabaseHelper db;


	public parseScheduleDataIntentService() {
		super("parseScheduleDataIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			List<String> urlList = null;
			urlList = new ArrayList<String>(intent.getStringArrayListExtra("urlList"));
			//List<String> data = null;
			Boolean running = TRUE;
			Type collectiontype = new TypeToken<List<jsonObject>>(){}.getType();
			List<Collection<jsonObject>> data= new ArrayList<Collection<jsonObject>>();

			if (urlList.size() <= 0) running = FALSE;

			if (running == TRUE) {
				HttpURLConnection connection = null;
				BufferedReader reader = null;
				//data = new ArrayList<String>();

				for (int i = 0; i < urlList.size(); i++) {
					try {
						URL url = new URL(urlList.get(i));
						connection = (HttpURLConnection) url.openConnection();
						connection.connect();

						InputStream stream = connection.getInputStream();

						/*reader = new BufferedReader(new InputStreamReader(stream));

						StringBuilder buffer = new StringBuilder();
						buffer.ensureCapacity(52);
						String line = "";

						while ((line = reader.readLine()) != null) {
							try {
								buffer.append(line + "\n");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						Log.i("ae", String.valueOf(i));

						data.add(String.valueOf(buffer));
						buffer = null;
						line = null;
						reader.close();
						stream.close();*/
						try {
							Reader read = new InputStreamReader(stream);
							data.add((Collection<jsonObject>) new Gson().fromJson(read, collectiontype));
							//jsonObject result = new Gson().fromJson(read, jsonObject.class);
							//JsonObject jsonObject = (JsonObject) new JsonParser().parse(new InputStreamReader(stream));
						} catch (Exception e) {
							e.printStackTrace();
						}
							Gson gson = new GsonBuilder().create();


					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (connection != null) {
							connection.disconnect();
						}
					}
				}
			} else {
			}
			urlList = null;
			if (data != null) {
				db = new OrmLiteDatabaseHelper(getApplicationContext());
				for (int j = 0; j < data.size(); j++) {
					HashSet<Schedule> schedules = new HashSet<Schedule>();
					HashMap<String, Station> stations = new HashMap<String, Station>();
					HashMap<String, Route> routes = new HashMap<String, Route>();
					HashMap<String, Station_route> station_routes = new HashMap<>();
					//HashSet<Station_route> station_routes = new HashSet<>();
					try {
						JSONArray jsonArray = null;
						//Type listType1 = new TypeToken<List<Collection<jsonObject>>>() {}.getType();
						//Type listType2 = new TypeToken<JSONArray>() {}.getType();
						Gson gson = new Gson();
						try {
							String json = gson.toJson(data.get(0));
							//jsonArray = gson.fromJson(json, listType2);
							jsonArray = new JSONArray(json);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							data.get(0).clear();
							gson = null;
						}
						String day;
						/*if (j == 0) {
							day = "radni dan";
						} else if (j == 1) {
							day = "subota";
						} else {
							day = "nedelja";
						}*/
						day = "radni dan";

						for (int i = 0; i < jsonArray.length(); i++) {

							JSONObject jsonObject = (JSONObject) jsonArray.get(i);
							List<String> parsedLinVarId = new ArrayList<String>();

							try {
								parsedLinVarId = Arrays.asList(jsonObject.getString("LinVarId").
										split("-"));
							} catch (JSONException e) {
								e.printStackTrace();
							}

							if (parsedLinVarId.size() != 3) {
								continue;
							} else {
								//
								String id = jsonObject.getString("StanicaId");
								Station station = null;
								station = db.queryStation_specific1(Integer.valueOf(id));
								if (station == null) {
									if (!stations.containsKey(id)) {
										String name = jsonObject.getString("Naziv");
										String gpsx = jsonObject.getString("GpsX");
										String gpsy = jsonObject.getString("GpsY");

										station = new Station(id, name, gpsx, gpsy, "1");
										stations.put(id, station);

										name = null;
										gpsx = null;
										gpsy = null;
									} else {
										station = stations.get(id);
									}
								}

								//
								String directionA = null;
								String directionB = null;
							/* 	LinVarId looks like this -> "route-direction-version" so we need
								route for routes and direction for directionA/B, variant is needed
							 	later
							*/
								String routeMarkName = jsonObject.getString("LinVarId").split("-")[0].
										concat("-").concat(jsonObject.getString("LinVarId").split("-")[2]);
								String routeMarkDirection = jsonObject.getString("LinVarId")
										.split("-")[1];
								Route route = null;
								route = db.queryRoot_specific1(routeMarkName);
								if (route == null) {
									if (!routes.containsKey(routeMarkName)) {
										String category = null;
										String temp = jsonObject.getString("Smjer");
										if (temp.equals(routeMarkDirection)) {
											/*	because of JSON is the way it is we will first add
												one direction for our route.
											*/
											if (jsonObject.getString("Smjer").equals("A")) {
												directionA = jsonObject.getString("NazivVarijanteLinije");
											} else {
												directionB = jsonObject.getString("NazivVarijanteLinije");
											}
										}

										String[] categorySplit = routeMarkName.split("(?=\\D)(?<=\\d)");
										if (categorySplit[0].equals("KBC")) {
											continue;
										}
										int routeMarkNumber;
										try {
											routeMarkNumber = Integer.parseInt(categorySplit[0]);
										} catch (Exception e) {
											e.printStackTrace();
											continue;
										}

										if ((routeMarkNumber <= 9) || (routeMarkNumber == 13)) {
											category = "city";
										} else if (routeMarkNumber < 100) {
											category = "suburb";
										} else {
											category = "night";
										}

										route = new Route(routeMarkName, directionA, directionB,
												category);
										routes.put(routeMarkName, route);
									} else {
										route = routes.get(routeMarkName);
										directionA = route.getDirectionA();
										directionB = route.getDirectionB();
										if ((directionA == null) && (jsonObject.getString("Smjer")
												.equals("A"))) {
											route.setDirectionA(jsonObject
													.getString("NazivVarijanteLinije"));
											routes.put(routeMarkName, route);
										} else if ((directionB == null) && (jsonObject.getString("Smjer")
												.equals("B"))) {
											route.setDirectionB(jsonObject.
													getString("NazivVarijanteLinije"));
											routes.put(routeMarkName, route);
										}
									}
								}

								//first we check for existing routeMarks in our helper routeMark list.
								//if it doesn't we add it.


								/*	for category we need to split our routeMarkName -> "INTCHAR"
									into "INT" and "CHAR" because all "INT" between 1 and 9
									including 13 are city buses, those under 100 are suburb buses
									and 10* are night buses. This is for now.
								*/
								/*
									\D matches all non-digit characters, while \d matches all
									digit characters. ?<= is a positive lookbehind
									(so everything before the current position is asserted to be a
									digit character), ?= is a positive lookahead
									(so everything after the current position is asserted as a
									non-digit character).
								*/



								Station_route station_route = null;
								String routeMark = parsedLinVarId.get(0).concat("-").concat
										(parsedLinVarId.get(2));
								station_route = db.queryStation_route_specific1(id, routeMark,
										routeMarkDirection.charAt(0), TRUE);
								if (station_route == null) {
									/*Log.e("Error", routeMark.concat("-".concat(jsonObject.
											getString("StanicaId"))));
									continue;*/
										Boolean turnAroundStation = FALSE;
										String stationNumber = jsonObject.getString("RedniBrojStanice");
										if (route != null && station != null && routeMarkDirection != null &&
												stationNumber != null) {
											try {
												/*station_route = new Station_route(station,
														route, routeMarkDirection.charAt(0), turnAroundStation,
														stationNumber);*/
											/*
											we want to add in db our station_route and than query it
											when checking
											*/
												String station_routeKey = routeMark.concat("-").
														concat(routeMarkDirection).concat("-").
														concat(id);
												if (!station_routes.containsKey(station_routeKey)) {
												//if (! station_routes.contains(station_route)) {
													station_route = new Station_route(station,
														route, routeMarkDirection.charAt(0), turnAroundStation,
														stationNumber);
													station_routes.put(station_routeKey, station_route);
												} else {
													//Log.d("tag", String.valueOf(station_route));
													station_route = station_routes.get(station_routeKey);
												}
												if (MapsActivity.DebugOn) {
													Log.i("Route station", String.valueOf(i)
															.concat(String.valueOf(station_route)));
												}
											} catch (Exception e) {
												e.printStackTrace();
											}
										} else {
											Log.e(TAG, String.valueOf(i));
										}
									/*} else {
										routeMark = null;
									}*/
								} else {
									routeMark = null;
								}
								String date = jsonObject.getString("Polazak");
								if (station_route != null && date != null && day != null ) {
									Schedule schedule = new Schedule(station_route, date, day);
									if (schedules.contains(schedule)) {
										Log.e("Eorro schedule exists ", String.valueOf(schedule));
										continue;
									}
									schedules.add(schedule);
									Log.i("insertSchedule - ", String.valueOf(i));
								}
								station_route = null;

							}
						}
					} catch (JSONException e) {
							e.printStackTrace();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
						try {
							db.insertStation(stations);
							db.insertRoute(routes);
							db.insertStation_route(station_routes);
							db.insertSchedule(schedules);
						} catch (Exception e) {
							e.printStackTrace();
						}
						finally {
							stations.clear();
							routes.clear();
							station_routes.clear();
							schedules.clear();
						}
				}
			}
		}
	}

	private class jsonObject {
		@SerializedName("LinVarId")
		private String LinVarId;

		@SerializedName("StanicaId")
		private String id;

		@SerializedName("Naziv")
		private String Name;

		@SerializedName("GpsX")
		private String gpsx;

		@SerializedName("GpsY")
		private String gpsy;

		@SerializedName("Smjer")
		private String direction;

		@SerializedName("NazivVarijanteLinije")
		private  String directionName;

		@SerializedName("RedniBrojStanice")
		private String stationNumber;

		@SerializedName("Polazak")
		private String date;
	}
}

package com.example.matija077.autotrolej;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class parseDataIntentService extends IntentService {

	OrmLiteDatabaseHelper db;


	public parseDataIntentService() {
		super("parseDataIntentService");
	}

	/**
	 * Starts this service to perform action Foo with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 *
	 * @see IntentService
	 */

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean check = isConnected(getApplicationContext());
		if (intent != null) {
			List<String> urlList = null;
			urlList = new ArrayList<String>(intent.getStringArrayListExtra("urlList"));
			List<String> data = null;
			Boolean running = TRUE;

			if (urlList.size() <= 0) running = FALSE;

			if (running == TRUE) {
				HttpURLConnection connection = null;
				BufferedReader reader = null;
				data = new ArrayList<String>();

				for (int i = 0; i < urlList.size(); i++) {
					try {
						URL url = new URL(urlList.get(i));
						connection = (HttpURLConnection) url.openConnection();
						connection.connect();

						InputStream stream = connection.getInputStream();

						reader = new BufferedReader(new InputStreamReader(stream));

						StringBuffer buffer = new StringBuffer();
						String line = "";

						while ((line = reader.readLine()) != null) {
							buffer.append(line + "\n");
						}

						//	testing poor internet conditions
				/*
				int loopCounter = 0;
				line = reader.readLine();
				while ((line != null)) {
					buffer.append(line + "\n");
					loopCounter++;
					try {
						line = reader.readLine();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				*/

						data.add(String.valueOf(buffer));
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

			//Thread waits for Debugger to be attached to its process.
			android.os.Debug.waitForDebugger();
			List<Station> stations = new ArrayList<Station>();
			List<Route> routes = new ArrayList<Route>();
			List<Station_route> station_routes = new ArrayList<Station_route>();
			urlList = null;

			if (data != null) {
				if (data.get(0) != null) {
					try {
						JSONArray jsonArray = new JSONArray(data.get(0));
						for (int i = 0; i < jsonArray.length(); i++) {

							JSONObject jsonObject = jsonArray.getJSONObject(i);

							String id = jsonObject.getString("StanicaId");
							String name = jsonObject.getString("Naziv");
							String gpsx = jsonObject.getString("GpsX");
							String gpsy = jsonObject.getString("GpsY");

							Station station = new Station(id, name, gpsx, gpsy, "1");
							stations.add(station);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					db  = new OrmLiteDatabaseHelper(getApplicationContext());
					insertStations(stations);
				}

				if (data.get(1) != null) {
					JSONArray jsonArray = null;
					try {
						jsonArray = new JSONArray(data.get(1));
						data = null;
					} catch (JSONException e) {
						e.printStackTrace();
					}
					//	IMPORTANT: USE STRING.EQUALS(STRING) FOR STRING COMPARISION.
					try {
						List<String> routeMarkListExisting = new ArrayList<String>();
						//our error array just in case.
						List<String> routeErrors = new ArrayList<String>();

						//routes
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							String routeMark = null;
							String directionA = null;
							String directionB = null;
							String category = null;
							/* 	LinVarId looks like this -> "route-direction-version" so we need
								route for routes and direction for directionA/B, variant is needed
							 	later
							*/
							String routeMarkName = jsonObject.getString("LinVarId").split("-")[0].
									concat("-").concat(jsonObject.getString("LinVarId").split("-")[2]);
							String routeMarkDirection = jsonObject.getString("LinVarId")
									.split("-")[1];
							/*	Both "Smjer" and "routeMarkDirection" need to be the same. If not
								we populate error array with "LinVarId".
							*/

							//first we check for existing routeMarks in our helper routeMark list.
							//if it doesn't we add it.
							if (! routeMarkListExisting.contains(routeMarkName)) {
								//if JSON is not correct populate error array.
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
								} else {
									routeErrors.add(jsonObject.getString("LinVarId"));
								}

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

								Route route = new Route(routeMarkName, directionA, directionB,
										category);
								routes.add(route);
								routeMarkListExisting.add(routeMarkName);

								// if it contains the route we want to add the other direction
							} else {
								/*	using index of routeMarkName array because both arrays are
									populated at the same time
								*/
								int index = routeMarkListExisting.indexOf(routeMarkName);
								//	using setDirection methods without using additional memory.
								if ((directionA == null) && (jsonObject.getString("Smjer")
										.equals("A"))) {
									routes.get(index).setDirectionA(jsonObject
											.getString("NazivVarijanteLinije"));
								} else if ((directionB == null) && (jsonObject.getString("Smjer")
										.equals("B"))) {
									routes.get(index).setDirectionB(jsonObject.
											getString("NazivVarijanteLinije"));
								}
							}
						}
					} catch(Exception e) {

					}

					insertRoutes(routes);

					//release memory
					stations = null;
					routes = null;
					try {

						//route-lines actually
						for (int i = 0; i < jsonArray.length(); i++) {

							//defining in advance because of if statements.
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							//Log.d(TAG, String.valueOf(i));
							//Log.d(TAG, String.valueOf(jsonObject));
							List<String> parsedLinVarId = new ArrayList<String>();

							try {
								parsedLinVarId = Arrays.asList(jsonObject.getString("LinVarId").
										split("-"));
							} catch (JSONException e) {
								e.printStackTrace();
							}

							// check if LinVarId is not in correct form (3 parts)
							if (parsedLinVarId.size() != 3) {
								continue;
							} else {
								// for now only simple lines whose variant(index 2) is 0.
								//if (parsedLinVarId.get(2).equals("0")) {
									/*
										check if station_route already exists because of duplicates
										check route-direction-variant route part equals existing
										routeMark and StanicaId equals existing stanicaId.

										Lets implement query check for station_route. it is probably
										faster.
									*/
									/*
									int j = 0;
									Boolean station_route_exists = FALSE;
									for (j = 0; j < station_routes.size(); j++) {
										Station_route tmpSR = station_routes.get(j);
										if (parsedLinVarId.get(0).equals(tmpSR.getRoute().
												getRouteMark()) && jsonObject.getString("StanicaId")
												.equals(tmpSR.getStation().getStringId()) &&
												(parsedLinVarId.get(1).equals(tmpSR.getDirection()))
											) {
												//	we dont want to keep checking anymore.
												station_route_exists = TRUE;
												break;
										} else {
											if ((i == 1000) || (i>2500)) {
												Log.v(TAG, "outer" + String.valueOf(i) + "inner" +
														String.valueOf(j));
											}
										}
									}

										//	if station_route already exists continue to next object.
									if (station_route_exists == TRUE) {
										continue;
									}
									*/
									Station_route station_route = null;
									String routeMark = parsedLinVarId.get(0).concat("-").concat
											(parsedLinVarId.get(2));
									station_route = queryStation_route_specific1(jsonObject.
											getString("StanicaId"), routeMark,
											parsedLinVarId.get(1));
									if (station_route != null) {
										continue;
									}
									routeMark = null;


									/*
										Check if route exists in routes. if it does return
									 	Route object, if not go to the next object in JSON. Also
									 	check for direction
									*/
									//j = 0;
									Route route = null;
									Character direction = null;
									String stationRouteName = jsonObject.getString
											("NazivVarijanteLinije");

									/*
										NEW ROUTE CHECK. QUERY DB WITHOUT LOOPS TO PRESERVE MEMORY.
										Query for directionA or directionB and routeMark all at
										once. Bellow is optimization attempt that needs more work.
									*/
									/*
										for optimization TODO: check todo in OrmLiteDatabaseHelper
									*/
									/*
									String[] tempColumnNames = new String[] {"routeMark",
											"directionA", "directionB"};
									String[] tempColumnValues = new String[] {parsedLinVarId.get(0),
											stationRouteName, stationRouteName};
									String[] tempConnectors = new String[] {"or", "or"};
									route = queryRoot(tempColumnNames, tempColumnValues,
											tempConnectors);
									*/

									route = queryRoot_specific1(parsedLinVarId.get(0).concat("-").
													concat(parsedLinVarId.get(2)), stationRouteName);
									if (route == null) {
										continue;
									} else {
										stationRouteName = null;
										direction = jsonObject.getString("Smjer").charAt(0);
									}
									//	old route check
									/*do {
										if (parsedLinVarId.get(0).equals(routes.get(j).
												getRouteMark())) {
											route = routes.get(j);
										} else {
											continue;
										}


										//	check for direction in route
										String stationRouteName = jsonObject.getString
												("NazivVarijanteLinije");
										if ((!stationRouteName.equals(routes.get(j).
												getDirectionA())) && (!stationRouteName.equals
												(routes.get(j).getDirectionB()))) {
											continue;
											//	trick for converting String to Character is charAt.
										} else {
											direction = jsonObject.getString("Smjer").charAt(0);
										}
										j++;
									} while (j < routes.size() && route == null);
								*/



									/*
										Check if station exists in routes. if it does return
									 	Station object, if not go to the next object in JSON.

									 	Now we wil try to query it instead of using stations array.
									*/
									/*
									for (j= 0; j < stations.size(); j++) {
										if (jsonObject.getString("StanicaId").equals(
												stations.get(j).getStringId())) {
											station = stations.get(j);
											break;
										} else {
											continue;
										}
									}
									*/
									Station station = null;
									String stanicaId = jsonObject.getString("StanicaId");
									station = queryStation_specific1(stanicaId);
									//stations = db.getAllStations();
									if (station == null) {
										continue;
									} else {
										stanicaId = null;
									}



									/*
										for now turnArundStation is empty, we proceed to
										stationNumber
									*/
									Boolean turnAroundStation = FALSE;
									String stationNumber = jsonObject.getString("RedniBrojStanice");
									if (route != null && station != null && direction != null &&
											stationNumber != null) {
										try {
											station_route = new Station_route(station,
													route, direction, turnAroundStation,
													stationNumber);
											/*
											we want to add in db our station_route and than query it
											when checking
											*/
											//station_routes.add(station_route);
											insertStationRoute(station_route);
											Log.i("Route station", String.valueOf(i)
													.concat(String.valueOf(station_route)));
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else {
										Log.e(TAG, String.valueOf(i));
									}
								}
							//}
						}

						/*
							30 days is expiration value for parsing all lines, stations and
							station routes. To know when to do it we save a 30 days ahead date
							in shared preferences.
						*/
						station_routes = db.getAllStation_routes();
						db.close();
						//	garbage collector.
						jsonArray = null;
						/*
							we want to save 1 month ahead time in shared preferences for any updates
						*/
						SharedPreferences sharedPreferences = getApplicationContext().
								getSharedPreferences(MapsActivity.preferenceName,
										Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						Calendar calendar = Calendar.getInstance();
						DateFormat dateFormat = new java.text.SimpleDateFormat("dd MM yyyy, HH:mm");
						calendar.add(Calendar.HOUR, 24*30);
						String time = dateFormat.format(calendar.getTime());
						editor.putString(MapsActivity.linesExpireKey, time);
						editor.commit();
					} catch (JSONException e) {
						e.printStackTrace();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void insertStations(List<Station> stations) {
		for (int i = 0; i < stations.size(); i++) {
			db.insertStation(stations.get(i));
		}
	}

	private void insertRoutes(List<Route> routes) {
		for (int i = 0; i < routes.size(); i++) {
			db.insertRoute(routes.get(i));
		}
	}

	private void insertStationRoute(Station_route station_route) {
			db.insertStation_route(station_route);
	}

	/*
		optimization part which we will go back to when we have time.
	*/
	/*
	private Route queryRoot(String[] columnNames, String[] params, String[] connectors) {
		Route route = db.queryRoute(columnNames, params, connectors);
		return route;
	}
	*/

	/*
		specific query that is explained in OrmLiteDatabaseHelper class.
	*/
	private Route queryRoot_specific1(String routeMarkValue, String directionValue) {
		Route route = db.queryRoot_specific1(routeMarkValue, directionValue);
		return route;
	}

	private Station queryStation_specific1(String id){
		Station station = db.queryStation_specific1(Integer.valueOf(id));
		return station;
	}

	private Station_route queryStation_route_specific1(String stanicaId, String routeMarkValue,
													   String direction) {
		Station_route station_route = db.queryStation_route_specific1(stanicaId, routeMarkValue,
				direction.charAt(0));
		return station_route;
	}

	private List<Station_route> queryStation_route_specific2(String routeMarkValue) {
		List<Station_route> station_routes = db.queryStation_route_specific2(routeMarkValue);
		return station_routes;
	}

	private static boolean isConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			try {
				URL url = new URL("http://www.google.com/");
				HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
				/*
				urlc.setRequestProperty("User-Agent", "test");
				urlc.setRequestProperty("Connection", "close");
				urlc.setConnectTimeout(1000); // mTimeout is in seconds
				*/
				urlc.connect();
				if (urlc.getResponseCode() == 200) {
					return true;
				} else {
					return false;
				}
			} catch (IOException e) {
				Log.i("warning", "Error checking internet connection", e);
				return false;
			} catch (Exception e) {
				Log.e("error","error", e);
			}
		}

		return false;

	}


}

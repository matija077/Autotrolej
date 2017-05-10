package com.example.matija077.autotrolej;

import android.app.ProgressDialog;
import android.nfc.Tag;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.content.ContentValues.TAG;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by Matija077 on 4/9/2017.
 */

public class autotrolej {

    private Boolean ready = false;

    /*public List<String> getResponse() {
        List<String> data = null;

        if (ready == FALSE) {
            data = new ArrayList<String>();
            data.add("Error task not finished or task not initiated");
            return data;
        }

        try {
            data = getResponse();
        } catch (Error e) {
            e.printStackTrace();
            data.add("Error retreiving");
            return data;
        } finally {
            data.add("OK");
            return data;
        }
    }*/

    public interface asyncResponse {
        void processfinish(List<Station> stations, List<Route> routes,  List<Station_route>
				stationRoutes);
    }

    public static class jsonTask extends AsyncTask {

        asyncResponse delegate = null;
        List<String> data;
        List<String> urlList = null;
        Boolean running = TRUE;

        public jsonTask(List<String> list) {
            this.urlList = list;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (urlList.size() <= 0) running = FALSE;
        }

        @Override
        protected Object doInBackground(Object[] params) {
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
                return null;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
			//Thread waits for Debugger to be attached to its process.
			android.os.Debug.waitForDebugger();

            super.onPostExecute(o);
			List<Station> stations = new ArrayList<Station>();
			List<Route> routes = new ArrayList<Route>();
			List<Station_route> station_routes = new ArrayList<Station_route>();

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
				}

				if (data.get(1) != null) {
					//	IMPORTANT: USE STRING.EQUALS(STRING) FOR STRING COMPARISION.
					try {
						JSONArray jsonArray = new JSONArray(data.get(1));
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
							String routeMarkName= jsonObject.getString("LinVarId").split("-")[0];
							String routeMarkDirection = jsonObject.getString("LinVarId")
									.split("-")[1];
							/*	Both "Smjer" and "routeMarkDirection" need to be the same. If not
								we populate error array with "LinVarId".
							*/

							//first we check for existing routeMarks in our helper routeMark list.
							//if it doesn't we add it.
							if (!routeMarkListExisting.contains(routeMarkName)) {
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
									and 10* are night buses. This is for now
									TODO: get this complitly right.
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

						//route-lines actually
						for (int i = 0; i < jsonArray.length(); i++) {

							//defining in advance because of if statements.
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							//Log.d(TAG, String.valueOf(i));
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
								//TODO: add all lines
								if (parsedLinVarId.get(2).equals("0")) {
									/*
										check if station_route already exists because of duplicates
										check route-direction-variant route part equals existing
										routeMark and StanicaId equals existing stanicaId.
									*/
									int j = 0;
									Boolean station_route_exists = FALSE;
									for (j = 0; j < station_routes.size(); j++) {
										if (parsedLinVarId.get(0).equals(station_routes.get(j).
												getRoute().getRouteMark()) && jsonObject.
												getString("StanicaId").equals(station_routes.get(j).
												getStation().getStringId())) {
											//	we dont want to check anymore.
											station_route_exists = TRUE;
											break;

										}
									}
									//	if station_route already exists continue to next object.
									if (station_route_exists == TRUE) {
										continue;
									}

									/*
										Check if route exists in routes. if it does return
									 	Route object, if not go to the next object in JSON. Also
									 	check for direction
									*/
									j = 0;
									Route route = null;
									Character direction = null;
									do {
										if (parsedLinVarId.get(0).equals(routes.get(j).
												getRouteMark())) {
											route = routes.get(j);
										} else {
											continue;
										}

										/*
											check for direction in route
										 */
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

									/*
										Check if station exists in routes. if it does return
									 	Station object, if not go to the next object in JSON.
									*/
									Station station = null;
									for (j= 0; j < stations.size(); j++) {
										if (jsonObject.getString("StanicaId").equals(
												stations.get(j).getStringId())) {
											station = stations.get(j);
											break;
										} else {
											continue;
										}
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
											Station_route station_route = new Station_route(station,
													route, direction, turnAroundStation,
													stationNumber);
											station_routes.add(station_route);
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else {
										Log.e(TAG, String.valueOf(i));
									}
								}
							}
						}
						routeMarkListExisting.add("end");
					} catch (JSONException e) {
						e.printStackTrace();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
            delegate.processfinish(stations, routes, station_routes);
        }
    }
}

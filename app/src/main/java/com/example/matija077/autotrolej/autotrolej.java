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
import java.util.List;
import java.util.Objects;

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
        void processfinish(List<Station> stations, List<Route> routes);
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
									TODO: get this completly right.
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
								int routeMarkNumber = Integer.parseInt(categorySplit[0]);
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
						/*for (int i = 0; i < jsonArray.length(); i++) {

							//defining in advance because of if statements.
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							String routeMark = null;
							String directionA = null;
							String directionB = null;
							String category = null;

							//first we check for existing routeMarks in our helper routeMark list.
							//if it doesn't we add it.
							if (!routeMarkListExisting.contains(jsonObject.getString("LinVarId"))) {
								routeMark = jsonObject.getString("LinVarId");
								//because of JSON is the way it is we will first add one direction
								//for our route.
								if (jsonObject.getString("Smjer") == "A") {
									directionA = jsonObject.getString("NazivVarijanteLinije");
								} else {
									directionB = jsonObject.getString("NazivVarijanteLinije");
								}
								//currently can't add category
								//String category = jsonObject.getString("");

								Route route = new Route(routeMark, directionA, directionB,
										category);
								routes.add(route);
								routeMarkListExisting.add(routeMark);
							// if it contains the route we want to add the other direction
							} else {
								if ((directionA == null) && (jsonObject.getString("Smjer")
										== "A")) {
									directionA = jsonObject.getString("NazivVarijanteLinije");
								} else if ((directionB == null) && (jsonObject.getString("Smjer")
										== "B")) {
									directionB = jsonObject.getString("NazivVarijanteLinije");
								}
							}
						}*/
						routeMarkListExisting.add("end");
					} catch (JSONException e) {
						e.printStackTrace();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
            delegate.processfinish(stations, routes);
        }
    }
}

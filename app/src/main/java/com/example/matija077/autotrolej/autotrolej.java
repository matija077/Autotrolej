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
        void processfinish(List list);
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
            delegate.processfinish(stations);
        }
    }
}

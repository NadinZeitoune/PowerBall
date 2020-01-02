package com.example.powerball_results;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpConnection {

    public static String BASE_URL = "https://data.ny.gov/resource/d6yy-54nr.json";
    public static final int ERROR = 404;

    public static Object connection(String action, String filter) {
        URL url = null;
        InputStream inputStream = null;
        HttpURLConnection connection = null;

        try {
            // Get basic connection & Choose the filter / all.
            switch (action) {
                case "all":
                    url = new URL(BASE_URL);
                    break;
                case "date":
                    url = new URL(BASE_URL + "?draw_date=" + filter);
                    break;
                case "limit":
                    url = new URL(BASE_URL + "?$limit=" + filter);
                    break;
            }

            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setRequestMethod("GET");
            connection.connect();

            connection.getResponseCode();
            inputStream = connection.getErrorStream();

            if (inputStream == null) {
                inputStream = connection.getInputStream();
            }

            return getJsonFromInput(inputStream);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    private static JSONArray getJsonFromInput(InputStream inputStream) throws IOException {
        JSONArray resultsJson = null;

        String response = readStringFromInput(inputStream);

        if (response.length() == 0 || response == String.valueOf(ERROR)) {
            return resultsJson;
        }

        try {
            resultsJson = new JSONArray(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultsJson;
    }

    // Assistance method.
    private static String readStringFromInput(InputStream inputStream) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();

        byte[] buffer = new byte[64];
        int actuallyRead;

        while ((actuallyRead = inputStream.read(buffer)) != -1) {
            stringBuilder.append(new String(buffer, 0, actuallyRead));
        }

        if (stringBuilder.toString().getBytes().length < 4)
            return String.valueOf(ERROR);
        return stringBuilder.toString();

    }
}

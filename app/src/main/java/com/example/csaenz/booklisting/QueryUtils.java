package com.example.csaenz.booklisting;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
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
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by csaenz on 4/3/2017.
 */

public class QueryUtils {

    /** Tag for the log messages */
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private QueryUtils() {
    }

    /**
     *  Verify network connectivity
     */
    public static boolean isConnected(Context context){
        //verify network state
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)){
                return false;
        }else return true;
    }

    //TODO: Need to configure with textView input

    /**
     * Returns new URL object from the given string URL.
     */
    public static URL createUrl(String stringUrl) {
        URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    public static String makeHttpRequest(URL url) throws IOException {

        String jsonResponse = "";

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }

        } catch (IOException e) {
            // Handle the exception
            Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    public static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing a JSON response.
     */
    public static ArrayList<Book> extractFromJson(String json_string) {

        // Create an empty ArrayList that we can start adding earthquakes to
        ArrayList<Book> books = new ArrayList<>();
        // Try to parse the JSON Response String. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        try {
            // build up a list of books objects with the corresponding data.
            JSONObject rootObject = new JSONObject(json_string);

            //get Array with all required data
            JSONArray jsonArray = rootObject.getJSONArray("items");

            //parse through Array and extract needed data
            for(int i = 0; i < jsonArray.length(); i++){
                //grab object of seismic data within the i'th place of Array
                JSONObject itemObject = jsonArray.getJSONObject(i);

                JSONObject volumeInfoObject = itemObject.getJSONObject("volumeInfo");

                String title = volumeInfoObject.getString("title");

                JSONArray JSONAuthArray = volumeInfoObject.getJSONArray("authors");

                String allAuthors = "";

                for(int x = 0; x < JSONAuthArray.length(); x++){
                    String author = JSONAuthArray.getString(i);
                    allAuthors.concat(author + ",");
                }

                allAuthors = allAuthors.substring(0, allAuthors.length() - 1);

                //add data to ArrayList
                books.add(new Book(title, allAuthors));
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return books;
    }
}


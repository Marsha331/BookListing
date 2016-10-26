package net.swallowsnest.booklisting;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;
import java.util.StringTokenizer;

import static android.R.attr.button;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getName();
    /**
     * URL for book data from the Google Site
     */
    private static final String BOOK_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    private BookAdapter mAdapter;

    private EditText searchText;
    private String userInput;

    //textview for when data is empty//
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Find a reference to the {@link ListView} in the layout
        ListView bookListView = (ListView) findViewById(R.id.list);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty);
        bookListView.setEmptyView(mEmptyStateTextView);

        mAdapter = new BookAdapter(this, new ArrayList<Book>());
        bookListView.setAdapter(mAdapter);

        Button searchButton = (Button) findViewById(R.id.search_button);
        final EditText searchBar = (EditText) findViewById(R.id.search_bar);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchBar.getText().toString().trim().equals("")) {
                    searchBar.setError(getString(R.string.no_input));
                } else {
                    searchText = (EditText) findViewById(R.id.search_bar);
                    userInput = searchText.getText().toString().replace(" ", "+");
                    BookAsyncTask task = new BookAsyncTask();

                   ConnectivityManager connMgr = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);

                    //get info on network
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    //if there is a network connection, fetch data

                    if (networkInfo != null && networkInfo.isConnected()) {
                        task.execute();
                        mAdapter.clear();
                    } else {
                        //set empty state text to say no internet connection
                        mEmptyStateTextView.setText(R.string.no_internet);
                    }
                }
            }
        });
    }
    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    private class BookAsyncTask extends AsyncTask<URL, Void, List<Book>> {

        @Override
        protected List<Book> doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(BOOK_REQUEST_URL + userInput + "&maxresults=10");

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
                Log.e(LOG_TAG, "Error with creating URL", e);
                return null;
            }

            // Extract relevant fields from the JSON response and create an {@link Event} object
            List<Book> books = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link BookAsyncTask}
            return books;
        }

        private void updateUi(List<Book> book) {
            mAdapter.clear();
            mAdapter.addAll(book);
            mAdapter.notifyDataSetChanged();

        }

        /**
         * Update the screen with the given earthquake (which was the result of the
         * {@link BookAsyncTask}).
         */
        @Override
        protected void onPostExecute(List<Book> books) {
        if (books == null) {
                return;
           } else {
             updateUi(books);
            }
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
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
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } catch (IOException e) {
                // TODO: Handle the exception
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
        private String readFromStream(InputStream inputStream) throws IOException {
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

        List<Book> books = new ArrayList<>();
        /**
         * Return an {@link Book} object by parsing out information
         * about the first book from the input booksJSON string.
         */
        private List<Book> extractFeatureFromJson(String booksJSON) {
            try {
                JSONObject baseJsonResponse = new JSONObject(booksJSON);
                JSONArray itemArray = baseJsonResponse.getJSONArray("items");

                // If there are results in the features array
                if (itemArray.length() > 0);
                for (int i = 0; i < itemArray.length(); i++){
                    // Extract out the first feature (which is a book)
                    JSONObject firstFeature = itemArray.getJSONObject(0);
                    JSONObject volumeInfo = firstFeature.getJSONObject("volumeInfo");

                    // Extract out the title values
                    String title = volumeInfo.getString("title");

                    // Extract out the author values
                    String author = volumeInfo.getString("authors");

                    // Create a new {@link Event} object
                    books.add(new Book(title, author));
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the Book JSON results", e);
            }
            return books;
        }
    }

}


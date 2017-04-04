package com.example.csaenz.booklisting;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.app.LoaderManager;
import android.content.Loader;

import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>>{

    /**
     * All Global variables and Bindings
     */
    private static final int BOOK_LOADER_ID = 0;

    public static final String LOG_TAG = MainActivity.class.getName();

    private static final String BOOKS_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=android&maxResults=1";

    private Context mContext;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.search_button)
    ImageButton mSearchButton;

    @BindView(R.id.text_view_empty)
    TextView mTextView;

    @BindView(R.id.progress_spinner)
    ProgressBar mProgress_spinner;

    /**
     * MainActivity related methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mContext = MainActivity.this;

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new BookAsyncTask().execute(BOOKS_REQUEST_URL);
            }
        });
    }

    /**
     * Helper methods
     */
    public void showSpinner(){
        mProgress_spinner.setVisibility(View.VISIBLE);
        mTextView.setVisibility(View.GONE);
    }

    public void hideSpinner(){
        mProgress_spinner.setVisibility(View.GONE);
    }

    /**
     * Menu related methods
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Snackbar.make(mSearchButton, "Delete", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> data) {

    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {

    }

    public class BookAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            publishProgress();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            URL url = QueryUtils.createUrl(params[0]);
            Log.i("Background","URL = s" + url);

            String jsonResponse = "";

            try {
                jsonResponse = QueryUtils.makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
                jsonResponse = "Error!!!!!";
            }
            finally {
                Log.i("Background","jsonResponse: " + jsonResponse);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            showSpinner();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideSpinner();
        }
    }
}



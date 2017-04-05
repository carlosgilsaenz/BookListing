package com.example.csaenz.booklisting;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.BinderThread;
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
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity{

    /**
     * All Global variables and Bindings
     */
    public static final String LOG_TAG = MainActivity.class.getName();

    private static final String BOOKS_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=android&maxResults=1";

    private Context mContext;

    private boolean mAsyncIsRunning;

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

        mAsyncIsRunning = false;

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mAsyncIsRunning){
                    new BookAsyncTask().execute();
                } else{
                    Toast.makeText(mContext,"STILL PROCESSING", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Helper methods
     */
    public void startBackground(){
        mProgress_spinner.setVisibility(View.VISIBLE);
        mTextView.setVisibility(View.GONE);
        mAsyncIsRunning = true;
    }

    public void resultsFailed(){
        mProgress_spinner.setVisibility(View.GONE);
        mTextView.setVisibility(View.VISIBLE);
        mAsyncIsRunning = false;
    }

    public void resultsSuccesssful(){
        mProgress_spinner.setVisibility(View.GONE);
        mTextView.setVisibility(View.GONE);
        mAsyncIsRunning = false;
    }

    public void showTextView(){mTextView.setVisibility(View.VISIBLE);}

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

    /**
     *  Async inner class
     */

    public class BookAsyncTask extends AsyncTask<Void, Void, Integer>{

        @Override
        protected Integer doInBackground(Void... params) {
            //  Update user by displaying progressbar spinner
            publishProgress();

            //  Method to check Network Connectivity
            if(!QueryUtils.isConnected(mContext)){return 0;}

            //  Create URL with provided String
            URL url = QueryUtils.createUrl(BOOKS_REQUEST_URL);

            //  Verify URL creation was successful
            if(url == null){return 1;}

            // Variable to use for HttpRequest
            String jsonResponse = "";

            try {
                jsonResponse = QueryUtils.makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
                return 2;
            }

            // Verify jsonResponse is not empty
            if(jsonResponse.isEmpty() || jsonResponse.equals("")){return 2;}


            // Extract relevant fields from the JSON response and create an {@link Event} object
            List<Book> books = QueryUtils.extractFromJson(jsonResponse);

            if(books.isEmpty()){return 3;}
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            startBackground();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            switch(integer){
                case 0:
                    Toast.makeText(mContext,"No Internet connection",Toast.LENGTH_SHORT).show();
                    resultsFailed();
                    break;
                case 1:
                    Toast.makeText(mContext,"Error with URL creation",Toast.LENGTH_SHORT).show();
                    resultsFailed();
                    break;
                case 2:
                    Toast.makeText(mContext,"Problem connecting to server",Toast.LENGTH_SHORT).show();
                    resultsFailed();
                    break;
                case 3:
                    Toast.makeText(mContext,"Problem with parsing data",Toast.LENGTH_SHORT).show();
                    resultsFailed();
                    break;
            }
        }
    }
}



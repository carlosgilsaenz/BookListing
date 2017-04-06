package com.example.csaenz.booklisting;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>>{

    //  All result variables used to determine outcome
    private static final int RESULTS_ON_CREATE = 98;

    private static final int RESULT_DEFAULT = 99;

    private static final int RESULTS_NO_INTERNET = 0;

    private static final int RESULTS_BAD_URL = 1;

    private static final int RESULTS_NO_SERVER_CONNECT = 2;

    private static final int RESULTS_BAD_DATA_PARSE = 3;

    private static int mResults;

    //  Loader and Loader Manager related
    private static final int BOOK_LOADER_ID = 0;

    private LoaderManager.LoaderCallbacks<List<Book>> mCallback;

    private static final String BOOKS_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?maxResults=10&q=";

    private static String mQueryString;

    //  UI and Main Activity related variables
    private Context mContext;

    private BookAdapter mAdapter;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.search_button)
    ImageButton mSearchButton;

    @BindView(R.id.text_view_empty)
    TextView mTextView;

    @BindView(R.id.progress_spinner)
    ProgressBar mProgress_spinner;

    @BindView(R.id.list_view)
    ListView mListView;

    @BindView(R.id.search_edit_text)
    EditText mEditText;

    /**
     * MainActivity related methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mAdapter = new BookAdapter(this, new ArrayList<Book>());

        mListView.setAdapter(mAdapter);

        //  Save Callback in order to configure with OnClickListener
        mCallback = this;

        //  Used to ensure Loader stays active after orientation change
        getLoaderManager().initLoader(BOOK_LOADER_ID, null, mCallback);

        //  Used to skip most of LoadOnBackground whenever screen re-creates
        mResults = RESULTS_ON_CREATE;

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Save contents of editText
                String editTextString = mEditText.getText().toString().trim();

                //  Verify contents is not blank
                if (editTextString.isEmpty() || editTextString.equals("")){
                    Toast.makeText(mContext,getString(R.string.empty_edit_text), Toast.LENGTH_SHORT).show();
                    return;
                }
                //  Verify Loader is not currently active
                if(getLoaderManager().getLoader(BOOK_LOADER_ID).isStarted()){
                    //  Save Text into Global variable to access later
                    mQueryString = editTextString;

                    //  Update user by displaying progressbar spinner
                    startProgressBar();

                    //  clear contents ensuring progress bar spinner does'nt stack onto list view
                    mAdapter.clear();

                    getLoaderManager().restartLoader(BOOK_LOADER_ID, null, mCallback);
                } else{

                    Toast.makeText(mContext,getString(R.string.progress_bar_msg), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Helper methods
     */
    public void startProgressBar(){
        mProgress_spinner.setVisibility(View.VISIBLE);
        mTextView.setVisibility(View.GONE);
        mResults = RESULT_DEFAULT;
    }

    public void resultsFailed(){
        mProgress_spinner.setVisibility(View.GONE);
        mTextView.setVisibility(View.VISIBLE);
    }

    public void resultsSuccessful(){
        mProgress_spinner.setVisibility(View.GONE);
        mTextView.setVisibility(View.GONE);
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

            //  clear adapter and update screen
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            resultsFailed();

            // Prompting for deletion
            Snackbar.make(mSearchButton, getString(R.string.action_delete), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *  LoadManager methods
     */
    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        return new BookLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> data) {

        int results = mResults;

        switch(results){
            case RESULTS_NO_INTERNET:
                Toast.makeText(mContext,getString(R.string.result_no_internet),Toast.LENGTH_SHORT).show();
                resultsFailed();
                break;
            case RESULTS_BAD_URL:
                Toast.makeText(mContext,getString(R.string.result_bad_url),Toast.LENGTH_SHORT).show();
                resultsFailed();
                break;
            case RESULTS_NO_SERVER_CONNECT:
                Toast.makeText(mContext,getString(R.string.result_no_server_connect),Toast.LENGTH_SHORT).show();
                resultsFailed();
                break;
            case RESULTS_BAD_DATA_PARSE:
                Toast.makeText(mContext,getString(R.string.result_bad_data_parse),Toast.LENGTH_SHORT).show();
                resultsFailed();
                break;
            default:
                if(data == null){
                    resultsFailed();
                }
                else {
                    mAdapter.clear();
                    mAdapter.addAll(data);
                    mAdapter.notifyDataSetChanged();
                    resultsSuccessful();
                }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        mAdapter.clear();
    }

    /**
     *  AsyncTask Loader
     */
    public static class BookLoader extends AsyncTaskLoader<List<Book>> {

        public BookLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public List<Book> loadInBackground() {

            // Allows quick exit from loadInBackground for onCreate()
            if(mResults == RESULTS_ON_CREATE){
                //  Ensure results are reset after onCreate()
                mResults = RESULT_DEFAULT;
                return null;
            }

            //  Method to check Network Connectivity
            if(!QueryUtils.isConnected(getContext())){
                mResults = RESULTS_NO_INTERNET;
                return null;
            }

            //  Create URL with provided String
            URL url = QueryUtils.createUrl(BOOKS_REQUEST_URL, mQueryString);

            //  Verify URL creation was successful
            if(url == null){
                mResults = RESULTS_BAD_URL;
                return null;}

            // Variable to use for HttpRequest
            String jsonResponse = "";

            try {
                jsonResponse = QueryUtils.makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
                mResults = RESULTS_NO_SERVER_CONNECT;
                return null;
            }

            // Verify jsonResponse is not empty
            if(jsonResponse.isEmpty() || jsonResponse.equals("")){
                mResults = RESULTS_NO_SERVER_CONNECT;
                return null;}

            // Extract relevant fields from the JSON response and create an {@link Event} object
            ArrayList<Book> mBooks = QueryUtils.extractFromJson(jsonResponse);

            // Verify books is'nt empty
            if(mBooks.isEmpty()){
                mResults = RESULTS_BAD_DATA_PARSE;
                return null;}

            return mBooks;
        }
    }
}



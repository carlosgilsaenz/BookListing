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

    /**
     * All Global variables and Bindings
     */
    private static final int RESULTS_ON_CREATE = 98;

    private static final int RESULT_DEFAULT = 99;

    private static final int BOOK_LOADER_ID = 0;

    private static final String BOOKS_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?maxResults=10&q=";

    private LoaderManager.LoaderCallbacks<List<Book>> mCallback;

    private Context mContext;

    private BookAdapter mAdapter;

    private static int mResults;

    private static String mQueryString;

    private boolean mAsyncIsRunning;

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

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mContext = MainActivity.this;

        mAsyncIsRunning = false;

        mCallback = this;

        mAdapter = new BookAdapter(this, new ArrayList<Book>());

        mResults = RESULTS_ON_CREATE;

        mListView.setAdapter(mAdapter);

        getLoaderManager().initLoader(BOOK_LOADER_ID, null, mCallback);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editTextString = mEditText.getText().toString().trim();

                if (editTextString.isEmpty() || editTextString.equals("")){
                    Toast.makeText(mContext,"No Input on search", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!mAsyncIsRunning){

                    mQueryString = editTextString;

                    //  Update user by displaying progressbar spinner
                    startBackground();

                    mAdapter.clear();

                    getLoaderManager().restartLoader(BOOK_LOADER_ID, null, mCallback);

                    //new BookAsyncTask().execute(editTextString);
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

    public void resultsSuccessful(){
        mProgress_spinner.setVisibility(View.GONE);
        mTextView.setVisibility(View.GONE);
        mAsyncIsRunning = false;
    }

    public static int getResults() {
        return mResults;
    }

    public static void setResults(int results) {
        mResults = results;
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
            Snackbar.make(mSearchButton, "Delete", Snackbar.LENGTH_LONG)
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

        int results = getResults();

        switch(results){
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
            default:
                if(data == null){
                    resultsFailed();
                }
                else {
                    mAdapter.clear();
                    mAdapter.addAll(data);
                    resultsSuccessful();
                }
                mAdapter.notifyDataSetChanged();
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

            if(mResults == RESULTS_ON_CREATE){
                //  Ensure results are reset
                setResults(RESULT_DEFAULT);
                return null;
            }

            //  Ensure results are reset
            setResults(RESULT_DEFAULT);

            //  Method to check Network Connectivity
            if(!QueryUtils.isConnected(getContext())){
                setResults(0);
                return null;
            }

            //  Create URL with provided String
            URL url = QueryUtils.createUrl(BOOKS_REQUEST_URL, mQueryString);

            //  Verify URL creation was successful
            if(url == null){
                setResults(1);
                return null;}

            // Variable to use for HttpRequest
            String jsonResponse = "";

            try {
                jsonResponse = QueryUtils.makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
                setResults(2);
                return null;
            }

            // Verify jsonResponse is not empty
            if(jsonResponse.isEmpty() || jsonResponse.equals("")){
                setResults(2);
                return null;}


            // Extract relevant fields from the JSON response and create an {@link Event} object
            ArrayList<Book> mBooks = QueryUtils.extractFromJson(jsonResponse);

            // Verify books is'nt empty
            if(mBooks.isEmpty()){
                setResults(3);
                return null;}

            return mBooks;
        }
    }
}



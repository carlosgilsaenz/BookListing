package com.example.csaenz.booklisting;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


    private static final int BOOK_LOADER_ID = 0;

    public static final String LOG_TAG = MainActivity.class.getName();

    private static final String BOOKS_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=android&maxResults=1";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.search_button)
    ImageButton mSearchButton;

    @BindView(R.id.text_view_empty)
    TextView mTextView;

    @BindView(R.id.progress_spinner)
    ProgressBar mProgress_spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                URL url = QueryUtils.createUrl(BOOKS_REQUEST_URL);
                Log.i(LOG_TAG,"URL = s" + url);

                String jsonResponse = "";

                try {
                    jsonResponse = QueryUtils.makeHttpRequest(url);
                } catch (IOException e) {
                    e.printStackTrace();
                    jsonResponse = "Error!!!!!";
                }
                finally {
                    Log.i(LOG_TAG,"jsonResponse: " + jsonResponse);
                }
            }
        });
    }

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
}
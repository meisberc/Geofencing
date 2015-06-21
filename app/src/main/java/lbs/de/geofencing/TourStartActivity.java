package lbs.de.geofencing;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class TourStartActivity extends AppCompatActivity  {

    private String name;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_start);

        actionBar = getSupportActionBar();

        //Tourname aus Intent wieder auslesen
        Bundle b = getIntent().getExtras();
        //String name = getIntent().getExtras().getString(MainActivity.TOURNAME);
        ((TextView)findViewById(R.id.tourName)).setText(getIntent().getExtras().getString(MainActivity.TOURNAME));

        setupActivity();
    }

    public void startTour(View view)
    {
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra(MainActivity.TOURNAME, name);
        startActivity(i);
    }

    public void setupActivity()
    {
        actionBar.setDisplayShowTitleEnabled(false);
        TextView tourName = (TextView) findViewById(R.id.tourName);
        tourName.setText(name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tour_start, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package lbs.de.geofencing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import database.DbAdapter;
import database.Tour;

public class TourStartActivity extends AppCompatActivity {

    //    private static final int REQUEST_CODE = 1;
    private String name;
    private Tour tour;
    private DbAdapter dbAdapter = MainActivity.getDbAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_start);

        //Tourname aus Intent wieder auslesen
        name = getIntent().getExtras().getString(MainActivity.TOURNAME);
        ((TextView) findViewById(R.id.tourName)).setText(name);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tour: " + name);
        }

        /*Hier wird tour mit einem Aufruf aus der Datenbank initialisiert
        und die Werte des Objekts den Textfeldern zugeordnet
         */

        //tour = dbAdapter....
    }

    public void startTour(View view) {
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra(MainActivity.TOURNAME, name);
        startActivity(i);
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                name = data.getExtras().getString(MainActivity.TOURNAME);
            }
        }
    }*/

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

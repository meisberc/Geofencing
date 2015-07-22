package lbs.de.geofencing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import database.DbAdapter;
import database.Tour;

public class TourStartActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private String name;
    private DbAdapter dbAdapter = MainActivity.getDbAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_start);

        //Tourname aus Intent wieder auslesen
        name = getIntent().getExtras().getString(MainActivity.TOURNAME);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tour: " + name);
        }

        /*Hier wird tour mit einem Aufruf aus der Datenbank initialisiert
        und die Werte des Objekts den Textfeldern zugeordnet
         */
        dbAdapter.openRead();
        Tour tour = dbAdapter.getTour(name);
        ((TextView) findViewById(R.id.tourName)).setText(tour.getName());
        ((TextView) findViewById(R.id.tourDesc)).setText(tour.getDesc().replace("\\n", "\n"));

    }

    public void startTour(View view) {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            Intent i = new Intent(this, MapsActivity.class);
            i.putExtra(MainActivity.TOURNAME, name);
            startActivityForResult(i, REQUEST_CODE);
        } else {
            showConnectionAlert();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                name = data.getExtras().getString(MainActivity.TOURNAME);
            }
        }
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

    @Override
    protected void onStop() {
        super.onStop();
        dbAdapter.close();
    }

    private void showConnectionAlert() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.connError);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.connErrorText);

        // On pressing Settings button
        alertDialog.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startTour(null);
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }
}

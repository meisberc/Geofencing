package lbs.de.geofencing;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import database.DbAdapter;
import database.Point;
import geofence.GeofenceTransitionsIntentService;
/**
 * Erstellt von Christian Meisberger
 */
public class POIActivity extends AppCompatActivity {

    private String name;
    private DbAdapter dbAdapter = MainActivity.getDbAdapter();

    // Broadcast Receiver to close Activity on receiving Geofencing-Exit
    private final BroadcastReceiver finishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(GeofenceTransitionsIntentService.STOPPOI))
            {
                exitActivity();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi);


        Bundle b = getIntent().getExtras();
        name = b.getString(MapsActivity.POINT);
        String tourname = b.getString(MainActivity.TOURNAME);

        dbAdapter.openRead();
        Point point = dbAdapter.getPoint(tourname, name);

        ((TextView)findViewById(R.id.pointName)).setText(name);
        ((TextView) findViewById(R.id.pointDesc)).setText(point.getDesc().replace("\\n", "\n"));
        ((ImageView) findViewById(R.id.image)).setImageBitmap(point.getBitmap());

        // Close notification on activity start
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(GeofenceTransitionsIntentService.TAG,0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_poi, menu);
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


    // Handle action if the back button is pressed
    @Override
    public void onBackPressed() {
        exitActivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(finishReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(finishReceiver, new IntentFilter("exitPOI"));
    }

    private void exitActivity()
    {
        Intent i = getIntent();
        i.putExtra(MapsActivity.POINT, name);
        setResult(RESULT_OK, i);
        finish();
    }
}

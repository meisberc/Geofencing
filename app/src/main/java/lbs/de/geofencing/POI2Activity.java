package lbs.de.geofencing;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import database.DbAdapter;
import database.Point;
import geofence.GeofenceTransitionsIntentService;

public class POI2Activity extends AppCompatActivity {

    private String name;
    private Point point;
    private DbAdapter dbAdapter = MainActivity.getDbAdapter();

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

        Log.i(getLocalClassName(), "onCreate()");
        dbAdapter.openRead();
        Bundle b = getIntent().getExtras();
        name = b.getString(MapsActivity.POINT);
        String tourname = b.getString(MainActivity.TOURNAME);
        point = dbAdapter.getPoint(tourname, name);

        setupActivity();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(GeofenceTransitionsIntentService.TAG,0);
    }

    public void setupActivity() {
        DbAdapter dbAdapter = MainActivity.getDbAdapter();
        dbAdapter.openRead();
        ((TextView)findViewById(R.id.pointName)).setText(name);
        ((TextView) findViewById(R.id.pointDesc)).setText(point.getDesc().replace("\\n", "\n"));
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

    @Override
    public void onBackPressed() {
        //moveTaskToBack(true);
//        POIActivity.this.finish();
//        NavUtils.navigateUpFromSameTask(this);
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
        registerReceiver(finishReceiver, new IntentFilter("xyz"));
    }

    private void exitActivity()
    {
        Log.i(getLocalClassName(), "ExitActivity()");
        Intent i = getIntent();
        i.putExtra(MapsActivity.POINT, name);
        setResult(RESULT_OK, i);
        finish();
    }
}

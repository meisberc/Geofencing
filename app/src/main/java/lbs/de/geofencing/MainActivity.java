package lbs.de.geofencing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import database.DbAdapter;
/**
 * Erstellt von Daniel Schnitzius
 */

public class MainActivity extends AppCompatActivity {

    public static final String TOURNAME = "geofencing.tourName";

    private static DbAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbAdapter = new DbAdapter(this);
        dbAdapter.openRead();

        final ListView listView = (ListView) findViewById(R.id.listView);
        final ArrayList<String> list = dbAdapter.getTouren();

        //ArrayAdapter um die ListView zu befüllen
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.listview_design, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startTourStartActivity(list.get(position));
            }
        });
    }

    /**
     * Startet öffnet genaue Ansicht einer ausgewhälten Tour
     * @param name Name der angeklickten Tour
     */
    public void startTourStartActivity(String name) {
        Intent i = new Intent(this, TourStartActivity.class);
        i.putExtra(MainActivity.TOURNAME, name);
        startActivity(i);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop() {
        super.onStop();
        dbAdapter.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbAdapter.openRead();
    }

    public static DbAdapter getDbAdapter() {
        return dbAdapter;
    }
}

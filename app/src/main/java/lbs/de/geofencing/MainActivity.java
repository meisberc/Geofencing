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


public class MainActivity extends AppCompatActivity {



    public static final String TOURNAME = "geofencing.tourName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DbAdapter dbAdapter = new DbAdapter(this);


        final ListView listView = (ListView) findViewById(R.id.listView);

        //Wird später ersetzt durch den Aufruf aus der DB
        final ArrayList<String> list = new ArrayList<>();
        ArrayList<String> tempList;

        dbAdapter.openRead();
        tempList=dbAdapter.getTouren();
        dbAdapter.close();

        for(int i=0;i<tempList.size();i++){
            list.add(tempList.get(i));
        }


        //ArrayAdapter um die ListView zu befüllen
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String name = list.get(position);
                startTourStartActivity(list.get(position));
            }
        });
    }

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
}

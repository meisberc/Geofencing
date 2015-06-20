package lbs.de.geofencing;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * DataBase Funktion, jede Anfrage an die Datebank wird hier gehandelt!
 */
public class DataBase extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "gg";
    private Context myContext;
    private SQLiteDatabase db;

    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        myContext=context;
    }


    public void onCreate(SQLiteDatabase db) {

    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    protected void copyDataBase() throws IOException {

        // Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
        // Path to the just created empty db
        String outFileName =  "/data/data/"
                +myContext.getApplicationContext().getPackageName()
                + "/databases/" + DATABASE_NAME;
        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);

        }
        db=this.getReadableDatabase();
        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    protected ArrayList<String> getTouren(){
        Cursor c=db.rawQuery("SELECT Name FROM Touren", null);
        ArrayList<String> list=new ArrayList<>();
        int i=1;

        if(c.moveToFirst()){
            c.moveToFirst();
            Log.d("The Column values are: ", c.getString(0));
            list.add(c.getString(0));
            while (c.moveToNext()){
                list.add(c.getString(0));
                Log.d("The Column values are: ", c.getString(0));
            }

        }
        c.close();
        return list;
    }

}

package Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    private Cursor c;

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
    public void copyDataBase(Context context) throws IOException {

        // Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
        // Path to the just created empty db
        //String outFileName =  "/data/data/"
        String outFileName =  context.getFilesDir().getPath()
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

    public ArrayList<String> getTouren(){
        Cursor temp=setQuerry("SELECT Name FROM Touren");
        return addDataToList(temp);
    }

    private ArrayList<String> addDataToList(Cursor c){
        ArrayList<String> data=new ArrayList<>();
        //int i=1;

        if(c.moveToFirst()){
            c.moveToFirst();
            data.add(c.getString(0));
            while (c.moveToNext()){
                data.add(c.getString(0));
            }

        }
        c.close();
        return data;
    }

    private Cursor setQuerry(String querry){
        c=db.rawQuery(querry,null);
        return c;
    }

    protected ArrayList<String> getPoints(){
        Cursor test=setQuerry("SELECT Name FROM PointsOfInterest");
        return addDataToList(test);
    }

}

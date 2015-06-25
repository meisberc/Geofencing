package database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Christian Meisberger on 21.06.2015.
 */
public class DbAdapter {

    private SQLiteDatabase database;
    private DataBase dataBase;

    public DbAdapter(Context context) {
        dataBase = new DataBase(context);
        openWrite();
        copyDb();
        close();
    }

    public void openWrite() throws SQLException {
        database = dataBase.getWritableDatabase();
    }

    public void openRead() throws SQLException {
        database = dataBase.getReadableDatabase();
    }

    public void close() {
        dataBase.close();
    }

    public ArrayList<String> getTouren() {
        Cursor temp = setQuerry("SELECT Name FROM Touren");
        return addDataToList(temp);
    }

    private ArrayList<String> addDataToList(Cursor c) {
        ArrayList<String> data = new ArrayList<>();
            c.moveToFirst();

            while (!c.isAfterLast()) {
                data.add(c.getString(0));
            }
        c.close();
        return data;
    }

    private Cursor setQuerry(String querry) {
        Cursor c = database.rawQuery(querry, null);

        return c;
    }

    public ArrayList<Point> getPoints(String tourName) {
        Cursor test = setQuerry("SELECT p.name FROM PointsOfInterest as p" +
                " LEFT JOIN PointsOfInterestAndTouren as pt on p._id = pt.POI_ID" +
                " LEFT JOIN Touren as t on t._id = pt.Touren_ID\n" +
                " WHERE t.Name LIKE \"" + tourName + "\";");
        return addPointsToList(test);
    }

    private ArrayList<Point> addPointsToList(Cursor c) {
        ArrayList<Point> data = new ArrayList<>();
        c.moveToFirst();

        while (!c.isAfterLast()) {
            Point p = cursorToEntryPoints(c);
            data.add(p);
        }
        c.close();
        return data;
    }

    private Point cursorToEntryPoints(Cursor cursor)
    {
       /* Point p = new Point(cursor....);
        return p;

        */
        return null;
    }

    public void copyDb() {
        try {
            dataBase.copyDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

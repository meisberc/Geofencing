package database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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
        //int i=1;
        if (c.moveToFirst()) {
            c.moveToFirst();
            data.add(c.getString(0));
            while (c.moveToNext()) {
                data.add(c.getString(0));
            }

        }
        c.close();
        return data;
    }

    private Cursor setQuerry(String querry) {
        return database.rawQuery(querry, null);
    }


    public ArrayList<Point> getPoints(String tourName) {
        Cursor test = setQuerry("SELECT p.name,p.Data ,p.longtitude, p.latitude, p.picture FROM PointsOfInterest as p" +
                " LEFT JOIN PointsOfInterestAndTouren as pt on p._id = pt.POI_ID" +
                " LEFT JOIN Touren as t on t._id = pt.Touren_ID\n" +
                " WHERE t.Name LIKE \"" + tourName + "\";");
        return addPointsToList(test);
    }

    private ArrayList<Point> addPointsToList(Cursor c) {
        ArrayList<Point> data = new ArrayList<>();
        if (c.moveToFirst()) {
            c.moveToFirst();
            Point p = cursorToEntryPoints(c);
            data.add(p);
            while (c.moveToNext()) {
                p = cursorToEntryPoints(c);
                data.add(p);
            }

        }
        c.close();
        return data;
    }

    private Point cursorToEntryPoints(Cursor cursor) {
        byte [] image=cursor.getBlob(4);
        Bitmap picture;
        BitmapFactory.Options options = new BitmapFactory.Options();
        picture = BitmapFactory.decodeByteArray(image, 0, image.length, options);
        return new Point(cursor.getString(0), cursor.getString(1),
                cursor.getDouble(2), cursor.getDouble(3), picture);
    }

    public void copyDb() {
        try {
            dataBase.copyDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Tour getTour(String name) {
        Cursor temp = setQuerry("SELECT Name, Data FROM Touren");
        return addDataToTour(temp, name);
    }

    private Tour addDataToTour(Cursor c, String name) {
        Tour tour = null;
        if (c.moveToFirst()) {
            c.moveToFirst();
            if(c.getString(0).equals(name))
            {
                tour = new Tour(c.getString(0),c.getString(1));
                //tour = new Tour(c.getString(0),"");
            }
            while (c.moveToNext()) {
                if(c.getString(0).equals(name))
                {
                    tour = new Tour(c.getString(0),c.getString(1));
                    //tour = new Tour(c.getString(0),"");
                }
            }

        }
        c.close();
        return tour;
    }

}

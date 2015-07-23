package database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * DbAdapter
 * erstellt von Fabian Emmesberger
 * hier werden alle Operationen auf der Datenbank ausgeführt.
 */

public class DbAdapter {

    private SQLiteDatabase database;
    private DataBase dataBase;

    /**
     * Konstruktor - Datenbank wird beim Anlegen auf das Gerät kopiert.
     * @param context Context der Main_Activity
     */
    public DbAdapter(Context context) {
        dataBase = new DataBase(context);
        openWrite();
        copyDb();
        close();
    }

    /**
     * Öffnet die Datenbank mit schreibendem Zugriff
     * @throws SQLException
     */
    public void openWrite() throws SQLException {
        database = dataBase.getWritableDatabase();
    }

    /**
     * Öffnet die Datenbank mit lesendem Zugriff
     * @throws SQLException
     */
    public void openRead() throws SQLException {
        database = dataBase.getReadableDatabase();
    }

    /**
     * Schließt den Datenbankzugriff
     */
    public void close() {
        dataBase.close();
    }

    /**
     * Gibt alle vorhandenen Touren als String zurück
     * @return ArrayList<String>
     */
    public ArrayList<String> getTouren() {
        Cursor temp = setQuerry("SELECT Name FROM Touren");
        return addDataToList(temp);
    }

    /**
     * Sucht geforderte Daten in der Datenbank und gibt diese zurück
     * @param c Cursor, der über die Tabelle der Datenbank läuft
     * @return ArrayList<String>
     */
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

    /**
     * Setzt SQL-Statement in Cursor um
     * @param querry SQL-Statement
     * @return Cursor
     */
    private Cursor setQuerry(String querry) {
        return database.rawQuery(querry, null);
    }

    /**
     * Gibt alle Punkte einer Tour zurück
     * @param tourName Name der Tour
     * @return ArrayList<Point>
     */
    public ArrayList<Point> getPoints(String tourName) {
        Cursor test = setQuerry("SELECT p.name,p.Data ,p.longtitude, p.latitude, p.picture FROM PointsOfInterest as p" +
                " LEFT JOIN PointsOfInterestAndTouren as pt on p._id = pt.POI_ID" +
                " LEFT JOIN Touren as t on t._id = pt.Touren_ID\n" +
                " WHERE t.Name LIKE \"" + tourName + "\";");
        return addPointsToList(test);
    }

    /**
     * Gibt einen bestimmten Punkte einer Tour zurück
     * @param tourName Name der Tour
     * @param pointName Name des Punktes
     * @return Point
     */
    public Point getPoint(String tourName, String pointName) {
        Cursor c = setQuerry("SELECT p.name,p.Data ,p.longtitude, p.latitude, p.picture FROM PointsOfInterest as p" +
                " LEFT JOIN PointsOfInterestAndTouren as pt on p._id = pt.POI_ID" +
                " LEFT JOIN Touren as t on t._id = pt.Touren_ID\n" +
                " WHERE t.Name LIKE \"" + tourName + "\" and p.name LIKE \""+pointName+"\";");
        ArrayList<Point> point = addPointsToList(c);
        for (Point p : point)
        {
            if (p.getName().equals(pointName))
            {
                return p;
            }
        }
        return null;
    }

    /**
     * Fügt die geforderten Punkte einer ArrayList hinzu
     * @param c enhält Daten zur Verarbeitung
     * @return ArrayList<Point>
     */
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
            data.add(p);
        }
        c.close();
        return data;
    }

    /**
     * Punkt wird aus dem Cursor generiert
     * @param cursor enhält Daten zur Verarbeitung
     * @return Point
     */
    private Point cursorToEntryPoints(Cursor cursor) {
        byte [] image=cursor.getBlob(4);
        Bitmap picture;
        BitmapFactory.Options options = new BitmapFactory.Options();
        picture = BitmapFactory.decodeByteArray(image, 0, image.length, options);
        return new Point(cursor.getString(0), cursor.getString(1),
                cursor.getDouble(2), cursor.getDouble(3), picture);
    }

    /**
     * Kopiert die Datenbank aufs Gerät
     */
    public void copyDb() {
        try {
            dataBase.copyDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gibt eine Tour zurück
     * @param name Name der Tour
     * @return Tour
     */
    public Tour getTour(String name) {
        Cursor c = setQuerry("SELECT Name, Data FROM Touren");
        return addDataToTour(c, name);
    }

    /**
     * Fügt die Daten einer Tour hinzu
     * @param c enhält Daten zur Verarbeitung
     * @param name Name der Tour
     * @return ArrayList<Point>
     */
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

package database;

import android.graphics.Bitmap;

public class Point {
    private String name;
    private String desc;
    private double latitude;
    private double longitude;
    private Bitmap bitmap;

    public Point(String name, String desc, double longitude, double latitude, Bitmap bitmap) {
        this.name = name;
        this.desc = desc;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bitmap = bitmap;
    }

    public Point(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

}

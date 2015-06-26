package database;

/**
 * Created by Christian Meisberger on 25.06.2015.
 */
public class Point {
    private String name;
    private String desc;
    private double latitude;
    private double longitude;

    public Point(String name, String desc, double longitude, double latitude) {
        this.name = name;
        this.desc = desc;
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

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

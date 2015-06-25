package database;

/**
 * Created by Christian Meisberger on 25.06.2015.
 */
public class Point {
    private int id;
    private String name;
    private String desc;
    private double latitude;
    private double longitude;

    public Point(String name, String desc) {
        this.name = name;
        this.desc = desc;
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
}

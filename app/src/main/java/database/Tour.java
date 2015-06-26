package database;

import java.util.ArrayList;

/**
 * Created by Christian Meisberger on 25.06.2015.
 */
public class Tour {
    private String name;
    private String desc;
    private int numberOfPoints;
    private ArrayList<Point> points;

    public Tour(String name, String desc) {
        this.name = name;
        this.desc = desc;
        this.numberOfPoints = numberOfPoints;
        this.points = points;
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

    public int getNumberOfPoints() {
        return numberOfPoints;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }
}

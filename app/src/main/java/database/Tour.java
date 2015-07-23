package database;

/**
 * Erstellt von Christian Meisberger
 */
public class Tour {
    private String name;
    private String desc;

    public Tour(String name, String desc) {
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
}

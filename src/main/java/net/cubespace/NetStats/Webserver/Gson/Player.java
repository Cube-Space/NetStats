package net.cubespace.NetStats.Webserver.Gson;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Player {
    private String name;
    private Integer ping;
    private Float lat;
    private Float lon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPing() {
        return ping;
    }

    public void setPing(Integer ping) {
        this.ping = ping;
    }

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLon() {
        return lon;
    }

    public void setLon(Float lon) {
        this.lon = lon;
    }
}

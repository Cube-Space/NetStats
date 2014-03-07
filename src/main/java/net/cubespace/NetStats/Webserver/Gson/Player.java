package net.cubespace.NetStats.Webserver.Gson;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Player {
    private String name;
    private Integer ping;
    private Float lat;
    private Float lon;
    private Boolean online;

    public void setName(String name) {
        this.name = name;
    }

    public void setPing(Integer ping) {
        this.ping = ping;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public void setLon(Float lon) {
        this.lon = lon;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
}

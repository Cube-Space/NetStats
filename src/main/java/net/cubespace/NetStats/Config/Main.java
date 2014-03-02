package net.cubespace.NetStats.Config;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.lib.CubespacePlugin;

import java.io.File;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Main extends Config {
    public Main(CubespacePlugin plugin) {
        CONFIG_FILE = new File(plugin.getDataFolder(), "main.yml");

        Webserver_WebDir = plugin.getDataFolder() + File.separator + "web" + File.separator;
    }

    private String GeoIPLiteCityDatabase = "GeoLiteCity.dat";
    private Long LastGeoIPLiteTime = 0L;
    private String GoogleMapsAPIKey = "";
    private String Webserver_IP = "0.0.0.0";
    private Integer Webserver_Port = 8083;
    private Integer Webserver_Threads = 4;
    private String Webserver_WebDir;
    private String Database_URL = "jdbc:h2:{DIR}netstats";
    private String Database_User = "netstats";
    private String Database_Password = "netstats";

    public String getGeoIPLiteCityDatabase() {
        return GeoIPLiteCityDatabase;
    }

    public void setGeoIPLiteCityDatabase(String geoIPLiteCityDatabase) {
        GeoIPLiteCityDatabase = geoIPLiteCityDatabase;
    }

    public Long getLastGeoIPLiteTime() {
        return LastGeoIPLiteTime;
    }

    public void setLastGeoIPLiteTime(Long lastGeoIPLiteTime) {
        LastGeoIPLiteTime = lastGeoIPLiteTime;
    }

    public String getWebserver_IP() {
        return Webserver_IP;
    }

    public Integer getWebserver_Port() {
        return Webserver_Port;
    }

    public String getWebserver_WebDir() {
        return Webserver_WebDir;
    }

    public String getDatabase_URL() {
        return Database_URL;
    }

    public String getDatabase_User() {
        return Database_User;
    }

    public String getDatabase_Password() {
        return Database_Password;
    }

    public String getGoogleMapsAPIKey() {
        return GoogleMapsAPIKey;
    }

    public Integer getWebserver_Threads() {
        return Webserver_Threads;
    }
}

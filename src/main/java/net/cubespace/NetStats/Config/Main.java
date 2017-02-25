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

        WebserverWebDir = plugin.getDataFolder() + File.separator + "web" + File.separator;
    }

    private String GeoIPLiteCityDatabase = "GeoLiteCity.dat";
    private Long LastGeoIPLiteTime = 0L;
    private String GoogleMapsAPIKey = "";
    private String WebserverIP = "0.0.0.0";
    private Integer WebserverPort = 8083;
    private Integer WebserverThreads = 4;
    private String WebserverWebDir;
    private String DatabaseURL = "jdbc:h2:{DIR}netstats";
    private String DatabaseUser = "netstats";
    private String DatabasePassword = "netstats";

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
        return WebserverIP;
    }

    public Integer getWebserver_Port() {
        return WebserverPort;
    }

    public String getWebserver_WebDir() {
        return WebserverWebDir;
    }

    public String getDatabase_URL() {
        return DatabaseURL;
    }

    public String getDatabase_User() {
        return DatabaseUser;
    }

    public String getDatabase_Password() {
        return DatabasePassword;
    }

    public String getGoogleMapsAPIKey() {
        return GoogleMapsAPIKey;
    }

    public Integer getWebserver_Threads() {
        return WebserverThreads;
    }
}

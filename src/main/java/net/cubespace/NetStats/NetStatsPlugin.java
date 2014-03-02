package net.cubespace.NetStats;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.LruObjectCache;
import net.cubespace.NetStats.Config.Main;
import net.cubespace.NetStats.Database.Player;
import net.cubespace.NetStats.Listener.PlayerJoin;
import net.cubespace.NetStats.Listener.PlayerQuit;
import net.cubespace.NetStats.Lookup.GeoIPDownloader;
import net.cubespace.NetStats.Lookup.Lookup;
import net.cubespace.NetStats.Task.UpdatePing;
import net.cubespace.NetStats.Util.ExtractFile;
import net.cubespace.NetStats.Webserver.HTTPServer;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.lib.CubespacePlugin;
import net.cubespace.lib.Database.Database;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.impl.DeferredObject;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class NetStatsPlugin extends CubespacePlugin {
    public void onEnable() {
        final NetStatsPlugin plugin = this;
        final Main config = new Main(this);

        //Init the Config
        getConfigManager().initConfig("main", config);


        //Check for Web Contents
        File indexHtml = new File(config.getWebserver_WebDir(), "index.html");
        if(!indexHtml.exists()) {
            if(!indexHtml.getParentFile().exists()) {
                indexHtml.getParentFile().mkdirs();
            }

            ExtractFile.extractFile("/web/index.html", indexHtml);
        }

        //Setup Database
        database = new Database(this, config.getDatabase_URL(), config.getDatabase_User(), config.getDatabase_Password());
        try {
            Dao playerDao = DaoManager.createDao(database.getConnectionSource(), Player.class);
            playerDao.setObjectCache(new LruObjectCache(5000));

            database.registerDAO(playerDao, Player.class);
        } catch (SQLException e) {
            plugin.getPluginLogger().error("Could not init Database", e);
        }

        try {
            List<Player> playerList = database.getDAO(Player.class).queryForAll();

            for (Player player : playerList) {
                player.setOnline(false);
                database.getDAO(Player.class).update(player);
            }
        } catch (SQLException e) {
            getPluginLogger().warn("Could not reset online Status of all Players");
        }

        //Start up the Webserver
        plugin.getPluginLogger().info("Starting the Webserver");
        plugin.getProxy().getScheduler().runAsync(this, new HTTPServer(plugin));

        //Check if GeoIP Database has updated
        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                final Deferred<Long, Throwable, Void> checkUpdateDeferred = new DeferredObject<Long, Throwable, Void>();
                checkUpdateDeferred.fail(new FailCallback<Throwable>() {
                    @Override
                    public void onFail(Throwable throwable) {
                        getPluginLogger().error("Could not check for GeoIP Database Update", throwable);
                    }
                });

                checkUpdateDeferred.done(new DoneCallback<Long>() {
                    @Override
                    public void onDone(final Long aLong) {
                        final Main config = getConfigManager().getConfig("main");

                        if (aLong > config.getLastGeoIPLiteTime()) {
                            GeoIPDownloader.isUpdating(true);
                            plugin.getPluginLogger().info("There is a new GeoIP Database. Downloading...");
                            plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        GeoIPDownloader.downloadAndInstall(new File(plugin.getDataFolder(), config.getGeoIPLiteCityDatabase()));

                                        config.setLastGeoIPLiteTime(aLong);
                                        try {
                                            config.save();
                                            GeoIPDownloader.isUpdating(false);
                                        } catch (InvalidConfigurationException e) {
                                            getPluginLogger().error("Could not update Config", e);
                                        }
                                    } catch (Exception e) {
                                        getPluginLogger().error("Could not download new GeoIP Database Update", e);
                                    }
                                }
                            });
                        }
                    }
                });

                getProxy().getScheduler().runAsync(plugin, new Runnable() {
                    @Override
                    public void run() {
                        plugin.getPluginLogger().info("Checking for new GeoIP Database");
                        GeoIPDownloader.getLastModified(checkUpdateDeferred);
                    }
                });
            }
        }, 1, 600, TimeUnit.SECONDS);

        getProxy().getScheduler().schedule(this, new UpdatePing(this), 1, 1, TimeUnit.SECONDS);

        getManagerRegistry().registerManager("lookup", new Lookup(this));

        getProxy().getPluginManager().registerListener(this, new PlayerJoin(this));
        getProxy().getPluginManager().registerListener(this, new PlayerQuit(this));
    }
}

package net.cubespace.NetStats.Listener;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.maxmind.geoip.Location;
import net.cubespace.NetStats.Database.Player;
import net.cubespace.NetStats.Lookup.GeoIPDownloader;
import net.cubespace.NetStats.Lookup.Lookup;
import net.cubespace.NetStats.Util.FeatureDetector;
import net.cubespace.lib.CubespacePlugin;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.impl.DeferredObject;

import java.sql.SQLException;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class PlayerJoin implements Listener {
    private CubespacePlugin plugin;
    private Lookup lookup;

    public PlayerJoin(CubespacePlugin plugin) {
        this.plugin = plugin;
        this.lookup = plugin.getManagerRegistry().getManager("lookup");
    }

    @EventHandler
    public void onPlayerJoin(final PostLoginEvent event) {
        final Deferred<Player, Exception, Void> def = new DeferredObject<>();
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            public void run() {
                try {
                    Dao<Player, Integer> playerDao = plugin.getDatabase().getDAO(Player.class);
                    Where<Player, Integer> playerWhere = playerDao.queryBuilder().where();
                    Player player;

                    if(FeatureDetector.canUseUUID()) {
                        player = playerWhere.eq("uuid", event.getPlayer().getUUID()).queryForFirst();
                    } else {
                        player = playerWhere.eq("name", event.getPlayer().getName()).queryForFirst();
                    }

                    if (player == null) {
                        Player newPlayer = new Player();
                        newPlayer.setName(event.getPlayer().getName());
                        newPlayer.setIp(event.getPlayer().getPendingConnection().getAddress().getHostString());
                        newPlayer.setOnline(true);
                        newPlayer.setPing(event.getPlayer().getPing());
                        newPlayer.setLastOnline(System.currentTimeMillis());

                        if(FeatureDetector.canUseUUID()) {
                            newPlayer.setUuid(event.getPlayer().getUUID());
                        }

                        playerDao.create(newPlayer);

                        def.resolve(newPlayer);
                    } else {
                        player.setIp(event.getPlayer().getPendingConnection().getAddress().getHostString());
                        player.setOnline(true);
                        player.setPing(event.getPlayer().getPing());
                        player.setLastOnline(System.currentTimeMillis());
                        playerDao.update(player);

                        def.resolve(player);
                    }
                } catch (SQLException ex) {
                    def.reject(ex);
                }

            }
        });

        def.done(new DoneCallback<Player>() {
            public void onDone(final Player result) {
                plugin.getPluginLogger().info("Found User " + event.getPlayer().getName() + ". He/She has the ID: " + result.getId());

                if(GeoIPDownloader.currentlyUpdating()) return;

                plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
                    @Override
                    public void run() {
                        Location location = lookup.lookup(event.getPlayer().getAddress().getAddress());
                        if(location != null) {
                            result.setLat(location.latitude);
                            result.setLon(location.longitude);

                            try {
                                plugin.getDatabase().getDAO(Player.class).update(result);
                            } catch (SQLException e) {
                                plugin.getPluginLogger().warn("Could not update Location for Player", e);
                            }
                            plugin.getPluginLogger().info("Player " + event.getPlayer().getName() + " joined from " + location.city + " lat: " + location.latitude + " long: " + location.longitude);
                        } else {
                            plugin.getPluginLogger().warn("Player " + event.getPlayer().getName() + " could not be resolved");
                        }
                    }
                });
            }
        }).fail(new FailCallback<Exception>() {
            public void onFail(Exception e) {
                plugin.getPluginLogger().error("Error in creating a new Player entry", e);
            }
        });
    }
}

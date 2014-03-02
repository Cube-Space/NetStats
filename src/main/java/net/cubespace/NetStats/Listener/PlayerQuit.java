package net.cubespace.NetStats.Listener;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.maxmind.geoip.Location;
import net.cubespace.NetStats.Database.Player;
import net.cubespace.NetStats.Lookup.GeoIPDownloader;
import net.cubespace.NetStats.Lookup.Lookup;
import net.cubespace.NetStats.Util.FeatureDetector;
import net.cubespace.lib.CubespacePlugin;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
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
public class PlayerQuit implements Listener {
    private CubespacePlugin plugin;
    private Lookup lookup;

    public PlayerQuit(CubespacePlugin plugin) {
        this.plugin = plugin;
        this.lookup = plugin.getManagerRegistry().getManager("lookup");
    }

    @EventHandler
    public void onPlayerQuit(final PlayerDisconnectEvent event) {
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

                    if (player != null) {
                        player.setOnline(false);
                        playerDao.update(player);
                        def.resolve(player);
                    } else {
                        def.reject(new Exception("Player not found"));
                    }
                } catch (SQLException ex) {
                    def.reject(ex);
                }

            }
        });

        def.done(new DoneCallback<Player>() {
            public void onDone(final Player result) {
                plugin.getPluginLogger().info("Player " + event.getPlayer().getName() + " is now offline");
            }
        }).fail(new FailCallback<Exception>() {
            public void onFail(Exception e) {
                plugin.getPluginLogger().error("Error in updating Player entry", e);
            }
        });
    }
}

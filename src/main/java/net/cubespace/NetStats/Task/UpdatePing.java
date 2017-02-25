package net.cubespace.NetStats.Task;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import net.cubespace.NetStats.Bridge.Redis.RedisBungeeBridge;
import net.cubespace.NetStats.Database.Player;
import net.cubespace.NetStats.NetStatsPlugin;
import net.cubespace.NetStats.Util.FeatureDetector;
import net.cubespace.lib.CubespacePlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class UpdatePing implements Runnable {
    private NetStatsPlugin plugin;

    public UpdatePing( NetStatsPlugin plugin ) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Dao<Player, Integer> playerDao = plugin.getDatabase().getDAO( Player.class );
        for ( ProxiedPlayer player : plugin.getProxy().getPlayers() ) {
            try {
                Where<Player, Integer> playerWhere = playerDao.queryBuilder().where();
                Player player1;

                if ( FeatureDetector.canUseUUID() ) {
                    player1 = playerWhere.eq( "uuid", player.getUniqueId() ).queryForFirst();
                } else {
                    player1 = playerWhere.eq( "name", player.getName() ).queryForFirst();
                }

                if ( player1 != null ) {
                    player1.setPing( player.getPing() );
                    playerDao.update( player1 );

                    RedisBungeeBridge bridge = plugin.getManagerRegistry().getManager( "redisBungee" );
                    if ( bridge != null ) {
                        bridge.updatePing( player );
                    }
                }
            } catch ( Exception e ) {
                plugin.getLogger().warning( "Could not update ping for " + player.getName() + ": " + e.getMessage() );
            }
        }
    }
}

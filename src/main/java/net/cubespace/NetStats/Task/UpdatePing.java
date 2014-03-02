package net.cubespace.NetStats.Task;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import net.cubespace.NetStats.Database.Player;
import net.cubespace.NetStats.Util.FeatureDetector;
import net.cubespace.lib.CubespacePlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class UpdatePing implements Runnable {
    private CubespacePlugin plugin;

    public UpdatePing(CubespacePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Dao<Player, Integer> playerDao = plugin.getDatabase().getDAO(Player.class);
        for(ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            try {
                Where<Player, Integer> playerWhere = playerDao.queryBuilder().where();
                Player player1;

                if(FeatureDetector.canUseUUID()) {
                    player1 = playerWhere.eq("uuid", player.getUUID()).queryForFirst();
                } else {
                    player1 = playerWhere.eq("name", player.getName()).queryForFirst();
                }

                if (player1 != null) {
                    player1.setPing(player.getPing());
                    playerDao.update(player1);
                }
            } catch (Exception e) {
                plugin.getPluginLogger().warn("Could not update ping for " + player.getName(), e);
            }
        }
    }
}

package net.cubespace.NetStats.Listener;

import net.cubespace.NetStats.Bridge.Redis.RedisBungeeBridge;
import net.cubespace.NetStats.NetStatsPlugin;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class PlayerQuit implements Listener {
    private NetStatsPlugin plugin;

    public PlayerQuit( NetStatsPlugin plugin ) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit( final PlayerDisconnectEvent event ) {
        this.plugin.getPlayerManager().onQuit( event.getPlayer().getName(), event.getPlayer().getUniqueId() );

        RedisBungeeBridge bridge = this.plugin.getManagerRegistry().getManager( "redisBungee" );
        if ( bridge != null ) {
            bridge.onQuit( event.getPlayer() );
        }
    }
}

package net.cubespace.NetStats.Listener;

import net.cubespace.NetStats.Bridge.Redis.RedisBungeeBridge;
import net.cubespace.NetStats.NetStatsPlugin;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class PlayerJoin implements Listener {
    private NetStatsPlugin plugin;

    public PlayerJoin( NetStatsPlugin plugin ) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin( final PostLoginEvent event ) {
        this.plugin.getPlayerManager().onJoin( event.getPlayer().getName(),
                event.getPlayer().getUniqueId(),
                event.getPlayer().getAddress().getHostString(),
                event.getPlayer().getPing(),
                event.getPlayer().getAddress().getAddress() );

        RedisBungeeBridge bridge = this.plugin.getManagerRegistry().getManager( "redisBungee" );
        if ( bridge != null ) {
            bridge.onJoin( event.getPlayer() );
        }
    }
}

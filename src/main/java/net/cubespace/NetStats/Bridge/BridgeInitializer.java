package net.cubespace.NetStats.Bridge;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import net.cubespace.NetStats.Bridge.Redis.RedisBungeeBridge;
import net.cubespace.NetStats.NetStatsPlugin;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * @author geNAZt
 * @version 1.0
 */
public class BridgeInitializer {

    public BridgeInitializer( NetStatsPlugin plugin ) {
        // Check for RedisBungee
        Plugin redisBungee = plugin.getProxy().getPluginManager().getPlugin( "RedisBungee" );
        if ( redisBungee != null ) {
            plugin.getManagerRegistry().registerManager( "redisBungee", new RedisBungeeBridge( plugin, (RedisBungee) redisBungee ) );
        }
    }

}

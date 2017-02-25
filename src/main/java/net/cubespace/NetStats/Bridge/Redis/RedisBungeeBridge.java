package net.cubespace.NetStats.Bridge.Redis;

import com.google.gson.Gson;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.internal.jedis.Jedis;
import com.imaginarycode.minecraft.redisbungee.internal.jedis.JedisPubSub;
import net.cubespace.NetStats.NetStatsPlugin;
import net.cubespace.lib.Manager.IManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author geNAZt
 * @version 1.0
 */
public class RedisBungeeBridge implements IManager {

    private final Gson gson = new Gson();
    private final RedisBungee redisBungee;

    public RedisBungeeBridge( final NetStatsPlugin netStatsPlugin, final RedisBungee redisBungee ) {
        this.redisBungee = redisBungee;

        this.redisBungee.getProxy().getScheduler().runAsync( this.redisBungee, new Runnable() {
            @Override
            public void run() {
                try ( Jedis redis = redisBungee.getPool().getResource() ) {
                    redis.subscribe( new JedisPubSub() {
                        @Override
                        public void onMessage( String channel, String message ) {
                            switch ( channel ) {
                                case "netstats:join":
                                    JoinPayload joinPayload = gson.fromJson( message, JoinPayload.class );
                                    if ( netStatsPlugin.getProxy().getPlayer( joinPayload.getUuid() ) == null ) {
                                        netStatsPlugin.getPlayerManager().onJoin( joinPayload.getName(), joinPayload.getUuid(), joinPayload.getIp(), joinPayload.getPing(), joinPayload.getAddress() );
                                    }

                                    break;

                                case "netstats:quit":
                                    QuitPayload quitPayload = gson.fromJson( message, QuitPayload.class );
                                    if ( netStatsPlugin.getProxy().getPlayer( quitPayload.getUuid() ) == null ) {
                                        netStatsPlugin.getPlayerManager().onQuit( quitPayload.getName(), quitPayload.getUuid() );
                                    }

                                    break;

                                case "netstats:updatePing":
                                    UpdatePingPayload updatePingPayload = gson.fromJson( message, UpdatePingPayload.class );
                                    if ( netStatsPlugin.getProxy().getPlayer( updatePingPayload.getUuid() ) == null ) {
                                        netStatsPlugin.getPlayerManager().updatePing( updatePingPayload.getName(), updatePingPayload.getUuid(), updatePingPayload.getPing() );
                                    }

                                    break;
                            }
                        }
                    }, "netstats:join", "netstats:quit", "netstats:updatePing" );
                }
            }
        } );
    }

    public void onJoin( ProxiedPlayer player ) {
        JoinPayload joinPayload = new JoinPayload();
        joinPayload.setName( player.getName() );
        joinPayload.setUuid( player.getUniqueId() );
        joinPayload.setIp( player.getAddress().getHostString() );
        joinPayload.setPing( player.getPing() );
        joinPayload.setAddress( player.getAddress().getAddress() );

        final String json = gson.toJson( joinPayload );
        this.redisBungee.getProxy().getScheduler().runAsync( this.redisBungee, new Runnable() {
            @Override
            public void run() {
                try ( Jedis redis = redisBungee.getPool().getResource() ) {
                    redis.publish( "netstats:join", json );
                }
            }
        } );
    }

    public void onQuit( ProxiedPlayer player ) {
        QuitPayload quitPayload = new QuitPayload();
        quitPayload.setName( player.getName() );
        quitPayload.setUuid( player.getUniqueId() );

        final String json = gson.toJson( quitPayload );
        this.redisBungee.getProxy().getScheduler().runAsync( this.redisBungee, new Runnable() {
            @Override
            public void run() {
                try ( Jedis redis = redisBungee.getPool().getResource() ) {
                    redis.publish( "netstats:quit", json );
                }
            }
        } );
    }

    public void updatePing( ProxiedPlayer player ) {
        UpdatePingPayload updatePingPayload = new UpdatePingPayload();
        updatePingPayload.setName( player.getName() );
        updatePingPayload.setUuid( player.getUniqueId() );
        updatePingPayload.setPing( player.getPing() );

        final String json = gson.toJson( updatePingPayload );
        this.redisBungee.getProxy().getScheduler().runAsync( this.redisBungee, new Runnable() {
            @Override
            public void run() {
                try ( Jedis redis = redisBungee.getPool().getResource() ) {
                    redis.publish( "netstats:updatePing", json );
                }
            }
        } );
    }

}

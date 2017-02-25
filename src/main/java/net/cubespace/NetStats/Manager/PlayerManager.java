package net.cubespace.NetStats.Manager;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.maxmind.geoip.Location;
import net.cubespace.NetStats.Database.Player;
import net.cubespace.NetStats.Lookup.GeoIPDownloader;
import net.cubespace.NetStats.Lookup.Lookup;
import net.cubespace.NetStats.NetStatsPlugin;
import net.cubespace.NetStats.Util.FeatureDetector;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author geNAZt
 * @version 1.0
 */
public class PlayerManager {

    private final NetStatsPlugin plugin;
    private final Lookup lookup;

    public PlayerManager( NetStatsPlugin plugin ) {
        this.plugin = plugin;
        this.lookup = plugin.getManagerRegistry().getManager( "lookup" );
    }

    public void onQuit( final String name, final UUID uuid ) {
        final SettableFuture<Player> future = SettableFuture.create();
        plugin.getProxy().getScheduler().runAsync( plugin, new Runnable() {
            public void run() {
                try {
                    Dao<Player, Integer> playerDao = plugin.getDatabase().getDAO( Player.class );
                    Where<Player, Integer> playerWhere = playerDao.queryBuilder().where();
                    Player player;

                    if ( FeatureDetector.canUseUUID() ) {
                        player = playerWhere.eq( "uuid", uuid.toString() ).queryForFirst();
                    } else {
                        player = playerWhere.eq( "name", name ).queryForFirst();
                    }

                    if ( player != null ) {
                        player.setOnline( false );
                        playerDao.update( player );

                        future.set( player );
                    } else {
                        future.setException( new Exception( "Player not found" ) );
                    }
                } catch ( SQLException ex ) {
                    future.setException( ex );
                }
            }
        } );

        Futures.addCallback( future, new FutureCallback<Player>() {
            @Override
            public void onSuccess( Player player ) {
                plugin.getLogger().info( "Player " + name + " is now offline" );
            }

            @Override
            public void onFailure( Throwable throwable ) {
                plugin.getLogger().severe( "Error in updating Player entry: " + throwable.getMessage() );
            }
        } );
    }

    public void onJoin( final String name, final UUID uuid, final String ip, final int ping, final InetAddress address ) {
        final SettableFuture<Player> future = SettableFuture.create();
        plugin.getProxy().getScheduler().runAsync( plugin, new Runnable() {
            public void run() {
                try {
                    Dao<Player, Integer> playerDao = plugin.getDatabase().getDAO( Player.class );
                    Where<Player, Integer> playerWhere = playerDao.queryBuilder().where();
                    Player player;

                    if ( FeatureDetector.canUseUUID() ) {
                        player = playerWhere.eq( "uuid", uuid.toString() ).queryForFirst();
                    } else {
                        player = playerWhere.eq( "name", name ).queryForFirst();
                    }

                    if ( player == null ) {
                        Player newPlayer = new Player();
                        newPlayer.setName( name );
                        newPlayer.setIp( ip );
                        newPlayer.setOnline( true );
                        newPlayer.setPing( ping );
                        newPlayer.setLastOnline( System.currentTimeMillis() );

                        if ( FeatureDetector.canUseUUID() ) {
                            newPlayer.setUuid( uuid.toString() );
                        }

                        playerDao.create( newPlayer );

                        future.set( newPlayer );
                    } else {
                        player.setIp( ip );
                        player.setOnline( true );
                        player.setPing( ping );
                        player.setLastOnline( System.currentTimeMillis() );
                        playerDao.update( player );

                        future.set( player );
                    }
                } catch ( SQLException ex ) {
                    future.setException( ex );
                }
            }
        } );

        Futures.addCallback( future, new FutureCallback<Player>() {
            @Override
            public void onSuccess( final Player player ) {
                plugin.getLogger().info( "Found User " + name + ". He/She has the ID: " + player.getId() );

                if ( GeoIPDownloader.currentlyUpdating() ) return;

                plugin.getProxy().getScheduler().runAsync( plugin, new Runnable() {
                    @Override
                    public void run() {
                        Location location = lookup.lookup( address );
                        if ( location != null ) {
                            player.setLat( location.latitude );
                            player.setLon( location.longitude );

                            try {
                                plugin.getDatabase().getDAO( Player.class ).update( player );
                            } catch ( SQLException e ) {
                                plugin.getLogger().warning( "Could not update Location for Player: " + e.getMessage() );
                            }
                            plugin.getLogger().info( "Player " + name + " joined from " + location.city + " lat: " + location.latitude + " long: " + location.longitude );
                        } else {
                            plugin.getLogger().warning( "Player " + name + " could not be resolved" );
                        }
                    }
                } );
            }

            @Override
            public void onFailure( Throwable throwable ) {
                plugin.getLogger().severe( "Error in creating a new Player entry: " + throwable.getMessage() );
            }
        } );
    }

    public void updatePing( final String name, final UUID uuid, final int ping ) {
        final SettableFuture<Player> future = SettableFuture.create();

        plugin.getProxy().getScheduler().runAsync( plugin, new Runnable() {
            public void run() {
                try {
                    Dao<Player, Integer> playerDao = plugin.getDatabase().getDAO( Player.class );
                    Where<Player, Integer> playerWhere = playerDao.queryBuilder().where();
                    Player player1;

                    if ( FeatureDetector.canUseUUID() ) {
                        player1 = playerWhere.eq( "uuid", uuid.toString() ).queryForFirst();
                    } else {
                        player1 = playerWhere.eq( "name", name ).queryForFirst();
                    }

                    if ( player1 != null ) {
                        player1.setPing( ping );
                        playerDao.update( player1 );

                        future.set( player1 );
                    } else {
                        future.setException( new Exception( "Player not found" ) );
                    }
                } catch ( Exception e ) {
                    future.setException( e );
                }
            }
        } );

        Futures.addCallback( future, new FutureCallback<Player>() {
            @Override
            public void onSuccess( Player player ) {
                plugin.getLogger().fine( "Updated ping for " + player.getName() + " to " + player.getPing() );
            }

            @Override
            public void onFailure( Throwable throwable ) {
                plugin.getLogger().warning( "Could not update ping for " + name + ": " + throwable.getMessage() );
            }
        } );
    }
}
package net.cubespace.NetStats;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.LruObjectCache;
import net.cubespace.NetStats.Bridge.BridgeInitializer;
import net.cubespace.NetStats.Config.Main;
import net.cubespace.NetStats.Database.Player;
import net.cubespace.NetStats.Listener.PlayerJoin;
import net.cubespace.NetStats.Listener.PlayerQuit;
import net.cubespace.NetStats.Lookup.GeoIPDownloader;
import net.cubespace.NetStats.Lookup.Lookup;
import net.cubespace.NetStats.Manager.PlayerManager;
import net.cubespace.NetStats.Task.UpdatePing;
import net.cubespace.NetStats.Util.ExtractFile;
import net.cubespace.NetStats.Webserver.HTTPServer;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.lib.CubespacePlugin;
import net.cubespace.lib.Database.Database;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class NetStatsPlugin extends CubespacePlugin {
    private PlayerManager playerManager;

    public void onEnable() {
        getLogger().setLevel( Level.INFO );

        final NetStatsPlugin plugin = this;
        final Main config = new Main( this );

        //Init the Config
        getConfigManager().initConfig( "main", config );

        //Check for Web Contents
        File indexHtml = new File( config.getWebserver_WebDir(), "index.html" );
        if ( !indexHtml.exists() ) {
            if ( !indexHtml.getParentFile().exists() ) {
                indexHtml.getParentFile().mkdirs();
            }
        }

        indexHtml.delete();
        ExtractFile.extractFile( "/web/index.html", indexHtml );

        //Setup Database
        database = new Database( this, config.getDatabase_URL(), config.getDatabase_User(), config.getDatabase_Password() );
        try {
            Dao playerDao = DaoManager.createDao( database.getConnectionSource(), Player.class );
            playerDao.setObjectCache( new LruObjectCache( 5000 ) );

            database.registerDAO( playerDao, Player.class );
        } catch ( SQLException e ) {
            plugin.getLogger().severe( "Could not init Database: " + e.getMessage() );
        }

        getManagerRegistry().registerManager( "lookup", new Lookup( this ) );
        this.playerManager = new PlayerManager( this );

        try {
            List<Player> playerList = database.getDAO( Player.class ).queryForAll();

            for ( Player player : playerList ) {
                player.setOnline( false );
                database.getDAO( Player.class ).update( player );
            }
        } catch ( SQLException e ) {
            getLogger().warning( "Could not reset online Status of all Players" );
        }

        //Start up the Webserver
        plugin.getLogger().info( "Starting the Webserver" );
        plugin.getProxy().getScheduler().runAsync( this, new HTTPServer( plugin ) );

        //Check if GeoIP Database has updated
        getProxy().getScheduler().schedule( this, new Runnable() {
            @Override
            public void run() {
                final SettableFuture<Long> future = SettableFuture.create();
                Futures.addCallback( future, new FutureCallback<Long>() {
                    @Override
                    public void onSuccess( final Long aLong ) {
                        final Main config = getConfigManager().getConfig( "main" );

                        if ( aLong > config.getLastGeoIPLiteTime() ) {
                            GeoIPDownloader.isUpdating( true );
                            plugin.getLogger().info( "There is a new GeoIP Database. Downloading..." );
                            plugin.getProxy().getScheduler().runAsync( plugin, new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        GeoIPDownloader.downloadAndInstall( new File( plugin.getDataFolder(), config.getGeoIPLiteCityDatabase() ) );

                                        config.setLastGeoIPLiteTime( aLong );
                                        try {
                                            config.save();
                                            GeoIPDownloader.isUpdating( false );
                                        } catch ( InvalidConfigurationException e ) {
                                            getLogger().severe( "Could not update Config: " + e.getMessage() );
                                        }
                                    } catch ( Exception e ) {
                                        getLogger().severe( "Could not download new GeoIP Database Update: " + e.getMessage() );
                                    }
                                }
                            } );
                        }
                    }

                    @Override
                    public void onFailure( Throwable throwable ) {
                        getLogger().severe( "Could not check for GeoIP Database Update: " + throwable.getMessage() );
                    }
                } );

                getProxy().getScheduler().runAsync( plugin, new Runnable() {
                    @Override
                    public void run() {
                        plugin.getLogger().info( "Checking for new GeoIP Database" );
                        GeoIPDownloader.getLastModified( future );
                    }
                } );
            }
        }, 1, 600, TimeUnit.SECONDS );

        getProxy().getScheduler().schedule( this, new UpdatePing( this ), 1, 1, TimeUnit.SECONDS );

        getProxy().getPluginManager().registerListener( this, new PlayerJoin( this ) );
        getProxy().getPluginManager().registerListener( this, new PlayerQuit( this ) );

        new BridgeInitializer( this );
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }
}

package net.cubespace.NetStats.Lookup;

import com.google.common.util.concurrent.SettableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class GeoIPDownloader {
    private static final String Url = "http://geolite.maxmind.com/download/geoip/database/GeoLiteCity.dat.gz";
    private static boolean isUpdating = false;

    public static void getLastModified( SettableFuture<Long> future ) {
        try {
            URL url = new URL( Url );
            URLConnection connection = url.openConnection();
            future.set( connection.getLastModified() );
        } catch ( IOException e ) {
            future.setException( e );
        }
    }

    public static void downloadAndInstall( File file ) throws Exception {
        if ( file.exists() ) {
            file.delete();
        }

        URL url = new URL( Url );
        URLConnection connection = url.openConnection();

        GZIPInputStream gzis = new GZIPInputStream( connection.getInputStream() );
        byte[] buffer = new byte[1024];
        FileOutputStream out = new FileOutputStream( file );

        int len;
        while ( ( len = gzis.read( buffer ) ) > 0 ) {
            out.write( buffer, 0, len );
        }

        gzis.close();
        out.close();
    }

    public static void isUpdating( boolean b ) {
        isUpdating = b;
    }

    public static boolean currentlyUpdating() {
        return isUpdating;
    }
}

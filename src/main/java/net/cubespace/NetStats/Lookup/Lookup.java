package net.cubespace.NetStats.Lookup;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import net.cubespace.NetStats.Config.Main;
import net.cubespace.lib.CubespacePlugin;
import net.cubespace.lib.Manager.IManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Lookup implements IManager {
    private ScheduledTask task;
    private LookupService lookupService;

    public Lookup(final CubespacePlugin plugin) {
        task = plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    lookupService = new LookupService(new File(plugin.getDataFolder(), ((Main) plugin.getConfigManager().getConfig("main")).getGeoIPLiteCityDatabase()),
                            LookupService.GEOIP_MEMORY_CACHE | LookupService.GEOIP_CHECK_CACHE );

                    task.cancel();

                    plugin.getPluginLogger().info("GeoIP lookup did setup. Everything is good :)");
                } catch (IOException e) {
                    plugin.getPluginLogger().warn("Could not init GeoIP lookup. Retry in one second");
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public Location lookup(InetAddress ip) {
        if (lookupService == null) {
            return null;
        }

        return lookupService.getLocation(ip);
    }
}

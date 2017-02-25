package net.cubespace.NetStats.Bridge.Redis;

import java.util.UUID;

/**
 * @author geNAZt
 * @version 1.0
 */
public class UpdatePingPayload {

    private String name;
    private UUID uuid;
    private int ping;

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid( UUID uuid ) {
        this.uuid = uuid;
    }

    public int getPing() {
        return ping;
    }

    public void setPing( int ping ) {
        this.ping = ping;
    }
}

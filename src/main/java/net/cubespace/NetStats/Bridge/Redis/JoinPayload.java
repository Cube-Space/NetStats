package net.cubespace.NetStats.Bridge.Redis;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author geNAZt
 * @version 1.0
 */
public class JoinPayload {
    private String name;
    private UUID uuid;
    private String ip;
    private int ping;
    private InetAddress address;

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

    public String getIp() {
        return ip;
    }

    public void setIp( String ip ) {
        this.ip = ip;
    }

    public int getPing() {
        return ping;
    }

    public void setPing( int ping ) {
        this.ping = ping;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress( InetAddress address ) {
        this.address = address;
    }
}

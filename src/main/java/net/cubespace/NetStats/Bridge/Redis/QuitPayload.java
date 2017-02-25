package net.cubespace.NetStats.Bridge.Redis;

import java.util.UUID;

/**
 * @author geNAZt
 * @version 1.0
 */
public class QuitPayload {

    private String name;
    private UUID uuid;

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
}

package org.project.models;

import java.util.UUID;

public class Channel {
    private UUID channelId;
    private String channelName;
    private UUID channelOwnerId;

    public Channel(UUID channelId, String channelName, UUID channelOwnerId) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.channelOwnerId = channelOwnerId;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public UUID getChannelOwnerId() {
        return channelOwnerId;
    }
}

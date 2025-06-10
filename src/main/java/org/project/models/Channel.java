package org.project.chats;

import org.project.Message;

import java.util.ArrayList;
import java.util.UUID;

public class Channel implements Chats {
    private UUID channelId;
    private String channelName;
    private UUID channelOwnerId;
    private ArrayList<UUID> subscriberIDs;
    private ArrayList<Message> channelMessageHstory;

    //constructor
    public Channel (String channelName, UUID channelOwnerId) {
        this.channelId = UUID.randomUUID();
        this.channelName = channelName;
        this.channelOwnerId = channelOwnerId;
        this.subscriberIDs = new ArrayList<>();
        this.channelMessageHstory = new ArrayList<>();
        this.subscriberIDs.add(channelOwnerId);
    }

    //getters
    public UUID getChannelId() { return channelId; }
    public String getChannelName() { return channelName; }
    public UUID getChannelOwnerId() { return channelOwnerId; }
    public ArrayList<UUID> getSubscriberIDs() { return subscriberIDs; }
    public ArrayList<Message> getChannelMessageHstory() { return channelMessageHstory; }

    //setters
    public void setChannelName(String channelName) { this.channelName = channelName; }
}
package org.project.models;


import java.util.ArrayList;
import java.util.UUID;

public class Group implements Chats {
    private UUID groupId;
    private String groupName;
    private UUID groupCreatorId;
    private ArrayList<UUID> groupMemberIDs;
    private ArrayList<Message> groupMessageHistory;

    //constructor
    public Group (String groupName, UUID groupCreatorId) {
        this.groupId = UUID.randomUUID();
        this.groupName = groupName;
        this.groupCreatorId = groupCreatorId;
        this.groupMemberIDs = new ArrayList<>();
        this.groupMessageHistory = new ArrayList<>();
        this.groupMemberIDs.add(groupCreatorId);            //creator is the first member of the group
    }

    //getters
    public UUID getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public UUID getGroupCreatorId() { return groupCreatorId; }
    public ArrayList<UUID> getGroupMembersIDs() { return groupMemberIDs; }
    public ArrayList<Message> getGroupMessageHistory() { return groupMessageHistory; }

    //setters
    public void setGroupName(String groupName) { this.groupName = groupName; }
}

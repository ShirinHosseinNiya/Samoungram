package org.project.models;

import java.util.UUID;

public class Group {
    private UUID groupId;
    private String groupName;
    private UUID groupCreatorId;

    public Group(UUID groupId, String groupName, UUID groupCreatorId) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupCreatorId = groupCreatorId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public UUID getGroupCreatorId() {
        return groupCreatorId;
    }
}

package org.project.models;

import java.io.Serializable;
import java.util.UUID;

public class MemberViewModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID userId;
    private final String profileName;
    private final boolean isCreator;

    public MemberViewModel(UUID userId, String profileName, boolean isCreator) {
        this.userId = userId;
        this.profileName = profileName;
        this.isCreator = isCreator;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getProfileName() {
        return profileName;
    }

    public boolean isCreator() {
        return isCreator;
    }

    @Override
    public String toString() {
        return profileName + (isCreator ? " (Creator)" : "");
    }
}
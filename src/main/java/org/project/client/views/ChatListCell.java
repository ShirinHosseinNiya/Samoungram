package org.project.client.views;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.Set;
import java.util.UUID;

class ChatListCell extends ListCell<ChatItemViewModel> {
    private final HBox root = new HBox(10);
    private final Circle avatar = new Circle(22);
    private final Label name = new Label();
    private final Label last = new Label();
    private final Label time = new Label();
    private final Label unread = new Label();
    private final Circle onlineIndicator = new Circle(5);
    private final Set<UUID> onlineUsers;

    ChatListCell(Set<UUID> onlineUsers) {
        this.onlineUsers = onlineUsers;

        avatar.getStyleClass().add("avatar");
        name.getStyleClass().add("chat-name");
        last.getStyleClass().add("chat-last");
        time.getStyleClass().add("chat-time");
        unread.getStyleClass().add("unread-badge");
        onlineIndicator.getStyleClass().add("online-indicator");

        HBox nameBox = new HBox(6, name, onlineIndicator);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        VBox center = new VBox(3, nameBox, last);
        HBox.setHgrow(center, Priority.ALWAYS);

        VBox rightSide = new VBox(3, time, unread);
        rightSide.setAlignment(Pos.TOP_RIGHT);
        unread.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(avatar, center, rightSide);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("chat-row");
    }

    @Override protected void updateItem(ChatItemViewModel item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        name.setText(item.getDisplayName());
        last.setText(item.getLastMessage() == null ? "" : item.getLastMessage());
        time.setText(formatTime(item.getLastTimestamp()));

        boolean hasUnread = item.getUnread() > 0;
        unread.setText(hasUnread ? String.valueOf(item.getUnread()) : "");
        unread.setVisible(hasUnread);
        unread.setManaged(hasUnread);

        boolean isOnline = item.getType() == ChatItemViewModel.ChatType.PRIVATE && onlineUsers.contains(item.getChatId());
        onlineIndicator.setVisible(isOnline);
        onlineIndicator.setManaged(isOnline);

        setGraphic(root);
    }

    private String formatTime(long epochMillis) {
        if (epochMillis <= 0) return "";
        java.time.LocalDateTime dt = java.time.Instant.ofEpochMilli(epochMillis)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        return f.format(dt);
    }
}
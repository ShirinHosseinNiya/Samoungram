package org.project.client.views;


import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;


class ChatListCell extends ListCell<ChatItemViewModel> {
    private final HBox root = new HBox(10);
    private final Circle avatar = new Circle(18);
    private final Label name = new Label();
    private final Label last = new Label();
    private final Label time = new Label();
    private final Label unread = new Label();


    ChatListCell() {
        avatar.getStyleClass().add("avatar");
        name.getStyleClass().add("chat-name");
        last.getStyleClass().add("chat-last");
        time.getStyleClass().add("chat-time");
        unread.getStyleClass().add("unread-badge");


        var center = new javafx.scene.layout.VBox(2, name, last);
        var spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        root.getChildren().addAll(avatar, center, spacer, time, unread);
        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add("chat-row");
    }


    @Override protected void updateItem(ChatItemViewModel item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) { setGraphic(null); return; }
        name.setText(item.getDisplayName());
        last.setText(item.getLastMessage() == null ? "" : item.getLastMessage());
        time.setText(formatTime(item.getLastTimestamp()));
        unread.setText(item.getUnread() > 0 ? String.valueOf(item.getUnread()) : "");
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
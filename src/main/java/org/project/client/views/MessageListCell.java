package org.project.client.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.project.models.Message;
import org.project.models.MessageStatus;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Supplier;

class MessageListCell extends ListCell<Message> {
    private final Supplier<UUID> myIdProvider;
    private final HBox graphic = new HBox();
    private final VBox bubble = new VBox();
    private final Label senderName = new Label();
    private final Label text = new Label();
    private final Label time = new Label();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    MessageListCell(Supplier<UUID> myIdProvider) {
        this.myIdProvider = myIdProvider;

        senderName.getStyleClass().add("message-sender-name");
        text.setWrapText(true);
        time.getStyleClass().add("msg-time");
        bubble.getStyleClass().add("bubble");
        bubble.setPadding(new Insets(8));
        bubble.setMaxWidth(480);
        graphic.getChildren().add(bubble);

        // Ensure the cell has no default text or graphic to avoid artifacts
        setText(null);
    }

    @Override
    protected void updateItem(Message m, boolean empty) {
        super.updateItem(m, empty);

        if (empty || m == null) {
            setGraphic(null);
            return;
        }

        text.setText(m.getContent());
        time.setText(formatTime(m.getTimestamp() == null ? 0L : m.getTimestamp().getTime()));

        boolean sentByMe = m.getSenderId() != null && m.getSenderId().equals(myIdProvider.get());

        // Clear previous children and styles
        bubble.getChildren().clear();
        bubble.getStyleClass().removeAll("sent", "received");

        if (sentByMe) {
            graphic.setAlignment(Pos.CENTER_RIGHT);
            bubble.getStyleClass().add("sent");
            bubble.getChildren().addAll(text, time);
        } else {
            graphic.setAlignment(Pos.CENTER_LEFT);
            bubble.getStyleClass().add("received");
            // Only add sender name if it exists (for groups/channels)
            if (m.getSenderProfileName() != null) {
                senderName.setText(m.getSenderProfileName());
                bubble.getChildren().add(senderName);
            }
            bubble.getChildren().addAll(text, time);
        }

        setGraphic(graphic);
    }

    private String formatTime(long epochMillis) {
        if (epochMillis <= 0) return "";
        java.time.LocalDateTime dt = java.time.Instant.ofEpochMilli(epochMillis)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        return TIME_FORMATTER.format(dt);
    }
}
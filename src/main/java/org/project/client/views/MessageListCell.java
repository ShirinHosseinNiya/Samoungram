package org.project.client.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.project.models.Message;
import org.project.models.MessageStatus;

import java.util.UUID;
import java.util.function.Supplier;

class MessageListCell extends ListCell<Message> {
    private final Supplier<UUID> myIdProvider;


    MessageListCell(Supplier<UUID> myIdProvider) {
        this.myIdProvider = myIdProvider;
    }


    @Override protected void updateItem(Message m, boolean empty) {
        super.updateItem(m, empty);
        if (empty || m == null) { setGraphic(null); return; }


        boolean sentByMe = m.getSenderId() != null && m.getSenderId().equals(myIdProvider.get());


        Label text = new Label(m.getContent());
        text.setWrapText(true);


        String statusIcon = sentByMe ? statusToIcon(m.getStatus()) : "";
        Label time = new Label(formatTime(m.getTimestamp() == null ? 0L : m.getTimestamp().getTime()) +
                (statusIcon.isEmpty()? "" : " " + statusIcon));
        time.getStyleClass().add("msg-time");


        VBox bubble = new VBox(text, time);
        bubble.getStyleClass().addAll("bubble", sentByMe ? "sent" : "received");
        bubble.setPadding(new Insets(8));
        bubble.setMaxWidth(480);


        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);


        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER);
        if (sentByMe) row.getChildren().addAll(spacer, bubble); else row.getChildren().addAll(bubble, spacer);


        setGraphic(row);
        setText(null);
    }


    private String statusToIcon(String status) {
        MessageStatus st = MessageStatus.fromDb(status);
// سبک تلگرام‌مانند با یونیکد
        return switch (st) {
            case SENT -> "✓"; // sent
            case DELIVERED -> "✓✓"; // delivered
            case READ -> "✓✓"; // read — می‌توانید رنگ را در CSS متفاوت کنید
        };
    }


    private String formatTime(long epochMillis) {
        if (epochMillis <= 0) return "";
        java.time.LocalDateTime dt = java.time.Instant.ofEpochMilli(epochMillis)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        return f.format(dt);
    }
}
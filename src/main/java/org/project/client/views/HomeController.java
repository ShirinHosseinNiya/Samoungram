package org.project.client.views;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.project.client.NetworkClient;
import org.project.models.Message;
import org.project.models.Packet;
import org.project.models.PacketType;

import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeController implements Initializable {
    @FXML private TextField chatSearchField;
    @FXML private ListView<ChatItemViewModel> chatListView;
    @FXML private ListView<Message> messageListView;
    @FXML private Label chatTitleLabel;
    @FXML private Label typingLabel;
    @FXML private TextArea messageField;
    @FXML private Button sendButton;

    private final ExecutorService listenerPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "packet-listener");
        t.setDaemon(true); return t;
    });

    private NetworkClient client;
    private UUID myUserId;
    private ChatItemViewModel currentChat;

    private final ObservableList<ChatItemViewModel> allChats = FXCollections.observableArrayList();
    private FilteredList<ChatItemViewModel> filteredChats;

    public void initWith(NetworkClient client, UUID myUserId) {
        this.client = client;
        this.myUserId = myUserId;
        startPacketListener();
        requestInitialChats();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chatListView.setCellFactory(new Callback<>() {
            @Override public ListCell<ChatItemViewModel> call(ListView<ChatItemViewModel> param) {
                return new ChatListCell();
            }
        });
        filteredChats = new FilteredList<>(allChats, c -> true);
        chatListView.setItems(filteredChats);

        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                currentChat = newV;
                chatTitleLabel.setText(newV.getDisplayName());
                requestMessagesFor(newV);
            }
        });

        chatSearchField.textProperty().addListener((obs, old, q) -> {
            String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
            filteredChats.setPredicate(item -> query.isEmpty() ||
                    item.getDisplayName().toLowerCase(Locale.ROOT).contains(query) ||
                    (item.getLastMessage() != null && item.getLastMessage().toLowerCase(Locale.ROOT).contains(query))
            );
        });

        messageListView.setCellFactory(list -> new MessageListCell(() -> myUserId));

        messageField.setOnKeyPressed(ev -> {
            switch (ev.getCode()) {
                case ENTER -> {
                    if (!ev.isShiftDown()) { ev.consume(); onSend(); }
                }
            }
        });
    }

    private void requestInitialChats() {
        if (client == null) return;
        Packet p = new Packet(PacketType.FETCH_CHATS);
        p.setSenderId(myUserId);
        client.sendPacket(p);
    }

    private void requestMessagesFor(ChatItemViewModel chat) {
        if (client == null || chat == null) return;
        Packet p = new Packet(PacketType.FETCH_CHAT_HISTORY);
        p.setSenderId(myUserId);
        p.setReceiverId(chat.getChatId());
        client.sendPacket(p);
        messageListView.getItems().clear();
    }

    private void startPacketListener() {
        if (client == null) return;
        listenerPool.submit(() -> {
            try {
                while (true) {
                    Packet packet = client.getReceivedPacket();
                    Platform.runLater(() -> handleIncoming(packet));
                }
            } catch (InterruptedException ignored) {}
        });
    }

    private void handleIncoming(Packet packet) {
        if (packet == null) return;
        PacketType t = packet.getType();
        if (t == null) return;
        switch (t) {
            case CHATS_LIST -> renderChats(parseChats(packet.getContent()));
            case MESSAGES_LIST -> renderMessages(parseMessages(packet.getContent()));
            case NEW_MESSAGE -> appendMessage(parseSingleMessage(packet));
            case MESSAGE_STATUS_UPDATE -> updateMessageStatus(packet);
            case TYPING -> typingLabel.setText("در حال تایپ...");
            case SUCCESS -> { /* برای لاگین/ثبت‌نام موفق */ }
            case ERROR -> { /* نمایش خطا */ }
            default -> { /* سایر PacketTypeها */ }
        }
    }



    // --- متدهای کمکی (مثل قبل) ---
    private List<ChatItemViewModel> parseChats(String content) { /* ... */ return List.of(); }
    private List<Message> parseMessages(String content) { /* ... */ return List.of(); }
    private Message parseSingleMessage(Packet packet) { /* ... */ return null; }
    private void updateMessageStatus(Packet packet) { /* ... */ }
    private void renderChats(List<ChatItemViewModel> chats) { allChats.setAll(chats); }
    private void renderMessages(List<Message> messages) { messageListView.getItems().setAll(messages); }
    private void appendMessage(Message m) { messageListView.getItems().add(m); }

    @FXML
    private void onSend() {
        String text = Optional.ofNullable(messageField.getText()).orElse("").trim();
        if (text.isEmpty() || currentChat == null) return;

        Message local = new Message(UUID.randomUUID(), myUserId, currentChat.getChatId(), text,
                new Timestamp(System.currentTimeMillis()), "SENT");
        appendMessage(local);
        messageField.clear();

        Packet p = new Packet(PacketType.SEND_MESSAGE);
        p.setSenderId(myUserId);
        p.setReceiverId(currentChat.getChatId());
        p.setContent(text);
        p.setTimestamp(new Timestamp(System.currentTimeMillis()));
        client.sendPacket(p);
    }

}
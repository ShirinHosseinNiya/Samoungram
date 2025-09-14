package org.project.client.views;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.Pair;
import org.project.client.NetworkClient;
import org.project.models.*;

import java.lang.reflect.Type;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeController implements Initializable {
    private enum SearchMode { USERS, CHANNELS }

    @FXML private BorderPane mainPane;
    @FXML private VBox sidebarVBox;
    @FXML private TextField chatSearchField;
    @FXML private ListView<ChatItemViewModel> chatListView;
    @FXML private ListView<Message> messageListView;
    @FXML private Label chatTitleLabel;
    @FXML private Label typingLabel;
    @FXML private TextArea messageField;
    @FXML private Button sendButton;
    @FXML private Button themeToggleButton;
    @FXML private Button createChatButton;
    @FXML private Button addMemberButton;
    @FXML private Button viewMembersButton;
    @FXML private Button leaveChatButton;
    @FXML private Button renameChatButton;
    @FXML private HBox composerHBox;

    private final ExecutorService listenerPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "packet-listener");
        t.setDaemon(true);
        return t;
    });
    private NetworkClient client;
    private UUID myUserId;
    private ChatItemViewModel currentChat;
    private boolean isDarkMode = false;
    private final ObservableList<ChatItemViewModel> allChats = FXCollections.observableArrayList();
    private final PauseTransition searchDebouncer = new PauseTransition(Duration.millis(400));
    private final ObservableList<ChatItemViewModel> originalChats = FXCollections.observableArrayList();
    private final Set<UUID> onlineUsers = new HashSet<>();
    private final Gson gson = new Gson();
    private SearchMode currentSearchMode = SearchMode.USERS;
    private boolean isShowingSearchResults = false;

    public void initWith(NetworkClient client, UUID myUserId) {
        this.client = client;
        this.myUserId = myUserId;
        startPacketListener();
        requestInitialChats();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addMemberButton.setVisible(false);
        viewMembersButton.setVisible(false);
        leaveChatButton.setVisible(false);
        renameChatButton.setVisible(false);
        addMemberButton.setManaged(false);
        viewMembersButton.setManaged(false);
        leaveChatButton.setManaged(false);
        renameChatButton.setManaged(false);

        chatListView.setCellFactory(param -> new ChatListCell(onlineUsers));
        chatListView.setItems(allChats);
        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                clearChatView();
                return;
            }

            if (isShowingSearchResults && newV.getType() == ChatItemViewModel.ChatType.CHANNEL) {
                handleChannelJoin(newV);
                chatListView.getSelectionModel().clearSelection();
            } else {
                currentChat = newV;
                chatTitleLabel.setText(newV.getDisplayName());
                requestMessagesFor(newV);

                boolean isGroupOrChannel = newV.getType() == ChatItemViewModel.ChatType.GROUP || newV.getType() == ChatItemViewModel.ChatType.CHANNEL;
                leaveChatButton.setVisible(isGroupOrChannel);
                leaveChatButton.setManaged(isGroupOrChannel);

                boolean isOwner = newV.getOwnerId() != null && newV.getOwnerId().equals(myUserId);
                renameChatButton.setVisible(isOwner);
                renameChatButton.setManaged(isOwner);

                if (newV.getType() == ChatItemViewModel.ChatType.CHANNEL) {
                    composerHBox.setVisible(isOwner);
                    composerHBox.setManaged(isOwner);
                    addMemberButton.setVisible(isOwner);
                    addMemberButton.setManaged(isOwner);
                    viewMembersButton.setVisible(isOwner);
                    viewMembersButton.setManaged(isOwner);
                } else {
                    composerHBox.setVisible(true);
                    composerHBox.setManaged(true);
                    addMemberButton.setVisible(isGroupOrChannel);
                    addMemberButton.setManaged(isGroupOrChannel);
                    viewMembersButton.setVisible(isGroupOrChannel);
                    viewMembersButton.setManaged(isGroupOrChannel);
                }

                if (newV.getUnread() > 0) {
                    Packet readPacket = new Packet(PacketType.MARK_AS_READ);
                    readPacket.setSenderId(myUserId);
                    readPacket.setReceiverId(newV.getChatId());
                    client.sendPacket(readPacket);
                    newV.setUnread(0);
                    chatListView.refresh();

                    PauseTransition pause = new PauseTransition(Duration.millis(500));
                    pause.setOnFinished(e -> requestInitialChats());
                    pause.play();
                }
            }
        });

        searchDebouncer.setOnFinished(event -> sendSearchRequest());
        chatSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                searchDebouncer.playFromStart();
            } else {
                searchDebouncer.stop();
                allChats.setAll(originalChats);
                isShowingSearchResults = false;
            }
        });

        messageListView.setCellFactory(list -> new MessageListCell(() -> myUserId, () -> (currentChat != null) ? currentChat.getType() : null));

        messageField.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ENTER && !ev.isShiftDown()) {
                ev.consume();
                onSend();
            }
        });
        Platform.runLater(this::updateTheme);
    }

    private void clearChatView() {
        currentChat = null;
        chatTitleLabel.setText("Select a chat");
        messageListView.getItems().clear();
        addMemberButton.setVisible(false);
        viewMembersButton.setVisible(false);
        leaveChatButton.setVisible(false);
        renameChatButton.setVisible(false);
        addMemberButton.setManaged(false);
        viewMembersButton.setManaged(false);
        leaveChatButton.setManaged(false);
        renameChatButton.setManaged(false);
        composerHBox.setVisible(true);
        composerHBox.setManaged(true);
    }

    private void handleChannelJoin(ChatItemViewModel channelItem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Join Channel");
        alert.setHeaderText("Do you want to join the channel '" + channelItem.getDisplayName() + "'?");
        alert.setContentText("You will be added to the channel and can see its messages.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Packet p = new Packet(PacketType.ADD_MEMBER);
            p.setSenderId(myUserId);
            p.setContent(channelItem.getChatId().toString() + ";" + myUserId.toString());
            client.sendPacket(p);

            chatSearchField.clear();
            allChats.setAll(originalChats);
            isShowingSearchResults = false;

            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(e -> requestInitialChats());
            pause.play();
        }
    }

    private void sendSearchRequest() {
        String query = chatSearchField.getText().trim();
        if (query.isEmpty()) return;

        isShowingSearchResults = true;
        Packet searchPacket;
        if (query.startsWith("#")) {
            currentSearchMode = SearchMode.CHANNELS;
            searchPacket = new Packet(PacketType.SEARCH_CHANNELS);
            searchPacket.setContent(query.substring(1));
        } else {
            currentSearchMode = SearchMode.USERS;
            searchPacket = new Packet(PacketType.SEARCH_USER);
            searchPacket.setContent(query);
        }
        searchPacket.setSenderId(myUserId);
        client.sendPacket(searchPacket);
    }

    private void renderSearchResults(String json) {
        List<ChatItemViewModel> results = new ArrayList<>();
        if (currentSearchMode == SearchMode.USERS) {
            Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
            List<User> users = gson.fromJson(json, userListType);
            for (User user : users) {
                if (!user.getId().equals(myUserId)) {
                    results.add(new ChatItemViewModel(user.getId(), user.getProfileName(), ChatItemViewModel.ChatType.PRIVATE, "@" + user.getUsername(), 0, 0));
                }
            }
        } else {
            Type channelListType = new TypeToken<ArrayList<Channel>>() {}.getType();
            List<Channel> channels = gson.fromJson(json, channelListType);
            for (Channel channel : channels) {
                results.add(new ChatItemViewModel(channel.getChannelId(), channel.getChannelName(), ChatItemViewModel.ChatType.CHANNEL, "Click to join channel", 0, 0, channel.getChannelOwnerId()));
            }
        }
        allChats.setAll(results);
    }

    @FXML
    private void onCreateChat() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Create New Chat");
        dialog.setHeaderText("Create a new Group or Channel");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().addAll(mainPane.getScene().getStylesheets());
        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.setGraphic(null);

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Group", "Channel");
        typeComboBox.setValue("Group");
        typeComboBox.setMaxWidth(Double.MAX_VALUE);

        TextField nameField = new TextField();
        nameField.setPromptText("Enter name for chat");
        nameField.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("Chat Type:"), 0, 0);
        grid.add(typeComboBox, 1, 0);
        grid.add(new Label("Chat Name:"), 0, 1);
        grid.add(nameField, 1, 1);

        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(typeComboBox.getValue(), nameField.getText().trim());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(details -> {
            String type = details.getKey();
            String name = details.getValue();
            Packet p;
            if (type.equals("Group")) {
                p = new Packet(PacketType.CREATE_GROUP);
            } else {
                p = new Packet(PacketType.CREATE_CHANNEL);
            }
            p.setSenderId(myUserId);
            p.setContent(name);
            client.sendPacket(p);
        });
    }

    @FXML
    private void onAddMember() {
        if (currentChat == null) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Member");
        dialog.setHeaderText("Enter username to add to '" + currentChat.getDisplayName() + "'");
        dialog.setContentText("Username:");

        dialog.getDialogPane().getStylesheets().addAll(mainPane.getScene().getStylesheets());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(username -> {
            if (!username.trim().isEmpty()) {
                Packet p = new Packet(PacketType.ADD_MEMBER);
                p.setSenderId(myUserId);
                p.setContent(currentChat.getChatId().toString() + ";" + username.trim());
                client.sendPacket(p);
            }
        });
    }

    @FXML
    private void onViewMembers() {
        if (currentChat == null) return;
        Packet p = new Packet(PacketType.FETCH_MEMBERS);
        p.setSenderId(myUserId);
        p.setReceiverId(currentChat.getChatId());
        client.sendPacket(p);
    }

    @FXML
    private void onLeaveChat() {
        if (currentChat == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leave Chat");
        alert.setHeaderText("Are you sure you want to leave '" + currentChat.getDisplayName() + "'?");
        alert.setContentText("You will be removed from this chat.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Packet p = new Packet(PacketType.LEAVE_CHAT);
            p.setSenderId(myUserId);
            p.setContent(currentChat.getChatId().toString());
            client.sendPacket(p);

            clearChatView();

            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(e -> requestInitialChats());
            pause.play();
        }
    }

    @FXML
    private void onRenameChat() {
        if (currentChat == null) return;

        TextInputDialog dialog = new TextInputDialog(currentChat.getDisplayName());
        dialog.setTitle("Rename Chat");
        dialog.setHeaderText("Enter a new name for '" + currentChat.getDisplayName() + "'");
        dialog.setContentText("New Name:");

        dialog.getDialogPane().getStylesheets().addAll(mainPane.getScene().getStylesheets());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.equals(currentChat.getDisplayName())) {
                Packet p = new Packet(PacketType.RENAME_CHAT);
                p.setSenderId(myUserId);
                p.setContent(currentChat.getChatId().toString() + ";" + newName.trim());
                client.sendPacket(p);
            }
        });
    }

    private void showMembersDialog(String jsonContent) {
        Type listType = new TypeToken<ArrayList<MemberViewModel>>(){}.getType();
        List<MemberViewModel> members = gson.fromJson(jsonContent, listType);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Members of " + currentChat.getDisplayName());
        dialog.setHeaderText("List of all members:");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().addAll(mainPane.getScene().getStylesheets());
        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        ListView<MemberViewModel> listView = new ListView<>();
        listView.setCellFactory(param -> new MemberListCell());
        ObservableList<MemberViewModel> observableList = FXCollections.observableArrayList(members);
        listView.setItems(observableList);

        dialogPane.setContent(listView);
        dialog.showAndWait();
    }

    private class MemberListCell extends ListCell<MemberViewModel> {
        private final HBox hbox = new HBox(10);
        private final Label nameLabel = new Label();
        private final Label creatorLabel = new Label("(Creator)");
        private final Button kickButton = new Button("Kick");
        private final Region spacer = new Region();

        public MemberListCell() {
            creatorLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #7f6065;");
            kickButton.getStyleClass().add("header-button-leave");
            HBox.setHgrow(spacer, Priority.ALWAYS);
            hbox.getChildren().addAll(nameLabel, creatorLabel, spacer, kickButton);
            hbox.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(MemberViewModel member, boolean empty) {
            super.updateItem(member, empty);
            if (empty || member == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(member.getProfileName());
                creatorLabel.setVisible(member.isCreator());

                boolean amIOwner = currentChat != null && currentChat.getOwnerId() != null && currentChat.getOwnerId().equals(myUserId);
                kickButton.setVisible(amIOwner && !member.isCreator());

                kickButton.setOnAction(event -> {
                    Packet p = new Packet(PacketType.KICK_MEMBER);
                    p.setSenderId(myUserId);
                    p.setContent(currentChat.getChatId().toString() + ";" + member.getUserId().toString());
                    client.sendPacket(p);

                    getListView().getItems().remove(member);
                });

                setGraphic(hbox);
            }
        }
    }

    @FXML
    private void onToggleTheme() {
        isDarkMode = !isDarkMode;
        updateTheme();
    }

    private void updateTheme() {
        Scene scene = mainPane.getScene();
        if (scene == null) {
            Platform.runLater(this::updateTheme);
            return;
        }

        String lightThemePath = getClass().getResource("/org/project/client/views/css/styles.css").toExternalForm();
        String darkThemePath = getClass().getResource("/org/project/client/views/css/dark_styles.css").toExternalForm();

        scene.getStylesheets().clear();

        if (isDarkMode) {
            scene.getStylesheets().add(darkThemePath);
            mainPane.setStyle("-fx-background-color: #2F3136;");
            sidebarVBox.setStyle("-fx-background-color: #36393F;");
            themeToggleButton.setText("☀");
        } else {
            scene.getStylesheets().add(lightThemePath);
            mainPane.setStyle("-fx-background-color: #FAF0E6;");
            sidebarVBox.setStyle("-fx-background-color: #EFEBE6;");
            themeToggleButton.setText("🌙");
        }
    }

    private void startPacketListener() {
        if (client == null) return;
        listenerPool.submit(() -> {
            try {
                while (true) {
                    Packet packet = client.getReceivedPacket();
                    Platform.runLater(() -> handleIncoming(packet));
                }
            } catch (InterruptedException ignored) {
            }
        });
    }

    private void handleIncoming(Packet packet) {
        if (packet == null || packet.getType() == null) return;
        switch (packet.getType()) {
            case CHATS_LIST:
                UUID selectedChatId = (currentChat != null && !isShowingSearchResults) ? currentChat.getChatId() : null;
                List<ChatItemViewModel> chats = parseChats(packet.getContent());
                originalChats.setAll(chats);
                allChats.setAll(chats);
                if (selectedChatId != null) {
                    for (ChatItemViewModel newChat : allChats) {
                        if (newChat.getChatId().equals(selectedChatId)) {
                            chatListView.getSelectionModel().select(newChat);
                            break;
                        }
                    }
                }
                isShowingSearchResults = false;
                break;
            case MESSAGES_LIST:
                renderMessages(parseMessages(packet.getContent()));
                break;
            case SEARCH_RESULTS:
                renderSearchResults(packet.getContent());
                break;
            case NEW_MESSAGE:
                handleNewMessage(packet.getContent());
                break;
            case ONLINE_USERS_LIST:
                Type listType = new TypeToken<Set<UUID>>(){}.getType();
                Set<UUID> users = gson.fromJson(packet.getContent(), listType);
                onlineUsers.clear();
                onlineUsers.addAll(users);
                chatListView.refresh();
                break;
            case USER_ONLINE:
                onlineUsers.add(UUID.fromString(packet.getContent()));
                chatListView.refresh();
                break;
            case USER_OFFLINE:
                onlineUsers.remove(UUID.fromString(packet.getContent()));
                chatListView.refresh();
                break;
            case MEMBERS_LIST:
                Platform.runLater(() -> showMembersDialog(packet.getContent()));
                break;
            default:
                break;
        }
    }

    private void handleNewMessage(String jsonContent) {
        Message newMessage = gson.fromJson(jsonContent, Message.class);
        if (currentChat != null &&
                (newMessage.getReceiverId().equals(currentChat.getChatId()) ||
                        newMessage.getSenderId().equals(currentChat.getChatId()))) {
            messageListView.getItems().add(newMessage);
        }
    }

    @FXML
    private void onSend() {
        String text = Optional.ofNullable(messageField.getText()).orElse("").trim();
        if (text.isEmpty() || currentChat == null) return;

        Message local = new Message(UUID.randomUUID(), myUserId, currentChat.getChatId(), text, new Timestamp(System.currentTimeMillis()), "SENT", "You");
        messageListView.getItems().add(local);
        messageField.clear();

        Packet p = new Packet(PacketType.SEND_MESSAGE);
        p.setSenderId(myUserId);
        p.setReceiverId(currentChat.getChatId());
        p.setContent(text);
        client.sendPacket(p);

        PauseTransition pause = new PauseTransition(Duration.millis(500));
        pause.setOnFinished(event -> requestInitialChats());
        pause.play();
    }

    private void requestInitialChats() {
        if (client != null) {
            Packet p = new Packet(PacketType.FETCH_CHATS);
            p.setSenderId(myUserId);
            client.sendPacket(p);
        }
    }

    private void requestMessagesFor(ChatItemViewModel chat) {
        if (client != null && chat != null) {
            Packet p = new Packet(PacketType.FETCH_CHAT_HISTORY);
            p.setSenderId(myUserId);
            p.setReceiverId(chat.getChatId());
            client.sendPacket(p);
            messageListView.getItems().clear();
        }
    }

    private List<ChatItemViewModel> parseChats(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<ChatItemViewModel>>(){}.getType();
        return gson.fromJson(json, type);
    }

    private List<Message> parseMessages(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<Message>>(){}.getType();
        return gson.fromJson(json, type);
    }

    private void renderMessages(List<Message> messages) {
        messageListView.getItems().setAll(messages);
    }
}
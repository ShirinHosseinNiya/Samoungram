package org.project.models;



import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrivateChat implements Chats {
    private String chatId;
    private UUID participant1Id;
    private UUID participant2Id;
    private List<Message> chatMessageHistory;

    //constructor
    public PrivateChat(UUID participant1Id, UUID participant2Id) {
        this.participant1Id = participant1Id;
        this.participant2Id = participant2Id;
        this.chatId = generateChatId(participant1Id, participant2Id);
        this.chatMessageHistory = new ArrayList<>();
    }

    //getters
    public String getChatId() { return chatId; }
    public UUID getParticipant1Id() { return participant1Id; }
    public UUID getParticipant2Id() { return participant2Id; }
    public List<Message> getChatMessageHistory() { return chatMessageHistory; }

    public static String generateChatId(UUID id1, UUID id2) {
        return (id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1);        //ensures that both users will use yhe same chat id
    }
}

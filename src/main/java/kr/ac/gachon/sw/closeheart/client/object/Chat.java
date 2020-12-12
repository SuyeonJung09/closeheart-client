package kr.ac.gachon.sw.closeheart.client.object;

import java.util.Calendar;

public class Chat {
    int chatType;
    String chatOwner;
    String chatMsg;
    Calendar chatTime;

    public Chat(int chatType, String chatOwner, String chatMsg, Calendar chatTime) {
        this.chatType = chatType;
        this.chatOwner = chatOwner;
        this.chatMsg = chatMsg;
        this.chatTime = chatTime;
    }

    public int getChatType() {
        return chatType;
    }

    public String getChatOwner() {
        return chatOwner;
    }

    public String getChatMsg() {
        return chatMsg;
    }

    public Calendar getChatTime() {
        return chatTime;
    }
}

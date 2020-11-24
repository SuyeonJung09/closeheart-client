package kr.ac.gachon.sw.closeheart.client.customlayout.friendlist;

import kr.ac.gachon.sw.closeheart.client.user.User;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;

public class FriendListModel implements ListModel {
    private ArrayList<User> friendList;

    public FriendListModel() {
        friendList = new ArrayList<>();
    }

    @Override
    public int getSize() {
        return friendList.size();
    }

    @Override
    public User getElementAt(int index) {
        return friendList.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {

    }

    @Override
    public void removeListDataListener(ListDataListener l) {

    }

    public void add(User user) {
        friendList.add(user);
    }

    public void remove(int index) {
        friendList.remove(index);
    }
}

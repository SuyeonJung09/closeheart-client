package kr.ac.gachon.sw.closeheart.client.customlayout.friendlist;

import kr.ac.gachon.sw.closeheart.client.object.User;
import kr.ac.gachon.sw.closeheart.client.util.Util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class FriendListRenderer extends JPanel implements ListCellRenderer<User> {
    JPanel userInfo = new JPanel();
    JLabel userOnline = new JLabel();
    JLabel userNick = new JLabel();
    JLabel userMsg = new JLabel();;

    public FriendListRenderer() {
        // Layout 설정 및 Component 배치 등은 여기서 이루어져야 함
        // getListCellRendererComponent에서 이루어지면 Item들이 겹쳐서 나오는 문제가 있음
        this.setLayout(new BorderLayout());
        this.add(userInfo, BorderLayout.CENTER);
        this.add(userOnline, BorderLayout.EAST);

        userInfo.setLayout(new BorderLayout());
        userInfo.add(userNick, BorderLayout.NORTH);
        userInfo.add(userMsg, BorderLayout.SOUTH);

        this.setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList list, User value, int index, boolean isSelected, boolean cellHasFocus) {
        // Background Color는 White
        this.setBackground(new Color(255, 255, 255));

        // 닉네임 표시 Label
        userNick.setText(value.getUserNick());
        userNick.setFont(new Font(Util.getStrFromProperties(getClass(), "default_font"), Font.BOLD, 20));

        // 상태 메시지 표시 Label
        userMsg.setText(value.getUserMsg());
        userMsg.setFont(new Font(Util.getStrFromProperties(getClass(), "default_font"), Font.PLAIN, 15));

        if(value.getOnline()) userOnline.setText("Online");
        else userOnline.setText("Offline");
        userOnline.setFont(new Font(Util.getStrFromProperties(getClass(), "default_font"), Font.PLAIN, 12));

        // Item마다 LineBorder
        LineBorder lineBorder = new LineBorder(Color.GRAY, 1);
        this.setBorder(lineBorder);

        // EmptyBorder 설정으로 간격 띄워주기
        userInfo.setBorder(new EmptyBorder(5, 20, 5,0));
        userOnline.setBorder(new EmptyBorder(0, 0, 0,10));

        // 뒷 배경 하얀색
        this.setBackground(new Color(255, 255, 255));
        userInfo.setBackground(new Color(255, 255, 255));

        // JPanel 반환
        return this;
    }
}

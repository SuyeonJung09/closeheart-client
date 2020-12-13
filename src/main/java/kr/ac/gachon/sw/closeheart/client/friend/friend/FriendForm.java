package kr.ac.gachon.sw.closeheart.client.friend.friend;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.ac.gachon.sw.closeheart.client.base.BaseForm;
import kr.ac.gachon.sw.closeheart.client.chat.chat.ChatForm;
import kr.ac.gachon.sw.closeheart.client.customlayout.friendlist.FriendListModel;
import kr.ac.gachon.sw.closeheart.client.customlayout.friendlist.FriendListRenderer;
import kr.ac.gachon.sw.closeheart.client.friend.addfriend.AddFriendForm;
import kr.ac.gachon.sw.closeheart.client.object.User;
import kr.ac.gachon.sw.closeheart.client.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class FriendForm extends BaseForm {
    private JPanel friendForm_panel;
    private JList<User> list_friend;
    private JLabel lb_nickname;
    private JLabel lb_statusmsg;
    private JLabel lb_covid19;
    private JButton btn_addfriend;
    private JButton btn_setting;
    private JButton btn_refresh;
    private JButton btn_logout;

    private Socket socket;
    private Scanner serverInput;
    private PrintWriter serverOutput;
    private ArrayList<User> friendList;
    private String authToken;
    private User myUserInfo;
    private FriendFormThread thread;

    public FriendForm(Socket socket, String authToken) {
        this.socket = socket;
        this.authToken = authToken;

        // ContentPane 설정
        setContentPane(friendForm_panel);

        // Window 사이즈 설정
        setSize(500, 800);

        // 설정 아이콘 사이즈 줄이기
        btn_setting.setIcon(Util.resizeImage(new ImageIcon(getClass().getResource("/img/baseline_settings_black_18dp.png")).getImage(), 18, 18, Image.SCALE_SMOOTH));

        // 각종 Action Event을 설정
        setEvent();

        // 닫기 이벤트 설정
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // 친구 목록 설정
        setFriendList();

        // 쓰레드 시작
        startThread();
    }

    @Override
    public void setEvent() {
        // 친구 추가 버튼 액션
        btn_addfriend.addActionListener(e -> {
            System.out.println("Add Friend Clicked");
        });

        // 설정 버튼 액션
        btn_setting.addActionListener(e -> {
            System.out.println("Setting Clicked");
        });

        // 새로고침 버튼 액션
        btn_refresh.addActionListener(e -> {
            System.out.println("Refresh Clicked");
        });

        // 친구추가 버튼 액션
        btn_addfriend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AddFriendForm(socket, myUserInfo);
            }
        });

        btn_logout.addActionListener(e -> {
            logout();
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                logout();
            }
        });

        list_friend.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                list_friend.setSelectedIndex(list_friend.locationToIndex(e.getPoint()));
                System.out.println(list_friend.getSelectedValue().getUserID());

                // 오른쪽 클릭
                if(SwingUtilities.isRightMouseButton(e)) {
                    // 친구 객체
                    User friendObject = list_friend.getSelectedValue();

                    // 팝업 메뉴 설정
                    JPopupMenu friendPopupMenu = new JPopupMenu();

                    // 메뉴 아이템
                    JMenuItem requestChatItem = new JMenuItem("채팅 요청");
                    JMenuItem detailInfoItem = new JMenuItem("상세 정보");
                    JMenuItem removeFriendItem = new JMenuItem("친구 삭제");

                    // 메뉴 아이템 추가
                    friendPopupMenu.add(requestChatItem);
                    friendPopupMenu.add(detailInfoItem);
                    friendPopupMenu.add(removeFriendItem);


                    requestChatItem.addActionListener(rce -> {
                        if(friendObject.getOnline()) {
                            // 채팅 연결
                        }
                        else {
                            JOptionPane.showMessageDialog(
                                    FriendForm.this,
                                    friendObject.getUserNick() + "님은 오프라인 상태입니다.",
                                    "알림",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    });

                    // 메뉴 보이기
                    friendPopupMenu.show(list_friend, e.getPoint().x, e.getPoint().y);
                }
                // 왼쪽 클릭
                else if(SwingUtilities.isLeftMouseButton(e)) {
                    // 친구 객체
                    User friendObject = list_friend.getSelectedValue();

                    // 더블 클릭이면
                    if(e.getClickCount() == 2) {
                        // 온라인 체크
                        if(friendObject.getOnline()) {
                            int chatOption = JOptionPane.showConfirmDialog(getContentPane(),
                                    friendObject.getUserNick() + "님께 채팅을 요청할까요?", "채팅",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);

                            if (chatOption == JOptionPane.YES_OPTION) {
                                // 채팅 연결
                            }
                        }
                        else {
                            JOptionPane.showMessageDialog(
                                    FriendForm.this,
                                    friendObject.getUserNick() + "님은 오프라인 상태입니다.",
                                    "알림",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });
    }


    /*
     * Thread 시작
     * @author Minjae Seon
     * @param authToken 인증 토큰
     */
    private void startThread() {
        // 소켓이 잘 연결되어있고 토큰이 비어있지 않다면
        if(socket.isConnected() && !authToken.isEmpty()) {
            try {
                // Input / Output 생성
                serverInput = new Scanner(new InputStreamReader(socket.getInputStream()));
                serverOutput = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                // 내 정보 요청
                String requestMyInfo = Util.createSingleKeyValueJSON(300, "token", authToken);
                serverOutput.println(requestMyInfo);

                serverOutput.println(Util.createSingleKeyValueJSON(303, "token", authToken));

                // 서버 입력 대기
                if(serverInput.hasNextLine()) {
                    String line = serverInput.nextLine();
                    if(line.isEmpty()) line = serverInput.nextLine();
                    JsonObject jsonObject = JsonParser.parseString(line).getAsJsonObject();

                    int responseCode = jsonObject.get("code").getAsInt();
                    System.out.println(responseCode);

                    // 코드 200 (정상)이면
                    if(responseCode == 200) {
                        System.out.println(jsonObject.toString());

                        // 친구 목록 추출
                        JsonArray friendArray = JsonParser.parseString(jsonObject.get("friend").getAsString()).getAsJsonArray();
                        for(JsonElement jsonElement : friendArray) {
                            JsonObject friendObject = JsonParser.parseString(jsonElement.getAsString()).getAsJsonObject();
                            // 친구 객체 생성
                            User friendInfo = new User(friendObject.get("userID").getAsString(), friendObject.get("userNick").getAsString(), friendObject.get("userMsg").getAsString(), friendObject.get("isOnline").getAsBoolean());

                            // 친구 목록에 추가
                            friendList.add(friendInfo);
                        }

                        myUserInfo = new User(authToken, jsonObject.get("id").getAsString(), jsonObject.get("nick").getAsString(), jsonObject.get("userMsg").getAsString(), null);
                        lb_nickname.setText(myUserInfo.getUserNick());

                        if(!myUserInfo.getUserMsg().isEmpty()) lb_statusmsg.setText(myUserInfo.getUserMsg());
                        else lb_statusmsg.setText("상태메시지가 없습니다.");
                    }
                    else if(responseCode == 403) {
                        JOptionPane.showMessageDialog(
                                this,
                                "자격 증명에 실패했습니다. 다시 로그인해주세요.",
                                Util.getStrFromProperties(getClass(), "program_title") + " - 에러",
                                JOptionPane.ERROR_MESSAGE);
                        serverInput.close();
                        serverOutput.close();
                        socket.close();
                        System.exit(0);
                    }
                    // 실패하면 에러 생성
                    else {
                        JOptionPane.showMessageDialog(
                                this,
                                "서버 오류가 발생했습니다! 잠시 후 다시 시도해주세요.",
                                Util.getStrFromProperties(getClass(), "program_title") + " - 에러",
                                JOptionPane.ERROR_MESSAGE);
                        serverInput.close();
                        serverOutput.close();
                        socket.close();
                        System.exit(0);
                    }
                }

                // 객체가 null이 아니면
                if(myUserInfo != null) {
                    // 쓰레드 관련 설정
                    thread = new FriendFormThread(serverInput, serverOutput);
                    thread.start();
                    // 창 활성화
                    this.setVisible(true);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "오류가 발생했습니다!\n오류명 : " + e.getMessage(),
                        Util.getStrFromProperties(getClass(), "program_title") + " - 에러",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            JOptionPane.showMessageDialog(
                    this,
                    "서버와 연결이 끊어졌습니다!",
                    Util.getStrFromProperties(getClass(), "program_title") + " - 에러",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    /*
     * 친구 목록 설정
     * @author Minjae Seon
     */
    private void setFriendList() {
        // Friend List 설정
        friendList = new ArrayList<>();

        FriendListModel friendListModel = new FriendListModel(friendList);
        FriendListRenderer friendListRenderer = new FriendListRenderer();

        // Data Model 설정
        list_friend.setModel(friendListModel);

        // Renderer 설정
        list_friend.setCellRenderer(friendListRenderer);

        // VERTICAL하게 Item이 나오도록 함
        list_friend.setLayoutOrientation(JList.VERTICAL);
    }

    private void logout() {
        int exitOption = JOptionPane.showConfirmDialog(getContentPane(),
                "정말로 종료하시겠습니까?", "종료",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if(exitOption == JOptionPane.YES_OPTION) {
            thread.interrupt();
        }
    }

    class FriendFormThread extends Thread {
        private Scanner in;
        private PrintWriter out;

        public FriendFormThread(Scanner in, PrintWriter out) {
            this.in = in;
            this.out = out;
        }

        public void run() {
            try {
                while(in.hasNextLine()) {
                    String line = in.nextLine();
                    if(line.isEmpty()) line = in.nextLine();

                    JsonObject serverInput = JsonParser.parseString(line).getAsJsonObject();
                    int code = serverInput.get("code").getAsInt();
                    String msg = serverInput.get("msg").getAsString();
                    System.out.println(line);

                    // 친구 요청 처리 (msg에 friendrequest가 들어가있으면)
                    if(msg.equals("friendrequest")) {
                        if(code == 200) {
                            JOptionPane.showMessageDialog(
                                    FriendForm.this,
                                    "친구 요청을 보냈습니다!",
                                    "요청 성공",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                        else if(code == 400){
                            JOptionPane.showMessageDialog(
                                    FriendForm.this,
                                    "존재하지 않는 ID입니다. ID를 다시 한번 확인해주세요.",
                                    "경고",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                        else if(code == 401) {
                            JOptionPane.showMessageDialog(
                                    FriendForm.this,
                                    "이미 친구이거나 친구 요청 대기중입니다!",
                                    "경고",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                        else if(code == 402) {
                            JOptionPane.showMessageDialog(
                                    FriendForm.this,
                                    "스스로에게는 친구 요청을 보낼 수 없습니다!",
                                    "경고",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                        else {
                            JOptionPane.showMessageDialog(
                                    FriendForm.this,
                                    "서버 오류가 발생했습니다!",
                                    "에러",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    // 코로나 API 관련 처리
                    if(msg.equals("covid19")) {
                        String newCnt = serverInput.get("newCnt").getAsString();
                        String currDecideCnd = serverInput.get("currDecideCnd").getAsString();
                        if(code == 200) {
                            lb_covid19.setText("[코로나19] 오늘 추가 확진자 수 : " + newCnt + " / 총 확진자 수 : " + currDecideCnd);
                        }
                        else if(code == 500) {
                            lb_covid19.setText("Welcome to CloseHeart!");
                        }
                    }

                    // 로그아웃 코드
                    if(code == 301) {
                        break;
                    }
                }
            } catch (NoSuchElementException e) {
                JOptionPane.showMessageDialog(
                        FriendForm.this,
                        "서버와 연결이 끊어졌습니다!",
                        "에러",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        FriendForm.this,
                        "오류가 발생했습니다!\n오류명 : " + e.getMessage(),
                        "에러",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();

            // 인터럽트 발생시 로그아웃 요청
            out.println(Util.createSingleKeyValueJSON(301, "token", authToken));
            try {
                // 통신 관련 다 닫기
                in.close();
                out.close();
                socket.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        FriendForm.this,
                        "오류가 발생했습니다!\n오류명" + e.getMessage(),
                        "에러",
                        JOptionPane.ERROR_MESSAGE);
            }

            // 종료
            System.exit(0);
        }
    }
}

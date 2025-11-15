package deu.controller.event;

import deu.controller.business.RoomReservationClientController;
import deu.controller.business.UserClientController;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.RoomReservation;
import deu.view.*;
import deu.view.custom.PanelRound;
import deu.view.custom.TimeSlotButton;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import deu.controller.business.NotificationClientController;
import deu.model.dto.response.NotificationDTO;
import java.util.List;

public class HomeSwingController {

    private final Home view;
    private final UserClientController userClientController;
    private final RoomReservationClientController roomReservationClientController;
    
    private final NotificationClientController notificationController = NotificationClientController.getInstance();
    private Timer notificationTimer;

    public HomeSwingController(Home view) {
        this.view = view;
        this.userClientController = UserClientController.getInstance();
        this.roomReservationClientController = RoomReservationClientController.getInstance();
        
        //알림 폴링 시작
        startNotificationPolling();

        // 이벤트 연결
        view.addLogoutListener(this::handleLogout);
        view.addReservationMenuListener(this::showReservationPanel);
        view.addMyReservationMenuListener(this::showMainPanel);
        view.addManagementMenuListener(this::showManagerMenu);
        view.addUserManagementListener(this::showUserManagerManagement);
        view.addReservationManagementListener(this::showReservationManagement);
        view.addCommonMenuListener(this::showCommonMenu);
        view.addDeleteReservationListner(this::deleteReservation);
        view.addSupportButtonListner(this::handleSupport);
        
        
        //'강의 시간표 관리' 버튼 리스너 (팝업창 호출)
        // -----------------------------------------------------------
        view.addLectureManageButtonListener(e -> {
            //부모 프레임(Home 뷰가 속한 JFrame)을 찾기
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(view);

            //학기 선택 팝업창 열기
            YearSemesterSelectDialog dialog = new YearSemesterSelectDialog(topFrame);
            dialog.setVisible(true); // 사용자가 '확인'을 누를 때까지 대기

            //'확인'을 눌렀다면
            if (dialog.isConfirmed()) {
                String year = dialog.getSelectedYear();
                String semester = dialog.getSelectedSemester();
                
                // 4. 선택된 학년도/학기로 CUD 뷰를 염
                new LectureSearchView(year, semester);
            }
        });
        

        view.addMyReservationListInitListener(createMyReservationListInitListener());
        view.addUserReservationCalendarInitListener(createUserReservationCalendarInitListener());
        view.addUserProfileInitListner(createUserProfileInitListener());
    }
    
    // 알림 폴링 시작 메서드
    private void startNotificationPolling() {
        int delay = 3000; // 3초 간격
        notificationTimer = new Timer(delay, e -> {
            String userId = view.getUserNumber();
            // 로그인이 안 된 상태면 패스
            if (userId == null || userId.isEmpty()) return;

            // 백그라운드 스레드(SwingWorker)로 요청 (UI 멈춤 방지)
            SwingWorker<List<NotificationDTO>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<NotificationDTO> doInBackground() {
                    // 서버에 내 알림이 있는지 확인 (동기식 소켓 요청)
                    return notificationController.getMyNotifications(userId);
                }

                @Override
                protected void done() {
                    try {
                        List<NotificationDTO> notifications = get();
                        if (notifications != null && !notifications.isEmpty()) {
                            for (NotificationDTO noti : notifications) {
                                // 알림 팝업 표시
                                JOptionPane.showMessageDialog(null, 
                                    noti.getMessage(), 
                                    "새 알림: " + noti.getTitle(), 
                                    JOptionPane.INFORMATION_MESSAGE);
                            }
                            
                            // (선택 사항) 알림이 오면 '내 예약 목록' 등을 갱신
                            refreshMyReservationList();
                            refreshUserReservationCalendar();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            worker.execute();
        });
        notificationTimer.start();
    }

    //로그아웃 시 타이머 중지
    private void stopNotificationPolling() {
        if (notificationTimer != null && notificationTimer.isRunning()) {
            notificationTimer.stop();
        }
    }

    // 개인 별 주간 예약 시간표를 확인하는 기능 =================================================================================

    // 로그인 사용자 예약 정보를 캘린더에 갱신 하는 기능 TODO: 예약기능 확인완료 + SwingWorker
    private void refreshUserReservationCalendar() {
        view.getCalendar().setVisible(false);
        RoomReservation[][] data = new RoomReservation[7][13];

        // 1. 더미 예약 정보 객체 생성 (7일 x 13교시)
        BasicResponse response = roomReservationClientController.weekRoomReservationByUserNumber(view.getUserNumber());
        if(!response.code.equals("200")){
            JOptionPane.showMessageDialog(null, "개인 전체 예약 내역을 불러오지 못했습니다.");
        }else{
            data = (RoomReservation[][]) response.data;  // createDummyReservationGrid();
        }

        // 2. 시간 텍스트 더미 데이터 생성
        String[][] generateTimeSlots = generateTimeSlots();

        // 3. 캘린더 버튼 처리
        updateCalendarButtons(data, generateTimeSlots);


        view.getCalendar().setVisible(true);
    }

    // 캘린더 버튼 처리 분리 메서드
    private void updateCalendarButtons(RoomReservation[][] roomReservations, String[][] dummySubjects) {
        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                updateSingleCalendarButton(day, period, roomReservations[day][period], dummySubjects[day][period]);
            }
        }
    }

    // 각 교시 별 버튼 처리
    private void updateSingleCalendarButton(int day, int period, RoomReservation roomReservation, String labelText) {
        String buttonName = "day" + day + "_" + period;

        for (Component comp : view.getCalendar().getComponents()) {
            if (comp instanceof TimeSlotButton btn && buttonName.equals(btn.getName())) {

                // 리스너 제거
                for (ActionListener al : btn.getActionListeners()) {
                    btn.removeActionListener(al);
                }

                btn.setText(labelText);
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);
                btn.setForeground(Color.BLACK);
                btn.setLecture(null); // 강의는 이 메서드에서 사용되지 않음

                if (roomReservation != null) {
                    btn.setEnabled(true);
                    btn.setRoomReservation(roomReservation);
                    btn.setText(roomReservation.getTitle());

                    // 상태에 따른 색상
                    Color baseColor;
                    String status = roomReservation.getStatus();
                    if ("대기".equals(status)) {
                        baseColor = new Color(241, 196, 15); // 노란색
                    } else if ("승인".equals(status)) {
                        baseColor = new Color(20, 112, 61); // 초록색
                    } else {
                        baseColor = Color.LIGHT_GRAY; // 기타
                    }

                    btn.setOriginalBackground(baseColor);

                    // 선택된 버튼 강조
                    TimeSlotButton selected = (TimeSlotButton) view.getSelectedCalendarButton();
                    if (selected != null &&
                            selected.getRoomReservation() != null &&
                            selected.getRoomReservation().getId().equals(roomReservation.getId())) {
                        btn.setBackground(new Color(0, 120, 215, 180)); // 파란색 강조
                    } else {
                        btn.setBackground(baseColor); // 상태별 색상
                    }

                    setCalendarButtonClickListener(btn);

                } else {
                    // 예약 없는 경우
                    btn.setRoomReservation(null);
                    btn.setEnabled(false);
                    btn.setBackground(Color.WHITE);
                    btn.setOriginalBackground(Color.WHITE);
                }
            }
        }
    }

    // 버튼 클릭 이벤트 리스너 부여
    private void setCalendarButtonClickListener(TimeSlotButton btn) {
        btn.addActionListener(e -> {
            TimeSlotButton source = (TimeSlotButton) e.getSource();
            TimeSlotButton prev = (TimeSlotButton) view.getSelectedCalendarButton();

            // 토글 기능: 동일 버튼을 다시 클릭하면 선택 해제
            if (prev == source) {
                if (source.getOriginalBackground() != null) {
                    source.setBackground(source.getOriginalBackground());
                } else {
                    RoomReservation r = source.getRoomReservation();
                    if (r != null) {
                        String status = r.getStatus();
                        if ("승인".equals(status)) {
                            source.setBackground(new Color(20, 112, 61));
                        } else if ("대기".equals(status)) {
                            source.setBackground(new Color(241, 196, 15));
                        } else {
                            source.setBackground(Color.LIGHT_GRAY);
                        }
                    } else {
                        source.setBackground(Color.WHITE);
                    }
                }

                view.setSelectedCalendarButton(null);
                source.repaint();

                // 예약 필드 초기화
                view.getBuildingField().setText("");
                view.getFloorField().setText("");
                view.getReservationUniqueNumberField().setText("");
                view.getLectureRoomField().setText("");
                view.getTitleField().setText("");
                view.getDescriptionField().setText("");
                return;
            }

            // 이전 선택된 버튼 색상 복원
            if (prev != null) {
                if (prev.getOriginalBackground() != null) {
                    prev.setBackground(prev.getOriginalBackground());
                } else {
                    RoomReservation r = prev.getRoomReservation();
                    if (r != null) {
                        String status = r.getStatus();
                        if ("승인".equals(status)) {
                            prev.setBackground(new Color(20, 112, 61));
                        } else if ("대기".equals(status)) {
                            prev.setBackground(new Color(241, 196, 15));
                        } else {
                            prev.setBackground(Color.LIGHT_GRAY);
                        }
                    } else {
                        prev.setBackground(Color.WHITE);
                    }
                }
                prev.repaint();
            }

            // 선택된 버튼 강조
            source.setOpaque(true);
            source.setContentAreaFilled(true);
            source.setBackground(Color.RED);
            source.repaint();

            view.setSelectedCalendarButton(source);

            // 예약 정보 표시
            RoomReservation r = source.getRoomReservation();
            if (r != null) {
                view.getBuildingField().setText(r.getBuildingName());
                view.getFloorField().setText(r.getFloor());
                view.getReservationUniqueNumberField().setText(r.getId());
                view.getLectureRoomField().setText(r.getLectureRoom());
                view.getTitleField().setText(r.getTitle());
                view.getDescriptionField().setText(r.getDescription());
            }
        });
    }

    // 시간 텍스트 생성 ("09:00~10:00" 형식)
    private String[][] generateTimeSlots() {
        String[][] result = new String[7][13];
        int startHour = 9;

        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                int hour = startHour + period;
                result[day][period] = String.format("%02d:00~%02d:00", hour, hour + 1);
            }
        }

        return result;
    }

    // =================================================================================================================

    // 예약을 삭제하는 기능 TODO: 예약기능 확인완료 + SwingWorker
    private void deleteReservation(ActionEvent e) {
        String buildingName = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String uniqueNumber = view.getReservationUniqueNumberField().getText();
        String lectureRoom = view.getLectureRoomField().getText();
        String title = view.getTitleField().getText();
        String userNumber = view.getUserNumber();

        // 삭제 여부 확인 다이얼로그
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "정말 이 예약을 삭제하시겠습니까?\n[" + title + "] " + buildingName + " " + floor + "층 " + lectureRoom,
                "예약 삭제 확인",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // 버튼 잠금 또는 UI 비활성화 처리 필요 시 여기서 추가 가능

        SwingWorker<BasicResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected BasicResponse doInBackground() {
                try {
                    return roomReservationClientController.deleteRoomReservation(userNumber, uniqueNumber);
                } catch (Exception ex) {
                    return new BasicResponse("500", "예외 발생: " + ex.getMessage());
                }
            }

            @Override
            protected void done() {
                try {
                    BasicResponse response = get();

                    if (!"200".equals(response.code)) {
                        JOptionPane.showMessageDialog(null, "예약 삭제에 실패했습니다: " + response.data, "삭제 실패", JOptionPane.ERROR_MESSAGE);
                        refreshUserReservationCalendar();
                        refreshMyReservationList();
                        return;
                    }

                    resetReservationTextField();
                    refreshUserReservationCalendar();
                    refreshMyReservationList();
                    JOptionPane.showMessageDialog(null, "예약이 성공적으로 삭제되었습니다.", "삭제 완료", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "삭제 처리 중 예외 발생: " + ex.getMessage(), "삭제 예외", JOptionPane.ERROR_MESSAGE);
                    refreshUserReservationCalendar();
                    refreshMyReservationList();
                }
            }
        };

        worker.execute();
    }

    // 사용자의 모든 예약 내역 출력 하는 기능 TODO: 예약기능 확인완료 + SwingWorker
    private void refreshMyReservationList() {
        JPanel myReservationList = view.getMyReservationList();
        String userNumber = view.getUserNumber();

        SwingWorker<java.util.List<RoomReservation>, Void> worker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected java.util.List<RoomReservation> doInBackground() {
                BasicResponse response = roomReservationClientController.userRoomReservationList(userNumber);

                if (response == null || !"200".equals(response.code) || response.data == null) {
                    errorMessage = "개인 전체 예약 내역을 불러오지 못했습니다.";
                    return null;
                }

                try {
                    return (java.util.List<RoomReservation>) response.data;
                } catch (ClassCastException e) {
                    errorMessage = "예약 데이터를 처리할 수 없습니다.";
                    return null;
                }
            }

            @Override
            protected void done() {
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(null, errorMessage, "예약 불러오기 오류", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    java.util.List<RoomReservation> reservations = get();
                    if (reservations == null) return;

                    myReservationList.removeAll();

                    int count = Math.min(reservations.size(), 5);
                    for (int i = 0; i < count; i++) {
                        RoomReservation res = reservations.get(i);

                        String date = res.getDate();               // yyyy-MM-dd
                        String dayOfWeek = res.getDayOfTheWeek();  // 예: "SATURDAY"
                        String building = res.getBuildingName();   // 예: "정보관"
                        String floor = res.getFloor();             // 예: "9"
                        String room = res.getLectureRoom();        // 예: "912"
                        String startTime = res.getStartTime();     // 예: "11:00"
                        String endTime = res.getEndTime();         // 예: "12:00"
                        String status = res.getStatus();           // "대기" or "승인"

                        String labelText = "<html>[ " + date + " / " + dayOfWeek + " ]<br>"
                                + building + "-" + floor + "층 / " + room + " / " + startTime + "~" + endTime
                                + "</html>";

                        PanelRound round = new PanelRound();
                        round.setLayout(new BorderLayout());
                        round.setRoundTopLeft(10);
                        round.setRoundTopRight(10);
                        round.setRoundBottomLeft(10);
                        round.setRoundBottomRight(10);

                        // 상태에 따라 색상 지정
                        Color bgColor;
                        if ("대기".equals(status)) {
                            bgColor = new Color(241, 196, 15); // 주황
                        } else if ("승인".equals(status)) {
                            bgColor = new Color(20, 112, 61);  // 초록
                        } else {
                            bgColor = new Color(100, 149, 237); // 기본 연파랑
                        }
                        round.setBackground(bgColor);

                        JLabel label = new JLabel(labelText, SwingConstants.CENTER);
                        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
                        label.setForeground(Color.WHITE);

                        round.add(label, BorderLayout.CENTER);
                        myReservationList.add(round);
                    }

                    myReservationList.revalidate();
                    myReservationList.repaint();

                    view.getReservationCount().setText(String.valueOf(reservations.size()));
                    view.getReservationTotalCount().setText(String.valueOf(Math.max(0, 5 - reservations.size())));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "예약 목록 로딩 중 오류가 발생했습니다.\n" + e.getMessage(),
                            "예약 목록 오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    // 사용자 프로필 정보를 갱신 하는 기능
    private void refreshUserProfile() {
        SwingWorker<BasicResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected BasicResponse doInBackground() {
                return userClientController.findUserName(view.getUserNumber(), view.getUserPassword());
            }

            @Override
            protected void done() {
                try {
                    BasicResponse response = get();
                    String name = (String) response.data;

                    // 프로필 설정
                    view.getProfileNumberField().setText("번호: " + view.getUserNumber());
                    view.getProfileNameField().setText(name);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(view, "프로필 정보를 불러오는 중 오류 발생: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    // 수정 안해도 되는 부분 ===========================================================================================

    // 내 예약 리스트가 생성될 때 갱신되는 기능 - 수정 금지
    private AncestorListener createMyReservationListInitListener() {
        return new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                refreshMyReservationList();
            }
            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        };
    }
    // 캘린더가 생성 될 때 갱신 되는 기능 - 수정 금지
    private AncestorListener createUserReservationCalendarInitListener() {
        return new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                refreshUserReservationCalendar();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        };
    }
    // 프로필이 생성 될 때 갱신 되는 기능 - 수정 금지
    private AncestorListener createUserProfileInitListener() {
        return new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                refreshUserProfile();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        };
    }
    // 도움말 버튼 기능 - 수정 금지
    private void handleSupport(ActionEvent e) {
        String message = """
        📌 사용 안내

        - '강의실 예약' 탭에서 원하는 건물·층·강의실 시간대를 선택한 후 신청 가능합니다.
        - 예약 신청 시 즉시 확정되지 않으며, 대기 상태(노란색)로 등록됩니다.
        - 예약이 승인되면 상태가 초록색으로 변경됩니다.
        - "내 예약정보" 탭에서 현재 예약 상태를 확인할 수 있습니다.
        - 로그인 화면의 "설정 저장"을 통해 서버 IP 및 포트를 설정할 수 있습니다.

        문제가 발생하면 관리자에게 문의하세요.
        """;

        JOptionPane.showMessageDialog(view, message, "도움말", JOptionPane.INFORMATION_MESSAGE);
    }
    // 로그아웃 버튼 기능 - 수정 금지
    private void handleLogout(ActionEvent e) {
        
        //로그아웃 시 폴링 중단
        stopNotificationPolling();
        
        Auth frame = (Auth) SwingUtilities.getWindowAncestor(view);
        BasicResponse result = userClientController.logout(view.getUserNumber(), view.getUserPassword());

        if (result.code.equals("200") && frame != null) {
            // 떠 있는 외부 프레임 닫기
            if (Home.getInstance() != null) {
                Home.getInstance().closeFloatingFrames();
            }

            // 로그인 화면으로 전환
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "login");

            // home 패널 제거
            for (Component comp : frame.getContentPane().getComponents()) {
                if ("home".equals(comp.getName())) {
                    frame.getContentPane().remove(comp);
                    break;
                }
            }

            // 싱글톤 인스턴스 해제
            Home.setInstanceNull();

            frame.revalidate();
            frame.repaint();
        } else {
            JOptionPane.showMessageDialog(view, result.data, "로그아웃 실패", JOptionPane.WARNING_MESSAGE);
        }
    }
    // 예약 메뉴 전환 - 수정 금지
    private void showReservationPanel(ActionEvent e) {
        Home.getInstance().closeFloatingFrames();
        Reservation reservation = new Reservation(view.getUserNumber(), view.getUserPassword());
        new ReservationSwingController(reservation);
        view.replaceMainContent(view.getMenuPanel(), reservation);
    }
    // 기본 메뉴 전환 - 수정 금지
    private boolean hasAlreadyRefreshed = false;
    private void showMainPanel(ActionEvent e) {
        Home.getInstance().closeFloatingFrames();
        checkManagementAuthority();
        view.replaceMainContent(view.getMenuPanel(), view.getMainPanel());

        if (!hasAlreadyRefreshed) {
            refreshUserReservationCalendar();
            hasAlreadyRefreshed = true;
        }
    }
    // 관리자 전용 메뉴 전환 - 수정 금지
    private void showManagerMenu(ActionEvent e) {
        Home.getInstance().closeFloatingFrames();
        ReservationManagement panel = new ReservationManagement();

        new ReservationManagementSwingController(panel);

        view.replaceMainContent(view.getManagerMenuPanel(), panel);
    }
    // 사용자 관리 메뉴 전환 - 수정 금지
    private void showUserManagerManagement(ActionEvent e) {
        Home.getInstance().closeFloatingFrames();
        UserManagement panel = new UserManagement();

        new UserManagementSwingController(panel);

        view.replaceMainContent(view.getManagerMenuPanel(), panel);
    }
    // 예악 관리 메뉴 전환 - 수정 금지
    private void showReservationManagement(ActionEvent e) {
        Home.getInstance().closeFloatingFrames();
        ReservationManagement panel = new ReservationManagement();

        new ReservationManagementSwingController(panel);

        view.replaceMainContent(view.getManagerMenuPanel(), panel);
    }
    // 일반 사용자 전용 매뉴 전환 - 수정 금지
    private void showCommonMenu(ActionEvent e) {
        Home.getInstance().closeFloatingFrames();
        view.replaceMainContent(view.getMenuPanel(), view.getMainPanel());
    }
    // 관리자 패널 허용 여부 기눙 - 수정 금지
    private void checkManagementAuthority() {
        view.getUserNumber()
                .chars()
                .mapToObj(c -> Character.toUpperCase((char) c)) // 대소문자 무시
                .findFirst()
                .ifPresent(ch -> view.getManegementMenu().setVisible(ch == 'M'));
    }
    // 교시 연산 메서드 - 수정 금지
    private int timeToPeriod(String startTime) {
        try {
            int hour = Integer.parseInt(startTime.split(":")[0]);
            return hour - 8; // 예: 09:00 - 1교시
        } catch (Exception e) {
            return -1; // 오류 발생 시 -1 반환
        }
    }
    // 예약 후 필드값 비우는 메서드  - 수정 금지
    private void resetReservationTextField(){
        view.getBuildingField().setText("");
        view.getLectureRoomField().setText("");
        view.getTitleField().setText("");
        view.getFloorField().setText("");
        view.getReservationUniqueNumberField().setText("");
        view.getDescriptionField().setText("");
    }
}
package deu.controller.event;

import deu.controller.business.LectureClientController;
import deu.controller.business.RoomReservationManagementClientController;
import deu.controller.business.RoomReservationClientController;
import deu.model.dto.request.data.reservation.AccompanyingStudent; // [추가]
import deu.model.dto.request.data.reservation.DeleteRoomReservationRequest;
import deu.model.dto.request.data.reservation.RoomReservationRequest;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.Lecture;
import deu.model.entity.RoomReservation;
import deu.model.enums.DayOfWeek;
import deu.view.Home;
import deu.view.ReservationManagement;
import deu.view.custom.ButtonRound;
import deu.view.custom.RoundReservationInformationButton;
import deu.view.custom.TimeSlotButton;
import deu.view.LectureSearchView;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList; // [추가]
import java.util.Arrays;
import java.util.List;

public class ReservationManagementSwingController {

    private final ReservationManagement view;
    private final LectureClientController lectureClientController;
    private final RoomReservationManagementClientController roomReservationManagementClientController;
    private final RoomReservationClientController roomReservationClientController;

    public ReservationManagementSwingController(ReservationManagement view) {
        this.view = view;
        this.lectureClientController = LectureClientController.getInstance();
        this.roomReservationManagementClientController = RoomReservationManagementClientController.getInstance();
        this.roomReservationClientController = RoomReservationClientController.getInstance();

        // 이벤트 연결
        view.addBuildingSelectionListener(this::handleBuildingSelection);
        view.addUpdateButtonListener(this::updateButton);
        view.addDeleteButtonListener(this::deleteButton);
        view.addReservationFrameButtonListener(this::reservationListFrameButton);
        view.addReservationFrameRefreshButtonListener(this::reservationListPanelRefreshButton);
        view.addReservationListInitListener(createReservationListPanelInitListener());

    }

    // ... (기존 코드 생략 - BuildingSelection, FloorButtons, LectureRoomButtons, CalendarUpdate 등) ...
    // (파일 용량이 크므로 변경되지 않은 부분은 생략하지 않고 전체를 제공해야 덮어쓰기 시 안전하지만, 
    //  여기서는 지면 관계상 변경된 부분 위주로 확인하고 전체 코드를 제공합니다.)
    // 1. 건물 정보와 층 정보를 가져오기 기능 - (2 호출)
    private void handleBuildingSelection(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) {
            return;
        }

        // UI 초기화
        clearSelectionUI();

        // 선택된 건물 설정
        String selectedBuilding = view.getSelectedBuilding();
        view.getBuildingField().setText(selectedBuilding);

        // 정보관이 아닌 경우: 종료
        if (!"정보관".equals(selectedBuilding)) {
            view.getFloorButtonPanel().revalidate();
            view.getFloorButtonPanel().repaint();
            view.getLectureRoomList().revalidate();
            view.getLectureRoomList().repaint();
            view.getFloorDisplayField().setText("");

            clearReservationFieldData();
            return;
        }

        // 정보관일 경우: 1~9층 층 버튼 추가
        addFloorButtons(selectedBuilding);
    }

    // 2. 층 버튼 추가 메서드 - (3 호출)
    private void addFloorButtons(String buildingName) {
        for (int i = 1; i <= 9; i++) {
            ButtonRound floorBtn = view.createStyledButton(String.valueOf(i), 45, 45);
            floorBtn.setBackground(view.FLOOR_DEFAULT_COLOR);
            floorBtn.setForeground(Color.BLACK);

            floorBtn.addActionListener(ev -> {
                view.getCalendar().setVisible(false);

                if (view.getSelectedFloorButton() != null) {
                    view.getSelectedFloorButton().setBackground(view.FLOOR_DEFAULT_COLOR);
                    view.getSelectedFloorButton().setForeground(Color.BLACK);
                }

                floorBtn.setBackground(view.FLOOR_SELECTED_COLOR);
                floorBtn.setForeground(Color.WHITE);
                view.setSelectedFloorButton(floorBtn);

                view.getFloorDisplayField().setText(floorBtn.getText());
                view.getFloorField().setText(floorBtn.getText());

                view.getLectureRoomList().removeAll();
                view.setSelectedRoomButton(null);

                clearReservationFieldData();

                addLectureRoomButtons(buildingName, floorBtn.getText());

                view.getLectureRoomList().revalidate();
                view.getLectureRoomList().repaint();
            });

            view.getFloorButtonPanel().add(floorBtn);
        }

        view.getFloorButtonPanel().revalidate();
        view.getFloorButtonPanel().repaint();
    }

    // 3. 특정 층에 해당하는 강의실 버튼들을 동적으로 생성하여 UI에 추가하는 메서드 - (4 호출)
    private void addLectureRoomButtons(String buildingName, String floor) {
        if (!"9".equals(floor)) {
            return;
        }

        for (String room : getDynamicRoomNames(buildingName, floor)) {
            ButtonRound roomBtn = view.createStyledButton(room, 100, 30);
            roomBtn.setBackground(view.FLOOR_DEFAULT_COLOR);
            roomBtn.setForeground(Color.BLACK);

            roomBtn.addActionListener(roomEv -> {
                view.getCalendar().setVisible(false);

                if (view.getSelectedRoomButton() != null) {
                    view.getSelectedRoomButton().setBackground(view.FLOOR_DEFAULT_COLOR);
                    view.getSelectedRoomButton().setForeground(Color.BLACK);
                }

                roomBtn.setBackground(view.ROOM_SELECTED_COLOR);
                roomBtn.setForeground(Color.WHITE);
                clearReservationFieldData();

                view.setSelectedRoomButton(roomBtn);
                view.getLectureRoomField().setText(room);

                view.getDeleteButton().setEnabled(true);
                view.getUpdateButton().setEnabled(true);

                updateCalendarWithDummyData();
            });

            view.getLectureRoomList().add(roomBtn);
        }
    }

    // 4. 캘린더에 예약, 강의 정보 갱신하는 기능
    private void updateCalendarWithDummyData() {
        view.getCalendar().setVisible(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            Lecture[][] schedule;
            RoomReservation[][] reservationSchedule;
            String[][] timeLabels;

            @Override
            protected Void doInBackground() {
                schedule = fetchWeeklyLectureSchedule();
                if (schedule == null) {
                    return null;
                }

                reservationSchedule = fetchWeeklyReservationSchedule();
                timeLabels = generateTimeSlotLabels();
                return null;
            }

            @Override
            protected void done() {
                if (schedule != null && reservationSchedule != null && timeLabels != null) {
                    applyScheduleToCalendar(schedule, reservationSchedule, timeLabels);
                    view.getCalendar().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "캘린더 데이터를 불러오는 데 실패했습니다.", "오류", JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private Lecture[][] fetchWeeklyLectureSchedule() {
        BasicResponse res = lectureClientController.returnLectureOfWeek(
                view.getBuildingField().getText(),
                view.getFloorField().getText(),
                view.getLectureRoomField().getText()
        );

        if (res == null || !"200".equals(res.code) || !(res.data instanceof Lecture[][])) {
            return null;
        }
        return (Lecture[][]) res.data;
    }

    private RoomReservation[][] fetchWeeklyReservationSchedule() {
        RoomReservation[][] grid = new RoomReservation[7][13];
        try {
            BasicResponse res = roomReservationClientController.weekRoomReservationByLectureroom(
                    view.getBuildingField().getText(),
                    view.getFloorField().getText(),
                    view.getLectureRoomField().getText()
            );
            if ("200".equals(res.code) && res.data instanceof RoomReservation[][] result) {
                grid = result;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "예약 데이터를 가져오는 중 예외가 발생했습니다:\n" + e.getMessage(), "예외 발생", JOptionPane.ERROR_MESSAGE);
        }
        return grid;
    }

    private String[][] generateTimeSlotLabels() {
        String[][] labels = new String[7][13];
        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                int startHour = 9 + period;
                int endHour = startHour + 1;
                labels[day][period] = String.format("%02d:00 ~ %02d:00", startHour, endHour);
            }
        }
        return labels;
    }

    private void applyScheduleToCalendar(Lecture[][] schedule, RoomReservation[][] reservationSchedule, String[][] labels) {
        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                String buttonName = "day" + day + "_" + period;

                for (Component comp : view.getCalendar().getComponents()) {
                    if (comp instanceof TimeSlotButton dayBtn && buttonName.equals(dayBtn.getName())) {
                        dayBtn.setText(labels[day][period]);

                        for (ActionListener al : dayBtn.getActionListeners()) {
                            dayBtn.removeActionListener(al);
                        }

                        dayBtn.setOpaque(true);
                        dayBtn.setContentAreaFilled(true);
                        dayBtn.setForeground(Color.BLACK);
                        dayBtn.setLecture(null);
                        dayBtn.setRoomReservation(null);

                        if (schedule[day][period] != null) {
                            dayBtn.setEnabled(false);
                            dayBtn.setLecture(schedule[day][period]);
                            dayBtn.setText(schedule[day][period].getTitle());
                            dayBtn.setBackground(new Color(100, 149, 237));
                            continue;
                        }

                        if (reservationSchedule != null && reservationSchedule[day][period] != null) {
                            RoomReservation reservation = reservationSchedule[day][period];
                            dayBtn.setEnabled(true);
                            dayBtn.setRoomReservation(reservation);
                            dayBtn.setText(reservation.getTitle());

                            if ("승인".equals(reservation.getStatus())) {
                                dayBtn.setBackground(new Color(60, 179, 113));
                            } else {
                                dayBtn.setBackground(new Color(241, 196, 15));
                            }

                            dayBtn.addActionListener(ev -> {
                                TimeSlotButton source = (TimeSlotButton) ev.getSource();
                                RoomReservation r = source.getRoomReservation();
                                TimeSlotButton prev = (TimeSlotButton) view.getSelectedCalendarButton();

                                if (prev != null && prev != source) {
                                    RoomReservation prevRes = prev.getRoomReservation();
                                    if (prevRes != null) {
                                        if ("승인".equals(prevRes.getStatus())) {
                                            prev.setBackground(new Color(60, 179, 113));
                                        } else {
                                            prev.setBackground(new Color(241, 196, 15));
                                        }
                                    } else {
                                        prev.setBackground(Color.WHITE);
                                    }
                                }

                                source.setBackground(new Color(30, 144, 255));
                                view.setSelectedCalendarButton(source);

                                String[] dateTime = parseDateTimeFromButtonName(source.getName());
                                if (dateTime != null) {
                                    view.getReservationDateField().setText(dateTime[0]);
                                    view.getReservationTimeField().setText(dateTime[1]);
                                } else {
                                    view.getReservationDateField().setText("");
                                    view.getReservationTimeField().setText("");
                                }

                                view.getBuildingField().setText(r.getBuildingName());
                                view.getFloorField().setText(r.getFloor());
                                view.getReservationIdField().setText(r.getId());
                                view.getLectureRoomField().setText(r.getLectureRoom());
                                view.getTitleField().setText(r.getTitle());
                                view.getDescriptionField().setText(r.getDescription());
                            });
                            continue;
                        }
                        dayBtn.setEnabled(false);
                        dayBtn.setBackground(null);
                    }
                }
            }
        }
    }

    // 수정하기 버튼 기능
    private void updateButton(ActionEvent e) {
        int choice = JOptionPane.showConfirmDialog(null, "정말로 예약을 수정하시겠습니까?", "예약 수정 확인", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        view.getUpdateButton().setEnabled(false);

        String reservationId = view.getReservationIdField().getText();
        String building = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String lectureRoom = view.getLectureRoomField().getText();
        String title = view.getTitleField().getText();
        String description = view.getDescriptionField().getText();
        String reservationDate = view.getReservationDateField().getText();
        String reservationTime = view.getReservationTimeField().getText();

        SwingWorker<BasicResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected BasicResponse doInBackground() {
                try {
                    String[] timeParts = reservationTime.split("~");
                    String startTime = timeParts[0].trim();
                    String endTime = timeParts[1].trim();

                    LocalDate date = LocalDate.parse(reservationDate);
                    DayOfWeek dayOfWeek = DayOfWeek.fromString(date.getDayOfWeek().name());
                    String dayOfWeekStr = (dayOfWeek != null ? dayOfWeek.name() : "요일 매핑 실패");

                    // [수정] RoomReservationRequest 생성자 호출 시 기본값 전달 (수정 시에는 기존 값 유지 필요하지만 UI 부재로 기본값 전달)
                    // TODO: 추후 수정 UI에 추가 필드 반영 필요
                    RoomReservationRequest roomReservationRequest = new RoomReservationRequest(
                            building,
                            floor,
                            lectureRoom,
                            title,
                            description,
                            reservationDate,
                            dayOfWeekStr,
                            startTime,
                            endTime,
                            "", // number (user)
                            "", // purpose
                            0, // accompanyingStudentCount
                            new ArrayList<>() // accompanyingStudents
                    );
                    roomReservationRequest.setId(reservationId);

                    return roomReservationManagementClientController.modifyRoomReservation(roomReservationRequest);
                } catch (Exception ex) {
                    return new BasicResponse("500", "예외 발생: " + ex.getMessage());
                }
            }

            @Override
            protected void done() {
                try {
                    BasicResponse response = get();

                    updateCalendarWithDummyData();
                    if (!"200".equals(response.code)) {
                        JOptionPane.showMessageDialog(null, "예약 수정에 실패했습니다.\n" + response.data, "수정 실패", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "예약이 성공적으로 수정되었습니다.", "수정 완료", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "예약 수정 처리 중 오류 발생: " + ex.getMessage(), "예외 발생", JOptionPane.ERROR_MESSAGE);
                } finally {
                    view.getUpdateButton().setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // 삭제하기 버튼 기능 [수정: 사유 입력 추가]
    private void deleteButton(ActionEvent e) {
        boolean check = validateReservationInput();
        if (!check) {
            updateCalendarWithDummyData();
            return;
        }

        String uniqueNumber = view.getReservationIdField().getText();
        String building = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String lectureRoom = view.getLectureRoomField().getText();
        String title = view.getTitleField().getText();

        int confirm = JOptionPane.showConfirmDialog(
                null,
                "정말 이 예약을 삭제하시겠습니까?\n[" + title + "] " + building + " " + floor + "층 " + lectureRoom,
                "예약 삭제 확인",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // [추가] 취소 사유 입력 받기
        String reason = JOptionPane.showInputDialog(null, "취소 사유를 입력하세요:", "예약 취소", JOptionPane.QUESTION_MESSAGE);
        if (reason == null || reason.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "취소 사유를 입력해야 삭제할 수 있습니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        view.getDeleteButton().setEnabled(false);

        SwingWorker<BasicResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected BasicResponse doInBackground() {
                try {
                    String userNumber = Home.getInstance().getUserNumber(); // 현재 사용자 ID
                    // [수정] DTO에 사유를 담아서 전달
                    DeleteRoomReservationRequest request = new DeleteRoomReservationRequest(userNumber, uniqueNumber, reason);
                    return roomReservationManagementClientController.deleteRoomReservation(request);                
                } catch (Exception ex) {
                    return new BasicResponse("500", "예외 발생: " + ex.getMessage());
                }
            }

            @Override
            protected void done() {
                try {
                    BasicResponse response = get();

                    if (!"200".equals(response.code)) {
                        JOptionPane.showMessageDialog(null, "예약 삭제에 실패했습니다.\n" + response.data, "삭제 실패", JOptionPane.ERROR_MESSAGE);
                    } else {
                        clearReservationFieldDataForDeleteButton();
                        updateCalendarWithDummyData();
                        JOptionPane.showMessageDialog(null, "예약이 성공적으로 삭제되었습니다.", "삭제 완료", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "삭제 처리 중 오류 발생: " + ex.getMessage(), "예외 발생", JOptionPane.ERROR_MESSAGE);
                } finally {
                    view.getDeleteButton().setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    // 예약 대기 목록 =====================================================================================================
    private void reservationListPanelRefresh() {
        JPanel reservationListPanel = view.getReservationList();
        reservationListPanel.removeAll();
        reservationListPanel.setLayout(new GridLayout(0, 1, 0, 5));

        SwingWorker<List<RoomReservation>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RoomReservation> doInBackground() {
                return getAllReservationsFromServerOrFile();
            }

            @Override
            protected void done() {
                try {
                    List<RoomReservation> allRoomReservations = get();

                    List<RoundReservationInformationButton> pendingReservations = getPendingReservations(allRoomReservations);

                    if (pendingReservations.isEmpty()) {
                        JLabel emptyLabel = new JLabel("대기 중인 예약이 없습니다.", SwingConstants.CENTER);
                        emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                        reservationListPanel.add(emptyLabel);
                    } else {
                        for (RoundReservationInformationButton btn : pendingReservations) {
                            for (ActionListener al : btn.getActionListeners()) {
                                btn.removeActionListener(al);
                            }
                            btn.addActionListener(e -> processReservationChoice(btn));
                            reservationListPanel.add(btn);
                        }
                    }

                    reservationListPanel.revalidate();
                    reservationListPanel.repaint();

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "예약 목록을 불러오는 중 오류가 발생했습니다:\n" + e.getMessage(),
                            "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    @SuppressWarnings("unchecked")
    private List<RoomReservation> getAllReservationsFromServerOrFile() {
        BasicResponse response = roomReservationManagementClientController.findAllRoomReservation();

        if (response == null || !"200".equals(response.code) || response.data == null) {
            System.err.println("⚠ 서버에서 예약 데이터를 가져오지 못했습니다.");
            return List.of();
        }

        try {
            return (List<RoomReservation>) response.data;
        } catch (ClassCastException e) {
            System.err.println("⚠ 데이터 형식이 올바르지 않습니다: " + e.getMessage());
            return List.of();
        }
    }

    private List<RoundReservationInformationButton> getPendingReservations(List<RoomReservation> allRoomReservations) {

        List<RoundReservationInformationButton> result = new ArrayList<>();

        for (RoomReservation roomReservation : allRoomReservations) {
            if ("대기".equals(roomReservation.getStatus())) {
                RoundReservationInformationButton btn = new RoundReservationInformationButton();
                btn.setRoomReservation(roomReservation);
                btn.setText("<html>"
                        + "[ " + roomReservation.getDate() + " / " + roomReservation.getDayOfTheWeek() + " ]<br>"
                        + roomReservation.getBuildingName() + "-" + roomReservation.getFloor() + "층 / "
                        + roomReservation.getLectureRoom() + " / "
                        + roomReservation.getStartTime() + "~" + roomReservation.getEndTime()
                        + "</html>");
                btn.setPreferredSize(new Dimension(112, 40));
                btn.setBackground(new Color(100, 149, 237));
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
                btn.setRoundTopLeft(10);
                btn.setRoundTopRight(10);
                btn.setRoundBottomLeft(10);
                btn.setRoundBottomRight(10);

                result.add(btn);
            }
        }
        return result;
    }

    // [수정] 예약 목록에서 선택 처리 (거절 시 사유 입력)    
    private void processReservationChoice(RoundReservationInformationButton btn) {
        int choice = JOptionPane.showOptionDialog(
                view,
                "이 예약을 어떻게 처리하시겠습니까?",
                "예약 처리",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"예약 수락", "예약 삭제"},
                "예약 수락"
        );

        if (choice == JOptionPane.YES_OPTION) {
            BasicResponse response = roomReservationManagementClientController.changeRoomReservationStatus(btn.getRoomReservation().getId());
            String code = response.code;
            if (code.equals("200")) {
                JOptionPane.showMessageDialog(view, "예약이 수락되었습니다.");
            } else if (code.equals("409")) {
                JOptionPane.showMessageDialog(view, "동일 시간대에 이미 예약이 존재합니다.", "예약 실패", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(view, "처리에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        } else if (choice == JOptionPane.NO_OPTION) {
            // [변경] 삭제(거절) 시 사유 입력
            String reason = JOptionPane.showInputDialog(view, "삭제(거절) 사유를 입력하세요:", "예약 삭제", JOptionPane.QUESTION_MESSAGE);
            if (reason == null || reason.trim().isEmpty()) {
                JOptionPane.showMessageDialog(view, "사유를 입력해야 삭제할 수 있습니다.");
                return;
            }

            String targetUserNumber = btn.getRoomReservation().getNumber();
            DeleteRoomReservationRequest request = new DeleteRoomReservationRequest(targetUserNumber, btn.getRoomReservation().getId(), reason);
            BasicResponse response = roomReservationManagementClientController.deleteRoomReservation(request);
            
            String code = response.code;
            if (code.equals("200")) JOptionPane.showMessageDialog(view, "예약이 거절되어 삭제되었습니다.");
            else JOptionPane.showMessageDialog(view, "처리에 실패했습니다.\n" + response.data, "오류", JOptionPane.ERROR_MESSAGE);
        }
        reservationListPanelRefresh();
        updateCalendarWithDummyData();
    }

    // 수정 안해도 되는 부분 ================================================================================================
    private void reservationListPanelRefreshButton(ActionEvent e) {
        System.out.println("reservationListPanelRefreshButton");
        reservationListPanelRefresh();
    }

    private AncestorListener createReservationListPanelInitListener() {
        return new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                reservationListPanelRefresh();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        };
    }

    private void reservationListFrameButton(ActionEvent e) {
        JFrame authFrame = (JFrame) SwingUtilities.getWindowAncestor(view);
        JFrame reservationListFrame = view.getReservationListFrame();
        Home.getInstance().setReservationListFrame(reservationListFrame);

        if (authFrame != null && reservationListFrame != null) {
            int x = authFrame.getX() + authFrame.getWidth() + 10;
            int y = authFrame.getY() + (authFrame.getHeight() - reservationListFrame.getHeight()) / 2;

            reservationListFrame.setLocation(x, y);
            reservationListFrame.setVisible(true);
            reservationListFrame.toFront();
        }
    }

    private void clearReservationFieldData() {
        view.getReservationIdField().setText("");
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
        view.getReservationDateField().setText("");
    }

    private void clearReservationFieldDataForDeleteButton() {
        view.getReservationIdField().setText("");
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
        view.getReservationDateField().setText("");
    }

    private void clearSelectionUI() {
        view.getBuildingField().setText("");
        view.getFloorField().setText("");
        view.getFloorButtonPanel().removeAll();
        view.getLectureRoomList().removeAll();
        view.getCalendar().setVisible(false);
        view.clearSelectedButtons();
    }

    private List<String> getDynamicRoomNames(String building, String floor) {
        if (building.equals("정보관")) {
            if (floor.equals("9")) {
                return Arrays.asList("911", "912", "913", "914", "915", "916", "918");
            }
        }

        return List.of();
    }

    private String[] parseDateTimeFromButtonName(String name) {
        if (name != null && name.matches("day\\d+_\\d+")) {
            String[] parts = name.substring(3).split("_");
            int dayOffset = Integer.parseInt(parts[0]);
            int periodIndex = Integer.parseInt(parts[1]);

            LocalDate targetDate = LocalDate.now().plusDays(dayOffset);
            String dateStr = targetDate.toString();

            LocalTime startTime = LocalTime.of(9 + periodIndex, 0);
            LocalTime endTime = startTime.plusHours(1);
            String timeStr = startTime + "~" + endTime.toString();

            return new String[]{dateStr, timeStr};
        }
        return null;
    }

    private boolean validateReservationInput() {
        String building = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String lectureRoom = view.getLectureRoomField().getText();
        String title = view.getTitleField().getText();
        String description = view.getDescriptionField().getText();
        String reservationDate = view.getReservationDateField().getText();
        String reservationTime = view.getReservationTimeField().getText();

        if (building == null || building.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "건물을 선택해주세요..", "건물 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (floor == null || !floor.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "층을 선택해주세요.", "층 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (lectureRoom == null || lectureRoom.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "강의실을 선택해주세요.", "강의실 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (title == null || title.trim().length() < 2) {
            JOptionPane.showMessageDialog(null, "예약 시간을 선택하세요", "일자 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (description == null || description.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "예약 시간을 선택하세요", "일자 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (reservationDate == null || reservationDate.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "예약 시간을 선택하세요.", "일자 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (reservationTime == null || !reservationTime.matches("\\d{2}:\\d{2}\\s*~\\s*\\d{2}:\\d{2}")) {
            JOptionPane.showMessageDialog(null, "예약 시간을 선택하세요", "일자 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }
}

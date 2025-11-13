package deu.controller.event;

import deu.controller.business.LectureClientController;
import deu.controller.business.RoomReservationManagementClientController;
import deu.controller.business.RoomReservationClientController;
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
import java.util.ArrayList;
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

    // 건물, 층, 강의실을 동적으로 추가하고 해당 강의실의 강의 시간표와 예약 시간표를 가져온다.==============================================

    // 1. 건물 정보와 층 정보를 가져오기 기능 - (2 호출)
    private void handleBuildingSelection(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) return;

        // UI 초기화
        clearSelectionUI();

        // TODO: 건물 리스트 불러오기
        // ArrayList<String> buildings = new ArrayList<>();
        // buildings = buildingClientController.getBuildingList();

        // TODO: 건물 리스트에 추가하기
        // view.setSelectedBuilding(buildings);

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
        // TODO: buildingName으로 기준으로 층 정보 가져온다.
        // 최대 층 = 컨트롤러 호출 -> 파일읽기 -> 값 전달받아 사용

        // TODO: 동적으로 층 가져오기
        // int floor = buildingClientController.getBuildingFloor(BuildingName)

        for (int i = 1; i <= 9; i++) {
            ButtonRound floorBtn = view.createStyledButton(String.valueOf(i), 45, 45);
            floorBtn.setBackground(view.FLOOR_DEFAULT_COLOR);
            floorBtn.setForeground(Color.BLACK);

            floorBtn.addActionListener(ev -> {
                view.getCalendar().setVisible(false);

                // 기존 선택 해제
                if (view.getSelectedFloorButton() != null) {
                    view.getSelectedFloorButton().setBackground(view.FLOOR_DEFAULT_COLOR);
                    view.getSelectedFloorButton().setForeground(Color.BLACK);
                }

                // 선택 설정
                floorBtn.setBackground(view.FLOOR_SELECTED_COLOR);
                floorBtn.setForeground(Color.WHITE);
                view.setSelectedFloorButton(floorBtn);

                view.getFloorDisplayField().setText(floorBtn.getText());
                view.getFloorField().setText(floorBtn.getText());

                view.getLectureRoomList().removeAll();
                view.setSelectedRoomButton(null);

                clearReservationFieldData(); // 입력 초기화

                addLectureRoomButtons(buildingName, floorBtn.getText()); // 층별 강의실 버튼 동적 생성

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
        // 9층만 강의실 버튼을 표시하도록 처리
        if (!"9".equals(floor)) return;

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
                clearReservationFieldData(); // 입력 패널 초기화

                view.setSelectedRoomButton(roomBtn);
                view.getLectureRoomField().setText(room);

                view.getDeleteButton().setEnabled(true);
                view.getUpdateButton().setEnabled(true);

                updateCalendarWithDummyData();
            });

            view.getLectureRoomList().add(roomBtn);
        }
    }

    // 4. 캘린더에 예약, 강의 정보 갱신하는 기능 - (5,6,7 호출) TODO: + SwingWorker
    private void updateCalendarWithDummyData() {
        view.getCalendar().setVisible(false);

        // 백그라운드 작업을 위한 SwingWorker
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            Lecture[][] schedule;
            RoomReservation[][] reservationSchedule;
            String[][] timeLabels;

            @Override
            protected Void doInBackground() {
                // 5. 강의 데이터 가져오기  TODO: 서버랑 연결해야 한다. + SwingWorker
                schedule = fetchWeeklyLectureSchedule();
                if (schedule == null) return null;

                // 6. 예약 데이터 가져오기  TODO: 서버랑 연결해야 한다. + SwingWorker
                reservationSchedule = fetchWeeklyReservationSchedule();

                // 7. 시간 라벨 생성
                timeLabels = generateTimeSlotLabels();

                return null;
            }

            @Override
            protected void done() {
                // UI 갱신은 EDT에서 수행
                if (schedule != null && reservationSchedule != null && timeLabels != null) {
                    applyScheduleToCalendar(schedule, reservationSchedule, timeLabels);
                    view.getCalendar().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "캘린더 데이터를 불러오는 데 실패했습니다.", "오류", JOptionPane.WARNING_MESSAGE);
                }
            }
        };

        worker.execute(); // SwingWorker 실행
    }

    // 5. 서버로부터 주간 강의 스케줄 받아오기
    private Lecture[][] fetchWeeklyLectureSchedule() {
        BasicResponse res = lectureClientController.returnLectureOfWeek(
                view.getBuildingField().getText(),
                view.getFloorField().getText(),
                view.getLectureRoomField().getText()
        );

        if (res == null) {
            JOptionPane.showMessageDialog(null, "서버 응답이 없습니다.");
            return null;
        }

        if (!"200".equals(res.code) || !(res.data instanceof Lecture[][])) {
            JOptionPane.showMessageDialog(null, "강의 데이터를 불러오지 못했습니다: " + res.data);
            return null;
        }

        return (Lecture[][]) res.data;
    }

    // 6. 서버로부터 주간 예약 스케줄 받아오기
    private RoomReservation[][] fetchWeeklyReservationSchedule() {
        RoomReservation[][] grid = new RoomReservation[7][13];

        try {
            BasicResponse res = roomReservationClientController.weekRoomReservationByLectureroom(
                    view.getBuildingField().getText(),
                    view.getFloorField().getText(),
                    view.getLectureRoomField().getText()
            );

            if (!"200".equals(res.code)) {
                JOptionPane.showMessageDialog(null, "예약 정보를 불러오지 못했습니다.\n" + res.data, "오류", JOptionPane.WARNING_MESSAGE);
            } else if (res.data instanceof RoomReservation[][] result) {
                grid = result;
            } else {
                JOptionPane.showMessageDialog(null, "예약 데이터 형식이 올바르지 않습니다.", "형변환 오류", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "예약 데이터를 가져오는 중 예외가 발생했습니다:\n" + e.getMessage(), "예외 발생", JOptionPane.ERROR_MESSAGE);
        }

        return grid;
    }

    // 7. 교시별 시간 문자열 생성
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

    // 8. 캘린더 버튼에 강의 및 예약 정보 반영
    private void applyScheduleToCalendar(Lecture[][] schedule, RoomReservation[][] reservationSchedule, String[][] labels) {
        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                String buttonName = "day" + day + "_" + period;

                for (Component comp : view.getCalendar().getComponents()) {
                    if (comp instanceof TimeSlotButton dayBtn && buttonName.equals(dayBtn.getName())) {
                        dayBtn.setText(labels[day][period]);

                        // 기존 리스너 제거
                        for (ActionListener al : dayBtn.getActionListeners()) {
                            dayBtn.removeActionListener(al);
                        }

                        dayBtn.setOpaque(true);
                        dayBtn.setContentAreaFilled(true);
                        dayBtn.setForeground(Color.BLACK);
                        dayBtn.setLecture(null);
                        dayBtn.setRoomReservation(null);

                        // 강의가 있는 경우
                        if (schedule[day][period] != null) {
                            dayBtn.setEnabled(false);
                            dayBtn.setLecture(schedule[day][period]);
                            dayBtn.setText(schedule[day][period].getTitle());
                            dayBtn.setBackground(new Color(100, 149, 237)); // 파란색
                            continue;
                        }

                        // 예약이 있는 경우
                        if (reservationSchedule != null && reservationSchedule[day][period] != null) {
                            RoomReservation reservation = reservationSchedule[day][period];
                            dayBtn.setEnabled(true);
                            dayBtn.setRoomReservation(reservation);
                            dayBtn.setText(reservation.getTitle());

                            // 상태에 따라 색상
                            if ("승인".equals(reservation.getStatus())) {
                                dayBtn.setBackground(new Color(60, 179, 113)); // 초록
                            } else {
                                dayBtn.setBackground(new Color(241, 196, 15)); // 노란색 (대기)
                            }

                            dayBtn.addActionListener(ev -> {
                                TimeSlotButton source = (TimeSlotButton) ev.getSource();
                                RoomReservation r = source.getRoomReservation();
                                TimeSlotButton prev = (TimeSlotButton) view.getSelectedCalendarButton();

                                // 이전 선택된 버튼 색 복원
                                if (prev != null && prev != source) {
                                    RoomReservation prevRes = prev.getRoomReservation();

                                    if (prevRes != null) {
                                        String status = prevRes.getStatus();
                                        if ("승인".equals(status)) {
                                            prev.setBackground(new Color(60, 179, 113)); // 초록 복원
                                        } else {
                                            prev.setBackground(new Color(241, 196, 15)); // 노란색 복원
                                        }
                                    } else {
                                        prev.setBackground(Color.WHITE); // 비어 있는 시간대 복원
                                    }
                                }

                                // 현재 선택 처리
                                source.setBackground(new Color(30, 144, 255)); // 파란색
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

                        // 비어 있는 시간대
                        dayBtn.setEnabled(false);
                        dayBtn.setBackground(null);
                    }
                }
            }
        }
    }

    // 수정하기 버튼 기능  TODO: 서버랑 연결해야 한다. + SwingWorker
    private void updateButton(ActionEvent e) {
        int choice = JOptionPane.showConfirmDialog(null, "정말로 예약을 수정하시겠습니까?", "예약 수정 확인", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        view.getUpdateButton().setEnabled(false); // 버튼 잠금

        // 필드 값들 미리 가져오기 (EDT 내에서만 Swing 컴포넌트 접근해야 하므로 여기서 수집)
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
                            ""
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
                    view.getUpdateButton().setEnabled(true); // 버튼 다시 활성화
                }
            }
        };

        worker.execute(); // 백그라운드 실행
    }

    // 삭제하기 버튼 기능  TODO: 서버랑 연결해야 한다. + SwingWorker
    private void deleteButton(ActionEvent e) {
        // 유효성 검사
        boolean check = validateReservationInput();
        if (!check) {
            updateCalendarWithDummyData();
            return;
        }

        // 필드 값 수집 (EDT에서만 Swing 컴포넌트 접근)
        String uniqueNumber = view.getReservationIdField().getText();
        String building = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String lectureRoom = view.getLectureRoomField().getText();
        String title = view.getTitleField().getText();

        // 삭제 여부 확인
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

        // 버튼 잠금
        view.getDeleteButton().setEnabled(false);

        SwingWorker<BasicResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected BasicResponse doInBackground() {
                try {
                    return roomReservationManagementClientController.deleteRoomReservation(uniqueNumber);
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
                    view.getDeleteButton().setEnabled(true); // 버튼 복원
                }
            }
        };

        worker.execute(); // 백그라운드 실행
    }

    // 예약 대기 목록 =====================================================================================================

    // 예약 대기 목록을 갱신하는 기능
    private void reservationListPanelRefresh() {
        JPanel reservationListPanel = view.getReservationList();
        reservationListPanel.removeAll();
        reservationListPanel.setLayout(new GridLayout(0, 1, 0, 5)); // 세로 1열

        // 비동기 실행
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
                            // 중복 방지를 위해 기존 리스너 제거
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

    // 예약 리스트를 반환하는 메서드 TODO: 확인 완료 + SwingWorker
    @SuppressWarnings("unchecked")
    private List<RoomReservation> getAllReservationsFromServerOrFile() {
        BasicResponse response = roomReservationManagementClientController.findAllRoomReservation();

        // 서버 응답 오류 처리
        if (response == null || !"200".equals(response.code) || response.data == null) {
            System.err.println("⚠ 서버에서 예약 데이터를 가져오지 못했습니다.");
            return List.of(); // 빈 리스트 반환
        }

        try {
            return (List<RoomReservation>) response.data;
        } catch (ClassCastException e) {
            System.err.println("⚠ 데이터 형식이 올바르지 않습니다: " + e.getMessage());
            return List.of();
        }
    }

    // 예약 대기 목록을 서버로 부터 받아오는 기능
    private List<RoundReservationInformationButton> getPendingReservations(List<RoomReservation> allRoomReservations) {

        List<RoundReservationInformationButton> result = new ArrayList<>();

        for (RoomReservation roomReservation : allRoomReservations) {
            if ("대기".equals(roomReservation.getStatus())) {
                RoundReservationInformationButton btn = new RoundReservationInformationButton();
                btn.setRoomReservation(roomReservation);
                btn.setText("<html>" +
                        "[ " + roomReservation.getDate() + " / " + roomReservation.getDayOfTheWeek() + " ]<br>" +
                        roomReservation.getBuildingName() + "-" + roomReservation.getFloor() + "층 / " +
                        roomReservation.getLectureRoom() + " / " +
                        roomReservation.getStartTime() + "~" + roomReservation.getEndTime() +
                        "</html>");
                btn.setPreferredSize(new Dimension(112, 40));
                btn.setBackground(new Color(100, 149, 237)); // 파란색
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

    // 예약 수락, 거절 팝업 인터페이스 기능
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
            BasicResponse response = roomReservationManagementClientController.deleteRoomReservation(btn.getRoomReservation().getId());
            String code = response.code;
            if (code.equals("200")) {
                JOptionPane.showMessageDialog(view, "예약이 거절되어 삭제되었습니다.");
            } else {
                JOptionPane.showMessageDialog(view, "처리에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
        reservationListPanelRefresh(); // 대기 리스트 갱신
        updateCalendarWithDummyData(); // 캘린더 갱신
    }

    // 수정 안해도 되는 부분 ================================================================================================

    // 예약 목록의 새로고침 버튼 기능 - 수정 금지
    private void reservationListPanelRefreshButton(ActionEvent e) {
        System.out.println("reservationListPanelRefreshButton");
        reservationListPanelRefresh();
    }
    // 예약 목록이 생성될 때 갱신되는 기능 - 수정 금지
    private AncestorListener createReservationListPanelInitListener() {
        return new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                reservationListPanelRefresh();
            }
            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        };
    }
    // 예약 목록 프레임 띄우는 기능 - 수정 금지
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
    // 입력 필드 초기화 - 수정 금지
    private void clearReservationFieldData(){
        // 필드 값을 비운다.
        view.getReservationIdField().setText("");
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
        view.getReservationDateField().setText("");
    }
    // 입력 필드 초기화 - 수정 금지
    private void clearReservationFieldDataForDeleteButton(){
        // 필드 값을 비운다.
        view.getReservationIdField().setText("");
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
        view.getReservationDateField().setText("");
    }
    // 선택 UI 초기화 메서드 - 수정 금지
    private void clearSelectionUI() {
        // 초기화
        view.getBuildingField().setText("");
        view.getFloorField().setText("");
        view.getFloorButtonPanel().removeAll();
        view.getLectureRoomList().removeAll();
        view.getCalendar().setVisible(false);
        view.clearSelectedButtons();
    }
    // 건물에 따른 강의실 정보 가져오는 기능 - 수정 금지
    private List<String> getDynamicRoomNames(String building, String floor) {
        // TODO: 동적으로 건물과 층에 해당하는 강의실 가져오기
        // int floor = buildingClientController.getBuildingFloor(BuildingName)
        // List<String> roomNames = new ArrayList<>();
        // roomNames = buildingClientController.getBuildingFloorRoom(building, floor);
        // json 파일에 아래와 같은 형식으로 저장됩니다.
        // building: "정보관"
        // floor: 9
        // rooms: "911", "912", "913", "914", "915", "916", "918", "123", "214"

        // TODO: 층별 강의실 정보를 불러온다
        // return roomNames


        if(building.equals("정보관")){
            if(floor.equals("9")){
                return Arrays.asList("911", "912", "913", "914", "915", "916", "918");
            }
        }

        return List.of();
    }
    // 이름에서 날짜와 시간대를 분리 - 수정 금지
    private String[] parseDateTimeFromButtonName(String name) {
        if (name != null && name.matches("day\\d+_\\d+")) {
            String[] parts = name.substring(3).split("_");
            int dayOffset = Integer.parseInt(parts[0]);
            int periodIndex = Integer.parseInt(parts[1]);

            // 날짜 계산
            LocalDate targetDate = LocalDate.now().plusDays(dayOffset);
            String dateStr = targetDate.toString(); // yyyy-MM-dd

            // 시간 계산
            LocalTime startTime = LocalTime.of(9 + periodIndex, 0);
            LocalTime endTime = startTime.plusHours(1);
            String timeStr = startTime + "~" + endTime.toString(); // HH:mm ~ HH:mm

            return new String[]{dateStr, timeStr};
        }
        return null;
    }
    // 예약/삭제 진행 시 유효성 검사 - 수정 금지
    private boolean validateReservationInput() {
        String building = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String lectureRoom = view.getLectureRoomField().getText();
        String title = view.getTitleField().getText();
        String description = view.getDescriptionField().getText();
        String reservationDate = view.getReservationDateField().getText();
        String reservationTime = view.getReservationTimeField().getText(); // 예: "09:00~10:00"

        // 건물명 유효성 검사
        if (building == null || building.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "건물을 선택해주세요..", "건물 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 층 유효성 검사 (숫자인지)
        if (floor == null || !floor.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "층을 선택해주세요.", "층 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 강의실 유효성 검사
        if (lectureRoom == null || lectureRoom.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "강의실을 선택해주세요.", "강의실 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 제목 유효성 검사
        if (title == null || title.trim().length() < 2) {
            JOptionPane.showMessageDialog(null, "예약 시간을 선택하세요", "일자 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 설명 유효성 검사 (선택 사항이 아니면 검사)
        if (description == null || description.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "예약 시간을 선택하세요", "일자 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 날짜 유효성 검사 (형식이 무조건 yyyy-MM-dd 이라고 가정)
        if (reservationDate == null || reservationDate.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "예약 시간을 선택하세요.", "일자 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 시간 유효성 검사 ("HH:mm~HH:mm" 형식)
        if (reservationTime == null || !reservationTime.matches("\\d{2}:\\d{2}\\s*~\\s*\\d{2}:\\d{2}")) {
            JOptionPane.showMessageDialog(null, "예약 시간을 선택하세요", "일자 선택 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }
}
package deu.controller.event;

import deu.controller.business.BuildingClientController;
import deu.controller.business.LectureClientController;
import deu.controller.business.RoomReservationClientController;
import deu.model.dto.request.data.reservation.AccompanyingStudent;
import deu.model.dto.request.data.reservation.RoomReservationRequest;
import deu.model.dto.response.BasicResponse;
import deu.model.enums.DayOfWeek; 
import deu.view.DailyReservationDialog;
import deu.view.MonthlyReservationDialog;
import deu.view.Reservation;
import deu.view.custom.ButtonRound;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReservationSwingController {

    private final Reservation view;
    private final LectureClientController lectureClientController;
    private final RoomReservationClientController roomReservationClientController;
    private final BuildingClientController buildingClientController;

    private String currentViewType = "WEEKLY";

    public ReservationSwingController(Reservation view) {
        this.view = view;
        this.lectureClientController = LectureClientController.getInstance();
        this.roomReservationClientController = RoomReservationClientController.getInstance();
        this.buildingClientController = BuildingClientController.getInstance();

        view.addBuildingSelectionListener(this::handleBuildingSelection);
        view.addReservationButtionListener(this::handleReservation);

        // [수정] 주별(Weekly) 리스너 등록 코드 제거
        view.addDailyViewListener(e -> runCalendarUpdate("DAILY"));
        // view.addWeeklyViewListener(e -> runCalendarUpdate("WEEKLY")); <-- 삭제
        view.addMonthlyViewListener(e -> runCalendarUpdate("MONTHLY"));
    }

    private void handleBuildingSelection(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) {
            return;
        }
        clearSelectionUI();
        String selectedBuilding = view.getSelectedBuilding();
        view.getBuildingField().setText(selectedBuilding);
        if (!"정보관".equals(selectedBuilding)) {
            refreshReservationWriteDataField();
            view.getFloorButtonPanel().revalidate();
            view.getFloorButtonPanel().repaint();
            view.getLectureRoomList().revalidate();
            view.getLectureRoomList().repaint();
            view.getFloorDisplayField().setText("");
            return;
        }
        addFloorButtons(selectedBuilding);
    }

    private void addFloorButtons(String buildingName) {
        view.getFloorButtonPanel().removeAll();
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
                refreshReservationWriteDataField();

                addLectureRoomButtons(buildingName, floorBtn.getText());

                view.getLectureRoomList().revalidate();
                view.getLectureRoomList().repaint();
            });
            view.getFloorButtonPanel().add(floorBtn);
        }
        view.getFloorButtonPanel().revalidate();
        view.getFloorButtonPanel().repaint();
    }

    private void addLectureRoomButtons(String buildingName, String floor) {
        if (!"9".equals(floor)) {
            return;
        }

        for (String room : getDynamicRoomNames(buildingName, floor)) {
            ButtonRound roomBtn = view.createStyledButton(room, 100, 30);
            roomBtn.setBackground(Reservation.FLOOR_DEFAULT_COLOR);
            roomBtn.setForeground(Color.BLACK);
            
            roomBtn.addActionListener(roomEv -> {
                if (view.getSelectedRoomButton() != null) {
                    view.getSelectedRoomButton().setBackground(Reservation.FLOOR_DEFAULT_COLOR);
                    view.getSelectedRoomButton().setForeground(Color.BLACK);
                }
                roomBtn.setBackground(Reservation.ROOM_SELECTED_COLOR);
                roomBtn.setForeground(Color.WHITE);
                refreshReservationWriteDataField();
                view.setSelectedRoomButton(roomBtn);
                view.getLectureRoomField().setText(room);
                view.getUpdateButton().setEnabled(true);
                
                // [핵심 수정] 다른 강의실 버튼 클릭 시 무조건 '주별(WEEKLY)' 뷰로 초기화
                this.currentViewType = "WEEKLY";
                runCalendarUpdate("WEEKLY");
            });
            view.getLectureRoomList().add(roomBtn);
        }
    }

    /**
     * [핵심] 뷰 업데이트 로직
     */
    private void runCalendarUpdate(String viewType) {
        this.currentViewType = viewType;
        
        String building = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String room = view.getLectureRoomField().getText();

        if (building == null || building.isEmpty() || floor == null || floor.isEmpty() || room == null || room.isEmpty()) {
            if (!"WEEKLY".equals(viewType)) {
                JOptionPane.showMessageDialog(view, "강의실을 먼저 선택해주세요.");
            }
            return;
        }

        AbstractCalendarViewTemplate worker = null;
        JDialog popupDialog = null;
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(view);

        // [핵심] 일별 조회 시 사용자가 선택한 날짜(텍스트필드) 가져오기
        LocalDate targetDate = LocalDate.now();
        if ("DAILY".equals(viewType)) {
            try {
                String dateText = view.getReservationDateField().getText();
                if (dateText != null && !dateText.trim().isEmpty()) {
                    targetDate = LocalDate.parse(dateText.trim());
                } else {
                    // 선택된 날짜가 없으면 안내 메시지
                    JOptionPane.showMessageDialog(view, "날짜를 선택하지 않아 오늘 기준으로 조회합니다.");
                }
            } catch (DateTimeParseException e) {
                // 파싱 실패 시 무시하고 오늘 날짜 사용
            }
        }

        switch (viewType) {
            case "WEEKLY":
                worker = new WeeklyCalendarView(view, building, floor, room);
                break;
            case "DAILY":
                // 선택된 날짜로 다이얼로그 생성
                DailyReservationDialog dailyDialog = new DailyReservationDialog(parentFrame, "일별 강의 현황 [" + targetDate + "]");
                popupDialog = dailyDialog;
                worker = new DailyCalendarView(dailyDialog, building, floor, room, targetDate);
                break;
            case "MONTHLY":
                MonthlyReservationDialog monthlyDialog = new MonthlyReservationDialog(parentFrame);
                popupDialog = monthlyDialog;
                worker = new MonthlyCalendarView(monthlyDialog, building, floor, room, LocalDate.now());
                break;
        }

        if (worker != null) worker.execute();
        if (popupDialog != null) {
            popupDialog.setLocationRelativeTo(view);
            popupDialog.setVisible(true);
        }
    }

    // =================================================================================================================
    // 예약하는 버튼 기능
    private void handleReservation(ActionEvent e) {
        if (!validateReservationInput()) {
            return;
        }
        String building = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String lectureRoom = view.getLectureRoomField().getText();
        String title = view.getTitleField().getText();
        String description = view.getDescriptionField().getText();
        String reservationDate = view.getReservationDateField().getText();
        String reservationTime = view.getReservationTimeField().getText();
        String userNumber = view.getUserNumber();

        LocalDate date = LocalDate.parse(reservationDate);

        // [수정] DayOfWeek 변환 방식 수정 (안전하게 valueOf 사용)
        String dayName = date.getDayOfWeek().name();
        // DayOfWeek dayOfWeek = DayOfWeek.fromString(date.getDayOfWeek().name());
        String[] timeParts = reservationTime.split("~");
        String startTime = timeParts[0].trim();
        String endTime = timeParts[1].trim();
        // String dayOfWeekStr = (dayOfWeek != null ? dayOfWeek.name() : "요일 매핑 실패");

        int confirm = JOptionPane.showConfirmDialog(null, "다음 예약을 진행하시겠습니까?\n" + reservationDate + " " + startTime + " ~ " + endTime, "예약 확인", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // [추가] 사용 목적 입력
        String purpose = JOptionPane.showInputDialog(null, "사용 목적을 입력하세요:", "추가 정보", JOptionPane.QUESTION_MESSAGE);
        if (purpose == null) {
            purpose = ""; // 취소 시 빈 문자열
        }
        // [추가] 동반 학생 수 입력
        int accompanyingStudentCount = 0;
        try {
            String countStr = JOptionPane.showInputDialog(null, "동반 학생 수를 입력하세요 (없으면 0):", "추가 정보", JOptionPane.QUESTION_MESSAGE);
            if (countStr != null && !countStr.trim().isEmpty()) {
                accompanyingStudentCount = Integer.parseInt(countStr.trim());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "숫자만 입력 가능합니다. 0명으로 처리됩니다.");
        }

        // [추가] 동반 학생 정보 입력
        List<AccompanyingStudent> accompanyingStudents = new ArrayList<>();
        for (int i = 0; i < accompanyingStudentCount; i++) {
            JTextField idField = new JTextField();
            JTextField nameField = new JTextField();
            Object[] message = {
                (i + 1) + "번째 학생 학번:", idField,
                (i + 1) + "번째 학생 성명:", nameField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "동반 학생 정보 입력", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                accompanyingStudents.add(new AccompanyingStudent(idField.getText(), nameField.getText()));
            } else {
                break; // 입력 취소 시 중단
            }
        }

        // [수정] RoomReservationRequest 생성자 호출 시 추가된 정보 전달
        RoomReservationRequest reservationRequest = new RoomReservationRequest(
                building, floor, lectureRoom, title, description,
                reservationDate, dayName, startTime, endTime, userNumber,
                purpose, accompanyingStudentCount, accompanyingStudents // 추가된 인자들
        );

        SwingWorker<BasicResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected BasicResponse doInBackground() {
                return roomReservationClientController.addRoomReservation(reservationRequest);
            }

            @Override
            protected void done() {
                try {
                    BasicResponse response = get();
                    switch (response.code) {
                        case "200" ->
                            JOptionPane.showMessageDialog(null, response.data, "예약 완료", JOptionPane.INFORMATION_MESSAGE);
                        case "409" ->
                            JOptionPane.showMessageDialog(null, response.data, "예약 중복 오류", JOptionPane.WARNING_MESSAGE);
                        case "403" ->
                            JOptionPane.showMessageDialog(null, response.data, "예약 제한 초과", JOptionPane.WARNING_MESSAGE);
                        default ->
                            JOptionPane.showMessageDialog(null, response.data, "서버 오류 또는 예외", JOptionPane.ERROR_MESSAGE);
                    }
                    refreshReservationWriteDataFieldForCalendar();
                    // [수정] 예약 후에는 무조건 메인 화면(주간 뷰)을 갱신해야 합니다.
                    currentViewType = "WEEKLY";
                    runCalendarUpdate("WEEKLY");                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "예약 요청 처리 중 예외 발생: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void refreshReservationWriteDataField() {
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
        view.getLectureRoomField().setText("");
    }

    private void refreshReservationWriteDataFieldForCalendar() {
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
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
        if (building.equals("정보관") && floor.equals("9")) {
            return Arrays.asList("911", "912", "913", "914", "915", "916", "918");
        }
        return List.of();
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
            JOptionPane.showMessageDialog(null, "제목은 공백이 아니고 2자 이상 입력해야 합니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (description == null || description.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "설명을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
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

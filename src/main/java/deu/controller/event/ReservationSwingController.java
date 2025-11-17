package deu.controller.event;

import deu.controller.business.BuildingClientController;
import deu.controller.business.LectureClientController;
import deu.controller.business.RoomReservationClientController;
import deu.model.dto.request.data.reservation.RoomReservationRequest;
import deu.model.dto.response.BasicResponse;
import deu.model.enums.DayOfWeek;
import deu.view.Reservation;
import deu.view.custom.ButtonRound;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class ReservationSwingController {
    private final Reservation view;
    private final LectureClientController lectureClientController;
    private final RoomReservationClientController roomReservationClientController;
    private final BuildingClientController buildingClientController;
    
    // [신규] 현재 선택된 뷰 타입 (기본: 주별)
    private String currentViewType = "WEEKLY";

    public ReservationSwingController(Reservation view) {
        this.view = view;
        this.lectureClientController = LectureClientController.getInstance();
        this.roomReservationClientController = RoomReservationClientController.getInstance();
        this.buildingClientController = BuildingClientController.getInstance();

        // 이벤트 연결
        view.addBuildingSelectionListener(this::handleBuildingSelection);
        view.addReservationButtionListener(this::handleReservation); 

        // [신규] '일별/주별' 버튼 리스너 연결
        view.addDailyViewListener(e -> runCalendarUpdate("DAILY"));
        view.addWeeklyViewListener(e -> runCalendarUpdate("WEEKLY"));
        // '월별'은 제외
    }

    // 1. 건물 선택
    private void handleBuildingSelection(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) return;
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

    // 2. 층 버튼 생성
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

    // 3. 강의실 버튼 생성 (템플릿 호출로 변경)
    private void addLectureRoomButtons(String buildingName, String floor) {
        if (!"9".equals(floor)) return;

        for (String room : getDynamicRoomNames(buildingName, floor)) {
            ButtonRound roomBtn = view.createStyledButton(room, 100, 30);
            roomBtn.setBackground(view.FLOOR_DEFAULT_COLOR);
            roomBtn.setForeground(Color.BLACK);

            roomBtn.addActionListener(roomEv -> {
                if (view.getSelectedRoomButton() != null) {
                    view.getSelectedRoomButton().setBackground(view.FLOOR_DEFAULT_COLOR);
                    view.getSelectedRoomButton().setForeground(Color.BLACK);
                }
                roomBtn.setBackground(view.ROOM_SELECTED_COLOR);
                roomBtn.setForeground(Color.WHITE);
                refreshReservationWriteDataField(); 
                view.setSelectedRoomButton(roomBtn);
                view.getLectureRoomField().setText(room); 
                view.getUpdateButton().setEnabled(true);

                // [수정] 템플릿 실행 메서드 호출 (기본값: 주별)
                runCalendarUpdate(this.currentViewType); 
            });
            view.getLectureRoomList().add(roomBtn);
        }
    }

    /**
     * [신규] 템플릿 메서드 패턴을 실행하는 "Client" 메서드
     * 이 메서드가 '일별/주별' 전략을 선택하여 SwingWorker(템플릿)를 실행합니다.
     */
    private void runCalendarUpdate(String viewType) {
        this.currentViewType = viewType; 
        
        String building = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String room = view.getLectureRoomField().getText();

        if (building == null || building.isEmpty() || 
            floor == null || floor.isEmpty() || 
            room == null || room.isEmpty()) {
            return; 
        }

        AbstractCalendarViewTemplate worker; 

        switch(viewType) {
            case "WEEKLY":
                worker = new WeeklyCalendarView(view, building, floor, room);
                break;
            case "DAILY":
                worker = new DailyCalendarView(view, building, floor, room);
                break;
            // '월별' 제외
            default:
                return;
        }
        
        worker.execute(); 
    }

    // =================================================================================================================
    // (이하 예약/헬퍼 메서드들은 기존 코드와 동일)
    // =================================================================================================================

    // 예약하는 버튼 기능
    private void handleReservation(ActionEvent e) {
        if (!validateReservationInput()) {
            runCalendarUpdate(this.currentViewType); 
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
        DayOfWeek dayOfWeek = DayOfWeek.fromString(date.getDayOfWeek().name());
        String[] timeParts = reservationTime.split("~");
        String startTime = timeParts[0].trim();
        String endTime = timeParts[1].trim();
        String dayOfWeekStr = (dayOfWeek != null ? dayOfWeek.name() : "요일 매핑 실패");

        int confirm = JOptionPane.showConfirmDialog(null, "다음 예약을 진행하시겠습니까?\n" + reservationDate + " " + startTime + " ~ " + endTime, "예약 확인", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        RoomReservationRequest reservationRequest = new RoomReservationRequest(
            building, floor, lectureRoom, title, description,
            reservationDate, dayOfWeekStr, startTime, endTime, userNumber
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
                       case "200" -> JOptionPane.showMessageDialog(null, response.data, "예약 완료", JOptionPane.INFORMATION_MESSAGE);
                       case "409" -> JOptionPane.showMessageDialog(null, response.data, "예약 중복 오류", JOptionPane.WARNING_MESSAGE);
                       case "403" -> JOptionPane.showMessageDialog(null, response.data, "예약 제한 초과", JOptionPane.WARNING_MESSAGE);
                       default -> JOptionPane.showMessageDialog(null, response.data, "서버 오류 또는 예외", JOptionPane.ERROR_MESSAGE);
                    }
                    refreshReservationWriteDataFieldForCalendar();
                    runCalendarUpdate(currentViewType); // 캘린더 갱신
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "예약 요청 처리 중 예외 발생: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // 필드 초기화
    private void refreshReservationWriteDataField(){
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
        view.getLectureRoomField().setText("");
    }
    
    // 예약 추가/삭제 후 필드를 초기화
    private void refreshReservationWriteDataFieldForCalendar(){
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
    }

    // 선택 UI 초기화
    private void clearSelectionUI() {
        view.getBuildingField().setText("");
        view.getFloorField().setText("");
        view.getFloorButtonPanel().removeAll();
        view.getLectureRoomList().removeAll();
        view.getCalendar().setVisible(false);
        view.clearSelectedButtons();
    }

    // 건물에 따른 강의실 정보
    private List<String> getDynamicRoomNames(String building, String floor) {
        if(building.equals("정보관") && floor.equals("9")){
            return Arrays.asList("911", "912", "913", "914", "915", "916", "918");
        }
        return List.of();
    }

    // 이름에서 날짜와 시간대를 분리
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

    // 예약 진행 시 유효성 검사
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
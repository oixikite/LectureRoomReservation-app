package deu.controller.event;

import deu.controller.business.BuildingClientController;
import deu.controller.business.LectureClientController;
import deu.controller.business.RoomReservationClientController;
import deu.model.dto.request.data.reservation.RoomReservationRequest;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.Lecture;
import deu.model.entity.RoomReservation;
import deu.model.enums.DayOfWeek;
import deu.view.Reservation;
import deu.view.custom.ButtonRound;
import deu.view.custom.TimeSlotButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class ReservationSwingController {
    private final Reservation view;
    private final LectureClientController lectureClientController;
    private final RoomReservationClientController roomReservationClientController;
    private final BuildingClientController buildingClientController;

    public ReservationSwingController(Reservation view) {
        this.view = view;
        this.lectureClientController = LectureClientController.getInstance();
        this.roomReservationClientController = RoomReservationClientController.getInstance();
        this.buildingClientController = BuildingClientController.getInstance();

        // 이벤트 연결
        view.addBuildingSelectionListener(this::handleBuildingSelection);
        view.addReservationButtionListener(this::lectureRoomReservationButton);
    }

    // 건물, 층, 강의실을 동적으로 추가하고 해당 강의실의 강의 시간표와 예약 시간표를 가져온다.==============================================

    // 1. 건물과 층을 선택 하는 기능 - (2 호출)
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
            refreshReservationWriteDataField();
            view.getFloorButtonPanel().revalidate();
            view.getFloorButtonPanel().repaint();
            view.getLectureRoomList().revalidate();
            view.getLectureRoomList().repaint();
            view.getFloorDisplayField().setText("");
            return;
        }

        // 정보관일 경우: 1~9층 층 버튼 추가
        addFloorButtons(selectedBuilding);
    }

    // 2. 층 버튼 생성해서 UI에 추가하는 메서드 - (3 호출)
    private void addFloorButtons(String buildingName) {
        // TODO: buildingName으로 기준으로 층 정보 가져온다.
        // 최대 층 = 컨트롤러 호출 -> 파일읽기 -> 값 전달받아 사용

        // TODO: 동적으로 층 가져오기
        // int floor = buildingClientController.getBuildingFloor(BuildingName)

        for (int i = 1; i <= 9; i++) {
            // final int currentFloor = i;
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

                refreshReservationWriteDataField(); // 입력 초기화

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
                refreshReservationWriteDataField(); // 입력 패널 초기화

                view.setSelectedRoomButton(roomBtn);
                view.getLectureRoomField().setText(room);
                view.getUpdateButton().setEnabled(true);
                updateCalendarWithDummyData();
            });

            view.getLectureRoomList().add(roomBtn);
        }
    }

    // 4. 캘린더에 예약, 강의 정보 갱신하는 기능 - (5,6,7 호출) TODO: 확인 완료 + SwingWorker
    private void updateCalendarWithDummyData() {
        System.out.println("[DEBUG] updateCalendarWithDummyData() 시작");

        view.getCalendar().setVisible(false);
        System.out.println("[DEBUG] 캘린더 비활성화 완료");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            Lecture[][] schedule;
            RoomReservation[][] reservationSchedule;
            String[][] timeLabels;

            @Override
            protected Void doInBackground() {
                System.out.println("[DEBUG] SwingWorker.doInBackground() 진입");

                // 5. 강의 데이터 가져오기
                schedule = fetchWeeklyLectureSchedule();
                System.out.println("[DEBUG] 강의 스케줄 수신: " + (schedule != null ? "성공" : "실패"));

                if (schedule == null) return null;

                // 6. 예약 데이터 가져오기
                reservationSchedule = fetchWeeklyReservationSchedule();
                System.out.println("[DEBUG] 예약 스케줄 수신: " + (reservationSchedule != null ? "성공" : "실패"));

                // 7. 라벨 생성
                timeLabels = generateTimeSlotLabels();
                System.out.println("[DEBUG] 시간 라벨 생성 완료");

                return null;
            }

            @Override
            protected void done() {
                System.out.println("[DEBUG] SwingWorker.done() 진입");

                if (schedule != null && reservationSchedule != null && timeLabels != null) {
                    System.out.println("[DEBUG] 캘린더 데이터 완전 → applyScheduleToCalendar 호출");

                    view.setSelectedCalendarButton(null); // 선택 초기화
                    applyScheduleToCalendar(schedule, reservationSchedule, timeLabels);

                    SwingUtilities.invokeLater(() -> {
                        view.getCalendar().setVisible(true);
                        System.out.println("[DEBUG] 캘린더 다시 보이도록 설정 완료");
                    });
                } else {
                    System.out.println("[ERROR] 캘린더 데이터가 일부 null → 로딩 실패");
                    JOptionPane.showMessageDialog(null, "캘린더 데이터를 불러오지 못했습니다.", "오류", JOptionPane.WARNING_MESSAGE);
                }
            }
        };

        worker.execute();
        System.out.println("[DEBUG] SwingWorker 실행 완료");
    }

    // 5. 서버에서 주간 강의 데이터를 요청하여 반환
    private Lecture[][] fetchWeeklyLectureSchedule() {
        String building = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String lectureRoom = view.getLectureRoomField().getText();

        System.out.println("[DEBUG] fetchWeeklyLectureSchedule() 시작");
        System.out.println("[DEBUG] 요청 정보 → 건물: " + building + ", 층: " + floor + ", 강의실: " + lectureRoom);

        BasicResponse res = lectureClientController.returnLectureOfWeek(building, floor, lectureRoom);

        if (res == null) {
            System.out.println("[ERROR] 서버 응답이 null");
            JOptionPane.showMessageDialog(null, "서버 응답이 없습니다.");
            return null;
        }

        System.out.println("[DEBUG] 서버 응답 코드: " + res.code);
        System.out.println("[DEBUG] 응답 데이터 타입: " + (res.data != null ? res.data.getClass().getName() : "null"));

        if (!"200".equals(res.code)) {
            System.out.println("[WARN] 응답 코드가 200이 아님 → " + res.code);
            JOptionPane.showMessageDialog(null, "강의 데이터를 불러오지 못했습니다: " + res.data);
            return null;
        }

        if (!(res.data instanceof Lecture[][])) {
            System.out.println("[ERROR] 응답 데이터가 Lecture[][] 타입이 아님");
            JOptionPane.showMessageDialog(null, "강의 데이터 형식 오류: " + res.data);
            return null;
        }

        Lecture[][] result = (Lecture[][]) res.data;
        System.out.println("[DEBUG] 강의 데이터 수신 완료 → 배열 크기: " + result.length + "일 × " +
                (result.length > 0 ? result[0].length : "0") + "교시");
        System.out.println("[DEBUG] fetchWeeklyLectureSchedule() 종료");

        return result;
    }

    // 6. 서버로부터 주간 예약 스케줄 받아오기
    private RoomReservation[][] fetchWeeklyReservationSchedule() {
        System.out.println("[DEBUG] fetchWeeklyReservationSchedule 시작");

        RoomReservation[][] grid = new RoomReservation[7][13];

        String building = view.getBuildingField().getText();
        String floor = view.getFloorField().getText();
        String room = view.getLectureRoomField().getText();

        System.out.println("[DEBUG] 요청 정보 → 건물: " + building + ", 층: " + floor + ", 강의실: " + room);

        BasicResponse res = roomReservationClientController.weekRoomReservationByLectureroom(
                building, floor, room
        );

        System.out.println("[DEBUG] 서버 응답 코드: " + (res != null ? res.code : "null"));

        if (res == null) {
            System.out.println("[ERROR] 서버 응답이 null입니다.");
            return grid;
        }

        if (!res.code.equals("200")) {
            System.out.println("[WARN] 예약된 정보가 존재하지 않습니다. → " + res.data);
        } else {
            try {
                grid = (RoomReservation[][]) res.data;
                System.out.println("[DEBUG] 예약 데이터 수신 완료");
            } catch (Exception e) {
                System.out.println("[ERROR] 데이터 파싱 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("[DEBUG] fetchWeeklyReservationSchedule 종료");
        return grid;
    }

    // 7. 교시별 시간 문자열 생성
    private String[][] generateTimeSlotLabels(){
        String[][] dummySubjects = new String[7][13];
        for (int j = 0; j < 7; j++) {
            for (int k = 0; k < 13; k++) {
                int startHour = 9 + k;
                int endHour = startHour + 1;
                dummySubjects[j][k] = String.format("%02d:00 ~ %02d:00", startHour, endHour);
            }
        }
        return dummySubjects;
    }

    // 8. 캘린더 버튼에 강의 및 예약 정보 반영
    private void applyScheduleToCalendar(Lecture[][] schedule, RoomReservation[][] reservationSchedule, String[][] labels) {
        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                String buttonName = "day" + day + "_" + period;

                for (Component comp : view.getCalendar().getComponents()) {
                    if (!(comp instanceof TimeSlotButton dayBtn)) continue;
                    if (!buttonName.equals(dayBtn.getName())) continue;

                    // 초기화
                    dayBtn.setText(labels[day][period]);
                    dayBtn.setOpaque(true);
                    dayBtn.setContentAreaFilled(true);
                    dayBtn.setForeground(Color.BLACK);
                    dayBtn.setLecture(null);
                    dayBtn.setRoomReservation(null);

                    // 기존 리스너 제거
                    for (ActionListener al : dayBtn.getActionListeners()) {
                        dayBtn.removeActionListener(al);
                    }

                    // 1. 강의가 있는 경우
                    if (schedule[day][period] != null) {
                        dayBtn.setEnabled(false);
                        dayBtn.setLecture(schedule[day][period]);
                        dayBtn.setText(schedule[day][period].getTitle());
                        dayBtn.setBackground(new Color(100, 149, 237)); // 파란색
                        continue;
                    }

                    // 2. 예약이 있는 경우 (선택 불가)
                    if (reservationSchedule != null && reservationSchedule[day][period] != null) {
                        RoomReservation r = reservationSchedule[day][period];
                        dayBtn.setEnabled(false);
                        dayBtn.setRoomReservation(r);
                        dayBtn.setText(r.getTitle());

                        if ("승인".equals(r.getStatus())) {
                            dayBtn.setBackground(new Color(20, 112, 61)); // 초록
                        } else {
                            dayBtn.setBackground(new Color(241, 196, 15)); // 노란색 (대기)
                        }
                        continue;
                    }

                    // 3. 비어있는 시간대 - 선택 가능
                    dayBtn.setEnabled(true);
                    dayBtn.setBackground(Color.WHITE);

                    dayBtn.addActionListener(ev -> {
                        TimeSlotButton source = (TimeSlotButton) ev.getSource();
                        TimeSlotButton prev = (TimeSlotButton) view.getSelectedCalendarButton();

                        // 같은 버튼 다시 누르면 → 선택 해제 (토글)
                        if (prev == source) {
                            source.setBackground(Color.WHITE);
                            view.setSelectedCalendarButton(null);
                            view.getReservationDateField().setText("");
                            view.getReservationTimeField().setText("");
                            return;
                        }

                        // 상태별 색상 복원 로직 추가
                        if (prev != null) {
                            RoomReservation r = prev.getRoomReservation();

                            if (r != null) {
                                String status = r.getStatus();
                                if ("승인".equals(status)) {
                                    prev.setBackground(new Color(20, 112, 61)); // 승인 색상으로 복원
                                } else if ("대기".equals(status)) {
                                    prev.setBackground(new Color(241, 196, 15)); // 대기 상태 복원
                                } else {
                                    prev.setBackground(Color.LIGHT_GRAY); // 기타 예약 상태
                                }
                            } else {
                                prev.setBackground(Color.WHITE); // 빈 칸 복원
                            }
                        }

                        // 새 버튼 선택
                        source.setBackground(new Color(30, 144, 255)); // 선택: 파란색
                        view.setSelectedCalendarButton(source);

                        String[] dateTime = parseDateTimeFromButtonName(source.getName());
                        if (dateTime != null) {
                            view.getReservationDateField().setText(dateTime[0]);
                            view.getReservationTimeField().setText(dateTime[1]);
                        } else {
                            view.getReservationDateField().setText("");
                            view.getReservationTimeField().setText("");
                        }
                    });
                }
            }
        }
    }

    // =================================================================================================================

    // 예약하는 버튼 기능 TODO: 확인 완료 + SwingWorker
    private void lectureRoomReservationButton(ActionEvent e) {

        if (!validateReservationInput()) {
            updateCalendarWithDummyData();
            return;
        }

        // 데이터 수집
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

        // 예약 확인 다이얼로그
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "다음 예약을 진행하시겠습니까?\n" +
                        reservationDate + " " + startTime + " ~ " + endTime + "\n" +
                        building + " " + floor + "층 " + lectureRoom + "호\n제목: " + title,
                "예약 확인",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        // DTO 생성
        RoomReservationRequest reservationRequest = new RoomReservationRequest(
                building, floor, lectureRoom, title, description,
                reservationDate, dayOfWeekStr, startTime, endTime, userNumber
        );
        System.out.println(title + " | " +description);

        // 비동기 실행
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
                        case "200" -> {
                            JOptionPane.showMessageDialog(null, response.data, "예약 완료", JOptionPane.INFORMATION_MESSAGE);
                        }
                        case "409" -> {
                            JOptionPane.showMessageDialog(null, response.data, "예약 중복 오류", JOptionPane.WARNING_MESSAGE);
                        }
                        case "403" -> {
                            JOptionPane.showMessageDialog(null, response.data, "예약 제한 초과", JOptionPane.WARNING_MESSAGE);
                        }
                        default -> {
                            JOptionPane.showMessageDialog(null, response.data, "서버 오류 또는 예외", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    refreshReservationWriteDataFieldForCalendar();
                    updateCalendarWithDummyData(); // 또는 updateCalendarAsync()

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "예약 요청 처리 중 예외 발생: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    // 수정 안해도 되는 부분 ================================================================================================

    // 필드 초기화 하는 기능 - 수정 금지
    private void refreshReservationWriteDataField(){
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
        view.getLectureRoomField().setText("");
    }
    // 예약 추가/삭제 후 필드를 초기화 하는 기능 - 수정 금지
    private void refreshReservationWriteDataFieldForCalendar(){
        view.getTitleField().setText("");
        view.getDescriptionField().setText("");
        view.getReservationTimeField().setText("");
    }
    // 선택 UI 초기화 메서드 - 수정 금지
    private void clearSelectionUI() {
        view.getBuildingField().setText("");
        view.getFloorField().setText("");
        view.getFloorButtonPanel().removeAll();
        view.getLectureRoomList().removeAll();
        view.getCalendar().setVisible(false);
        view.clearSelectedButtons();
    }
    // 건물에 따른 강의실 정보 가져오는 기능( 추후 파일에서 정보를 읽어오는 형태로 변경 예정) - 수정 금지
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
    // 예약 진행 시 유효성 검사 - 수정 금지
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
            JOptionPane.showMessageDialog(null, "제목은 공백이 아니고 2자 이상 입력해야 합니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 설명 유효성 검사 (선택 사항이 아니면 검사)
        if (description == null || description.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "설명을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
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
    // 안전한 text 연산 추가
    private String safeText(String text) {
        if (text != null && text.matches("^[가-힣\\s]+$")) {
            return text + " "; // 한글-only 문자열일 경우 공백 추가
        }
        return text;
    }
}
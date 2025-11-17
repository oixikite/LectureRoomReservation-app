/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.controller.event;

import deu.controller.business.LectureClientController;
import deu.controller.business.RoomReservationClientController;
import deu.model.entity.Lecture;
import deu.model.entity.RoomReservation;
import deu.view.Reservation; // Reservation 뷰
import deu.view.custom.TimeSlotButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author oixikite
 */

/**
 * [템플릿] '강의실 예약' 탭의 캘린더 뷰(일별/주별)를 위한 템플릿 메서드
 * SwingWorker를 상속받아 백그라운드에서 데이터를 조회하고, UI를 갱신합니다.
 */

public abstract class AbstractCalendarViewTemplate extends SwingWorker<Object[], Void> {

    protected Reservation view; // 제어할 Reservation 뷰
    protected LectureClientController lectureClient;
    protected RoomReservationClientController reservationClient;
    protected String building;
    protected String floor;
    protected String room;

    public AbstractCalendarViewTemplate(Reservation view,
                                        String building, String floor, String room) {
        this.view = view;
        this.lectureClient = LectureClientController.getInstance();
        this.reservationClient = RoomReservationClientController.getInstance();
        this.building = building;
        this.floor = floor;
        this.room = room;
    }

    /**
     * 1. 템플릿 메서드 (doInBackground)
     * - 백그라운드 스레드에서 실행될 알고리즘 뼈대
     */
    @Override
    protected Object[] doInBackground() throws Exception {
        Object lectureData = fetchLectureData();
        Object reservationData = fetchReservationData();
        String[][] timeLabels = generateTimeSlotLabels(); 

        return new Object[]{lectureData, reservationData, timeLabels};
    }

    /**
     * 2. 템플릿 메서드 (done)
     * - UI 스레드에서 실행될 알고리즘 뼈대
     */
    @Override
    protected void done() {
        try {
            prepareCalendar();
            Object[] data = get(); 
            applyScheduleToCalendar(data[0], data[1], data[2]); 
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "캘린더 갱신 중 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        } finally {
            finalizeCalendar();
        }
    }

    // [Abstract] '강의' 데이터를 가져오는 방법
    protected abstract Object fetchLectureData(); 

    // [Abstract] '예약' 데이터를 가져오는 방법
    protected abstract Object fetchReservationData(); 

    // [Common] 캘린더 UI 준비
    protected void prepareCalendar() {
        view.getCalendar().setVisible(false);
        view.setSelectedCalendarButton(null); 

        for (Component comp : view.getCalendar().getComponents()) {
            if (comp instanceof TimeSlotButton btn) {
                for (ActionListener al : btn.getActionListeners()) {
                    btn.removeActionListener(al);
                }
                btn.setText("");
                btn.setBackground(Color.WHITE);
                btn.setOriginalBackground(Color.WHITE);
                btn.setLecture(null);
                btn.setRoomReservation(null);
                btn.setEnabled(true); 
            }
        }
    }

    // [Common] 캘린더 UI 갱신
    protected void applyScheduleToCalendar(Object lectureData, Object reservationData, Object labelData) {
        
        Lecture[][] lectures = (Lecture[][]) lectureData;
        RoomReservation[][] reservations = (RoomReservation[][]) reservationData;
        String[][] timeLabels = (String[][]) labelData;

        for (int day = 0; day < 7; day++) {
            for (int period = 0; period < 13; period++) {
                String buttonName = "day" + day + "_" + period;
                Lecture lecture = (lectures != null && lectures[day] != null) ? lectures[day][period] : null;
                RoomReservation reservation = (reservations != null && reservations[day] != null) ? reservations[day][period] : null;

                for (Component comp : view.getCalendar().getComponents()) {
                    if (!(comp instanceof TimeSlotButton dayBtn)) continue;
                    if (!buttonName.equals(dayBtn.getName())) continue;

                    dayBtn.setText(timeLabels[day][period]);
                    dayBtn.setOpaque(true);
                    dayBtn.setContentAreaFilled(true);
                    dayBtn.setForeground(Color.BLACK);
                    dayBtn.setLecture(null);
                    dayBtn.setRoomReservation(null);
                    
                    if (lecture != null) {
                        dayBtn.setEnabled(false);
                        dayBtn.setLecture(lecture);
                        dayBtn.setText(lecture.getTitle());
                        dayBtn.setBackground(new Color(100, 149, 237)); 
                        dayBtn.setOriginalBackground(new Color(100, 149, 237));
                        
                    } else if (reservation != null) {
                        dayBtn.setEnabled(true); 
                        dayBtn.setRoomReservation(reservation);
                        dayBtn.setText(reservation.getTitle());
                        
                        if ("승인".equals(reservation.getStatus())) {
                            dayBtn.setBackground(new Color(20, 112, 61)); // 초록
                            dayBtn.setOriginalBackground(new Color(20, 112, 61));
                        } else {
                            dayBtn.setBackground(new Color(241, 196, 15)); // 노랑
                            dayBtn.setOriginalBackground(new Color(241, 196, 15));
                        }
                        addCalendarButtonListener(dayBtn); 
                        
                    } else {
                        dayBtn.setEnabled(true);
                        dayBtn.setBackground(Color.WHITE);
                        dayBtn.setOriginalBackground(Color.WHITE);
                        addCalendarButtonListener(dayBtn); 
                    }
                }
            }
        }
    }

    // [Common] 캘린더 표시
    protected void finalizeCalendar() {
        view.getCalendar().revalidate();
        view.getCalendar().repaint();
        view.getCalendar().setVisible(true);
    }
    
    // [Common] 캘린더 버튼 클릭 리스너
    private void addCalendarButtonListener(TimeSlotButton dayBtn) {
        dayBtn.addActionListener(ev -> {
            TimeSlotButton source = (TimeSlotButton) ev.getSource();
            TimeSlotButton prev = (TimeSlotButton) view.getSelectedCalendarButton();

            if (prev == source) {
                if (source.getOriginalBackground() != null) {
                    source.setBackground(source.getOriginalBackground());
                } else {
                    source.setBackground(Color.WHITE);
                }
                view.setSelectedCalendarButton(null);
                view.getReservationDateField().setText("");
                view.getReservationTimeField().setText("");
                return;
            }

            if (prev != null) {
                if (prev.getOriginalBackground() != null) {
                    prev.setBackground(prev.getOriginalBackground());
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
        });
    }

    // [Common] 시간표 텍스트 생성
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
    
    // [Common] 버튼 이름에서 날짜/시간 파싱
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
}
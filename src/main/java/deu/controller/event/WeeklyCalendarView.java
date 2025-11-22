/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.controller.event;

import deu.model.dto.response.BasicResponse;
import deu.model.entity.Lecture;
import deu.model.entity.RoomReservation;
import deu.view.Reservation;
import deu.view.custom.TimeSlotButton;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.time.LocalDate;

/**
 *
 * @author oixikite
 */

/**
 * [구상 클래스] '주별' 캘린더 뷰를 구현합니다.
 */
public class WeeklyCalendarView extends AbstractCalendarViewTemplate {

    public WeeklyCalendarView(CalendarViewContainer view, String building, String floor, String room) {
        super(view, building, floor, room);
    }

    @Override
    protected Object fetchLectureData() {
        BasicResponse res = lectureClient.returnLectureOfWeek(building, floor, room);
        if (res != null && "200".equals(res.code) && res.data instanceof Lecture[][]) {
            return res.data;
        }
        return new Lecture[7][13];
    }

    @Override
    protected Object fetchReservationData() {
        BasicResponse res = reservationClient.weekRoomReservationByLectureroom(building, floor, room);
        if (res != null && "200".equals(res.code) && res.data instanceof RoomReservation[][]) {
            return res.data;
        }
        return new RoomReservation[7][13];
    }

    @Override
    protected void applyScheduleToCalendar(Object lectureData, Object reservationData, Object labelData) {
        Lecture[][] lectures = (Lecture[][]) lectureData;
        RoomReservation[][] reservations = (RoomReservation[][]) reservationData;
        
        Reservation resView = (view instanceof Reservation) ? (Reservation) view : null;

        for (Component comp : view.getCalendarPanel().getComponents()) {
            if (!(comp instanceof TimeSlotButton dayBtn)) continue;
            
            for (ActionListener al : dayBtn.getActionListeners()) {
                dayBtn.removeActionListener(al);
            }

            String name = dayBtn.getName(); 
            if (name == null || !name.startsWith("day")) continue;
            
            try {
                String[] parts = name.substring(3).split("_");
                int day = Integer.parseInt(parts[0]);
                int period = Integer.parseInt(parts[1]);

                int startHour = 9 + period;
                String timeLabel = String.format("%02d:00", startHour);
                
                dayBtn.setText(timeLabel);
                dayBtn.setOpaque(true);
                dayBtn.setContentAreaFilled(true);
                dayBtn.setForeground(Color.BLACK); 
                dayBtn.setLecture(null);
                dayBtn.setRoomReservation(null);
                dayBtn.setEnabled(true); 
                dayBtn.setBackground(Color.WHITE);
                dayBtn.setOriginalBackground(Color.WHITE);

                Lecture lecture = (lectures != null && lectures[day] != null) ? lectures[day][period] : null;
                RoomReservation reservation = (reservations != null && reservations[day] != null) ? reservations[day][period] : null;

                // 1. 강의 (진한 파랑)
                if (lecture != null) {
                    dayBtn.setLecture(lecture);
                    dayBtn.setText("<html><center>" + lecture.getTitle() + "</center></html>");
                    
                    Color lectureColor = new Color(65, 105, 225); // RoyalBlue
                    dayBtn.setBackground(lectureColor); 
                    dayBtn.setOriginalBackground(lectureColor);
                    dayBtn.setForeground(Color.WHITE); 
                } 
                // 2. 예약 (진한 초록 / 밝은 노랑)
                else if (reservation != null) {
                    dayBtn.setRoomReservation(reservation);
                    dayBtn.setText("<html><center>" + reservation.getTitle() + "</center></html>");
                    
                    if ("승인".equals(reservation.getStatus())) {
                        Color approvedColor = new Color(34, 139, 34); // ForestGreen
                        dayBtn.setBackground(approvedColor); 
                        dayBtn.setOriginalBackground(approvedColor);
                        dayBtn.setForeground(Color.WHITE); 
                    } else {
                        // [수정] 밝은 노란색 (Gold보다 약간 밝은 느낌) - 검정 글씨가 잘 보임
                        Color pendingColor = new Color(255, 215, 0); // Gold
                        dayBtn.setBackground(pendingColor); 
                        dayBtn.setOriginalBackground(pendingColor);
                        dayBtn.setForeground(Color.BLACK); // 밝은 노랑 위에는 검정 글씨가 가독성 좋음
                    }
                } 
                // 3. 빈 칸 (예약 가능)
                else {
                    if (resView != null) {
                        dayBtn.addActionListener(e -> {
                            if (resView.getSelectedCalendarButton() != null) {
                                TimeSlotButton prev = (TimeSlotButton) resView.getSelectedCalendarButton();
                                prev.setBackground(prev.getOriginalBackground() != null ? prev.getOriginalBackground() : Color.WHITE);
                                prev.setForeground(Color.BLACK);
                            }
                            
                            dayBtn.setBackground(new Color(30, 144, 255)); 
                            dayBtn.setForeground(Color.WHITE);
                            resView.setSelectedCalendarButton(dayBtn);
                            
                            LocalDate targetDate = LocalDate.now().plusDays(day); 
                            String startTimeStr = String.format("%02d:00", 9 + period);
                            String endTimeStr = String.format("%02d:00", 10 + period);
                            
                            resView.getReservationDateField().setText(targetDate.toString());
                            resView.getReservationTimeField().setText(startTimeStr + " ~ " + endTimeStr);
                            
                            if (resView.getUpdateButton() != null) {
                                resView.getUpdateButton().setEnabled(true);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                // 무시
            }
        }
    }
}

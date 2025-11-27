/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.controller.event;

import deu.controller.business.LectureClientController;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.Lecture;
import deu.view.custom.TimeSlotButton;

import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;

/**
 *
 * @author oixikite
 */

/**
 * [구상 클래스] '일별' 캘린더 뷰를 구현합니다.
 */
public class DailyCalendarView extends AbstractCalendarViewTemplate {
    
    private final LocalDate targetDate;

    public DailyCalendarView(CalendarViewContainer view, String building, String floor, String room, LocalDate date) {
        super(view, building, floor, room);
        this.targetDate = date;
    }

    @Override
    protected Object fetchLectureData() {
        BasicResponse res = LectureClientController.getInstance()
                .returnLectureOfDay(building, floor, room, targetDate);
        
        // [수정] 응답 코드 검증 로직 강화
        if (res == null) {
             throw new RuntimeException("응답이 없습니다.");
        }
        
        // 성공(200)일 때만 데이터 반환
        if ("200".equals(res.code) && res.data instanceof Lecture[]) {
            return res.data;
        } else if ("200".equals(res.code)) {
             // 데이터가 없어서 빈 리스트가 온 경우 (정상) -> 빈 배열 반환
             return new Lecture[13];
        } else {
            // [핵심] 500 등 에러 코드인 경우 예외 발생 -> 팝업으로 이어짐
            throw new RuntimeException(res.data.toString());
        }
    }

    @Override
    protected Object fetchReservationData() { return null; }

    @Override
    protected void applyScheduleToCalendar(Object lectureData, Object reservationData, Object labelData) {
        Lecture[] lectures = (Lecture[]) lectureData; 
        Component[] components = view.getCalendarPanel().getComponents();

        view.setDateHeader(targetDate.toString()); 

        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof TimeSlotButton btn) {
                // 배경색 표시를 위한 설정
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.BLACK);
                btn.setEnabled(true); 
                
                // [요청] 빈 칸 시간 표시
                String timeLabel = String.format("%02d:00 ~ %02d:00", 9 + i, 10 + i);
                
                if (lectures != null && i < lectures.length && lectures[i] != null) {
                    Lecture l = lectures[i];
                    btn.setText("<html><b>" + timeLabel + "</b><br>&nbsp;&nbsp;" + l.getTitle() + " (" + l.getProfessor() + ")</html>");
                    btn.setBackground(new Color(65, 105, 225)); // RoyalBlue
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setText(timeLabel);
                }
            }
        }
    }
}

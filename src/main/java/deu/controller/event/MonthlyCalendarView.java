/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.controller.event;

import deu.controller.business.LectureClientController;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.Lecture;

import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.UIManager;

/**
 *
 * @author oixikite
 */

/**
 * [구상 클래스] '월별' 캘린더 뷰를 구현합니다.
 */
public class MonthlyCalendarView extends AbstractCalendarViewTemplate {

    private final LocalDate targetMonth; 

    public MonthlyCalendarView(CalendarViewContainer view, String building, String floor, String room, LocalDate targetMonth) {
        super(view, building, floor, room);
        this.targetMonth = targetMonth;
    }

    @Override
    protected Object fetchLectureData() {
        BasicResponse res = LectureClientController.getInstance()
                .returnLectureOfMonth(building, floor, room, targetMonth);
        if (res != null && "200".equals(res.code) && res.data instanceof Lecture[][]) {
            return res.data;
        }
        return new Lecture[YearMonth.from(targetMonth).lengthOfMonth()][13];
    }

    @Override
    protected Object fetchReservationData() { return null; }

    @Override
    protected void applyScheduleToCalendar(Object lectureData, Object reservationData, Object labelData) {
        Lecture[][] monthlyData = (Lecture[][]) lectureData; 
        Component[] components = view.getCalendarPanel().getComponents();

        YearMonth ym = YearMonth.from(targetMonth);
        view.setDateHeader(ym.getYear() + "년 " + ym.getMonthValue() + "월");

        int firstDayOfWeekVal = ym.atDay(1).getDayOfWeek().getValue(); 
        int startOffset = (firstDayOfWeekVal == 7) ? 0 : firstDayOfWeekVal; 
        int daysInMonth = ym.lengthOfMonth();

        for (int i = 0; i < components.length; i++) {
            if (!(components[i] instanceof JButton btn)) continue;

            btn.setText("");
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
            btn.setEnabled(true); 
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setBorder(UIManager.getBorder("Button.border"));

            int day = i - startOffset + 1;

            if (day > 0 && day <= daysInMonth) {
                StringBuilder html = new StringBuilder("<html>");
                
                // 날짜 색상
                int dayOfWeekIndex = i % 7;
                String dateColor = (dayOfWeekIndex == 0) ? "red" : (dayOfWeekIndex == 6 ? "blue" : "black");
                boolean hasClass = false;

                if (monthlyData != null && (day - 1) < monthlyData.length) {
                    Lecture[] lecturesOfDay = monthlyData[day - 1];
                    if (lecturesOfDay != null) {
                        for (Lecture l : lecturesOfDay) {
                            if (l != null) { hasClass = true; break; }
                        }
                    }
                }

                // 강의 있음 -> 진한 파랑 배경 + 흰색 날짜
                if (hasClass) {
                    btn.setBackground(new Color(65, 105, 225)); // RoyalBlue
                    dateColor = "white";
                }

                html.append(String.format("<b><font color='%s'>%d</font></b><br>", dateColor, day));

                // 강의 목록 표시
                if (monthlyData != null && (day - 1) < monthlyData.length) {
                    Lecture[] lecturesOfDay = monthlyData[day - 1];
                    if (lecturesOfDay != null) {
                        Set<String> displayed = new HashSet<>();
                        for (Lecture l : lecturesOfDay) {
                            if (l != null) {
                                String key = l.getTitle();
                                if (!displayed.contains(key)) {
                                    // 흰색 글씨로 강의명 표시
                                    html.append(String.format("<span style='font-size:9px; color:white;'>%s</span><br>", l.getTitle()));
                                    displayed.add(key);
                                }
                            }
                        }
                    }
                }
                html.append("</html>");
                btn.setText(html.toString());
            } else {
                btn.setEnabled(false);
                btn.setBackground(new Color(240, 240, 240));
                btn.setBorder(BorderFactory.createEmptyBorder());
            }
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package deu.controller.event;

import javax.swing.JPanel;

/**
 *
 * @author oixikite
 */
public interface CalendarViewContainer {

    /**
     * 캘린더 버튼들이 담긴 패널을 반환합니다.
     * @return 
     */
    JPanel getCalendarPanel();

    /**
     * 상단 날짜/월 제목을 설정합니다.(예: 2025. 11) 주간 뷰는 자체적으로 처리하므로 비워둘 수 있습니다.
     * @param text
     */
    void setDateHeader(String text);
}

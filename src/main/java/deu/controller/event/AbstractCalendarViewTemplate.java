/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.controller.event;

import deu.controller.business.LectureClientController;
import deu.controller.business.RoomReservationClientController;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author oixikite
 */
/**
 * 캘린더 뷰 템플릿 (주간, 월별, 일별 공통 로직)
 */
public abstract class AbstractCalendarViewTemplate extends SwingWorker<Object[], Void> {

    // [핵심 변경] 구체적인 Reservation 뷰 대신 인터페이스 사용
    protected CalendarViewContainer view;

    protected LectureClientController lectureClient;
    protected RoomReservationClientController reservationClient;
    protected String building;
    protected String floor;
    protected String room;

    // [핵심 변경] 생성자 매개변수 타입 변경
    public AbstractCalendarViewTemplate(CalendarViewContainer view, String building, String floor, String room) {
        this.view = view;
        this.lectureClient = LectureClientController.getInstance();
        this.reservationClient = RoomReservationClientController.getInstance();
        this.building = building;
        this.floor = floor;
        this.room = room;
    }

    @Override
    protected Object[] doInBackground() throws Exception {
        Object lectureData = fetchLectureData();
        Object reservationData = fetchReservationData();
        return new Object[]{lectureData, reservationData};
    }

    @Override
    protected void done() {
        try {
            prepareCalendar();
            Object[] data = get();
            // 데이터가 null일 경우에 대비해 null 처리 후 호출
            applyScheduleToCalendar(
                    data[0],
                    data.length > 1 ? data[1] : null,
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "캘린더 갱신 중 오류: " + e.getMessage());
        } finally {
            finalizeCalendar();
        }
    }

    // [Abstract] 하위 클래스에서 구현할 데이터 조회 메소드
    protected abstract Object fetchLectureData();

    protected abstract Object fetchReservationData();

    // [Abstract] 하위 클래스에서 구현할 UI 그리기 메소드
    protected abstract void applyScheduleToCalendar(Object lectureData, Object reservationData, Object labelData);

    // [Common] 캘린더 준비 (화면 숨김 등)
    protected void prepareCalendar() {
        if (view != null && view.getCalendarPanel() != null) {
            view.getCalendarPanel().setVisible(false);
        }
    }

    // [Common] 캘린더 마무리 (화면 표시)
    protected void finalizeCalendar() {
        if (view != null && view.getCalendarPanel() != null) {
            view.getCalendarPanel().revalidate();
            view.getCalendarPanel().repaint();
            view.getCalendarPanel().setVisible(true);
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.controller.event;

import deu.controller.business.LectureClientController;
import deu.controller.business.RoomReservationClientController;

import java.util.concurrent.ExecutionException;
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

    /**
     * [기존 생성자 유지]
     * 기존 코드와의 호환성을 위해 유지하며, 내부적으로 아래의 protected 생성자를 호출합니다.
     * 실제 앱 실행 시에는 싱글톤 인스턴스를 자동으로 주입합니다.
     * @param view
     * @param building
     * @param floor
     * @param room
     */
    public AbstractCalendarViewTemplate(CalendarViewContainer view, String building, String floor, String room) {
        this(view, building, floor, room, LectureClientController.getInstance(), RoomReservationClientController.getInstance());
    }

    /**
     * [Refactoring - 신규 추가] 테스트를 위한 생성자 (Dependency Injection)
     * 테스트 코드에서 컨트롤러(Mock 객체 등)를 직접 주입할 수 있도록 합니다.protected로 선언하여 외부(다른 패키지)에서의 무분별한 사용을 막고 상속/테스트에서만 사용합니다.
     * @param view
     * @param building
     * @param floor
     * @param room
     * @param lectureClient
     * @param reservationClient
     */
    protected AbstractCalendarViewTemplate(CalendarViewContainer view, String building, String floor, String room,
                                           LectureClientController lectureClient, RoomReservationClientController reservationClient) {
        this.view = view;
        this.building = building;
        this.floor = floor;
        this.room = room;
        this.lectureClient = lectureClient;
        this.reservationClient = reservationClient;
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
            // [수정] get()을 직접 호출하지 않고 래퍼 메소드 사용 (테스트 오버라이드 허용)
            Object[] data = getResult();            // 데이터가 null일 경우에 대비해 null 처리 후 호출
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

    // [신규] 테스트에서 이 메소드를 오버라이드하여 가짜 데이터를 반환하도록 함
    protected Object[] getResult() throws Exception {
        return get(); // 실제 런타임에선 SwingWorker.get() 호출
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

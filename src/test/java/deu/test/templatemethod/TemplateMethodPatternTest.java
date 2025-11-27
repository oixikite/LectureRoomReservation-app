/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.test.templatemethod;

import deu.controller.event.AbstractCalendarViewTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author oixikite
 */

/**
 * [단위 테스트] 템플릿 메소드 패턴 구조 검증
 * 목적: 상위 클래스(Template)가 정의한 알고리즘 골격(순서)대로 하위 클래스 메소드가 호출되는지 확인
 */
public class TemplateMethodPatternTest {
    // 테스트를 위한 Mock 구현체 (서버 통신 X, 로직 기록 O)
    static class MockCalendarView extends AbstractCalendarViewTemplate {
        public List<String> executionLog = new ArrayList<>();

        // 앞서 리팩토링한 'Protected 생성자'를 사용하여 의존성(Controller)에 null 주입
        public MockCalendarView() {
            super(null, "TestBldg", "1F", "101", null, null);
        }

        @Override
        protected Object fetchLectureData() {
            executionLog.add("STEP 1: fetchLectureData"); // 호출 기록
            return "LectureData";
        }

        @Override
        protected Object fetchReservationData() {
            executionLog.add("STEP 2: fetchReservationData"); // 호출 기록
            return "ReservationData";
        }

        @Override
        protected void applyScheduleToCalendar(Object lectureData, Object reservationData, Object labelData) {
            executionLog.add("STEP 3: applyScheduleToCalendar"); // 호출 기록
            
            // 데이터가 상위 클래스를 통해 잘 전달되었는지 확인
            if ("LectureData".equals(lectureData) && "ReservationData".equals(reservationData)) {
                executionLog.add("Check: Data Passed Successfully");
            }
        }

        // [핵심] SwingWorker.get()은 final이라 오버라이드 불가 -> getResult()를 오버라이드
        @Override
        protected Object[] getResult() {
            return new Object[]{"LectureData", "ReservationData"};
        }
        
        // 테스트용: protected 메소드 외부 노출
        public void executeDoInBackground() throws Exception {
            doInBackground();
        }

        public void executeDone() {
            done();
        }
    }

    @Test
    @DisplayName("템플릿 메소드 패턴의 실행 순서 검증 (데이터 조회 -> UI 반영)")
    void testTemplateMethodExecutionFlow() throws Exception {
        // Given
        MockCalendarView mockView = new MockCalendarView();

        // When (템플릿 메소드 실행 시뮬레이션)
        mockView.executeDoInBackground(); // 1단계: 백그라운드 작업
        mockView.executeDone();           // 2단계: 완료 후 UI 작업

        // Then
        List<String> logs = mockView.executionLog;

        // 1. 전체 단계 실행 확인
        assertEquals(4, logs.size());

        // 2. 순서 검증 (템플릿 메소드 패턴의 핵심)
        assertEquals("STEP 1: fetchLectureData", logs.get(0));
        assertEquals("STEP 2: fetchReservationData", logs.get(1));
        assertEquals("STEP 3: applyScheduleToCalendar", logs.get(2));
        
        // 3. 데이터 전달 검증
        assertEquals("Check: Data Passed Successfully", logs.get(3));
    }
}

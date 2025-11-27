/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.test.observer;

/**
 *
 * @author scq37
 */

import deu.controller.observer.NotificationObserver;
import deu.model.dto.response.NotificationDTO;
import deu.service.NotificationPollingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions; // 검증용

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * [단위 테스트] 옵저버 패턴 로직 검증
 */
public class ObserverPatternTest {
@Test
    public void testObserverPatternLifecycle() {
        System.out.println("\n==================================================");
        System.out.println("[Test] 옵저버 패턴(Observer Pattern) 생명주기 검증");
        System.out.println("==================================================\n");

        // 1. 준비 (Arrange)
        System.out.println("--- [Step 1] 주체(Subject)와 관찰자(Observer) 준비 ---");
        NotificationPollingService service = NotificationPollingService.getInstance();
        System.out.println("Subject(PollingService) 준비 완료.");

        TestObserver observer1 = new TestObserver("학생1");
        TestObserver observer2 = new TestObserver("학생2");
        System.out.println("Mock Observer 2명 생성 완료.");

        // 2. 구독 (Subscribe)
        System.out.println("\n--- [Step 2] 구독(Subscribe) 신청 ---");
        service.addObserver(observer1);
        System.out.println(" " + observer1.getName() + "가 구독했습니다.");
        
        service.addObserver(observer2);
        System.out.println(" " + observer2.getName() + "가 구독했습니다.");

        // 3. 1차 실행 (Notify) - 둘 다 받아야 함
        System.out.println("\n--- [Step 3] 1차 알림 전파 (둘 다 수신해야 함) ---");
        triggerNotify(service, "1차 알림: 모두 주목!");

        // 검증
        Assertions.assertTrue(observer1.isReceived(), "학생1 수신 실패");
        Assertions.assertTrue(observer2.isReceived(), "학생2 수신 실패");
        System.out.println("1차 검증 완료: 학생1(O), 학생2(O)");

        // -----------------------------------------------------------
        // 4.구독 해지 (Unsubscribe) 테스트
        // -----------------------------------------------------------
        System.out.println("\n--- [Step 4] '학생1' 구독 해지 및 2차 전파 ---");
        
        // 상태 초기화 (수신 여부 리셋)
        observer1.reset(); 
        observer2.reset();
        
        // 학생1 구독 해지
        service.removeObserver(observer1); 
        System.out.println("➖ " + observer1.getName() + " 구독 취소 (removeObserver)");
        
        // 2차 전파 (Notify)
        triggerNotify(service, "2차 알림: 학생1은 못 들어야 함");
        
        // 5. 최종 검증 (Assert)
        System.out.println("\n--- [Step 5] 최종 검증 (해지된 옵저버 확인) ---");
        
        // 학생2는 여전히 받아야 함 (True)
        Assertions.assertTrue(observer2.isReceived(), "오류: 학생2는 구독 중인데 못 받았습니다.");
        
        //학생1은 받으면 안 됨 (False여야 성공)
        Assertions.assertFalse(observer1.isReceived(), "오류: 학생1은 구독을 취소했는데 알림을 받았습니다!");
        
        System.out.println("최종 검증 완료: 학생2(O), 학생1(X)");

        // 뒷정리
        service.removeObserver(observer2);
        System.out.println("\n==================================================");
        System.out.println("[Test] 옵저버 패턴 테스트 성공 (Passed)");
        System.out.println("==================================================");
    }

    // [리플렉션 헬퍼 메서드] 강제 알림 전파
    private void triggerNotify(NotificationPollingService service, String msg) {
        try {
            List<NotificationDTO> fakeData = new ArrayList<>();
            fakeData.add(new NotificationDTO("Test", msg, System.currentTimeMillis()));

            Method notifyMethod = NotificationPollingService.class.getDeclaredMethod("notifyObservers", List.class);
            notifyMethod.setAccessible(true);
            notifyMethod.invoke(service, fakeData);
        } catch (Exception e) {
            Assertions.fail("리플렉션 실행 중 오류: " + e.getMessage());
        }
    }

    // 테스트용 내부 클래스 (가짜 옵저버)
    static class TestObserver implements NotificationObserver {
        private final String name;
        private boolean received = false;

        public TestObserver(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
        // 상태 초기화 (재사용을 위해)
        public void reset() {
            this.received = false;
        }

        @Override
        public void onNotificationReceived(List<NotificationDTO> notifications) {
            this.received = true;
            String msg = notifications.get(0).getMessage();
            System.out.println("  ➔ [" + name + "] 수신 확인! 메시지: \"" + msg + "\"");
        }

        public boolean isReceived() {
            return received;
        }
    }
}


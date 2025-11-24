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
    public void testObserverPatternFlow() {
        System.out.println("\n==================================================");
        System.out.println("[Test] 옵저버 패턴(Observer Pattern) 동작 검증 시작");
        System.out.println("==================================================\n");

        // 1. 준비 (Arrange)
        System.out.println("--- [Step 1] 주체(Subject)와 관찰자(Observer) 준비 ---");
        NotificationPollingService service = NotificationPollingService.getInstance();
        System.out.println("Subject(PollingService) 준비 완료.");

        TestObserver observer1 = new TestObserver("학생1(Observer 1)");
        TestObserver observer2 = new TestObserver("학생2(Observer 2)");
        System.out.println("Mock Observer 2명 생성 완료.");


        // 2. 구독 (Subscribe)
        System.out.println("\n--- [Step 2] 구독(Subscribe) 신청 ---");
        service.addObserver(observer1);
        System.out.println("  " + observer1.getName() + "가 구독했습니다.");
        
        service.addObserver(observer2);
        System.out.println("  " + observer2.getName() + "가 구독했습니다.");


        // 3. 실행 (Act) - 강제 알림 전파
        System.out.println("\n--- [Step 3] 상태 변화 발생 및 전파 (Notify) ---");
        System.out.println("Subject: \"새로운 알림이 왔습니다! 모두에게 알립니다!\"");

        try {
            // 가짜 데이터 생성
            List<NotificationDTO> fakeData = new ArrayList<>();
            fakeData.add(new NotificationDTO("테스트 알림", "옵저버 패턴이 정상 작동합니다!", System.currentTimeMillis()));

            // 리플렉션으로 private 메서드인 notifyObservers 강제 호출
            Method notifyMethod = NotificationPollingService.class.getDeclaredMethod("notifyObservers", List.class);
            notifyMethod.setAccessible(true);
            notifyMethod.invoke(service, fakeData);
            
        } catch (Exception e) {
            Assertions.fail("리플렉션 실행 중 오류 발생: " + e.getMessage());
        }


        // 4. 검증 (Assert)
        System.out.println("\n--- [Step 4] 결과 검증 (Assert) ---");
        
        Assertions.assertTrue(observer1.isReceived(), "학생1이 알림을 받지 못했습니다.");
        Assertions.assertTrue(observer2.isReceived(), "학생2가 알림을 받지 못했습니다.");
        
        System.out.println("검증 완료: 모든 옵저버가 알림을 정상적으로 수신했습니다.");


        // 5. 뒷정리
        service.removeObserver(observer1);
        service.removeObserver(observer2);
        System.out.println("\n==================================================");
        System.out.println("[Test] 옵저버 패턴 테스트 성공 (Passed)");
        System.out.println("==================================================");
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

        @Override
        public void onNotificationReceived(List<NotificationDTO> notifications) {
            this.received = true;
            String msg = notifications.get(0).getMessage();
            // 수신 시 콘솔에 출력
            System.out.println("   ➔ [" + name + "] 수신 확인! 메시지: \"" + msg + "\"");
        }

        public boolean isReceived() {
            return received;
        }
    }
}


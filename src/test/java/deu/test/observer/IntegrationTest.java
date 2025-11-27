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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * [통합 테스트] JUnit 5 기반
 * 목적: 실제 서버와 통신하여 옵저버 패턴이 작동하는지 검증
 * 조건: 로컬 서버(DeuLectureRoomServer)가 실행 중이어야 함
 * 테스트 방법 : 서버 실행시키고, 예약을 승인 또는 취소를 시켜 알림 발생시킬 것. 알림이 발생되면 성공. 안되면 실패
 */

public class IntegrationTest {
    private NotificationPollingService service;
    // 테스트할 사용자 ID (실제 서버에 데이터 변경을 일으킬 ID)
    private final String TEST_USER_ID = "s20233016"; 
    // 테스트 대기 시간 (초)
    private final int TIMEOUT_SECONDS = 15;

    @BeforeEach
    void setUp() {
        service = NotificationPollingService.getInstance();
        // 테스트 전 확실하게 초기화
        service.stop(); 
    }

    @AfterEach
    void tearDown() {
        // 테스트 종료 후 서비스 중단
        service.stop();
    }

    @Test
    @DisplayName("실제 서버 통신 및 옵저버 알림 수신 테스트")
    public void testRealServerNotificationFlow() throws InterruptedException {
        System.out.println("\n==================================================");
        System.out.println("[Integration Test] 서버 통신 및 옵저버 알림 검증");
        System.out.println("조건: 서버가 켜져 있어야 하며, 15초 내에 예약 변경이 발생해야 함");
        System.out.println("==================================================\n");

        // 1. 동기화 도구 생성 (알림이 올 때까지 1번 기다리겠다는 뜻)
        CountDownLatch latch = new CountDownLatch(1);

        // 2. 실제 작동할 테스트 옵저버 생성
        NotificationObserver testObserver = new NotificationObserver() {
            @Override
            public void onNotificationReceived(List<NotificationDTO> notifications) {
                System.out.println("\n[Observer] 서버로부터 알림 수신 성공!");
                for (NotificationDTO dto : notifications) {
                    System.out.println("   [" + dto.getTitle() + "] " + dto.getMessage());
                }
                
                // 3. 알림을 받으면 빗장을 풂 (대기 해제)
                latch.countDown(); 
            }
        };

        // 4. 서비스 시작
        service.addObserver(testObserver);
        service.start(TEST_USER_ID);
        System.out.println("[대기 중] " + TIMEOUT_SECONDS + "초 안에 이벤트를 발생시키세요!");
        System.out.println("1. 관리자(m-admin)로 로그인하세요.");
        System.out.println("폴링 시작됨. [" + TEST_USER_ID + "] 계정의 알림을 대기합니다...");
        System.out.println("지금 관리자 프로그램에서 [" + TEST_USER_ID + "]의 예약을 승인/삭제 해주세요!");

        // 5. 대기 (최대 15초 동안 latch가 풀리기를 기다림)
        // 알림이 오면 true, 시간이 초과되면 false 반환
        boolean received = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // 6. 검증 (Assert)
        if (received) {
            System.out.println("테스트 성공: 제한 시간 내에 알림을 수신했습니다.");
        } else {
            System.out.println("테스트 실패: 제한 시간(" + TIMEOUT_SECONDS + "초) 내에 알림이 오지 않았습니다.");
        }

        // JUnit 단언문 (실패 시 빨간불)
        Assertions.assertTrue(received, "서버로부터 알림을 받지 못하고 타임아웃 되었습니다.");
    }
}

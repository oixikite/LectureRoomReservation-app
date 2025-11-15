/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.service;

/**
 *
 * @author scq37
 */
import deu.controller.business.NotificationClientController;
import deu.controller.observer.NotificationObserver;
import deu.model.dto.response.NotificationDTO;

import javax.swing.Timer;
import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;

/**
 * [Subject] 옵저버 패턴의 주체
 * 주기적으로 서버를 폴링하고, 새 데이터가 있으면 옵저버들에게 전파
 */
public class NotificationPollingService {

    private static final NotificationPollingService instance = new NotificationPollingService();
    
    // 관찰자(Observer) 목록
    private final List<NotificationObserver> observers = new ArrayList<>();
    
    // 서버 통신 담당 컨트롤러
    private final NotificationClientController clientController = NotificationClientController.getInstance();
    
    private Timer timer;
    private String currentUserId;

    private NotificationPollingService() {}

    public static NotificationPollingService getInstance() {
        return instance;
    }

    // ========================================================
    // 옵저버 패턴 핵심 로직 (구독/해지/전파)
    // ========================================================
    
    public void addObserver(NotificationObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(List<NotificationDTO> notifications) {
        for (NotificationObserver observer : observers) {
            observer.onNotificationReceived(notifications);
        }
    }

    // ========================================================
    // 폴링 로직 (타이머 & 서버 요청)
    // ========================================================

    /**
     * 폴링 시작
     * @param userId 알림을 조회할 사용자 ID
     */
    public void start(String userId) {
        this.currentUserId = userId;
        
        // 이미 실행 중이라면 중복 실행 방지
        if (timer != null && timer.isRunning()) return;

        // 3초마다 poll() 실행
        timer = new Timer(3000, e -> poll()); 
        timer.start();
        System.out.println("[NotificationPollingService] 알림 감시 시작 (User: " + userId + ")");
    }

    /**
     * 폴링 중지 (로그아웃 시 호출)
     */
    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
            System.out.println("[NotificationPollingService] 알림 감시 중단");
        }
        // 안전을 위해 옵저버 목록도 초기화
        observers.clear();
    }

    /**
     * 실제 서버 요청 수행 (백그라운드 스레드)
     */
    private void poll() {
        if (currentUserId == null || currentUserId.isEmpty()) return;

        // UI 멈춤 방지를 위해 SwingWorker 사용
        new SwingWorker<List<NotificationDTO>, Void>() {
            @Override
            protected List<NotificationDTO> doInBackground() {
                // 서버에 요청 (동기식 소켓)
                return clientController.getMyNotifications(currentUserId);
            }

            @Override
            protected void done() {
                try {
                    List<NotificationDTO> result = get();
                    // ★ 새 알림이 있을 때만 옵저버들에게 알림(Notify)!
                    if (result != null && !result.isEmpty()) {
                        notifyObservers(result);
                    }
                } catch (Exception ex) {
                    System.err.println("[NotificationPollingService] 폴링 중 오류: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.controller.observer;

/**
 *
 * @author scq37
 */
import deu.model.dto.response.NotificationDTO;
import java.util.List;

//관찰자(Observer)가 가져야 할 공통 행동(메서드)을 정의


public interface NotificationObserver {
    /**
     * 새로운 알림이 도착했을 때 호출되는 메서드
     * @param notifications 서버로부터 받은 알림 리스트
     */
    void onNotificationReceived(List<NotificationDTO> notifications);
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.controller.business;

/**
 *
 * @author scq37
 */
import deu.config.Config;
import deu.config.ConfigLoader;
import deu.model.dto.request.command.NotificationCommandRequest;
import deu.model.dto.request.command.UserCommandRequest;
import deu.model.dto.response.BasicResponse;
import deu.model.dto.response.NotificationDTO;
import lombok.Getter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NotificationClientController {

    @Getter
    private static final NotificationClientController instance = new NotificationClientController();

    private NotificationClientController() {}

    // 서버 설정 불러오기
    private final Config config = ConfigLoader.getConfig();
    private final String host = config.server.host;
    private final int port = config.server.port;

    /**
     * 서버에 '내 알림 목록'을 요청 (안 읽은 알림 위주)
     * @param userId 사용자 ID (학번)
     * @return 알림 리스트 (없거나 실패 시 빈 리스트 반환)
     */
    public List<NotificationDTO> getMyNotifications(String userId) {
        return requestNotifications("알림 조회", userId);
    }
    
    /**
     * 서버에 '모든 알림 내역'을 요청 (알림함 히스토리용)
     * @param userId 사용자 ID
     * @return 전체 알림 리스트
     */
    public List<NotificationDTO> getAllMyNotifications(String userId) {
        return requestNotifications("알림 전체 조회", userId);
    }
    
    /**
     * [리팩토링] 알림 요청 공통 로직
     */
    @SuppressWarnings("unchecked")
    private List<NotificationDTO> requestNotifications(String command, String userId) {
        try (
            Socket socket = new Socket(host, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            // 1. 요청 객체 생성 (String userId는 Object payload로 전달됨)
            NotificationCommandRequest req = new NotificationCommandRequest(command, userId);
            
            // 2. 전송
            out.writeObject(req);
            out.flush();

            // 3. 응답 수신 (Blocking)
            Object response = in.readObject();

            if (response instanceof BasicResponse) {
                BasicResponse basicResponse = (BasicResponse) response;

                // [성공] 코드가 200이고, 데이터가 List 타입인 경우
                if ("200".equals(basicResponse.code) && basicResponse.data instanceof List) {
                    return (List<NotificationDTO>) basicResponse.data;
                } 
                // [실패] 서버가 에러 메시지(String)를 보낸 경우
                else {
                    System.err.println("[NotificationClientController] 요청 실패 (" + command + "): " + basicResponse.data);
                }
            }

        } catch (Exception e) {
            // 소켓 연결 실패 등 통신 에러 로그
            System.err.println("[NotificationClientController] 통신 오류: " + e.getMessage());
        }
        // 실패 시 빈 리스트 반환하여 UI 오류 방지
        return new ArrayList<>(); 
    }
}
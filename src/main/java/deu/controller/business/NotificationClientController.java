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
     * 서버에 '내 알림 목록'을 요청
     * @param userId 사용자 ID (학번)
     * @return 알림 리스트 (없거나 실패 시 빈 리스트 반환)
     */
    public List<NotificationDTO> getMyNotifications(String userId) {
        try (
            Socket socket = new Socket(host, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            // 1. 요청 객체 생성
            NotificationCommandRequest req = new NotificationCommandRequest("알림 조회", userId);
            
            // 2. 전송
            out.writeObject(req);
            out.flush();

            // 3. 응답 수신
            Object response = in.readObject();

            if (response instanceof BasicResponse basicResponse) {
                if ("200".equals(basicResponse.code) && basicResponse.data instanceof List) {
                    return (List<NotificationDTO>) basicResponse.data;
                }
            }

        } catch (Exception e) {
            // 폴링 중 에러는 사용자에게 계속 띄우면 방해되므로 콘솔에만 로그
            System.err.println("[NotificationClientController] 알림 조회 실패: " + e.getMessage());
        }
        return new ArrayList<>(); // 실패 시 빈 리스트 반환
    }
    
    /**
     * 서버에 '모든 알림 내역'을 요청 (알림함용)
     */
    public List<NotificationDTO> getAllMyNotifications(String userId) {
        try (
            Socket socket = new Socket(host, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            // 커맨드를 "알림 전체 조회"로 변경
            NotificationCommandRequest req = new NotificationCommandRequest("알림 전체 조회", userId);
            
            out.writeObject(req);
            out.flush();

            Object response = in.readObject();

            if (response instanceof BasicResponse basicResponse) {
                if ("200".equals(basicResponse.code) && basicResponse.data instanceof List) {
                    return (List<NotificationDTO>) basicResponse.data;
                }
            }
        } catch (Exception e) {
            System.err.println("[NotificationClientController] 전체 알림 조회 실패: " + e.getMessage());
        }
        return new ArrayList<>();
    }
}

package deu.controller.business;

import deu.model.dto.request.command.UserManagementCommandRequest;
import deu.model.dto.request.data.user.*;
import deu.model.dto.response.BasicResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class UserManagementClientControllerTest {

    private static final int PORT = 9999;
    private Thread mockServerThread;

    @BeforeEach
    void setup() {
        if (mockServerThread != null && mockServerThread.isAlive()) return; // 이미 실행 중이면 패스

        mockServerThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("[MockServer] 테스트 서버 시작됨: " + PORT);
                while (true) {  // 여러 테스트 대응
                    Socket client = serverSocket.accept();
                    new Thread(() -> {
                        try (ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                             ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())) {

                            Object obj = in.readObject();
                            if (obj instanceof UserManagementCommandRequest req) {
                                switch (req.command) {
                                    case "사용자 수정" -> out.writeObject(new BasicResponse("200", "수정 완료"));
                                    case "사용자 삭제" -> out.writeObject(new BasicResponse("200", "삭제 완료"));
                                    case "사용자 조회" -> out.writeObject(new BasicResponse("200", new UserDataModificationRequest("s123", "pw", "홍길동", "소프트웨어")));
                                    case "전체 사용자 조회" -> out.writeObject(new BasicResponse("200", new String[]{"user1", "user2"}));
                                    default -> out.writeObject(new BasicResponse("400", "알 수 없는 요청"));
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("[MockServer-Thread] 오류: " + e.getMessage());
                        }
                    }).start();
                }
            } catch (Exception e) {
                System.out.println("[MockServer] 오류: " + e.getMessage());
            }
        });
        mockServerThread.setDaemon(true);
        mockServerThread.start();
    }

    @DisplayName("사용자 수정 요청 시 정상 응답 코드 확인")
    @Test
    void testUpdateUser() {
        UserManagementClientController client = UserManagementClientController.getInstance();
        BasicResponse res = client.updateUser("s123", "pw", "홍길동", "소프트웨어");
        assertNotNull(res);
        assertEquals("200", res.code);
    }

    @DisplayName("사용자 삭제 요청 시 정상 응답 코드 확인")
    @Test
    void testDeleteUser() {
        UserManagementClientController client = UserManagementClientController.getInstance();
        BasicResponse res = client.deleteUser("s123");
        assertNotNull(res);
        assertEquals("200", res.code);
    }

    @DisplayName("단일 사용자 조회 요청 시 정상 응답 코드 확인")
    @Test
    void testFindUser() {
        UserManagementClientController client = UserManagementClientController.getInstance();
        BasicResponse res = client.findUser("s123");
        assertNotNull(res);
        assertEquals("200", res.code);
    }

    @DisplayName("전체 사용자 조회 요청 시 정상 응답 코드 확인")
    @Test
    void testFindAllUsers() {
        UserManagementClientController client = UserManagementClientController.getInstance();
        BasicResponse res = client.findAllUsers();
        assertNotNull(res);
        assertEquals("200", res.code);
    }
}
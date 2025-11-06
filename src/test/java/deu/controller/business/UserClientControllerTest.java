package deu.controller.business;

import static org.junit.jupiter.api.Assertions.*;

import deu.model.dto.response.BasicResponse;
import deu.model.dto.response.CurrentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static org.mockito.Mockito.*;

public class UserClientControllerTest {

    @DisplayName("로그인 성공 시 응답 코드 200 확인")
    @Test
    void login_success_response_200() throws Exception {
        BasicResponse mockResponse = new BasicResponse("200", "로그인 성공");

        try (
                MockedConstruction<Socket> mockedSocket = mockConstruction(Socket.class);
                MockedConstruction<ObjectOutputStream> mockedOut = mockConstruction(ObjectOutputStream.class);
                MockedConstruction<ObjectInputStream> mockedIn = mockConstruction(ObjectInputStream.class,
                        (mock, context) -> when(mock.readObject()).thenReturn(mockResponse))
        ) {
            UserClientController controller = UserClientController.getInstance();
            BasicResponse response = controller.login("S2023001", "pass123");

            assertNotNull(response);
            assertEquals("200", response.code);
            assertEquals("로그인 성공", response.data);
        }
    }

    @DisplayName("로그인 실패 시 응답 코드 401 확인")
    @Test
    void login_invalid_password_response_401() throws Exception {
        BasicResponse mockResponse = new BasicResponse("401", "비밀번호 오류");

        try (
                MockedConstruction<Socket> mockedSocket = mockConstruction(Socket.class);
                MockedConstruction<ObjectOutputStream> mockedOut = mockConstruction(ObjectOutputStream.class);
                MockedConstruction<ObjectInputStream> mockedIn = mockConstruction(ObjectInputStream.class,
                        (mock, context) -> when(mock.readObject()).thenReturn(mockResponse))
        ) {
            UserClientController controller = UserClientController.getInstance();
            BasicResponse response = controller.login("S2023001", "wrongpw");

            assertNotNull(response);
            assertEquals("401", response.code);
            assertEquals("비밀번호 오류", response.data);
        }
    }

    @DisplayName("회원가입 성공 시 응답 코드 200 확인")
    @Test
    void signup_success_response_200() throws Exception {
        BasicResponse mockResponse = new BasicResponse("200", "회원가입 성공");

        try (
                MockedConstruction<Socket> mockedSocket = mockConstruction(Socket.class);
                MockedConstruction<ObjectOutputStream> mockedOut = mockConstruction(ObjectOutputStream.class);
                MockedConstruction<ObjectInputStream> mockedIn = mockConstruction(ObjectInputStream.class,
                        (mock, context) -> when(mock.readObject()).thenReturn(mockResponse))
        ) {
            UserClientController controller = UserClientController.getInstance();
            BasicResponse response = controller.signup("S2023001", "pw", "홍길동", "컴공");

            assertNotNull(response);
            assertEquals("200", response.code);
        }
    }

    @DisplayName("로그아웃 성공 시 응답 코드 200 확인")
    @Test
    void logout_success_response_200() throws Exception {
        BasicResponse mockResponse = new BasicResponse("200", "로그아웃 성공");

        try (
                MockedConstruction<Socket> mockedSocket = mockConstruction(Socket.class);
                MockedConstruction<ObjectOutputStream> mockedOut = mockConstruction(ObjectOutputStream.class);
                MockedConstruction<ObjectInputStream> mockedIn = mockConstruction(ObjectInputStream.class,
                        (mock, context) -> when(mock.readObject()).thenReturn(mockResponse))
        ) {
            UserClientController controller = UserClientController.getInstance();
            BasicResponse response = controller.logout("S2023001", "pw");

            assertNotNull(response);
            assertEquals("200", response.code);
        }
    }

    @DisplayName("현재 접속자 수 정상 반환 여부 확인")
    @Test
    void currentUserCounts_success() throws Exception {
        CurrentResponse mockResponse = new CurrentResponse(5);

        try (
                MockedConstruction<Socket> mockedSocket = mockConstruction(Socket.class);
                MockedConstruction<ObjectOutputStream> mockedOut = mockConstruction(ObjectOutputStream.class);
                MockedConstruction<ObjectInputStream> mockedIn = mockConstruction(ObjectInputStream.class,
                        (mock, context) -> when(mock.readObject()).thenReturn(mockResponse))
        ) {
            UserClientController controller = UserClientController.getInstance();
            CurrentResponse response = controller.currentUserCounts();

            assertNotNull(response);
            assertEquals(5, response.currentUserCount);
        }
    }

    @DisplayName("서버 예외 발생 시 -1 반환 여부 확인")
    @Test
    void currentUserCounts_serverError_returnsMinusOne() {
        try (
                MockedConstruction<Socket> mockedSocket = mockConstruction(Socket.class,
                        (mock, context) -> { throw new RuntimeException("서버 다운"); })
        ) {
            UserClientController controller = UserClientController.getInstance();
            CurrentResponse response = controller.currentUserCounts();

            assertNotNull(response);
            assertEquals(-1, response.currentUserCount);
        }
    }
}
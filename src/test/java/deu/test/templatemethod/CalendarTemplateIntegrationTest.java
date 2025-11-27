/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.test.templatemethod;

import deu.controller.business.LectureClientController;
import deu.controller.business.RoomReservationClientController;
import deu.controller.event.AbstractCalendarViewTemplate;
import deu.model.dto.request.command.LectureCommandRequest;
import deu.model.dto.request.command.ReservationCommandRequest;
import deu.model.dto.response.BasicResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author oixkite
 */

/**
 * [통합 테스트] 템플릿 메소드 패턴 + 네트워크 통신
 * 목적: 실제 컨트롤러를 연결했을 때도 템플릿 메소드가 서버에서 데이터를 받아와 처리하는지 검증
 */
public class CalendarTemplateIntegrationTest {
    private static final int PORT = 9999;
    private Thread mockServerThread;
    private ServerSocket serverSocket; // [추가]

    static class IntegrationCalendarView extends AbstractCalendarViewTemplate {
        Object loadedLectureData;
        Object loadedReservationData;

        public IntegrationCalendarView() {
            super(null, "공학관", "3층", "301호",
                  LectureClientController.getInstance(),
                  RoomReservationClientController.getInstance());
        }

        @Override
        protected Object fetchLectureData() {
            return lectureClient.returnLectureOfWeek("공학관", "3층", "301호");
        }

        @Override
        protected Object fetchReservationData() {
            return reservationClient.weekRoomReservationByLectureroom("공학관", "3층", "301호");
        }

        @Override
        protected void applyScheduleToCalendar(Object l, Object r, Object label) {
            this.loadedLectureData = l;
            this.loadedReservationData = r;
        }

        @Override
        protected Object[] getResult() {
             return new Object[]{loadedLectureData, loadedReservationData};
        }
        
        public void runTask() throws Exception {
            Object[] result = doInBackground(); 
            applyScheduleToCalendar(result[0], result[1], null);
        }
    }

    @BeforeEach
    void startMockServer() {
        // 안전 장치: 시작 전 정리
        stopMockServer();

        mockServerThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT); // 변수 할당
                while (!serverSocket.isClosed()) {
                    try {
                        Socket client = serverSocket.accept();
                        handleClient(client);
                    } catch (Exception e) {
                        // 소켓 닫힘 예외 무시
                    }
                }
            } catch (IOException e) {
                System.out.println("[CalendarTest] 서버 시작 실패: " + e.getMessage());
            }
        });
        mockServerThread.setDaemon(true);
        mockServerThread.start();
        
        try { Thread.sleep(500); } catch (Exception e) {}
    }

    private void handleClient(Socket client) {
        try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {

            Object req = in.readObject();

            if (req instanceof LectureCommandRequest) {
                out.writeObject(new BasicResponse("200", Collections.emptyList()));
            } else if (req instanceof ReservationCommandRequest) {
                out.writeObject(new BasicResponse("200", new Object[7][13])); 
            } else {
                out.writeObject(new BasicResponse("400", "Unknown"));
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void stopMockServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("[CalendarTest] 서버 소켓 닫음");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("[통합] 템플릿 메소드 흐름 내에서 실제 컨트롤러 통신 검증")
    void testIntegrationWithMockServer() throws Exception {
        IntegrationCalendarView view = new IntegrationCalendarView();
        view.runTask();

        assertNotNull(view.loadedLectureData, "강의 데이터 수신 실패");
        assertNotNull(view.loadedReservationData, "예약 데이터 수신 실패");
        
        assertTrue(view.loadedLectureData instanceof BasicResponse, "반환값은 BasicResponse여야 합니다.");
        BasicResponse response = (BasicResponse) view.loadedLectureData;
        assertTrue(response.data instanceof List, "BasicResponse 데이터는 List여야 합니다.");
    }
}
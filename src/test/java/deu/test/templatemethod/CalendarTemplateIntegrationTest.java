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
    private boolean isRunning = true;

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

        // 테스트 편의를 위해 doInBackground 실행 후 apply 호출까지 직접 수행
        public void runTask() throws Exception {
            Object[] result = doInBackground(); 
            applyScheduleToCalendar(result[0], result[1], null);
        }
        
        // AbstractCalendarViewTemplate의 getResult 오버라이드 (형식 맞춤)
        @Override
        protected Object[] getResult() {
             return new Object[]{loadedLectureData, loadedReservationData};
        }
    }

    @BeforeEach
    void startMockServer() {
        mockServerThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (isRunning) {
                    try (Socket client = serverSocket.accept();
                         ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                         ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {

                        Object req = in.readObject();

                        if (req instanceof LectureCommandRequest) {
                            // 강의 요청 시: BasicResponse 안에 빈 리스트를 담아 보냄
                            out.writeObject(new BasicResponse("200", Collections.emptyList()));
                        } else if (req instanceof ReservationCommandRequest) {
                            // 예약 요청 시: BasicResponse 안에 배열을 담아 보냄
                            out.writeObject(new BasicResponse("200", new Object[7][13])); 
                        } else {
                            out.writeObject(new BasicResponse("400", "Unknown"));
                        }
                        out.flush();
                    } catch (Exception e) {
                        // 소켓 연결 종료 시 예외 무시
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        mockServerThread.setDaemon(true);
        mockServerThread.start();
    }

    @AfterEach
    void stopMockServer() {
        isRunning = false;
        // 데몬 스레드이므로 JVM 종료 시 자동 정리됨
    }

    @Test
    @DisplayName("[통합] 템플릿 메소드 흐름 내에서 실제 컨트롤러 통신 검증")
    void testIntegrationWithMockServer() throws Exception {
        // Given
        IntegrationCalendarView view = new IntegrationCalendarView();
        
        // When
        view.runTask();

        // Then
        // 1. 통신 성공 여부 확인 (null이 아니어야 함)
        assertNotNull(view.loadedLectureData, "강의 데이터 수신 실패");
        assertNotNull(view.loadedReservationData, "예약 데이터 수신 실패");
        
        // 2. 데이터 타입 검증 (수정된 부분)
        // Controller는 BasicResponse 객체를 반환하므로, 이를 먼저 확인해야 함
        assertTrue(view.loadedLectureData instanceof BasicResponse, 
                "컨트롤러 반환값은 BasicResponse 타입이어야 합니다.");
        
        BasicResponse response = (BasicResponse) view.loadedLectureData;
        
        // BasicResponse 내부의 data 필드가 List인지 확인
        assertTrue(response.data instanceof List, 
                "BasicResponse 내부 데이터는 List 타입이어야 합니다.");
    }
}

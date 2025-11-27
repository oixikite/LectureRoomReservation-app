package deu.controller.business;

import deu.config.Config;
import deu.config.ConfigLoader;
import deu.model.dto.request.command.LectureCommandRequest;
import deu.model.dto.request.data.lecture.LectureRequest;
import deu.model.dto.request.data.lecture.LectureDateRequest; // [Import]
import deu.model.dto.response.BasicResponse;
import lombok.Getter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;

public class LectureClientController {

    // 설정파일 불러오기
    Config config = ConfigLoader.getConfig();
    String host = config.server.host;
    int port = config.server.port;

    // Singleton 인스턴스
    @Getter
    private static final LectureClientController instance = new LectureClientController();

    private LectureClientController() {
    }

    // [기존] 주간 강의 조회
    public BasicResponse returnLectureOfWeek(String building, String floor, String lectureroom) {
        return sendRequest("주간 강의 조회", new LectureRequest(building, floor, lectureroom));
    }

    // [신규] 월간 강의 조회
    public BasicResponse returnLectureOfMonth(String building, String floor, String lectureroom, LocalDate targetDate) {
        return sendRequest("월간 강의 조회", new LectureDateRequest(building, floor, lectureroom, targetDate));
    }

    // [신규] 일간 강의 조회 (나중에 사용)
    public BasicResponse returnLectureOfDay(String building, String floor, String lectureroom, LocalDate targetDate) {
        return sendRequest("일간 강의 조회", new LectureDateRequest(building, floor, lectureroom, targetDate));
    }

    // 통신 중복 코드 제거 메소드 (수정됨)
    private BasicResponse sendRequest(String command, Object payload) {
        try (Socket socket = new Socket(host, port); 
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); 
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            LectureCommandRequest req = new LectureCommandRequest(command, payload);
            out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                return r;
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패 [" + command + "]: " + e.getMessage());
            // [수정] null 대신 500 에러 객체 반환
            return new BasicResponse("500", "서버 연결 실패: " + e.getMessage());
        }
        return new BasicResponse("500", "알 수 없는 서버 응답입니다.");
    }
}

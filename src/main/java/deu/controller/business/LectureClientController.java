package deu.controller.business;

import deu.config.Config;
import deu.config.ConfigLoader;
import deu.model.dto.request.command.LectureCommandRequest;
import deu.model.dto.request.data.lecture.LectureRequest;
import deu.model.dto.response.BasicResponse;
import lombok.Getter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LectureClientController {

    // 설정파일 불러오기
    Config config = ConfigLoader.getConfig();
    String host = config.server.host;
    int port = config.server.port;

    // Singleton 인스턴스
    @Getter
    private static final LectureClientController instance = new LectureClientController();

    private LectureClientController() {}

    // 주간 강의 정보 요청 컨트롤러
    public BasicResponse returnLectureOfWeek(String building, String floor, String lectureroom) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            LectureRequest lectureRequest = new LectureRequest(building, floor, lectureroom);
            LectureCommandRequest req = new LectureCommandRequest("주간 강의 조회", lectureRequest);
            out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                // System.out.println("서버 응답: " + r.data);
                return r;
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }
}

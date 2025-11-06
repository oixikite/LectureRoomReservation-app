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


// 개발 중단
public class BuildingClientController {

    // 설정파일 불러오기
    Config config = ConfigLoader.getConfig();
    String host = config.server.host;
    int port = config.server.port;

    // Singleton 인스턴스
    @Getter
    private static final BuildingClientController instance = new BuildingClientController();

    private BuildingClientController() {}
}

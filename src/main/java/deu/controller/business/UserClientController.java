package deu.controller.business;

import deu.config.Config;
import deu.config.ConfigLoader;
import deu.model.dto.request.data.user.*;
import deu.model.dto.request.command.UserCommandRequest;
import deu.model.dto.response.BasicResponse;
import deu.model.dto.response.CurrentResponse;
import lombok.Getter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class UserClientController {

    // 설정파일 불러오기
    Config config = ConfigLoader.getConfig();
    String host = config.server.host;
    int port = config.server.port;

    // Singleton 인스턴스
    @Getter
    private static final UserClientController instance = new UserClientController();

    private UserClientController() {}

    // 로그인 요청 컨트롤러
    public BasicResponse login(String number, String pw) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            LoginRequest loginRequest = new LoginRequest(number, pw);
            UserCommandRequest req = new UserCommandRequest("로그인", loginRequest);
            out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                System.out.println("서버 응답: " + r.data);
                if ((r.code).equals("200")) {
                    return r; // 로그인 성공
                }
                else if ((r.code).equals("401")) {
                    return r; // 비밀번호 오류
                }
                else{
                    return r; // 존재하지 않는 사용자(아이디 오류)
                }
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }

    // 회원가입 요청 컨트롤러
    public BasicResponse signup(String number, String pw, String name, String major) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            SignupRequest signupRequest = new SignupRequest(number, pw, name, major);
            UserCommandRequest req = new UserCommandRequest("회원가입", signupRequest);
            out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                System.out.println("서버 응답: " + r.data);
                if (r.code.equals("200")) {
                    return r; // 회원가입 성공
                }else{
                    return r; // 회원가입 실패
                }
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }

    // 로그아웃 요청 컨트롤러
    public BasicResponse logout(String number, String pw) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            LogoutRequest logoutRequest = new LogoutRequest(number, pw);
            UserCommandRequest req = new UserCommandRequest("로그아웃", logoutRequest);
            out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                System.out.println("서버 응답: " + r.data);
                if ((r.code).equals("200")) {
                    return r; // 로그아웃 성공
                } else{
                    return r; // 로그아웃 실패
                }
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }

    // 동시 접속자 수 요청 컨트롤러
    public CurrentResponse currentUserCounts() {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            UserCommandRequest req = new UserCommandRequest("동시접속자", null);
            out.writeObject(req);
            out.flush();
            socket.shutdownOutput();

            Object res = in.readObject();
            if (res instanceof CurrentResponse r) {
                // System.out.println("접속자 수: " + r.currentUserCount + "명");
                return r;
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return new CurrentResponse(-1);
    }

    // 사용자 이름 요청 컨트롤러
    public BasicResponse findUserName(String number, String pw) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            FindUserNameRequest findUserNameRequest = new FindUserNameRequest(number, pw);
            UserCommandRequest req = new UserCommandRequest("사용자 이름 반환", findUserNameRequest);
            out.writeObject(req);

            Object res = in.readObject();
            if (res instanceof BasicResponse r) {
                return r;
            }
        } catch (Exception e) {
            System.out.println("서버 통신 실패: " + e.getMessage());
        }
        return null;
    }
}

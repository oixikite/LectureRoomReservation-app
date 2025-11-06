package deu.controller.event;

import deu.config.Config;
import deu.config.ConfigLoader;
import deu.controller.business.UserClientController;
import deu.model.dto.response.BasicResponse;
import deu.view.Auth;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;

public class AuthSwingController {
    private final Auth view;
    private final UserClientController userController;

    public AuthSwingController(Auth view) {
        this.view = view;
        this.userController = UserClientController.getInstance();

        // 이벤트 연결
        view.addLoginListener(this::handleLogin);
        view.addSignupListener(this::handleSignup);
        view.addSwitchToSignupListener(this::switchToSignup);
        view.addSwitchToLoginListener(this::switchToLogin);
        view.addGithubServerButtonListener(this::handleGithubServer);
        view.addGithubClientButtonListener(this::handleGithubClient);
        view.addSaveConfigButtonListener(this::handlSetHostPort);
        view.addMyConfigPanelInitListener(createConfigPanelInitListener());
    }

    // 로그인 버튼 기능
    private void handleLogin(ActionEvent e) {
        String id = view.getLoginId();
        String pw = view.getLoginPassword();

        try {
            BasicResponse res = userController.login(id, pw);

            if (res != null && res.code.equals("200")) {
                view.transitionToHome(id, pw);
            } else if (res != null && res.code.equals("401")) {
                view.showError("로그인 실패: " + res.data); // 예: 비밀번호 오류
            } else {
                view.showError("통신 오류\n" + (res != null ? res.data : "Host IP와 연결되지 않습니다\n올바른 Host IP와 Port를 작성해주세요."));
            }

        } catch (Exception ex) {
            view.showError("서버와의 통신 중 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.");
            ex.printStackTrace(); // 디버깅 로그 (개발자 콘솔)
        }
    }

    // 회원 가입 버튼 기능
    private void handleSignup(ActionEvent e) {
        String id = view.getSignupId();
        String pw = view.getSignupPassword();
        String name = view.getSignupName();
        String major = view.getSignupMajor();
        BasicResponse res = userController.signup(id, pw, name, major);

        if (res != null && res.code.equals("200")) {
            view.showSuccess("회원가입 성공!");
            view.switchToLoginPanel();
        } else {
            view.showError("회원가입 실패: " + (res != null ? res.data : "서버 오류"));
        }
    }

    // 깃허브 이동 버튼 기능
    private void handleGithubServer(ActionEvent e){
        openWebpage("https://github.com/oxxultus/lectureroom-reservation-server");
    }
    private void handleGithubClient(ActionEvent e){
        openWebpage("https://github.com/oxxultus/lectureroom-reservation-app");
    }

    // 아이피 포트 설정
    private void handlSetHostPort(ActionEvent e) {
        String host = view.getHostField().getText().trim();
        String portText = view.getPortField().getText().trim();

        // 기본값 처리
        if (host.isEmpty()) {
            host = "localhost";
        }

        int port;
        try {
            port = portText.isEmpty() ? 9999 : Integer.parseInt(portText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "포트는 숫자여야 합니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 설정 파일 수정 및 저장
        String finalHost = host;
        int finalPort = port;
        ConfigLoader.updateConfig(c -> {
            c.server.host = finalHost;
            c.server.port = finalPort;
        });

        JOptionPane.showMessageDialog(view, "설정이 저장되었습니다.\nIP: " + host + "\nPort: " + port,
                "설정 완료", JOptionPane.INFORMATION_MESSAGE);


        // 올바른 저장 확인을 위한 파일에서 읽어오기
        Config config = ConfigLoader.getConfig();
        host = config.server.host;
        port = config.server.port;
        view.getHostField().setText(host);
        view.getPortField().setText(String.valueOf(port));
    }

    // 수정 안해도 되는 부분 ===========================================================================================

    // 회원 가입 패널 전환 - 수정 금지
    private void switchToSignup(ActionEvent e) {
        view.switchToSignupPanel();
    }

    // 로그인 패널 전환 - 수정 금지
    private void switchToLogin(ActionEvent e) {
        view.switchToLoginPanel();
    }

    // 브라우저 여는 메서드 - 수정금지
    private void openWebpage(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "브라우저를 열 수 없습니다: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 설정 값 자동으로 불러오는 메서드 - 수정 금지
    private AncestorListener createConfigPanelInitListener() {
        return new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                //  configPanel이 화면에 나타날 때 실행할 작업
                Config config = ConfigLoader.getConfig();
                view.getHostField().setText(config.server.host);
                view.getPortField().setText(String.valueOf(config.server.port));
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        };
    }
}
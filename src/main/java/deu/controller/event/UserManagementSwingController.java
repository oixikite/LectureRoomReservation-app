package deu.controller.event;

import deu.controller.business.UserManagementClientController;
import deu.model.dto.response.BasicResponse;
import deu.model.entity.User;
import deu.view.UserManagement;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserManagementSwingController {
    private final UserManagement view;
    private final UserManagementClientController userManagementController;

    public UserManagementSwingController(UserManagement view) {
        this.view = view;
        this.userManagementController = UserManagementClientController.getInstance();

        // 이벤트 연결
        this.view.updateButtonListener(this::updateUserdata);
        this.view.deleteButtonListener(this::deleteUserdata);
        this.view.addTableInitListener(createTableInitListener());
        addTableSelectionListener();
    }

    // JTable에 사용자 목록을 불러오는 메서드
    private void refreshUserTable() {
        // 서버에서 사용자 목록 요청
        BasicResponse response = userManagementController.findAllUsers();

        // 응답 유효성 검사
        if (response == null || !"200".equals(response.code)) {
            JOptionPane.showMessageDialog(null, "사용자 목록 불러오기 실패");
            return;
        }

        // 사용자 목록 캐스팅
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) response.data;

        // 테이블 모델 초기화
        DefaultTableModel model = (DefaultTableModel) view.getUserTable().getModel();
        model.setRowCount(0); // 기존 행 모두 제거

        // 사용자 데이터 테이블에 추가
        for (User user : users) {
            model.addRow(new String[] {
                    user.number,
                    user.name,
                    user.password,
                    user.major
            });
        }
    }

    // 변경 사항 저장 버튼 기능
    private void updateUserdata(ActionEvent e) {
        // 1. 입력값 읽기
        String userName = view.getEditProfileNameField().getText().trim();
        String userNumber = view.getEditProfileNumberField().getText().trim();
        String userPassword = view.getEditProfilePasswordField().getText().trim();
        String userMajor = view.getEditProfileMajorField().getText().trim();

        // 2. 입력값 유효성 검사
        if (userName.isEmpty() || userNumber.isEmpty() || userPassword.isEmpty() || userMajor.isEmpty()) {
            JOptionPane.showMessageDialog(null, "모든 항목을 입력해야 합니다.");
            return;
        }

        try {
            // 3. 서버에 사용자 정보 수정 요청
            BasicResponse response = userManagementController.updateUser(userNumber, userPassword, userName, userMajor);

            // 4. 응답 결과 처리
            if (response == null) {
                JOptionPane.showMessageDialog(null, "서버 응답이 없습니다.");
            } else if ("200".equals(response.code)) {
                JOptionPane.showMessageDialog(null, "사용자 정보가 성공적으로 수정되었습니다.");
            } else {
                JOptionPane.showMessageDialog(null, "수정 실패: " + response.data);
            }

            // 5. 사용자 목록 테이블 갱신
            refreshUserTable();

            // 6. 입력 필드 초기화
            clearProfileField();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "예기치 못한 오류 발생: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // 삭제 버튼 기능
    private void deleteUserdata(ActionEvent e) {
        // 1. 입력값 읽기
        String userNumber = view.getEditProfileNumberField().getText().trim();

        // 2. 유효성 검사
        if (userNumber.isEmpty()) {
            JOptionPane.showMessageDialog(null, "삭제할 사용자의 학번을 입력해주세요.");
            return;
        }

        // 3. 사용자 확인 (선택)
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "정말로 해당 사용자를 삭제하시겠습니까?",
                "삭제 확인",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // 4. 서버에 삭제 요청
            BasicResponse response = userManagementController.deleteUser(userNumber);

            // 5. 응답 처리
            if (response == null) {
                JOptionPane.showMessageDialog(null, "서버 응답이 없습니다.");
            } else if ("200".equals(response.code)) {
                JOptionPane.showMessageDialog(null, "사용자 정보가 성공적으로 삭제되었습니다.");
            } else {
                JOptionPane.showMessageDialog(null, "삭제 실패: " + response.data);
            }

            // 6. 테이블 갱신 및 필드 초기화
            refreshUserTable();
            clearProfileField();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "예기치 못한 오류 발생: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // 수정 안해도 되는 부분 ===========================================================================================

    // 테이블 항목 선택 시 텍스트 필드에 채워 넣는 이벤트 - 수정 금지
    private void addTableSelectionListener() {
        view.getUserTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = view.getUserTable().getSelectedRow();
                if (selectedRow != -1) {
                    view.getProfileNumberField().setText(view.getUserTable().getValueAt(selectedRow, 0).toString());
                    view.getProfileNameField().setText(view.getUserTable().getValueAt(selectedRow, 2).toString());
                    view.getProfileMajorField().setText(view.getUserTable().getValueAt(selectedRow, 3).toString());

                    view.getEditProfileNumberField().setText(view.getUserTable().getValueAt(selectedRow, 0).toString());
                    view.getEditProfileNameField().setText(view.getUserTable().getValueAt(selectedRow, 1).toString());
                    view.getEditProfilePasswordField().setText(view.getUserTable().getValueAt(selectedRow, 2).toString());
                    view.getEditProfileMajorField().setText(view.getUserTable().getValueAt(selectedRow, 3).toString());
                }
            }
        });
    }

    // 테이블이 생성 될 때 갱신되는 기능 - 수정 금지
    private AncestorListener createTableInitListener() {
        return new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                refreshUserTable();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        };
    }

    // 불러온 사용자 데이터 필드 비우기 - 수정 금지
    private void clearProfileField(){
        view.getProfileNameField().setText("");
        view.getProfileNumberField().setText("");
        view.getProfileMajorField().setText("");

        view.getEditProfileNameField().setText("");
        view.getEditProfileNumberField().setText("");
        view.getEditProfilePasswordField().setText("");
        view.getEditProfileMajorField().setText("");
    }
}

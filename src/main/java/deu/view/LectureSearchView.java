package deu.view;

import deu.model.dto.request.command.LectureCommandRequest;
import deu.model.dto.request.data.lecture.LectureFilterRequest;
import deu.model.dto.response.BasicResponse; // BasicResponse 임포트
import deu.model.dto.response.LectureListResponse;
import deu.model.entity.Lecture;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList; // ArrayList 임포트
import java.util.List;

public class LectureSearchView extends JFrame {

    private JTextField yearField, semesterField, buildingField, floorField, roomField;
    private JTable lectureTable;
    private DefaultTableModel tableModel;

    // CUD 버튼 필드
    private JButton searchButton, addButton, editButton, deleteButton;

    // 서버 정보
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8080;
    
    //[신규] 조회된 강의 목록 원본을 저장할 리스트
    private List<Lecture> currentLectureList = new ArrayList<>();

    public LectureSearchView() {
        setTitle("강의실 강의 조회 및 관리");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        //상단 입력 패널
        JPanel inputPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        inputPanel.add(new JLabel("연도"));
        inputPanel.add(new JLabel("학기"));
        inputPanel.add(new JLabel("건물"));
        inputPanel.add(new JLabel("층"));
        inputPanel.add(new JLabel("호실"));

        yearField = new JTextField("2025");
        semesterField = new JTextField("FIRST");
        buildingField = new JTextField("정보관");
        floorField = new JTextField("9");
        roomField = new JTextField("911");

        inputPanel.add(yearField);
        inputPanel.add(semesterField);
        inputPanel.add(buildingField);
        inputPanel.add(floorField);
        inputPanel.add(roomField);

        add(inputPanel, BorderLayout.NORTH);

        //테이블 구성
        String[] columns = {"강의코드", "강의명", "요일", "시작시간", "종료시간"};
        tableModel = new DefaultTableModel(columns, 0);
        lectureTable = new JTable(tableModel);
        add(new JScrollPane(lectureTable), BorderLayout.CENTER);

        //하단 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchButton = new JButton("강의실 조회");
        addButton = new JButton("강의 추가");
        editButton = new JButton("강의 수정");
        deleteButton = new JButton("강의 삭제");

        buttonPanel.add(searchButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        //버튼 이벤트
        searchButton.addActionListener(this::handleSearchAction);
        addButton.addActionListener(this::handleAddAction);
        editButton.addActionListener(this::handleEditAction);
        deleteButton.addActionListener(this::handleDeleteAction);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ===============================================================
    //서버 요청 전송 헬퍼 메서드
    // ===============================================================
    private Object sendRequestToServer(Object request) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(HOST, PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("[CLIENT] 요청 전송 → " + request.getClass().getSimpleName());
            out.writeObject(request);
            out.flush();

            return in.readObject(); // 서버로부터 응답 객체 반환
        }
    }

    // ===============================================================
    //[수정] 조회 버튼 클릭 이벤트 (currentLectureList에 저장)
    // ===============================================================
    private void handleSearchAction(ActionEvent e) {
        try {
            //입력값 가져오기
            int year = Integer.parseInt(yearField.getText().trim());
            String semester = semesterField.getText().trim();
            String building = buildingField.getText().trim();
            String floor = floorField.getText().trim();
            String room = roomField.getText().trim();

            //요청 DTO 생성
            LectureFilterRequest filter = new LectureFilterRequest(year, semester, building, floor, room);
            LectureCommandRequest request = new LectureCommandRequest("강의실 강의 조회", filter);

            Object response = sendRequestToServer(request); // 헬퍼 메서드 호출

            if (response instanceof LectureListResponse res) {
                if ("200".equals(res.getStatus())) {
                    // 🎯 [신규] 조회 결과를 멤버 변수에 저장
                    this.currentLectureList = res.getLectures(); 
                    
                    updateTable(this.currentLectureList); // 🎯 저장된 리스트로 테이블 업데이트
                    JOptionPane.showMessageDialog(this, "조회 완료! 총 " + this.currentLectureList.size() + "개 강의가 있습니다.");
                } else {
                    this.currentLectureList.clear(); // 🎯 실패 시 리스트 비우기
                    updateTable(this.currentLectureList); // 🎯 빈 테이블로 업데이트
                    JOptionPane.showMessageDialog(this, "조회 실패: " + res.getMessage());
                }
            } else {
                 JOptionPane.showMessageDialog(this, "예상치 못한 응답 형식입니다.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "입력 형식이 올바르지 않습니다. (연도는 숫자)");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버 통신 오류: " + ex.getMessage());
        }
    }

    // ===============================================================
    //[완성] 추가 버튼 클릭 이벤트
    // ===============================================================
    private void handleAddAction(ActionEvent e) {
        try {
            // 1. 현재 조회 중인 강의실 정보를 가져옵니다. (팝업창에 넘겨주기 위해)
            int year = Integer.parseInt(yearField.getText().trim());
            String semester = semesterField.getText().trim();
            String building = buildingField.getText().trim();
            String floor = floorField.getText().trim();
            String room = roomField.getText().trim();

            // 2. '강의 추가' 팝업창(JDialog)을 생성하고 엽니다.
            LectureEditDialog dialog = new LectureEditDialog(this, year, semester, building, floor, room);
            dialog.setVisible(true); // 사용자가 '저장' 또는 '취소'를 누를 때까지 여기서 멈춤

            // 3. 팝업창이 '저장' 버튼을 눌러 닫혔는지 확인합니다.
            if (dialog.isSaved()) {
                // 4. 팝업창에서 완성된 Lecture 객체를 가져옵니다.
                Lecture newLecture = dialog.getLecture();

                // 5. 서버에 전송할 DTO를 생성합니다.
                LectureCommandRequest request = new LectureCommandRequest("강의 추가", newLecture);

                // 6. 서버에 전송하고 응답을 받습니다.
                Object response = sendRequestToServer(request);

                if (response instanceof deu.model.dto.response.BasicResponse res) {
                    if ("200".equals(res.code)) {
                        JOptionPane.showMessageDialog(this, (String) res.data); // "강의 추가 성공"
                        // 7. [중요] 테이블 새로고침
                        searchButton.doClick();
                    } else {
                        // (예: 409 - 중복 ID, 409 - 시간 겹침 등 서버가 보낸 메시지)
                        JOptionPane.showMessageDialog(this, "추가 실패: " + (String) res.data, "오류", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "예상치 못한 응답 형식입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
            // '취소'를 누른 경우 (dialog.isSaved() == false)는 아무것도 하지 않습니다.

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "상단의 '연도' 필드를 숫자로 올바르게 입력하세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버 통신 오류: " + ex.getMessage(), "서버 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===============================================================
    //[완성] 수정 버튼 클릭 이벤트
    // ===============================================================
    private void handleEditAction(ActionEvent e) {
        // 1. 테이블에서 선택된 행 확인
        int selectedRow = lectureTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "수정할 강의를 테이블에서 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 2. [중요] 테이블의 '강의 코드'로 원본 리스트(currentLectureList)에서 실제 Lecture 객체 찾기
            String selectedLectureId = (String) tableModel.getValueAt(selectedRow, 0);
            
            Lecture lectureToEdit = null;
            for (Lecture lec : currentLectureList) {
                if (lec.getId().equals(selectedLectureId)) {
                    lectureToEdit = lec;
                    break;
                }
            }

            if (lectureToEdit == null) {
                JOptionPane.showMessageDialog(this, "선택한 강의 정보를 찾을 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 3. '수정' 모드로 팝업창(JDialog) 열기 (기존 객체를 넘겨줌)
            LectureEditDialog dialog = new LectureEditDialog(this, lectureToEdit);
            dialog.setVisible(true);

            // 4. '저장' 버튼을 눌렀다면
            if (dialog.isSaved()) {
                // 5. 수정된 Lecture 객체를 가져옴
                Lecture updatedLecture = dialog.getLecture();

                // 6. 서버에 "강의 수정" 요청 전송
                LectureCommandRequest request = new LectureCommandRequest("강의 수정", updatedLecture);
                Object response = sendRequestToServer(request);

                if (response instanceof BasicResponse res) {
                    if ("200".equals(res.code)) {
                        JOptionPane.showMessageDialog(this, (String) res.data); // "강의 수정 성공"
                        // 7. 테이블 새로고침
                        searchButton.doClick();
                    } else {
                        // (예: 409 - 시간 겹침 등)
                        JOptionPane.showMessageDialog(this, "수정 실패: " + (String) res.data, "오류", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "예상치 못한 응답 형식입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
            // '취소' 시 아무것도 안 함

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "수정 처리 중 오류 발생: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }


    // ===============================================================
    //[완성] 삭제 버튼 클릭 이벤트 (BasicResponse.code 사용)
    // ===============================================================
    private void handleDeleteAction(ActionEvent e) {
        // 1. JTable에서 선택된 행을 확인합니다.
        int selectedRow = lectureTable.getSelectedRow();

        // 2. 선택이 안 된 경우
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 강의를 테이블에서 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. 선택된 행의 '강의 코드'(ID)를 가져옵니다. (0번째 열)
        String lectureId = (String) tableModel.getValueAt(selectedRow, 0);
        String lectureTitle = (String) tableModel.getValueAt(selectedRow, 1);

        // 4. 사용자에게 삭제 의사를 재확인합니다.
        int result = JOptionPane.showConfirmDialog(this,
                "[" + lectureId + "] " + lectureTitle + "\n강의를 정말 삭제하시겠습니까?",
                "삭제 확인",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        // 5. 사용자가 '예'를 누른 경우
        if (result == JOptionPane.YES_OPTION) {
            try {
                // 6. "강의 삭제" 커맨드와 'lectureId'를 담아 DTO 생성
                LectureCommandRequest request = new LectureCommandRequest("강의 삭제", lectureId);

                // 7. 서버에 전송 및 응답 받기
                Object response = sendRequestToServer(request);

                if (response instanceof deu.model.dto.response.BasicResponse res) {
                    if ("200".equals(res.code)) {
                        JOptionPane.showMessageDialog(this, (String) res.data); // "강의가 성공적으로 삭제되었습니다."
                        // 8. [중요] 테이블 새로고침 (조회 버튼 다시 누르기)
                        searchButton.doClick();
                    } else {
                        // (예: 404 - 삭제할 강의 못찾음, 500 - 서버 오류)
                        JOptionPane.showMessageDialog(this, "삭제 실패: " + (String) res.data, "오류", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "예상치 못한 응답 형식입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "서버 통신 오류: " + ex.getMessage(), "서버 오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===============================================================
    //테이블 갱신
    // ===============================================================
    private void updateTable(List<Lecture> lectures) {
        tableModel.setRowCount(0); // 기존 데이터 초기화
        for (Lecture lec : lectures) {
            tableModel.addRow(new Object[]{
                    lec.getId(),
                    lec.getTitle(),
                    lec.getDay(),
                    lec.getStartTime(),
                    lec.getEndTime()
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LectureSearchView::new);
    }
}
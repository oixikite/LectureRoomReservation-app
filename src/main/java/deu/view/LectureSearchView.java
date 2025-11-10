package deu.view;

import deu.model.dto.request.command.LectureCommandRequest;
import deu.model.dto.request.data.lecture.LectureFilterRequest;
import deu.model.dto.response.LectureListResponse;
import deu.model.entity.Lecture;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class LectureSearchView extends JFrame {

    private JTextField yearField, semesterField, buildingField, floorField, roomField;
    private JTable lectureTable;
    private DefaultTableModel tableModel;
    private JButton searchButton;

    public LectureSearchView() {
        setTitle("강의실 강의 조회");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 🔹 상단 입력 패널
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

        // 🔹 테이블 구성
        String[] columns = {"강의코드", "강의명", "요일", "시작시간", "종료시간"};
        tableModel = new DefaultTableModel(columns, 0);
        lectureTable = new JTable(tableModel);
        add(new JScrollPane(lectureTable), BorderLayout.CENTER);

        // 🔹 조회 버튼
        searchButton = new JButton("강의실 조회");
        add(searchButton, BorderLayout.SOUTH);

        // 🔹 버튼 이벤트
        searchButton.addActionListener(this::handleSearchAction);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ===============================================================
    // ✅ 버튼 클릭 이벤트 처리
    // ===============================================================
    private void handleSearchAction(ActionEvent e) {
        try {
            String host = "127.0.0.1";
            int port = 8080;

            // 입력값 가져오기
            int year = Integer.parseInt(yearField.getText().trim());
            String semester = semesterField.getText().trim();
            String building = buildingField.getText().trim();
            String floor = floorField.getText().trim();
            String room = roomField.getText().trim();

            // 요청 DTO 생성
            LectureFilterRequest filter = new LectureFilterRequest(year, semester, building, floor, room);
            LectureCommandRequest request = new LectureCommandRequest("강의실 강의 조회", filter);

            // 서버 통신
            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                System.out.println("[CLIENT] 요청 전송 → " + building + " " + floor + "층 " + room + "호");
                out.writeObject(request);
                out.flush();

                Object response = in.readObject();

                if (response instanceof LectureListResponse res) {
                    if ("200".equals(res.getStatus())) {
                        updateTable(res.getLectures());
                    } else {
                        JOptionPane.showMessageDialog(this, "조회 실패: " + res.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "예상치 못한 응답 형식입니다.");
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버 통신 오류: " + ex.getMessage());
        }
    }

    // ===============================================================
    // ✅ 테이블 갱신
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
        JOptionPane.showMessageDialog(this, "조회 완료! 총 " + lectures.size() + "개 강의가 있습니다.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LectureSearchView::new);
    }
}

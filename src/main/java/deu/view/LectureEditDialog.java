package deu.view;

import deu.model.entity.Lecture;
import deu.model.enums.Semester;

import javax.swing.*;
import java.awt.*;

// '강의 추가' 및 '강의 수정'을 위한 팝업 다이얼로그
public class LectureEditDialog extends JDialog {

    // 폼 필드
    private JTextField idField, titleField, professorField, dayField, startTimeField, endTimeField;

    // 부모 뷰(LectureSearchView)에서 전달받을 정보 ('추가' 모드용)
    private int year;
    private Semester semester;
    private String building, floor, room;

    // '추가' 또는 '수정'할 Lecture 객체
    private Lecture lecture = null;
    private boolean isSaved = false;

    /**
     * 1. 강의 '추가' 시 호출되는 생성자
     */
    public LectureEditDialog(JFrame parent, int year, String semester, String building, String floor, String room) {
        super(parent, "새 강의 추가", true); // 'true'는 modal 설정
        this.year = year;
        try {
            this.semester = Semester.valueOf(semester.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.semester = Semester.FIRST; // 기본값
            JOptionPane.showMessageDialog(parent, "학기 정보가 올바르지 않아 FIRST로 설정합니다.", "경고", JOptionPane.WARNING_MESSAGE);
        }
        this.building = building;
        this.floor = floor;
        this.room = room;

        this.lecture = new Lecture(); // '추가' 모드는 새 객체 생성

        setupUI();
        setTitle("새 강의 추가 (" + building + " " + room + "호)");
    }

    /**
     * 2. 강의 '수정' 시 호출되는 생성자
     */
    public LectureEditDialog(JFrame parent, Lecture existingLecture) {
        super(parent, "강의 수정", true); // 'true'는 modal 설정
        
        this.lecture = existingLecture; // '수정' 모드는 기존 객체 받기

        setupUI();
        setTitle("강의 수정 (" + lecture.getId() + ")");
        
        // 폼에 기존 데이터 채우기
        fillFormWithData();
    }

    // UI 구성
    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 350);
        setLocationRelativeTo(getParent()); // 부모 창 중앙에 띄우기

        // 폼 패널 (GridLayout)
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("강의 코드:"));
        idField = new JTextField();
        formPanel.add(idField);

        formPanel.add(new JLabel("강의명:"));
        titleField = new JTextField();
        formPanel.add(titleField);
        
        formPanel.add(new JLabel("교수명:"));
        professorField = new JTextField();
        formPanel.add(professorField);

        formPanel.add(new JLabel("요일:"));
        dayField = new JTextField("월"); // 예시 기본값
        formPanel.add(dayField);

        formPanel.add(new JLabel("시작 시간 (HH:mm):"));
        startTimeField = new JTextField("09:00"); // 예시 기본값
        formPanel.add(startTimeField);

        formPanel.add(new JLabel("종료 시간 (HH:mm):"));
        endTimeField = new JTextField("11:00"); // 예시 기본값
        formPanel.add(endTimeField);

        add(formPanel, BorderLayout.CENTER);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("취소");
        JButton saveButton = new JButton("저장");
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // '저장' 버튼 액션
        saveButton.addActionListener(e -> onSave());

        // '취소' 버튼 액션
        cancelButton.addActionListener(e -> onCancel());
    }

    /**
     * '수정' 모드일 때 폼을 기존 데이터로 채우는 메서드
     */
    private void fillFormWithData() {
        idField.setText(lecture.getId());
        // '수정' 모드에서는 강의 코드를 변경할 수 없도록 잠급니다. (ID는 Key값이므로)
        idField.setEditable(false); 
        
        titleField.setText(lecture.getTitle());
        professorField.setText(lecture.getProfessor());
        dayField.setText(lecture.getDay());
        startTimeField.setText(lecture.getStartTime());
        endTimeField.setText(lecture.getEndTime());
        
        // (참고: year, semester, building 등은 수정 불가능 항목으로 가정)
    }


    // '저장' 버튼 클릭 시
    private void onSave() {
        // (간단한 입력값 검증 예시)
        if (idField.getText().trim().isEmpty() || titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "강의 코드와 강의명은 필수입니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // '추가'/'수정' 모두 this.lecture 객체에 값을 덮어씁니다.
        
        // 1. 폼에서 입력받은 값 설정
        lecture.setId(idField.getText().trim()); // (수정 모드에선 어차피 비활성화되어 값이 같음)
        lecture.setTitle(titleField.getText().trim());
        lecture.setProfessor(professorField.getText().trim());
        lecture.setDay(dayField.getText().trim());
        lecture.setStartTime(startTimeField.getText().trim());
        lecture.setEndTime(endTimeField.getText().trim());

        // 2. '추가' 모드일 때만 부모 뷰(조회 조건)에서 상속받은 값 설정
        // (수정 모드일 때는 기존 lecture 객체에 이미 이 값들이 들어있음)
        if (lecture.getYear() == null || lecture.getYear() == 0) {
            lecture.setYear(this.year);
            lecture.setSemester(this.semester);
            lecture.setBuilding(this.building);
            lecture.setFloor(this.floor);
            lecture.setLectureroom(this.room);
        }

        this.isSaved = true;
        dispose(); // 다이얼로그 닫기
    }

    // '취소' 버튼 클릭 시
    private void onCancel() {
        this.isSaved = false;
        dispose(); // 다이얼로그 닫기
    }

    // LectureSearchView에서 이 메서드들을 호출하여 결과를 받음
    public boolean isSaved() {
        return isSaved;
    }

    public Lecture getLecture() {
        return lecture;
    }
}
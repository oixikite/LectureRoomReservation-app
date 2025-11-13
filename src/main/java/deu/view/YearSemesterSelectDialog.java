package deu.view;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author scq37
 */

//학년도와 학기를 선택받는JDialog
public class YearSemesterSelectDialog extends JDialog {

    private JTextField yearField;
    private JComboBox<String> semesterComboBox;
    private boolean isConfirmed = false;

    public YearSemesterSelectDialog(JFrame parent) {
        super(parent, "관리할 학기 선택", true); // Modal
        setLayout(new BorderLayout(10, 10));
        setSize(300, 150);
        setLocationRelativeTo(parent);

        //폼 패널
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        formPanel.add(new JLabel("학년도 (YYYY):"));
        yearField = new JTextField("2025"); // 기본값
        formPanel.add(yearField);

        formPanel.add(new JLabel("학기:"));
        semesterComboBox = new JComboBox<>(new String[]{"FIRST", "SECOND", "SUMMER", "WINTER"});
        formPanel.add(semesterComboBox);
        
        add(formPanel, BorderLayout.CENTER);

        // -----------------------------------------------------------
        // 버튼 패널
        // -----------------------------------------------------------
        JButton okButton = new JButton("확인");
        JButton cancelButton = new JButton("취소"); // 1. "취소" 버튼 생성

        okButton.addActionListener(e -> onConfirm());
        cancelButton.addActionListener(e -> onCancel()); // 2. "취소" 버튼 이벤트 연결
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton); // 3. "취소" 버튼 패널에 추가
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    //"확인" 버튼 로직
    private void onConfirm() {
        // 간단한 유효성 검사 (연도가 4자리 숫자인지)
        if (yearField.getText().matches("\\d{4}")) {
            this.isConfirmed = true;
            dispose(); // 창 닫기
        } else {
            JOptionPane.showMessageDialog(this, "학년도를 4자리 숫자로 입력하세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    //"취소" 버튼 로직
    private void onCancel() {
        this.isConfirmed = false;
        dispose(); // 창 닫기
    }

    //컨트롤러가 이 값들을 가져갈 수 있도록 getter 제공
    public boolean isConfirmed() {
        return isConfirmed;
    }

    public String getSelectedYear() {
        return yearField.getText();
    }

    public String getSelectedSemester() {
        return (String) semesterComboBox.getSelectedItem();
    }
}
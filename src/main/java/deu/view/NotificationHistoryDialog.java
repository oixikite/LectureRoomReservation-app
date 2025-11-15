/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.view;

/**
 *
 * @author scq37
 */
import deu.model.dto.response.NotificationDTO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class NotificationHistoryDialog extends JDialog {

    private JTable table;
    private DefaultTableModel model;

    public NotificationHistoryDialog(JFrame parent, List<NotificationDTO> notifications) {
        super(parent, "알림 보관함", true); // true = 모달(창 끄기 전까지 뒤에꺼 클릭 불가)
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // 테이블 모델 설정 (제목, 내용, 시간)
        String[] columnNames = {"시간", "제목", "내용"};
        model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 수정 불가능하게
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        
        // 컬럼 너비 조절
        table.getColumnModel().getColumn(0).setPreferredWidth(120); // 시간
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // 제목
        table.getColumnModel().getColumn(2).setPreferredWidth(350); // 내용

        // 데이터 채우기 (최신순으로 역순 정렬해서 넣기)
        if (notifications != null) {
            for (int i = notifications.size() - 1; i >= 0; i--) {
                NotificationDTO dto = notifications.get(i);
                model.addRow(new Object[]{
                    dto.getFormattedTimestamp(),
                    dto.getTitle(),
                    dto.getMessage()
                });
            }
        }

        // 스크롤판에 테이블 추가
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 닫기 버튼
        JButton closeButton = new JButton("닫기");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
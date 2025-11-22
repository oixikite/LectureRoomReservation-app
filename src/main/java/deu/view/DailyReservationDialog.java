/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.view;

import deu.controller.event.CalendarViewContainer;
import deu.view.custom.TimeSlotButton;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author oixikite
 */
/**
 * 일별 강의 현황을 보여주는 팝업 창
 */
public class DailyReservationDialog extends JDialog implements CalendarViewContainer {

    private JPanel calendarPanel; // 1열 x 13행 (9시~21시)
    private JLabel dateLabel;     // 상단 날짜 표시

    public DailyReservationDialog(JFrame parent, String title) {
        super(parent, title, true); // 모달 창
        setSize(400, 700);
        setLayout(new BorderLayout());

        // 1. 상단 헤더 (날짜 표시)
        JPanel header = new JPanel();
        header.setBackground(new Color(20, 90, 170)); // 브랜드 컬러
        header.setPreferredSize(new Dimension(400, 50));

        dateLabel = new JLabel("YYYY-MM-DD");
        dateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        dateLabel.setForeground(Color.WHITE);
        header.add(dateLabel);
        add(header, BorderLayout.NORTH);

        // 2. 중앙 타임라인 (1열 x 13행)
        calendarPanel = new JPanel(new GridLayout(13, 1, 0, 5)); // 수직 나열, 간격 5px
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        calendarPanel.setBackground(Color.WHITE);

        // 9시~21시 버튼 생성
        for (int i = 0; i < 13; i++) {
            TimeSlotButton btn = new TimeSlotButton();
            String timeLabel = String.format("%02d:00 ~ %02d:00", 9 + i, 10 + i);

            btn.setText(timeLabel);
            btn.setHorizontalAlignment(SwingConstants.LEFT); // 텍스트 좌측 정렬
            btn.setBackground(Color.WHITE);
            btn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

            calendarPanel.add(btn);
        }

        add(new JScrollPane(calendarPanel), BorderLayout.CENTER);
    }

    // [Interface 구현]
    @Override
    public JPanel getCalendarPanel() {
        return calendarPanel;
    }

    @Override
    public void setDateHeader(String text) {
        dateLabel.setText(text);
    }
}

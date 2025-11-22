/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.view;

import deu.controller.event.CalendarViewContainer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oixikite
 */
public class MonthlyReservationDialog extends JDialog implements CalendarViewContainer {

    private JPanel calendarPanel;
    private JLabel monthTitleLabel;

    public MonthlyReservationDialog(JFrame parent) {
        super(parent, "월별 강의 현황", true); // 모달 창
        setSize(1000, 750);
        setLayout(new BorderLayout());

        // 1. 상단 헤더 (년/월)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(20, 90, 170));
        headerPanel.setPreferredSize(new Dimension(1000, 60));

        monthTitleLabel = new JLabel("YYYY.MM");
        monthTitleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        monthTitleLabel.setForeground(Color.WHITE);
        headerPanel.add(monthTitleLabel);

        add(headerPanel, BorderLayout.NORTH);

        // 2. 중앙 컨텐츠 (요일 + 달력)
        JPanel centerPanel = new JPanel(new BorderLayout());

        // 2-1. 요일 헤더
        JPanel dayOfWeekPanel = new JPanel(new GridLayout(1, 7));
        dayOfWeekPanel.setPreferredSize(new Dimension(1000, 30));
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};

        for (String d : days) {
            JLabel label = new JLabel(d, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(new Color(235, 235, 235));
            label.setFont(new Font("맑은 고딕", Font.BOLD, 14));

            if ("일".equals(d)) {
                label.setForeground(Color.RED);
            } else if ("토".equals(d)) {
                label.setForeground(Color.BLUE);
            }

            dayOfWeekPanel.add(label);
        }
        centerPanel.add(dayOfWeekPanel, BorderLayout.NORTH);

        // 2-2. 달력 그리드 (6주 x 7일 = 42칸)
        calendarPanel = new JPanel(new GridLayout(6, 7, 1, 1));
        calendarPanel.setBackground(Color.LIGHT_GRAY); // 격자선 효과

        for (int i = 0; i < 42; i++) {
            JButton btn = new JButton();
            btn.setBackground(Color.WHITE);
            btn.setVerticalAlignment(SwingConstants.TOP);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setMargin(new Insets(5, 5, 5, 5));
            btn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            btn.setFocusPainted(false);

            calendarPanel.add(btn);
        }
        centerPanel.add(calendarPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    public JPanel getCalendarPanel() {
        return calendarPanel;
    }

    @Override
    public void setDateHeader(String text) {
        monthTitleLabel.setText(text);
    }
}

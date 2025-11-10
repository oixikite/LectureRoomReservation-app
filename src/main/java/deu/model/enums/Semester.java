/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.model.enums;

/**
 *
 * @author scq37
 */

public enum Semester {
    FIRST,   // 1학기
    SECOND;  // 2학기// 2학기

    public static Semester fromInt(int n) {
        return (n == 2) ? SECOND : FIRST;
    }

    public int toInt() {
        return (this == SECOND) ? 2 : 1;
    }
}

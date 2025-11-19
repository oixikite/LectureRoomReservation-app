/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.model.dto.request.data.reservation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 *
 * @author oixikite
 */
@Getter
@Setter
@NoArgsConstructor
public class AccompanyingStudent implements Serializable {

    private String studentId; // 학번
    private String name;      // 성명

    public AccompanyingStudent(String studentId, String name) {
        this.studentId = studentId;
        this.name = name;
    }
}

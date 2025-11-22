/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.model.dto.request.data.lecture;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author oixikite
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LectureDateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String building;
    private String floor;
    private String lectureroom;
    private LocalDate targetDate; // 월별 조회 시 해당 월의 1일 (예: 2025-11-01)
}

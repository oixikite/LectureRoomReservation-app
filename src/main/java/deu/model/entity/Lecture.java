package deu.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 *
 * @author oixikite
 * @modifier oxxultus
 * @since 2025.05.16
 */

@Getter
@Setter
@NoArgsConstructor
public class Lecture implements Serializable {
    private String id;
    private String title;
    private String lectureroom;
    private String building;
    private String floor;
    private String professor;
    private String day;  // 요일

    private String startTime;
    private String endTime;
    
    private Integer year; //연도
    private deu.model.enums.Semester semester; //FISRT(1학기), SECOND(2학기)
}

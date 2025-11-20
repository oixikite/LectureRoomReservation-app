package deu.model.dto.request.data.reservation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RoomReservationRequest implements Serializable {
    
    private String id;
    private String buildingName;
    private String floor;
    private String lectureRoom;
    private String title;
    private String description;
    private String date;
    private String dayOfTheWeek;
    private String startTime;
    private String endTime;
    private String number;
    private String status = "대기"; // 기본값 지정

    // [필수] 서버와 동일하게 필드 추가
    private String purpose; // 사용 목적
    private int accompanyingStudentCount; // 동반 학생 수
    private List<AccompanyingStudent> accompanyingStudents; // 동반 학생 목록

    public RoomReservationRequest(String buildingName, String floor, String lectureRoom,
            String title, String description, String date, String dayOfTheWeek,
            String startTime, String endTime, String number,
            String purpose, int accompanyingStudentCount, List<AccompanyingStudent> accompanyingStudents) {
        this.buildingName = buildingName;
        this.floor = floor;
        this.lectureRoom = lectureRoom;
        this.title = title;
        this.description = description;
        this.date = date;
        this.dayOfTheWeek = dayOfTheWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.number = number;
        this.status = "대기";

        this.purpose = purpose;
        this.accompanyingStudentCount = accompanyingStudentCount;
        this.accompanyingStudents = accompanyingStudents;
    }
    
    public void setId(String id) {
        this.id = id;
    }
}

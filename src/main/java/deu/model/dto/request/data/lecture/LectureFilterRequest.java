package deu.model.dto.request.data.lecture;

import deu.model.enums.Semester;
import java.io.Serializable;

/**
 * 연도/학기 + 건물/층/강의실 조건으로 강의 목록을 조회하기 위한 요청 DTO
 * 클라이언트→서버 전송용
 */
public class LectureFilterRequest implements Serializable {
    public int year;                 // 예: 2025
    public Semester semester;        // 예: Semester.FIRST
    public String building;          // 예: "정보관"
    public String floor;             // 예: "9"
    public String lectureroom;       // 예: "911"

    // 선택 필드(옵션): 요일 필터가 필요하면 사용 (MONDAY 등)
    public String day;               // null 가능

    public LectureFilterRequest() {} // 직렬화용 기본 생성자

    public LectureFilterRequest(int year, Semester semester,
                                String building, String floor, String lectureroom) {
        this.year = year;
        this.semester = semester;
        this.building = building;
        this.floor = floor;
        this.lectureroom = lectureroom;
    }
}

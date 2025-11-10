package deu.model.dto.request.data.lecture;

import java.io.Serializable;

/**
 * 강의 목록 필터 요청 DTO
 * 예: 2025, 1학기, 정보관, 9층, 911호
 */
public class LectureFilterRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer year;
    private String semester;     // "FIRST" / "SECOND"
    private String building;
    private String floor;
    private String lectureroom;

    public LectureFilterRequest() {}

    public LectureFilterRequest(Integer year, String semester, String building, String floor, String lectureroom) {
        this.year = year;
        this.semester = semester;
        this.building = building;
        this.floor = floor;
        this.lectureroom = lectureroom;
    }

    // --- Getter / Setter ---
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getLectureroom() { return lectureroom; }
    public void setLectureroom(String lectureroom) { this.lectureroom = lectureroom; }
}

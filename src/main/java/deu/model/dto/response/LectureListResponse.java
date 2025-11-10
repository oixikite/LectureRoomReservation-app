package deu.model.dto.response;

import deu.model.entity.Lecture;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 서버 → 클라이언트: 강의 목록 응답 DTO
 */
public class LectureListResponse implements Serializable {

    private String status;          // "200", "400" 등 상태 코드
    private String message;         // 결과 메시지
    private List<Lecture> lectures; // 강의 리스트
    private Integer totalCount;     // 전체 개수

    // 요청 Echo (필요 시)
    private Integer year;
    private String semester;
    private String building;
    private String floor;
    private String lectureroom;

    public LectureListResponse() {
        this.lectures = new ArrayList<>();
    }

    // ✅ 성공 응답 생성기
    public static LectureListResponse ok(List<Lecture> list) {
        LectureListResponse res = new LectureListResponse();
        res.status = "200";
        res.message = "success";
        res.lectures = (list != null) ? list : new ArrayList<>();
        res.totalCount = res.lectures.size();
        return res;
    }

    // ❌ 실패 응답 생성기
    public static LectureListResponse error(String code, String msg) {
        LectureListResponse res = new LectureListResponse();
        res.status = code;
        res.message = msg;
        res.lectures = new ArrayList<>();
        res.totalCount = 0;
        return res;
    }

    // --- Getter / Setter ---
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<Lecture> getLectures() { return lectures; }
    public void setLectures(List<Lecture> lectures) { this.lectures = lectures; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

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

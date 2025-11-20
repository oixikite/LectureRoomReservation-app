//package deu.model.entity;
//
//import deu.model.dto.request.data.reservation.AccompanyingStudent;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.io.Serializable;
//import java.util.UUID;
//import java.util.List;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor // Builder를 위해 필요
//@Builder(toBuilder = true) // 이 옵션이 있어야 서비스의 .toBuilder()가 작동
//public class RoomReservation implements Serializable {
//    private String id = UUID.randomUUID().toString(); // 예약 생성 시 UUID 자동 할당
//    
//    private String buildingName;
//    private String floor;
//    private String lectureRoom;
//
//    private String number;
//    @Builder.Default
//    private String status = "대기";
//
//    private String title; // 제목
//    private String description; // 설명
//
//    private String date; // 날짜
//    private String dayOfTheWeek; // 요일
//
//    private String startTime;
//    private String endTime;
//}

package deu.model.entity;

// DTO의 AccompanyingStudent를 임시 참조 (별도 엔티티 매핑 권장)
import deu.model.dto.request.data.reservation.AccompanyingStudent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter // [필수] YAML 라이브러리가 값을 설정하기 위해 필요
@NoArgsConstructor // [필수] YAML 라이브러리가 객체를 생성하기 위해 필요
@AllArgsConstructor // Builder를 위해 필요
@Builder(toBuilder = true) // 이 옵션이 있어야 서비스의 .toBuilder()가 작동
public class RoomReservation implements Serializable {

    @Builder.Default
    private String id = UUID.randomUUID().toString(); // UUID 자동 할당

    // YAML 처리를 위해 'final' 키워드 제거 (가변 객체로 변경)
    private String buildingName;
    private String floor;
    private String lectureRoom;
    private String number; // 예약자 학번

    @Builder.Default
    private String status = "대기"; // 기본값 지정

    private String title;
    private String description;
    private String date;
    private String dayOfTheWeek;
    private String startTime;
    private String endTime;

    // --- DTO와 맞추기 위한 [추가 필드] ---
    private String purpose;
    private int accompanyingStudentCount;
    private List<AccompanyingStudent> accompanyingStudents;
}

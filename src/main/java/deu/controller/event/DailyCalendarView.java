/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.controller.event;

import deu.model.dto.response.BasicResponse;
import deu.model.entity.Lecture;
import deu.model.entity.RoomReservation;
import deu.view.Reservation;

/**
 *
 * @author oixikite
 */

/**
 * [구상 클래스] '일별' 캘린더 뷰를 구현합니다.
 * '주별' 데이터를 가져온 뒤, 오늘(첫 번째 날)의 데이터만 추출하여 템플릿에 반환합니다.
 */


public class DailyCalendarView extends AbstractCalendarViewTemplate {

    public DailyCalendarView(Reservation view, String building, String floor, String room) {
        // 부모 템플릿에 view와 위치 정보 전달
        super(view, building, floor, room);
    }

    /**
     * [Abstract 구현] '일별' 강의 데이터를 조회합니다.
     */
    @Override
    protected Object fetchLectureData() {
        // 1. 서버에서는 '주별' 데이터를 그대로 가져옵니다.
        BasicResponse res = lectureClient.returnLectureOfWeek(building, floor, room);
        
        // 2. '일별' 표시에 사용할 7x13 빈 배열을 새로 생성합니다.
        Lecture[][] dailyData = new Lecture[7][13]; 
        
        if (res != null && "200".equals(res.code) && res.data instanceof Lecture[][]) {
            Lecture[][] weeklyData = (Lecture[][]) res.data;
            
            // 3. '주별' 데이터의 0번째(오늘) 데이터만 '일별' 배열의 0번째로 복사합니다.
            if (weeklyData[0] != null) {
                dailyData[0] = weeklyData[0]; 
            }
        }
        // 4. 오늘 하루치 데이터만 담긴 배열을 반환합니다.
        return dailyData;
    }

    /**
     * [Abstract 구현] '일별' 예약 데이터를 조회합니다.
     */
    @Override
    protected Object fetchReservationData() {
        // 1. 서버에서는 '주별' 데이터를 그대로 가져옵니다.
        BasicResponse res = reservationClient.weekRoomReservationByLectureroom(building, floor, room);
        
        // 2. '일별' 표시에 사용할 7x13 빈 배열을 새로 생성합니다.
        RoomReservation[][] dailyData = new RoomReservation[7][13];
        
        if (res != null && "200".equals(res.code) && res.data instanceof RoomReservation[][]) {
            RoomReservation[][] weeklyData = (RoomReservation[][]) res.data;
            
            // 3. '주별' 데이터의 0번째(오늘) 데이터만 '일별' 배열의 0번째로 복사합니다.
            if (weeklyData[0] != null) {
                dailyData[0] = weeklyData[0];
            }
        }
        // 4. 오늘 하루치 데이터만 담긴 배열을 반환합니다.
        return dailyData;
    }
}
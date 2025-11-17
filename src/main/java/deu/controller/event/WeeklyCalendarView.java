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
 * [구상 클래스] '주별' 캘린더 뷰를 구현합니다.
 */
public class WeeklyCalendarView extends AbstractCalendarViewTemplate {

    public WeeklyCalendarView(Reservation view, String building, String floor, String room) {
        super(view, building, floor, room);
    }

    /**
     * [Abstract 구현] '주별' 강의 데이터를 조회합니다.
     */
    @Override
    protected Object fetchLectureData() {
        BasicResponse res = lectureClient.returnLectureOfWeek(building, floor, room);
        if (res != null && "200".equals(res.code) && res.data instanceof Lecture[][]) {
            return res.data;
        }
        return new Lecture[7][13]; // 실패 시 빈 배열
    }

    /**
     * [Abstract 구현] '주별' 예약 데이터를 조회합니다.
     */
    @Override
    protected Object fetchReservationData() {
        BasicResponse res = reservationClient.weekRoomReservationByLectureroom(building, floor, room);
        if (res != null && "200".equals(res.code) && res.data instanceof RoomReservation[][]) {
            return res.data;
        }
        return new RoomReservation[7][13]; // 실패 시 빈 배열
    }
}
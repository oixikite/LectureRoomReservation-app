package deu.model.dto.request.data.reservation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class DeleteRoomReservationRequest implements Serializable {

    private String number;            // 사용자 ID (학번/교번)
    private String roomReservationId; // 예약 ID
    private String reason;            // [추가] 취소 사유

    // 생성자 업데이트
    public DeleteRoomReservationRequest(String number, String roomReservationId, String reason) {
        this.number = number;
        this.roomReservationId = roomReservationId;
        this.reason = reason;
    }
}

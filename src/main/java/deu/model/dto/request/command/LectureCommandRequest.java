package deu.model.dto.request.command;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 강의 관련 요청 커맨드 객체
 * - command: 요청 유형 (예: "주간 강의 조회")
 * - payload: 전송 데이터 (예: LectureRequest, Lecture 등)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LectureCommandRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String command;
    private Object payload; // [통일] 필드명을 data -> payload로 변경

    // 수동 Getter/Setter (Lombok 미동작 대비)
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
}

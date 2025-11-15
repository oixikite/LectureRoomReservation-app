/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.model.dto.response;

/**
 *
 * @author scq37
 */
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 알림 정보를 담는 DTO (Data Transfer Object)
 * 서버에서 -> 클라이언트로 전송됩니다.
 */
public class NotificationDTO implements Serializable {

    private static final long serialVersionUID = 1L; 

    private final String title;     // 알림 제목 (예: 예약 승인)
    private final String message;   // 알림 내용 (예: [정보관 912] 예약이 승인되었습니다.)
    private final long timestamp;   // 생성 시간
    private boolean isRead;

    public NotificationDTO(String title, String message, long timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isRead(){
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }

    // (편의 기능) 시간 포맷팅
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(timestamp));
    }

    @Override
    public String toString() {
        return "NotificationDTO{title='" + title + "', message='" + message + "'}";
    }
}
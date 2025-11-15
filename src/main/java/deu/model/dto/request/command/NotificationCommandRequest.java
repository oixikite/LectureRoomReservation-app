/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.model.dto.request.command;

/**
 *
 * @author scq37
 */
import java.io.Serializable;

public class NotificationCommandRequest implements Serializable {
    
    public String command; // 예: "알림 조회"
    public Object payload; // 예: 사용자 ID (String)

    public NotificationCommandRequest(String command, Object payload) {
        this.command = command;
        this.payload = payload;
    }
}

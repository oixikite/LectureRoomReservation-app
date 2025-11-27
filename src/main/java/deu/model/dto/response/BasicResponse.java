package deu.model.dto.response;

import java.io.Serializable;

public class BasicResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public String code;
    public Object data;

    public BasicResponse(String code, Object data) {
        this.code = code;
        this.data = data;
    }

   // [수정] 올바른 Getter 구현 추가
    public String getCode() {
        return this.code;
    }

    public Object getData() {
        return this.data;
    }
}

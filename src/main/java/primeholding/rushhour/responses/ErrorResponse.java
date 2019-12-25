package primeholding.rushhour.responses;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ErrorResponse implements Response {

    private int code;
    private String message;
    private String cause;

    public ErrorResponse() {
    }

    public ErrorResponse(int code, String message, String cause) {
        this.code = code;
        this.message = message;
        this.cause = cause;
    }
}

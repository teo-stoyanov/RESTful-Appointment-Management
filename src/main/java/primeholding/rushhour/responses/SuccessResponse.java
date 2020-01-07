package primeholding.rushhour.responses;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Data
@Component
public class SuccessResponse implements Response {
    private HttpStatus status;
    private String message;

    public SuccessResponse() {
    }

    public SuccessResponse(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}

package primeholding.rushhour.responses;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Data
public class ErrorResponse implements Response {
    private HttpStatus status;
    private String message;
    private List<String> errors;

    public ErrorResponse(HttpStatus status, String message, List<String> errors) {
        super();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public ErrorResponse(HttpStatus status, String message, String error) {
        super();
        this.status = status;
        this.message = message;
        errors = Arrays.asList(error);
    }
}

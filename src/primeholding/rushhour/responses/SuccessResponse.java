package primeholding.rushhour.responses;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class SuccessResponse implements Response {
    int code;
    String message;
}

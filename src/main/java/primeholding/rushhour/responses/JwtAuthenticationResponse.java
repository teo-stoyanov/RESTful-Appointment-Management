package primeholding.rushhour.responses;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class JwtAuthenticationResponse implements Response {
    private String accessToken;

    @Value("${app.token.type}")
    private String tokenType;

    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}

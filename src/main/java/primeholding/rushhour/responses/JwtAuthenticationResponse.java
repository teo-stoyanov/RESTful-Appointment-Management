package primeholding.rushhour.responses;

import lombok.Data;

@Data
public class JwtAuthenticationResponse implements Response {
    private String accessToken;
    private String tokenType = "Bearer";

    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}

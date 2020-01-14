package primeholding.rushhour.responses;

import lombok.Data;

@Data
public class JwtAuthenticationResponse implements Response {

    private String accessToken;

    private String tokenType;

    public JwtAuthenticationResponse(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }
}

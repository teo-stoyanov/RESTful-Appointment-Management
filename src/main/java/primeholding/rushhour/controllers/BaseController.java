package primeholding.rushhour.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import primeholding.rushhour.responses.Response;
import primeholding.rushhour.responses.SuccessResponse;

public abstract class BaseController {

    protected ResponseEntity<Response> successResponse() {
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setStatus(HttpStatus.OK);
        successResponse.setMessage("Successfully created!");
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }
}

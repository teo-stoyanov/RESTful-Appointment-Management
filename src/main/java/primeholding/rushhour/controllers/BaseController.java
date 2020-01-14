package primeholding.rushhour.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import primeholding.rushhour.responses.Response;
import primeholding.rushhour.responses.SuccessResponse;

import javax.validation.ConstraintViolationException;

public abstract class BaseController {

    protected ResponseEntity<Response> successResponse(String command) {
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setStatus(HttpStatus.OK);
        successResponse.setMessage("Successfully " + command);
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    protected void constraintViolationCheck(TransactionSystemException e) {
        Throwable t = e.getCause();
        while ((t != null) && !(t instanceof ConstraintViolationException)) {
            t = t.getCause();
        }
        if (t != null) {
            throw new ConstraintViolationException(t.getMessage(),((ConstraintViolationException) t).getConstraintViolations());
        }
    }
}

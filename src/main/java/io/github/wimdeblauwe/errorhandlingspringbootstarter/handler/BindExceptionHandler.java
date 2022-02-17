package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.*;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * {@link ApiExceptionHandler} for
 * {@link BindException}. This typically happens when there is validation
 * on request parameter objects.
 *
 * @see MethodArgumentNotValidApiExceptionHandler
 * @see ConstraintViolationApiExceptionHandler
 */
public class BindExceptionHandler extends AbstractApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BindExceptionHandler.class);

    public BindExceptionHandler(ErrorHandlingProperties properties,
                                HttpStatusMapper httpStatusMapper,
                                ErrorCodeMapper errorCodeMapper,
                                ErrorMessageMapper errorMessageMapper) {
        super(httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof BindException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {

        BindException ex = (BindException) exception;
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                                                         getErrorCode(exception),
                                                         getMessage(ex));
        ex.getGlobalErrors().stream()
          .map(objectError -> new ApiGlobalError(getCode(objectError),
                                                 getMessage(objectError)))
          .forEach(response::addGlobalError);

        ex.getFieldErrors().stream()
          .map(fieldError -> new ApiFieldError(getCode(fieldError),
                                               fieldError.getField(),
                                               getMessage(fieldError),
                                               fieldError.getRejectedValue()))
          .forEach(response::addFieldError);

        return response;
    }

    private Optional<Path.Node> getLeafNode(Path path) {
        return StreamSupport.stream(path.spliterator(), false).reduce((a, b) -> b);
    }

    private String getCode(ObjectError objectError) {
        String code = objectError.getCode();
        return errorCodeMapper.getErrorCode(code, code);
    }

    private String getCode(FieldError fieldError) {
        String code = fieldError.getCode();
        String fieldSpecificCode = fieldError.getField() + "." + code;
        return errorCodeMapper.getErrorCode(fieldSpecificCode, code);
    }

    private String getMessage(ObjectError objectError) {
        String code = objectError.getCode();
        return errorMessageMapper.getErrorMessage(code, code, objectError.getDefaultMessage());
    }

    private String getMessage(FieldError fieldError) {
        String code = fieldError.getCode();
        String fieldSpecificCode = fieldError.getField() + "." + code;
        return errorMessageMapper.getErrorMessage(fieldSpecificCode, code, fieldError.getDefaultMessage());
    }

    private String getMessage(BindException exception) {
        return "Validation failed. Error count: " + exception.getErrorCount();
    }
}

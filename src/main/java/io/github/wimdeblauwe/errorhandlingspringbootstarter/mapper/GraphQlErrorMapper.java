package io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper;

import graphql.ErrorClassification;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.GqlErrorType;
import org.springframework.http.HttpStatus;

public class GraphQlErrorMapper {

    private final ErrorHandlingProperties properties;

    public GraphQlErrorMapper(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    public ErrorClassification httpStatusToErrorClassification(HttpStatus httpStatus) {
        switch (httpStatus) {
            case BAD_REQUEST -> {
                return GqlErrorType.BAD_REQUEST;
            }
            case UNAUTHORIZED -> {
                return GqlErrorType.UNAUTHORIZED;
            }
            case FORBIDDEN -> {
                return GqlErrorType.FORBIDDEN;
            }
            case NOT_FOUND -> {
                return GqlErrorType.NOT_FOUND;
            }
            default -> {
                return GqlErrorType.INTERNAL_ERROR;
            }
        }
    }

    public HttpStatus errorClassificationToHttpStatus(ErrorClassification errorClassification) {
        switch ((GqlErrorType) errorClassification) {
            case BAD_REQUEST -> {
                return HttpStatus.BAD_REQUEST;
            }
            case UNAUTHORIZED -> {
                return HttpStatus.UNAUTHORIZED;
            }
            case FORBIDDEN -> {
                return HttpStatus.FORBIDDEN;
            }
            case NOT_FOUND -> {
                return HttpStatus.NOT_FOUND;
            }
            default -> {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
    }
}

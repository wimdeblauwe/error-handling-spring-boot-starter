package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import graphql.ErrorClassification;
public enum GqlErrorType implements ErrorClassification {
    BAD_REQUEST,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    INTERNAL_ERROR;

    GqlErrorType() {}
}

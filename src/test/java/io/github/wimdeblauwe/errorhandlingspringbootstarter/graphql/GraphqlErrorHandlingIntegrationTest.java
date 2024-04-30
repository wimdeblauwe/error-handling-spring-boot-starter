package io.github.wimdeblauwe.errorhandlingspringbootstarter.graphql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiFieldError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiGlobalError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.GqlErrorType;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet.ServletErrorHandlingConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {ServletErrorHandlingConfiguration.class})
@Import({PersonController.class})
@AutoConfigureHttpGraphQlTester
class GraphqlErrorHandlingIntegrationTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void findPersonByIdQueryResultOk() {
        String query = "{ findPersonById(id: 1) { id firstName lastName } }";
        Person person = graphQlTester.document(query)
                .execute()
                .path("data.findPersonById")
                .entity(Person.class)
                .get();
        Assertions.assertNotNull(person);
    }

    @Test
    void findPersonByIdQueryNotFoundException() {
        String query = "{ findPersonById(id: 9) { id firstName lastName } }";
        graphQlTester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getErrorType().toString().equals(GqlErrorType.NOT_FOUND.toString()))
                .expect(responseError -> responseError.getMessage().equalsIgnoreCase("Person was not found by id 9"))
                .expect(responseError -> responseError.getExtensions().get("code").equals("PERSON_NOT_FOUND"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void createPersonMutationAllFieldsConstraintViolationException() {
        String mutation = "mutation { createPersonMutation(personInput: { firstName: \"F\", lastName: \"D\", age: -1, children: -2 }) { id firstName lastName age children } }";
        graphQlTester.document(mutation)
                                     .execute()
                                     .errors()
                .expect(responseError -> responseError.getMessage().equalsIgnoreCase("Validation failed. Error count: 4"))
                .expect(responseError -> responseError.getErrorType().toString().equalsIgnoreCase("BAD_REQUEST"))
                .expect(responseError -> responseError.getExtensions().get("code").equals("VALIDATION_FAILED"))
                .expect(responseError -> {
                    // check the error details object
                    Map<String, Object> errorDetails = (Map<String, Object>) responseError.getExtensions().get("errorDetails");
                    String code = (String) errorDetails.get("code");
                    String message = (String) errorDetails.get("message");
                    List<?> fieldErrorList = (List<?>) errorDetails.get("fieldErrors");
                    // convert the LinkedHashMap from GraphQlClient response to POJOs
                    List<ApiFieldError> apiFieldErrorList = objectMapper.convertValue(fieldErrorList, new TypeReference<>(){});
                    // validate fields
                    boolean validatedFieldConstraints = apiFieldErrorList.get(0).getCode().equalsIgnoreCase("VALUE_LESS_THAN_MIN") &&
                            apiFieldErrorList.get(0).getMessage().equalsIgnoreCase("must be greater than or equal to 0") &&
                            apiFieldErrorList.get(0).getProperty().equalsIgnoreCase("age") &&
                            apiFieldErrorList.get(0).getRejectedValue().equals(-1) &&
                            apiFieldErrorList.get(0).getPath().equalsIgnoreCase("age") &&
                            apiFieldErrorList.get(1).getCode().equalsIgnoreCase("VALUE_LESS_THAN_MIN") &&
                            apiFieldErrorList.get(1).getMessage().equalsIgnoreCase("must be greater than or equal to 0") &&
                            apiFieldErrorList.get(1).getProperty().equalsIgnoreCase("children") &&
                            apiFieldErrorList.get(1).getRejectedValue().equals(-2) &&
                            apiFieldErrorList.get(1).getPath().equalsIgnoreCase("children") &&
                            apiFieldErrorList.get(2).getCode().equalsIgnoreCase("INVALID_SIZE") &&
                            apiFieldErrorList.get(2).getMessage().equalsIgnoreCase("size must be between 2 and 50") &&
                            apiFieldErrorList.get(2).getProperty().equalsIgnoreCase("firstName") &&
                            apiFieldErrorList.get(2).getRejectedValue().equals("F") &&
                            apiFieldErrorList.get(2).getPath().equalsIgnoreCase("firstName") &&
                            apiFieldErrorList.get(3).getCode().equalsIgnoreCase("INVALID_SIZE") &&
                            apiFieldErrorList.get(3).getMessage().equalsIgnoreCase("size must be between 2 and 50") &&
                            apiFieldErrorList.get(3).getProperty().equalsIgnoreCase("lastName") &&
                            apiFieldErrorList.get(3).getRejectedValue().equals("D") &&
                            apiFieldErrorList.get(3).getPath().equalsIgnoreCase("lastName");
                    return code.equalsIgnoreCase("VALIDATION_FAILED") && message.equalsIgnoreCase("Validation failed. Error count: 4") && validatedFieldConstraints;
                });
    }

    @SuppressWarnings("unchecked")
    @Test
    void createPersonMutationGlobalConstraintViolationException() {
        String mutation = "mutation { createPersonMutation(personInput: { firstName: \"Foo\", lastName: \"Dummy\", age: 16, children: 2 }) { id firstName lastName age children } }";
        graphQlTester.document(mutation)
                     .execute()
                     .errors().expect(responseError -> responseError.getMessage().equalsIgnoreCase("Validation failed. Error count: 1"))
                     .expect(responseError -> responseError.getErrorType().toString().equalsIgnoreCase("BAD_REQUEST"))
                     .expect(responseError -> responseError.getExtensions().get("code").equals("VALIDATION_FAILED"))
                     .expect(responseError -> {
                         Map<String, Object> errorDetails = (Map<String, Object>) responseError.getExtensions().get("errorDetails");
                         List<?> globalErrors = (List<?>) errorDetails.get("globalErrors");
                         // convert the LinkedHashMap from GraphQlClient response to POJOs
                         List<ApiGlobalError> apiGlobalErrorsList = objectMapper.convertValue(globalErrors, new TypeReference<>(){});
                         return apiGlobalErrorsList.get(0).getCode().equalsIgnoreCase("ValidPerson") &&
                                 apiGlobalErrorsList.get(0).getMessage().equalsIgnoreCase("Invalid person");
                     });
    }

    @SuppressWarnings("unchecked")
    @Test
    void createPersonMutationNestedFieldGlobalConstraintViolationException() {
        String mutation = "mutation { createPersonMutation(personInput: { firstName: \"F\", lastName: \"D\", age: -16, children: 2 }) { id firstName lastName age children } }";
        graphQlTester.document(mutation)
                     .execute()
                     .errors()
                     .expect(responseError -> responseError.getMessage().equalsIgnoreCase("Validation failed. Error count: 4"))
                     .expect(responseError -> responseError.getErrorType().toString().equalsIgnoreCase("BAD_REQUEST"))
                     .expect(responseError -> responseError.getExtensions().get("code").equals("VALIDATION_FAILED"))
                     .expect(responseError -> {
                         // check the error details object
                         Map<String, Object> errorDetails = (Map<String, Object>) responseError.getExtensions().get("errorDetails");
                         String code = (String) errorDetails.get("code");
                         String message = (String) errorDetails.get("message");
                         List<?> fieldErrorList = (List<?>) errorDetails.get("fieldErrors");
                         // convert the LinkedHashMap from GraphQlClient response to POJOs
                         List<ApiFieldError> apiFieldErrorList = objectMapper.convertValue(fieldErrorList, new TypeReference<>(){});
                         List<?> globalErrors = (List<?>) errorDetails.get("globalErrors");
                         // convert the LinkedHashMap from GraphQlClient response to POJOs
                         List<ApiGlobalError> apiGlobalErrorsList = objectMapper.convertValue(globalErrors, new TypeReference<>(){});
                         boolean validGlobal = apiGlobalErrorsList.get(0).getCode().equalsIgnoreCase("ValidPerson") &&
                                 apiGlobalErrorsList.get(0).getMessage().equalsIgnoreCase("Invalid person");
                         // validate fields
                         boolean validatedFieldConstraints = apiFieldErrorList.get(0).getCode().equalsIgnoreCase("VALUE_LESS_THAN_MIN") &&
                                 apiFieldErrorList.get(0).getMessage().equalsIgnoreCase("must be greater than or equal to 0") &&
                                 apiFieldErrorList.get(0).getProperty().equalsIgnoreCase("age") &&
                                 apiFieldErrorList.get(0).getRejectedValue().equals(-16) &&
                                 apiFieldErrorList.get(0).getPath().equalsIgnoreCase("age") &&
                                 apiFieldErrorList.get(1).getCode().equalsIgnoreCase("INVALID_SIZE") &&
                                 apiFieldErrorList.get(1).getMessage().equalsIgnoreCase("size must be between 2 and 50") &&
                                 apiFieldErrorList.get(1).getProperty().equalsIgnoreCase("firstName") &&
                                 apiFieldErrorList.get(1).getRejectedValue().equals("F") &&
                                 apiFieldErrorList.get(1).getPath().equalsIgnoreCase("firstName") &&
                                 apiFieldErrorList.get(2).getCode().equalsIgnoreCase("INVALID_SIZE") &&
                                 apiFieldErrorList.get(2).getMessage().equalsIgnoreCase("size must be between 2 and 50") &&
                                 apiFieldErrorList.get(2).getProperty().equalsIgnoreCase("lastName") &&
                                 apiFieldErrorList.get(2).getRejectedValue().equals("D") &&
                                 apiFieldErrorList.get(2).getPath().equalsIgnoreCase("lastName");
                         return code.equalsIgnoreCase("VALIDATION_FAILED") && message.equalsIgnoreCase("Validation failed. Error count: 4") && validatedFieldConstraints && validGlobal;
                     });
    }

    @Test
    void findPersonByIdQueryRuntimeException() {
        String query = "{ findPersonThrowRuntimeException { id firstName lastName } }";
        graphQlTester.document(query)
                     .execute()
                     .errors()
                     .expect(responseError -> responseError.getErrorType().toString().equals(GqlErrorType.INTERNAL_ERROR.toString()))
                     .expect(responseError -> responseError.getExtensions().get("code").equals("RUNTIME"));
    }

    /**
     * Unfortunately the invalid queries are treated in a default response mechanism with ValidationError error type. See <a href="https://github.com/graphql-java/graphql-java/discussions/2240">Github issue</a>.
     */
    @Test
    void findPersonByIdInvalidParamQueryValidationException() {
        String query = "{ findPersonById { id firstName lastName } }";
        graphQlTester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage().endsWith("Missing field argument 'id'"))
                .expect(responseError -> responseError.getErrorType().toString().equalsIgnoreCase("ValidationError"));
    }

    /**
     * Unfortunately the invalid queries are treated in a default response mechanism with ValidationError error type. See <a href="https://github.com/graphql-java/graphql-java/discussions/2240">Github issue</a>.
     */
    @Test
    void createPersonMutationInvalidParamModelFetchQueryValidationException() {
        String query = "mutation { createPersonMutation(personInput: { firstName: \"F\", lastNameA: \"D\", age: -1, children: -2 }) { id firstName lastName age children } }";
        graphQlTester.document(query)
                     .execute()
                .errors()
                .expect(responseError -> responseError.getMessage().endsWith("is missing required fields '[lastName]'"))
                .expect(responseError -> responseError.getErrorType().toString().equalsIgnoreCase("ValidationError"));
    }

    @Test
    void updatePersonMutationSqlException() {
        String mutation = "mutation { updatePersonMutationThrowSqlException(personInput: { firstName: \"Foo\", lastName: \"Dummy\", age: 10, children: 0 }) { id firstName lastName age children } }";
        graphQlTester.document(mutation)
                .execute()
                .errors()
                .expect(responseError -> responseError.getErrorType().toString().equalsIgnoreCase("INTERNAL_ERROR"))
                .expect(responseError -> responseError.getMessage().equalsIgnoreCase("Duplicate unique value"))
                .expect(responseError -> responseError.getExtensions().get("code").equals("SQL"));
    }
}

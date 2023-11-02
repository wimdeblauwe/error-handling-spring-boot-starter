package io.github.wimdeblauwe.errorhandlingspringbootstarter.graphql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.GraphQlTester;

@Import({PersonController.class, GraphqlErrorHandlingPersonTest.GraphqlPersonTestConfiguration.class})
@GraphQlTest(PersonController.class)
@AutoConfigureGraphQlTester
class GraphqlErrorHandlingPersonTest {

    /**
     * Test the autoconfiguration of Spring Graphql with a few example requests
     * This is more to check the autoconfiguration of the graphql schema and resolvers
     */
    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void findPersonByIdQueryResultOk() {
        String query = "{ findPersonById(id: 1) { id firstName lastName } }";
        Person person = graphQlTester.document(query)
                .execute()
                .path("data.findPersonById")
                .entity(Person.class)
                .get();
        Assertions.assertNotNull(person);
        Assertions.assertEquals(1, person.getId());
        Assertions.assertEquals("Joanna", person.getFirstName());
        Assertions.assertEquals("Hutchinson", person.getLastName());
        Assertions.assertNull(person.getAge());
        Assertions.assertNull(person.getChildren());
    }

    @Test
    void findPersonByIdQueryExceptionPersonNotFound() {
        String query = "{ findPersonById(id: 9) { id firstName lastName } }";
        graphQlTester.document(query)
                     .execute()
                     .errors()
                                  .expect(responseError -> responseError.getErrorType().equals(ErrorType.INTERNAL_ERROR));
    }

    @Test
    void createPersonMutationResultOk() {
        String mutation = "mutation { createPersonMutation(personInput: { firstName: \"Foo\", lastName: \"Dummy\", age: 10, children: 0 }) { id firstName lastName age children } }";
        Person person = graphQlTester.document(mutation)
                .execute()
                .path("data.createPersonMutation")
                .entity(Person.class)
                .get();
        Assertions.assertNotNull(person);
        Assertions.assertEquals(5,person.getId());
        Assertions.assertEquals("Foo", person.getFirstName());
        Assertions.assertEquals("Dummy", person.getLastName());
        Assertions.assertEquals(10, person.getAge());
        Assertions.assertEquals(0, person.getChildren());
    }

    @Test
    void updatePersonMutationResultOk() {
        String mutation = "mutation { updatePersonMutation(personInput: { id: 4, firstName: \"Foo\", lastName: \"Dummy\", age: 10, children: 0 }) { id firstName lastName age children } }";
        Person person = graphQlTester.document(mutation)
                                     .execute()
                                     .path("data.updatePersonMutation")
                                     .entity(Person.class)
                                     .get();
        Assertions.assertNotNull(person);
        Assertions.assertEquals(4, person.getId());
        Assertions.assertEquals("Foo", person.getFirstName());
        Assertions.assertEquals("Dummy", person.getLastName());
        Assertions.assertEquals(10, person.getAge());
        Assertions.assertEquals(0, person.getChildren());
    }

    @Configuration
    static class GraphqlPersonTestConfiguration {}
}

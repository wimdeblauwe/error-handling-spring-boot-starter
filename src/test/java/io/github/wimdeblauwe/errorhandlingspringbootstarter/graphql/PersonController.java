package io.github.wimdeblauwe.errorhandlingspringbootstarter.graphql;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.graphql.exception.PersonNotFoundException;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PersonController {

    public List<Person> personList = new ArrayList<>(){{
        add(new Person(1, "Joanna", "Hutchinson", 21, 0));
        add(new Person(2, "Travis", "Tyree", 35, 2));
        add(new Person(3, "Hannah", "Carrol", 28, 1));
        add(new Person(4, "Rylan", "Lang", 42, 3));
    }};

    @QueryMapping
    public Person findPersonById(@Nonnull @Argument Integer id) {
        return personList.stream().filter(person -> person.getId().equals(id))
                .findAny().orElseThrow(() -> new PersonNotFoundException("Person was not found by id %s".formatted(id)));
    }

    @QueryMapping
    public Person findPersonThrowRuntimeException() {
        throw new RuntimeException();
    }

    @QueryMapping
    public Person findPersonThrowAccessDeniedException() throws AccessDeniedException { throw new AccessDeniedException("GraphQL Access denied"); }

    @MutationMapping
    public Person createPersonMutation(@Argument @Valid PersonInput personInput) {
        return new Person(5, personInput.getFirstName(), personInput.getLastName(), personInput.getAge(), personInput.getChildren());
    }

    @MutationMapping
    public Person updatePersonMutation(@Argument @Valid PersonInput personInput) {
        return new Person(personInput.getId(), personInput.getFirstName(), personInput.getLastName(), personInput.getAge(), personInput.getChildren());
    }

    @MutationMapping
    public Person updatePersonMutationThrowSqlException(@Argument @Valid PersonInput personInput) throws SQLException {
        throw new SQLException("Duplicate unique value");
    }
}

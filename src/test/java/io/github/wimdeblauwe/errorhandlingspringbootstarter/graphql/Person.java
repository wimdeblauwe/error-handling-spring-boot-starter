package io.github.wimdeblauwe.errorhandlingspringbootstarter.graphql;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

public class Person {

    private Integer id;

    @NotEmpty
    @NotNull
    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;

    @NotEmpty
    @NotNull
    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;

    @Min(value = 0)
    private Integer age;

    @Min(value = 0)
    private Integer children;

    public Person() {
    }

    public Person(Integer id, String firstName, String lastName, Integer age, Integer children) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.children = children;
    }

    public Person(String firstName, String lastName, Integer age, Integer children) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.children = children;
    }

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Nullable
    public Integer getId() {
        return id;
    }

    public void setId(@Nullable Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getChildren() {
        return children;
    }

    public void setChildren(Integer children) {
        this.children = children;
    }
}

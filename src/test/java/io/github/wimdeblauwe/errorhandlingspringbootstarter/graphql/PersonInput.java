package io.github.wimdeblauwe.errorhandlingspringbootstarter.graphql;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

@ValidPerson
public class PersonInput {

    @Nullable
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

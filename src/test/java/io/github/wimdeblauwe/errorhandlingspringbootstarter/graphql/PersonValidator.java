package io.github.wimdeblauwe.errorhandlingspringbootstarter.graphql;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PersonValidator implements ConstraintValidator<ValidPerson, PersonInput> {

    @Override
    public boolean isValid(PersonInput personInput, ConstraintValidatorContext constraintValidatorContext) {
        return personInput.getAge() >= 18 || personInput.getChildren() <= 0;
    }
}

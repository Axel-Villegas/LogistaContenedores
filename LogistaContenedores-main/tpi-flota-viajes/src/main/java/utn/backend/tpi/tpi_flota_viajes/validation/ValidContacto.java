package utn.backend.tpi.tpi_flota_viajes.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidContactoValidator.class)
@Documented
public @interface ValidContacto {
    String message() default "Contacto debe ser un email válido o un teléfono válido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

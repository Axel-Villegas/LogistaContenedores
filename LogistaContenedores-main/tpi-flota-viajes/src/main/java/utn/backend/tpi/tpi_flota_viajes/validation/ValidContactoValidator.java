package utn.backend.tpi.tpi_flota_viajes.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Validador custom para contacto (email o teléfono)
 *
 * Acepta:
 * - Email: usuario@empresa.com, user.name@example.co.uk
 * - Teléfono: +54 9 11 1234-5678, 1123456789, +541112345678
 */
@Slf4j
public class ValidContactoValidator implements ConstraintValidator<ValidContacto, String> {
    // Patrón para email válido según RFC 5322 (simplificado)
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    // Patrón para teléfono:
    // Acepta: +54, +, espacios, guiones, paréntesis
    // Mínimo 7 dígitos, máximo 20 caracteres totales
    private static final String PHONE_PATTERN =
            "^[+]?[0-9\\s\\-()]{7,20}$";

    @Override
    public void initialize(ValidContacto annotation) {
        // No necesita inicialización adicional
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        // Si es null o vacío, la anotación @NotBlank ya lo valida
        if (value == null || value.isBlank()) {
            return true;  // Dejar que @NotBlank maneje esto
        }

        value = value.trim();

        // Validar que sea email O teléfono
        boolean esEmail = validarEmail(value);
        boolean esTelefono = validarTelefono(value);

        if (!esEmail && !esTelefono) {
            log.warn("Contacto inválido - No coincide con formato email ni teléfono: {}", value);

            // Personalizar mensaje de error
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("'%s' no es un email válido ni un teléfono válido", value)
            ).addConstraintViolation();

            return false;
        }

        log.debug("Contacto validado correctamente: {} (es {})",
                ocultarContacto(value),
                esEmail ? "email" : "teléfono");

        return true;
    }

    /**
     * Validar formato de email
     */
    private boolean validarEmail(String email) {
        if (!email.contains("@")) {
            return false;
        }

        boolean esValido = email.matches(EMAIL_PATTERN);

        if (esValido) {
            log.debug("Email validado: {}", ocultarContacto(email));
        }

        return esValido;
    }

    /**
     * Validar formato de teléfono
     * Acepta: +54 9 11 1234-5678, 1123456789, +541112345678
     */
    private boolean validarTelefono(String telefono) {
        // Contar solo los dígitos
        long digitos = telefono.chars()
                .filter(Character::isDigit)
                .count();

        // Debe tener entre 7 y 20 dígitos
        if (digitos < 7 || digitos > 20) {
            log.debug("Teléfono rechazado - Dígitos fuera de rango: {}", digitos);
            return false;
        }

        // Validar patrón general
        boolean esValido = telefono.matches(PHONE_PATTERN);

        if (esValido) {
            log.debug("Teléfono validado: {}", ocultarContacto(telefono));
        }

        return esValido;
    }

    /**
     * Ocultar contacto para logs (privacidad)
     * Ejemplo: usuario@empresa.com → u...m.com
     *          +54 9 11 1234-5678 → +54...5678
     */
    private String ocultarContacto(String contacto) {
        if (contacto == null || contacto.length() < 3) {
            return "***";
        }

        int inicio = Math.min(1, contacto.length() / 2);
        int fin = contacto.length() - 2;

        return contacto.substring(0, inicio) + "***" + contacto.substring(Math.min(fin, contacto.length()));
    }


}

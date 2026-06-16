package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class Usuario {
    private UUID id;
    private String cpf;
    private String senhaHash;
    private LocalDateTime criadoEm;

    public Usuario(UUID id, String cpf, String senhaHash, LocalDateTime criadoEm) {
        validateCpf(cpf);
        this.id = id != null ? id : UUID.randomUUID();
        this.cpf = cpf;
        this.senhaHash = senhaHash;
        this.criadoEm = criadoEm != null ? criadoEm : LocalDateTime.now();
    }

    private void validateCpf(String cpf) {
        if (cpf == null || cpf.length() != 11 || cpf.matches("^(.)\\1*$")) {
            throw new IllegalArgumentException("CPF inválido.");
        }
        if (!validateCpfDigits(cpf)) {
            throw new IllegalArgumentException("CPF inválido.");
        }
    }

    private boolean validateCpfDigits(String cpf) {
        // Primeiro dígito
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int num = Character.getNumericValue(cpf.charAt(i));
            sum += num * (10 - i);
        }
        int firstDigit = (sum * 10) % 11;
        if (firstDigit == 10) {
            firstDigit = 0;
        }
        if (firstDigit != Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }

        // Segundo dígito
        sum = 0;
        for (int i = 0; i < 10; i++) {
            int num = Character.getNumericValue(cpf.charAt(i));
            sum += num * (11 - i);
        }
        int secondDigit = (sum * 10) % 11;
        if (secondDigit == 10) {
            secondDigit = 0;
        }

        return secondDigit == Character.getNumericValue(cpf.charAt(10));
    }
}

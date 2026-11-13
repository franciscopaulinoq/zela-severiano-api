package io.github.franciscopaulinoq.zelaseveriano.domain.model;

public record Cpf(String valor) {

    private static final int TAMANHO = 11;

    public Cpf {
        if (!isValido(valor)) {
            throw new IllegalArgumentException("CPF inválido.");
        }
    }

    public static Cpf of(String valor) {
        return new Cpf(valor);
    }

    public static boolean isValido(String valor) {
        if (valor == null || valor.length() != TAMANHO || !valor.chars().allMatch(Character::isDigit)) {
            return false;
        }
        if (valor.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(valor, 9) == digito(valor, 9) && digitoVerificador(valor, 10) == digito(valor, 10);
    }

    private static int digitoVerificador(String valor, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += digito(valor, i) * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static int digito(String valor, int posicao) {
        return Character.getNumericValue(valor.charAt(posicao));
    }

    @Override
    public String toString() {
        return valor;
    }
}

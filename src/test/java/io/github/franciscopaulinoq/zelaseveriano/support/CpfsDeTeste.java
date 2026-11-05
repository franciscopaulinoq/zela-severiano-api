package io.github.franciscopaulinoq.zelaseveriano.support;

import java.util.concurrent.ThreadLocalRandom;

public final class CpfsDeTeste {

    private CpfsDeTeste() {}

    public static String novo() {
        int[] digitos = new int[11];
        do {
            for (int i = 0; i < 9; i++) {
                digitos[i] = ThreadLocalRandom.current().nextInt(10);
            }
        } while (todosIguais(digitos));

        digitos[9] = digitoVerificador(digitos, 9);
        digitos[10] = digitoVerificador(digitos, 10);

        StringBuilder cpf = new StringBuilder(11);
        for (int digito : digitos) {
            cpf.append(digito);
        }
        return cpf.toString();
    }

    private static int digitoVerificador(int[] digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += digitos[i] * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static boolean todosIguais(int[] digitos) {
        for (int i = 1; i < 9; i++) {
            if (digitos[i] != digitos[0]) {
                return false;
            }
        }
        return true;
    }
}

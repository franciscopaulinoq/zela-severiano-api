package io.github.franciscopaulinoq.zelaseveriano.application.usecase;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtualizarStatusRelatoUseCase {

    private final RelatoRepository relatoRepository;

    @Transactional
    public Relato execute(Long relatoId, StatusRelato novoStatus, String observacao) {
        Relato relato = relatoRepository.findById(relatoId)
                .orElseThrow(() -> new IllegalArgumentException("Relato não encontrado."));

        relato.atualizarStatus(novoStatus, observacao);

        return relatoRepository.save(relato);
    }
}

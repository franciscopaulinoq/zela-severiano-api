package io.github.franciscopaulinoq.zelaseveriano.domain.model;

import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.Role;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Usuario {
    private UUID id;
    private String cpf;
    private String senhaHash;
    private Role role;
    private OffsetDateTime criadoEm;

    @Builder
    public Usuario(UUID id, String cpf, String senhaHash, Role role, OffsetDateTime criadoEm) {
        this.id = id != null ? id : UUID.randomUUID();
        this.cpf = Cpf.of(cpf).valor();
        this.senhaHash = senhaHash;
        this.role = role != null ? role : Role.CIDADAO;
        this.criadoEm = criadoEm != null ? criadoEm : OffsetDateTime.now();
    }
}

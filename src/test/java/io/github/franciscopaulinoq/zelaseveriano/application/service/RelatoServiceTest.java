package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoDetalheDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoListagemDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RelatoServiceTest {

    private static final UUID USUARIO_ID = UUID.randomUUID();
    private static final Perfil PERFIL = Perfil.builder()
            .id(UUID.randomUUID())
            .nomeCompleto("Maria da Silva")
            .build();
    private static final Categoria CATEGORIA =
            Categoria.builder().id(1).nome("Iluminação").iconeNome("lampada").build();

    @Mock
    private RelatoRepository relatoRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private FileStorage fileStorage;

    @InjectMocks
    private RelatoService relatoService;

    @Test
    void registraRelatoSemFotoComoPendenteSemEnviarArquivo() {
        prepararPerfilECategoria();
        when(relatoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        Relato relato = relatoService.registrar(USUARIO_ID, novoRelato(), null, null, null);

        assertThat(relato.getStatus()).isEqualTo(StatusRelato.PENDENTE);
        assertThat(relato.getPerfil()).isSameAs(PERFIL);
        assertThat(relato.getCategoria()).isSameAs(CATEGORIA);
        assertThat(relato.getDescricao()).isEqualTo("Poste apagado na esquina");
        assertThat(relato.getLatitude()).isEqualTo(-6.08);
        assertThat(relato.getLongitude()).isEqualTo(-38.38);
        assertThat(relato.getUrlFoto()).isNull();
        verifyNoInteractions(fileStorage);
    }

    @Test
    void registraRelatoComFotoGuardandoAUrlDoArquivoEnviado() {
        prepararPerfilECategoria();
        byte[] foto = {1, 2, 3};
        when(fileStorage.upload(eq(foto), endsWith("_poste.jpg"), eq("image/jpeg")))
                .thenReturn("https://arquivos.example.com/poste.jpg");
        when(relatoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        Relato relato = relatoService.registrar(USUARIO_ID, novoRelato(), foto, "poste.jpg", "image/jpeg");

        assertThat(relato.getUrlFoto()).isEqualTo("https://arquivos.example.com/poste.jpg");
    }

    @Test
    void aceitaFotoPngEWebp() {
        prepararPerfilECategoria();
        when(fileStorage.upload(any(), anyString(), anyString())).thenReturn("https://arquivos.example.com/foto");
        when(relatoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        relatoService.registrar(USUARIO_ID, novoRelato(), new byte[] {1}, "buraco.png", "image/png");
        relatoService.registrar(USUARIO_ID, novoRelato(), new byte[] {1}, "buraco.webp", "IMAGE/WEBP");

        verify(fileStorage).upload(any(), endsWith("_buraco.png"), eq("image/png"));
        verify(fileStorage).upload(any(), endsWith("_buraco.webp"), eq("IMAGE/WEBP"));
    }

    @Test
    void recusaArquivoQueNaoEhImagemSemArmazenarNada() {
        assertThatThrownBy(() -> relatoService.registrar(
                        USUARIO_ID, novoRelato(), new byte[] {1}, "documento.pdf", "application/pdf"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de arquivo não permitido");

        verifyNoInteractions(fileStorage);
        verify(relatoRepository, never()).save(any());
    }

    @Test
    void recusaFotoSemTipoDeclarado() {
        assertThatThrownBy(() -> relatoService.registrar(USUARIO_ID, novoRelato(), new byte[] {1}, "foto", null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(fileStorage);
    }

    @Test
    void recusaRelatoDeUsuarioSemPerfil() {
        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> relatoService.registrar(USUARIO_ID, novoRelato(), null, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(relatoRepository, never()).save(any());
        verifyNoInteractions(fileStorage);
    }

    @Test
    void recusaRelatoComCategoriaInexistenteSemEnviarArquivo() {
        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(PERFIL));
        when(categoriaRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () -> relatoService.registrar(USUARIO_ID, novoRelato(), new byte[] {1}, "a.jpg", "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(relatoRepository, never()).save(any());
        verify(fileStorage, never()).upload(any(), anyString(), anyString());
    }

    @Test
    void detalheSoBuscaRelatosDoPerfilDoUsuarioAutenticado() {
        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(PERFIL));
        when(relatoRepository.findByPerfilIdAndId(PERFIL.getId(), 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> relatoService.obterDetalhe(USUARIO_ID, 42L))
                .isInstanceOf(IllegalArgumentException.class);

        verify(relatoRepository, never()).findById(any());
    }

    @Test
    void detalheDeRelatoProprioTrazOsDadosDoRelato() {
        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(PERFIL));
        when(relatoRepository.findByPerfilIdAndId(PERFIL.getId(), 42L)).thenReturn(Optional.of(relatoExistente()));

        RelatoDetalheDTO detalhe = relatoService.obterDetalhe(USUARIO_ID, 42L);

        assertThat(detalhe.getId()).isEqualTo(42L);
        assertThat(detalhe.getCategoriaNome()).isEqualTo("Iluminação");
        assertThat(detalhe.getStatus()).isEqualTo(StatusRelato.EM_ANALISE);
        assertThat(detalhe.getDescricao()).isEqualTo("Poste apagado na esquina");
    }

    @Test
    void listagemDoCidadaoUsaApenasOsRelatosDoSeuPerfil() {
        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(PERFIL));
        Pageable pagina = PageRequest.of(0, 10);
        when(relatoRepository.findAllByPerfilId(PERFIL.getId(), pagina))
                .thenReturn(new PageImpl<>(List.of(relatoExistente()), pagina, 1));

        Page<RelatoListagemDTO> relatos = relatoService.visualizarMeus(USUARIO_ID, pagina);

        assertThat(relatos.getTotalElements()).isEqualTo(1);
        assertThat(relatos.getContent()).singleElement().satisfies(relato -> {
            assertThat(relato.getId()).isEqualTo(42L);
            assertThat(relato.getCategoriaNome()).isEqualTo("Iluminação");
            assertThat(relato.getStatus()).isEqualTo(StatusRelato.EM_ANALISE);
        });
        verify(relatoRepository, never()).findAll();
    }

    private void prepararPerfilECategoria() {
        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(PERFIL));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(CATEGORIA));
    }

    private RelatoRegistroDTO novoRelato() {
        return RelatoRegistroDTO.builder()
                .categoriaId(1)
                .descricao("Poste apagado na esquina")
                .latitude(-6.08)
                .longitude(-38.38)
                .build();
    }

    private Relato relatoExistente() {
        return Relato.builder()
                .id(42L)
                .perfil(PERFIL)
                .categoria(CATEGORIA)
                .status(StatusRelato.EM_ANALISE)
                .descricao("Poste apagado na esquina")
                .build();
    }
}

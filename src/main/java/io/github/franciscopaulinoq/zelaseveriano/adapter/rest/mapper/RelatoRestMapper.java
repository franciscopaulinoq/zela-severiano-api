package io.github.franciscopaulinoq.zelaseveriano.adapter.rest.mapper;

import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.request.RelatoRegistroRequest;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.HistoricoStatusResponse;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoCriadoResponse;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoDetalheResponse;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoGestaoResponse;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoListagemResponse;
import io.github.franciscopaulinoq.zelaseveriano.adapter.rest.response.RelatoPublicoResponse;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.HistoricoStatusDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoDetalheDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoGestaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoListagemDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoPublicoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RelatoRestMapper {

    RelatoRegistroDTO map(RelatoRegistroRequest request);

    RelatoCriadoResponse map(Relato relato);

    RelatoDetalheResponse mapDetalhe(RelatoDetalheDTO dto);

    RelatoListagemResponse mapListagem(RelatoListagemDTO dto);

    List<RelatoListagemResponse> mapListagem(List<RelatoListagemDTO> dtos);

    RelatoGestaoResponse mapGestao(RelatoGestaoDTO dto);

    List<RelatoGestaoResponse> mapGestao(List<RelatoGestaoDTO> dtos);

    RelatoPublicoResponse mapPublico(RelatoPublicoDTO dto);

    List<RelatoPublicoResponse> mapPublico(List<RelatoPublicoDTO> dtos);

    HistoricoStatusResponse mapHistorico(HistoricoStatusDTO dto);

    List<HistoricoStatusResponse> mapHistorico(List<HistoricoStatusDTO> dtos);
}

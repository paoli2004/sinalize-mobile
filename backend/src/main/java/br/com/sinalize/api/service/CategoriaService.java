package br.com.sinalize.api.service;

import br.com.sinalize.api.dto.categoria.CategoriaAtualizacaoRequest;
import br.com.sinalize.api.dto.categoria.CategoriaRequest;
import br.com.sinalize.api.dto.categoria.CategoriaResponse;
import br.com.sinalize.api.entity.Categoria;
import br.com.sinalize.api.exception.RecursoNaoEncontradoException;
import br.com.sinalize.api.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    static final String CATEGORIA_NAO_ENCONTRADA = "Categoria não encontrada.";

    private final CategoriaRepository categoriaRepository;
    private final UsuarioLogadoProvider usuarioLogado;

    public CategoriaService(CategoriaRepository categoriaRepository, UsuarioLogadoProvider usuarioLogado) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioLogado = usuarioLogado;
    }

    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAllByOrderByCriadoEmAscIdAsc().stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    public CategoriaResponse buscar(Long id) {
        return CategoriaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        Categoria categoria = new Categoria(
                request.nome().trim(),
                request.descricao().trim(),
                usuarioLogado.obter());
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaAtualizacaoRequest request) {
        Categoria categoria = buscarEntidade(id);

        if (request.nome() != null) {
            categoria.setNome(request.nome().trim());
        }
        if (request.descricao() != null) {
            categoria.setDescricao(request.descricao().trim());
        }
        categoria.registrarAtualizacao(usuarioLogado.obter());

        return CategoriaResponse.from(categoria);
    }

    /** Exclui a categoria; os vínculos com palavras são removidos em cascata pelo banco. */
    @Transactional
    public void excluir(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException(CATEGORIA_NAO_ENCONTRADA);
        }
        categoriaRepository.deleteById(id);
    }

    /**
     * Busca as categorias pelos ids informados (ignorando repetições), para uso por outros serviços.
     *
     * @throws RecursoNaoEncontradoException se algum id não existir
     */
    public List<Categoria> buscarEntidadesPorIds(Collection<Long> ids) {
        Set<Long> idsUnicos = new LinkedHashSet<>(ids);
        Map<Long, Categoria> encontradas = categoriaRepository.findAllById(idsUnicos).stream()
                .collect(Collectors.toMap(Categoria::getId, Function.identity()));

        return idsUnicos.stream()
                .map(id -> Optional.ofNullable(encontradas.get(id))
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Categoria com id " + id + " não encontrada.")))
                .toList();
    }

    private Categoria buscarEntidade(Long id) {
        return categoriaRepository.findDetalhadaById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(CATEGORIA_NAO_ENCONTRADA));
    }
}

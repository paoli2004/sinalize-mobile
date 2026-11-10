package br.com.sinalize.api.service;

import br.com.sinalize.api.dto.usuario.UsuarioAtualizacaoRequest;
import br.com.sinalize.api.dto.usuario.UsuarioRequest;
import br.com.sinalize.api.dto.usuario.UsuarioResponse;
import br.com.sinalize.api.entity.Usuario;
import br.com.sinalize.api.exception.ConflitoException;
import br.com.sinalize.api.exception.RecursoNaoEncontradoException;
import br.com.sinalize.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    static final String USUARIO_NAO_ENCONTRADO = "Usuário não encontrado.";
    static final String EMAIL_EM_USO = "O e-mail informado já está em uso.";
    static final String USUARIO_COM_REGISTROS =
            "O usuário possui categorias, palavras ou quizzes associados e não pode ser excluído.";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllByOrderByCriadoEmAscIdAsc().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    public UsuarioResponse buscar(Long id) {
        return UsuarioResponse.from(buscarEntidade(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException(EMAIL_EM_USO);
        }

        Usuario usuario = new Usuario(request.nome().trim(), email, passwordEncoder.encode(request.senha()));

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioAtualizacaoRequest request) {
        Usuario usuario = buscarEntidade(id);

        if (request.nome() != null) {
            usuario.setNome(request.nome().trim());
        }
        if (request.email() != null) {
            String email = normalizarEmail(request.email());
            if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
                throw new ConflitoException(EMAIL_EM_USO);
            }
            usuario.setEmail(email);
        }
        if (request.senha() != null) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }

        return UsuarioResponse.from(usuario);
    }

    /**
     * Exclui o usuário. Quem já criou categorias ou palavras ou jogou quizzes é mantido,
     * para preservar a autoria e o histórico.
     */
    @Transactional
    public void excluir(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException(USUARIO_NAO_ENCONTRADO);
        }
        if (usuarioRepository.possuiRegistrosAssociados(id)) {
            throw new ConflitoException(USUARIO_COM_REGISTROS);
        }
        usuarioRepository.deleteById(id);
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(USUARIO_NAO_ENCONTRADO));
    }

    /** E-mails são gravados sem espaços e em minúsculas, para a verificação de duplicidade ser confiável. */
    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

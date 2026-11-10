package br.com.sinalize.api.service;

import br.com.sinalize.api.dto.usuario.UsuarioAtualizacaoRequest;
import br.com.sinalize.api.dto.usuario.UsuarioRequest;
import br.com.sinalize.api.dto.usuario.UsuarioResponse;
import br.com.sinalize.api.entity.Usuario;
import br.com.sinalize.api.exception.ConflitoException;
import br.com.sinalize.api.exception.RecursoNaoEncontradoException;
import br.com.sinalize.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4); // custo baixo: teste rápido

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void criarGravaHashDaSenhaEEmailNormalizado() {
        when(usuarioRepository.existsByEmailIgnoreCase("maria@email.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse resposta = usuarioService.criar(
                new UsuarioRequest(" Maria ", "  Maria@Email.com ", "Senha123"));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(salvo.capture());
        assertThat(salvo.getValue().getSenha()).isNotEqualTo("Senha123").startsWith("$2a$");
        assertThat(passwordEncoder.matches("Senha123", salvo.getValue().getSenha())).isTrue();
        assertThat(resposta.nome()).isEqualTo("Maria");
        assertThat(resposta.email()).isEqualTo("maria@email.com");
    }

    @Test
    void criarComEmailEmUsoLancaConflito() {
        when(usuarioRepository.existsByEmailIgnoreCase("admin@sinalize.com.br")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(
                new UsuarioRequest("Outro", "ADMIN@sinalize.com.br", "Senha123")))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("O e-mail informado já está em uso.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void atualizarEmailParaOutroJaUsadoLancaConflito() {
        Usuario usuario = new Usuario("Maria", "maria@email.com", "hash");
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("admin@sinalize.com.br", 2L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.atualizar(2L,
                new UsuarioAtualizacaoRequest(null, "admin@sinalize.com.br", null)))
                .isInstanceOf(ConflitoException.class);
        assertThat(usuario.getEmail()).isEqualTo("maria@email.com");
    }

    @Test
    void atualizarSenhaGravaNovoHashEMantemDemaisCampos() {
        Usuario usuario = new Usuario("Maria", "maria@email.com", "hash-antigo");
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        UsuarioResponse resposta = usuarioService.atualizar(2L, new UsuarioAtualizacaoRequest(null, null, "NovaSenha1"));

        assertThat(passwordEncoder.matches("NovaSenha1", usuario.getSenha())).isTrue();
        assertThat(resposta.nome()).isEqualTo("Maria");
        assertThat(resposta.email()).isEqualTo("maria@email.com");
    }

    @Test
    void excluirUsuarioComRegistrosLancaConflito() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(usuarioRepository.possuiRegistrosAssociados(1L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.excluir(1L))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("não pode ser excluído");
        verify(usuarioRepository, never()).deleteById(any());
    }

    @Test
    void excluirUsuarioSemRegistrosApaga() {
        when(usuarioRepository.existsById(2L)).thenReturn(true);
        when(usuarioRepository.possuiRegistrosAssociados(2L)).thenReturn(false);

        usuarioService.excluir(2L);

        verify(usuarioRepository).deleteById(2L);
    }

    @Test
    void buscarInexistenteLancaNaoEncontrado() {
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscar(9L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado.");
    }
}

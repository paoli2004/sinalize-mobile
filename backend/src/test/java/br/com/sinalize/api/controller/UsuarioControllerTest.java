package br.com.sinalize.api.controller;

import br.com.sinalize.api.dto.usuario.UsuarioRequest;
import br.com.sinalize.api.dto.usuario.UsuarioResponse;
import br.com.sinalize.api.exception.ConflitoException;
import br.com.sinalize.api.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    private static UsuarioResponse maria() {
        return new UsuarioResponse(2L, "Maria", "maria@email.com", LocalDateTime.of(2026, 11, 10, 21, 0));
    }

    @Test
    void listarNaoExpoeSenha() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of(maria()));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("maria@email.com"))
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    @Test
    void criarDevolve201SemSenha() throws Exception {
        when(usuarioService.criar(any())).thenReturn(maria());

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Maria", "email": "maria@email.com", "senha": "Senha123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/usuarios/2"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        verify(usuarioService).criar(new UsuarioRequest("Maria", "maria@email.com", "Senha123"));
    }

    @Test
    void criarComEmailESenhaInvalidosDevolve400() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Maria", "email": "maria", "senha": "senhafraca"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos", hasSize(2)))
                .andExpect(jsonPath("$.campos[0].campo").value("email"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("O e-mail informado é inválido."))
                .andExpect(jsonPath("$.campos[1].campo").value("senha"))
                .andExpect(jsonPath("$.campos[1].mensagem").value(
                        "A senha deve ter de 6 a 20 caracteres, com pelo menos 1 letra maiúscula, "
                                + "1 minúscula e 1 número ou símbolo."));

        verifyNoInteractions(usuarioService);
    }

    @Test
    void criarComSenhaCurtaDevolve400() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Maria", "email": "maria@email.com", "senha": "Ab1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos", hasSize(1)))
                .andExpect(jsonPath("$.campos[0].campo").value("senha"));
    }

    @Test
    void criarComEmailDuplicadoDevolve409() throws Exception {
        when(usuarioService.criar(any())).thenThrow(new ConflitoException("O e-mail informado já está em uso."));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Outro", "email": "admin@sinalize.com.br", "senha": "Senha123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("O e-mail informado já está em uso."));
    }

    @Test
    void atualizarSomenteNome() throws Exception {
        when(usuarioService.atualizar(any(), any())).thenReturn(maria());

        mockMvc.perform(patch("/usuarios/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Maria Silva"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void excluirUsuarioComRegistrosDevolve409() throws Exception {
        doThrow(new ConflitoException("O usuário possui categorias, palavras ou quizzes associados e não pode ser excluído."))
                .when(usuarioService).excluir(1L);

        mockMvc.perform(delete("/usuarios/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void excluirDevolve204() throws Exception {
        mockMvc.perform(delete("/usuarios/2"))
                .andExpect(status().isNoContent());

        verify(usuarioService).excluir(2L);
    }
}

package br.com.sinalize.api.controller;

import br.com.sinalize.api.dto.quiz.QuizPalavraResponse;
import br.com.sinalize.api.dto.quiz.QuizRequest;
import br.com.sinalize.api.dto.quiz.QuizResponse;
import br.com.sinalize.api.dto.quiz.RespostaRequest;
import br.com.sinalize.api.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public List<QuizResponse> listar() {
        return quizService.listar();
    }

    @GetMapping("/{id}")
    public QuizResponse buscar(@PathVariable Long id) {
        return quizService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<QuizResponse> criar(@Valid @RequestBody QuizRequest request) {
        QuizResponse criado = quizService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    /** Registra a resposta do jogador para uma palavra do quiz. */
    @PatchMapping("/palavras/{quizPalavraId}/responder")
    public QuizPalavraResponse responder(@PathVariable Long quizPalavraId,
                                         @Valid @RequestBody RespostaRequest request) {
        return quizService.responder(quizPalavraId, request);
    }
}

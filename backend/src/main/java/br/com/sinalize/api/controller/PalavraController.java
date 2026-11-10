package br.com.sinalize.api.controller;

import br.com.sinalize.api.dto.palavra.PalavraAtualizacaoRequest;
import br.com.sinalize.api.dto.palavra.PalavraRequest;
import br.com.sinalize.api.dto.palavra.PalavraResponse;
import br.com.sinalize.api.dto.palavra.PalavraResumoResponse;
import br.com.sinalize.api.service.PalavraService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/palavras")
public class PalavraController {

    private final PalavraService palavraService;

    public PalavraController(PalavraService palavraService) {
        this.palavraService = palavraService;
    }

    @GetMapping
    public List<PalavraResponse> listar() {
        return palavraService.listar();
    }

    /**
     * Sorteia palavras aleatórias, por exemplo {@code /palavras/aleatorias?excluir=3,8&quantidade=3}.
     */
    @GetMapping("/aleatorias")
    public List<PalavraResumoResponse> sortearAleatorias(
            @RequestParam(defaultValue = "") List<Long> excluir,
            @RequestParam(defaultValue = "3")
            @Min(value = 1, message = "O parâmetro quantidade deve ser no mínimo 1.")
            @Max(value = 20, message = "O parâmetro quantidade deve ser no máximo 20.")
            int quantidade) {
        return palavraService.sortearAleatorias(excluir, quantidade);
    }

    @GetMapping("/{id}")
    public PalavraResponse buscar(@PathVariable Long id) {
        return palavraService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<PalavraResponse> criar(@Valid @RequestBody PalavraRequest request) {
        PalavraResponse criada = palavraService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PatchMapping("/{id}")
    public PalavraResponse atualizar(@PathVariable Long id,
                                     @Valid @RequestBody PalavraAtualizacaoRequest request) {
        return palavraService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        palavraService.excluir(id);
    }
}

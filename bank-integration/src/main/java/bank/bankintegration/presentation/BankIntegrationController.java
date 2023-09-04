package bank.bankintegration.presentation;

import bank.bankintegration.application.BankIntegrationService;
import bank.bankintegration.domain.BankIntegration;
import bank.bankintegration.presentation.mapper.BankIntegrationMapper;
import bank.bankintegration.presentation.model.BankIntegrationModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/bank")
public class BankIntegrationController 
{
    @Autowired(required = true)
    private BankIntegrationService bankIntegrationService;
    @Autowired
    private BankIntegrationMapper bankIntegrationMapper;

    @Operation(summary = "Listar todos los monederos BankIntegration registrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se listaron todos los monederos BankIntegration registrados",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BankIntegration.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @GetMapping("/findAll")
    @CircuitBreaker(name = "bankIntegrationCircuit", fallbackMethod = "fallbackGetAllBankIntegration")
    @TimeLimiter(name = "bankIntegrationTimeLimiter")
    @Timed(description = "bankIntegrationGetAll")
    public Flux<BankIntegrationModel> getAll() {
        log.info("getAll executed");
        return bankIntegrationService.findAll()
                .map(bankIntegration -> bankIntegrationMapper.entityToModel(bankIntegration));
    }


    @Operation(summary = "Listar todos los monederos BankIntegration por Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se listaron todos los monederos bankIntegration por Id",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BankIntegration.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @GetMapping("/findById/{id}")
    @CircuitBreaker(name = "bankIntegrationCircuit", fallbackMethod = "fallbackFindById")
    @TimeLimiter(name = "bankIntegrationTimeLimiter")
    @Timed(description = "bankIntegrationsGetById")
    public Mono<ResponseEntity<BankIntegrationModel>> findById(@PathVariable String id){
        return bankIntegrationService.findById(id)
                .map(bankIntegration -> bankIntegrationMapper.entityToModel(bankIntegration))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todos los registros por DNI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se listaron todos los registros por DNI",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BankIntegration.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @GetMapping("/findByIdentityDni/{identityDni}")
    @CircuitBreaker(name = "bankIntegrationCircuit", fallbackMethod = "fallbackFindByIdentityDni")
    @TimeLimiter(name = "bankIntegrationTimeLimiter")
    public Mono<ResponseEntity<BankIntegrationModel>> findByIdentityDni(@PathVariable String identityDni){
        log.info("findByIdentityDni executed {}", identityDni);
        Mono<BankIntegration> response = bankIntegrationService.findByIdentityDni(identityDni);
        return response
                .map(bankIntegration -> bankIntegrationMapper.entityToModel(bankIntegration))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Registro de los Monederos BankIntegration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se registro el monedero de manera exitosa",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BankIntegration.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @PostMapping
    @CircuitBreaker(name = "bankIntegrationCircuit", fallbackMethod = "fallbackCreateBankIntegration")
    @TimeLimiter(name = "bankIntegrationTimeLimiter")
    public Mono<ResponseEntity<BankIntegrationModel>> create(@Valid @RequestBody BankIntegrationModel request){
        log.info("create executed {}", request);
        return bankIntegrationService.create(bankIntegrationMapper.modelToEntity(request))
                .map(bankIntegration -> bankIntegrationMapper.entityToModel(bankIntegration))
                .flatMap(c -> Mono.just(ResponseEntity.created(URI.create(String.format("http://%s:%s/%s/%s", "register", "9081", "bankIntegration", c.getId())))
                        .body(c)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar el monedero BankIntegration por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se actualizará el registro por el ID",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BankIntegration.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @PutMapping("/{id}")
    @CircuitBreaker(name = "bankIntegrationCircuit", fallbackMethod = "fallbackUpdateBankIntegration")
    @TimeLimiter(name = "bankIntegrationTimeLimiter")
    public Mono<ResponseEntity<BankIntegrationModel>> updateById(@PathVariable String id, @Valid @RequestBody BankIntegrationModel request){
        log.info("updateById executed {}:{}", id, request);
        return bankIntegrationService.update(id, bankIntegrationMapper.modelToEntity(request))
                .map(bankIntegration -> bankIntegrationMapper.entityToModel(bankIntegration))
                .flatMap(c -> Mono.just(ResponseEntity.created(URI.create(String.format("http://%s:%s/%s/%s", "register", "9081", "bankIntegration", c.getId())))
                        .body(c)))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Eliminar Monedero BankIntegration por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se elimino el registro por ID",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BankIntegration.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @DeleteMapping("/{id}")
    @CircuitBreaker(name = "bankIntegrationCircuit", fallbackMethod = "fallbackDeleteBankIntegration")
    @TimeLimiter(name = "bankIntegrationTimeLimiter")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable String id){
        log.info("deleteById executed {}", id);
        return bankIntegrationService.delete(id)
                .map( r -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}

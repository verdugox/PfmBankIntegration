package bank.bankintegration.application;

import bank.bankintegration.config.CircuitResilienceListener;
import bank.bankintegration.domain.BankIntegration;
import bank.bankintegration.domain.BankIntegrationRepository;
import bank.bankintegration.presentation.mapper.BankIntegrationMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
public class BankIntegrationService
{
    @Autowired
    private BankIntegrationRepository bankIntegrationRepository;
    @Autowired
    private CircuitResilienceListener circuitResilienceListener;
    @Autowired
    private TimeLimiterRegistry timeLimiterRegistry;
    @Autowired
    private BankIntegrationMapper bankIntegrationMapper;

    @Autowired
    private ReactiveHashOperations<String, String, BankIntegration> hashOperations;

    @CircuitBreaker(name = "bankIntegrationCircuit", fallbackMethod = "fallbackGetAllBankIntegration")
    @TimeLimiter(name = "bankIntegrationTimeLimiter")
    public Flux<BankIntegration> findAll(){
        log.debug("findAll executed");

        // Intenta obtener todos los monederos bankIntegration desde el caché de Redis
        Flux<BankIntegration> cachedBankIntegration = hashOperations.values("BankIntegrationRedis")
                .flatMap(bankIntegration -> Mono.justOrEmpty((BankIntegration) bankIntegration));

        // Si hay datos en la caché de Redis, retornarlos
        return cachedBankIntegration.switchIfEmpty(bankIntegrationRepository.findAll()
                .flatMap(bankIntegration -> {
                    // Almacena cada monedero bankIntegration en la caché de Redis
                    return hashOperations.put("BankIntegrationRedis", bankIntegration.getId(), bankIntegration)
                            .thenReturn(bankIntegration);
                }));

    }

    @CircuitBreaker(name = "bankIntegrationCircuit", fallbackMethod = "fallbackFindById")
    @TimeLimiter(name = "bankIntegrationTimeLimiter")
    public Mono<BankIntegration> findById(String bankIntegrationId)
    {
        log.debug("findById executed {}" , bankIntegrationId);
        return  hashOperations.get("BankIntegrationRedis",bankIntegrationId)
                .switchIfEmpty(bankIntegrationRepository.findById(bankIntegrationId)
                        .flatMap(bankIntegration -> hashOperations.put("BankIntegrationRedis",bankIntegration.getId(),bankIntegration)
                                .thenReturn(bankIntegration)));
    }

    @CircuitBreaker(name = "bankIntegrationCircuit", fallbackMethod = "fallbackGetAllItems")
    @TimeLimiter(name = "bankIntegrationTimeLimiter")
    public Mono<BankIntegration> findByIdentityDni(String identityDni){
        log.debug("findByIdentityDni executed {}" , identityDni);
        return bankIntegrationRepository.findByIdentityDni(identityDni);
    }

    @CircuitBreaker(name = "bankIntegrationCircuit", fallbackMethod = "fallbackFindByIdentityDni")
    @TimeLimiter(name = "bankIntegrationTimeLimiter")
    public Mono<BankIntegration> create(BankIntegration bankIntegration){
        log.debug("create executed {}",bankIntegration);
        bankIntegration.setDateRegister(LocalDate.now());
        return bankIntegrationRepository.save(bankIntegration);
    }

    @CircuitBreaker(name = "bankIntegrationCircuit", fallbackMethod = "fallbackUpdateBankIntegration")
    @TimeLimiter(name = "bankIntegrationTimeLimiter")
    public Mono<BankIntegration> update(String bankIntegrationId, BankIntegration bankIntegration){
        log.debug("update executed {}:{}", bankIntegrationId, bankIntegration);
        return bankIntegrationRepository.findById(bankIntegrationId)
                .flatMap(dbBankIntegration -> {
                    bankIntegration.setDateRegister(dbBankIntegration.getDateRegister());
                    bankIntegrationMapper.update(dbBankIntegration, bankIntegration);
                    return bankIntegrationRepository.save(dbBankIntegration);
                });
    }

    @CircuitBreaker(name = "bankIntegrationCircuit", fallbackMethod = "fallbackDeleteBankIntegration")
    @TimeLimiter(name = "bankIntegrationTimeLimiter")
    public Mono<BankIntegration>delete(String bankIntegrationId){
        log.debug("delete executed {}",bankIntegrationId);
        return bankIntegrationRepository.findById(bankIntegrationId)
                .flatMap(existingBankIntegration -> bankIntegrationRepository.delete(existingBankIntegration)
                        .then(Mono.just(existingBankIntegration)));
    }
}

package bank.bankintegration.domain;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface BankIntegrationRepository extends ReactiveMongoRepository<BankIntegration,String>
{
    Mono<BankIntegration> findByIdentityDni(String identityDni);
}

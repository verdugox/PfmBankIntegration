package bank.bankintegration.presentation.mapper;

import bank.bankintegration.domain.BankIntegration;
import bank.bankintegration.presentation.model.BankIntegrationModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BankIntegrationMapper
{
    BankIntegration modelToEntity (BankIntegrationModel model);
    BankIntegrationModel entityToModel(BankIntegration event);
    @Mapping(target = "id", ignore=true)
    void update(@MappingTarget BankIntegration entity, BankIntegration updateEntity);
}

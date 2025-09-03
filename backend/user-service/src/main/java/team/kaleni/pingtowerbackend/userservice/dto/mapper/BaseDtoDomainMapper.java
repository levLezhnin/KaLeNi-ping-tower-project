package team.kaleni.pingtowerbackend.userservice.dto.mapper;

public interface BaseDtoDomainMapper<RequestDtoClass, EntityClass, ResponseDtoClass> {
    ResponseDtoClass toDto(EntityClass domain);
    EntityClass toDomain(RequestDtoClass requestDto);
}

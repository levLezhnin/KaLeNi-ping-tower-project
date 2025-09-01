package team.kaleni.pingtowerbackend.dto.mapper;

public interface BaseDtoDomainMapper<RequestDtoClass, EntityClass, ResponseDtoClass> {
    ResponseDtoClass toDto(EntityClass domain);
    EntityClass toDomain(RequestDtoClass requestDto);
}

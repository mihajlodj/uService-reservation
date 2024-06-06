package ftn.reservationservice.domain.mappers;

import ftn.reservationservice.domain.dtos.RequestForReservationCreateRequest;
import ftn.reservationservice.domain.dtos.RequestForReservationDto;
import ftn.reservationservice.domain.entities.RequestForReservation;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RequestForReservationMapper {

    RequestForReservationMapper INSTANCE = Mappers.getMapper(RequestForReservationMapper.class);

    RequestForReservationDto toDto(RequestForReservation requestForReservation);

    RequestForReservation fromCreateRequest(RequestForReservationCreateRequest request);

}

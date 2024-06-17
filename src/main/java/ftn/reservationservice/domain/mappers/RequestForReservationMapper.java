package ftn.reservationservice.domain.mappers;

import ftn.reservationservice.domain.dtos.RequestForReservationCreateRequest;
import ftn.reservationservice.domain.dtos.RequestForReservationDto;
import ftn.reservationservice.domain.dtos.RequestForReservationStatusUpdateRequest;
import ftn.reservationservice.domain.entities.RequestForReservation;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RequestForReservationMapper {

    RequestForReservationMapper INSTANCE = Mappers.getMapper(RequestForReservationMapper.class);

    RequestForReservationDto toDto(RequestForReservation requestForReservation);

    List<RequestForReservationDto> toDto(List<RequestForReservation> requestForReservation);

    RequestForReservation fromCreateRequest(RequestForReservationCreateRequest request);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "lodgeId")
    @Mapping(ignore = true, target = "guestId")
    @Mapping(ignore = true, target = "ownerId")
    @Mapping(ignore = true, target = "price")
    @Mapping(ignore = true, target = "dateFrom")
    @Mapping(ignore = true, target = "dateTo")
    @Mapping(ignore = true, target = "numberOfGuests")
    void update(@MappingTarget RequestForReservation requestForReservation, RequestForReservationStatusUpdateRequest request);

}

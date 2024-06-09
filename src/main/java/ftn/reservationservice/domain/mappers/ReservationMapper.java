package ftn.reservationservice.domain.mappers;

import ftn.reservationservice.domain.dtos.ReservationDto;
import ftn.reservationservice.domain.entities.RequestForReservation;
import ftn.reservationservice.domain.entities.Reservation;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReservationMapper {

    ReservationMapper INSTANCE = Mappers.getMapper(ReservationMapper.class);

    ReservationDto toDto(Reservation reservation);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "status")
    Reservation toReservation(RequestForReservation requestForReservation);

}

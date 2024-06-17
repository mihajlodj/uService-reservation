package ftn.reservationservice.services;

import ftn.reservationservice.domain.dtos.LodgeAvailabilityPeriodDto;
import ftn.reservationservice.domain.dtos.LodgeDto;
import ftn.reservationservice.domain.dtos.UserDto;
import ftn.reservationservice.exception.exceptions.InternalException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestService {

    private final RestTemplate restTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${user.service}")
    private String userServiceUrl;
    @Value("${lodge.service}")
    private String lodgeServiceUrl;

    static final long EXPIRATION_TIME = 30L * 24 * 60 * 60 * 1000; // 1 month

    public UserDto getUserById(UUID userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + createAdminToken());
            HttpEntity<String> httpRequest = new HttpEntity<>(headers);

            String url = userServiceUrl + "/api/users/" + userId;
            ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, httpRequest, UserDto.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new InternalException("Failed to get user");
            }
        } catch (Exception e) {
            log.error("Error while getting user: ", e);
            throw new InternalException("Unexpected error while getting user");
        }
    }

    public LodgeDto getLodgeById(UUID lodgeId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + createAdminToken());
            HttpEntity<String> httpRequest = new HttpEntity<>(headers);

            String url = lodgeServiceUrl + "/api/lodge/interservice/" + lodgeId;
            ResponseEntity<LodgeDto> response = restTemplate.exchange(url, HttpMethod.GET, httpRequest, LodgeDto.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new InternalException("Failed to get lodge");
            }
        } catch (Exception e) {
            log.error("Error while getting lodge: ", e);
            throw new InternalException("Unexpected error while getting lodge");
        }
    }

    public List<LodgeAvailabilityPeriodDto> getLodgeAvailabilityPeriods(UUID lodgeId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + createAdminToken());
            HttpEntity<String> httpRequest = new HttpEntity<>(headers);

            String url = lodgeServiceUrl + "/api/lodge/availability/all/interservice/" + lodgeId;
            ResponseEntity<List<LodgeAvailabilityPeriodDto>> response = restTemplate.exchange(url, HttpMethod.GET, httpRequest, new ParameterizedTypeReference<List<LodgeAvailabilityPeriodDto>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new InternalException("Failed to get lodge");
            }
        } catch (Exception e) {
            log.error("Error while getting lodge: ", e);
            throw new InternalException("Unexpected error while getting lodge");
        }
    }

    private String createAdminToken() {
        return Jwts.builder()
                .setSubject("admin@ftn.com")
                .claim("role", "ADMIN")
                .claim("userId", "e40fcab5-d45b-4567-9d91-14e58178fea6")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

}

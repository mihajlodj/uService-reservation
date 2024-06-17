package ftn.reservationservice;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootTest(classes = ReservationServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AuthPostgresIntegrationTest extends PostgresIntegrationTest {

    public void authenticateGuest() {
        authenticate("GUEST", "guest", "e49fcaa5-d45b-4556-9d91-13e58187fea6");
    }

    public void authenticateHost() {
        authenticate("HOST", "host", "e49fcab5-d45b-4556-9d91-14e58177fea6");
    }

    public void authenticateAdmin() {
        authenticate("ADMIN", "admin", "e40fcab5-d45b-4567-9d91-14e58178fea6");
    }

    private void authenticate(String role, String username, String userId) {
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

        Map<String, Object> details = new HashMap<>();
        details.put("userId", userId);
        authentication.setDetails(details);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}

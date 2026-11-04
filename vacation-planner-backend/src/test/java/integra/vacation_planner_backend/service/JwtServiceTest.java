package integra.vacation_planner_backend.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
        .encodeToString(new byte[32]);

    private final JwtService jwtService = new JwtService(
        SECRET,
        3600000
    );

    @Test
    void generateToken_shouldContainUsernameAndRole() {
        String token = jwtService.generateToken("bob", "USER");

        var claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(SECRET)
            ))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertEquals("bob", claims.getSubject());
        assertEquals("USER", claims.get("role", String.class));
        assertEquals(
            3600000L,
            claims.getExpiration().getTime()
                - claims.getIssuedAt().getTime()
        );

        assertEquals("bob", jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_shouldRejectExpiredToken() {
        Instant now = Instant.now();

        String token = Jwts.builder()
            .subject("bob")
            .issuedAt(Date.from(now.minusSeconds(120)))
            .expiration(Date.from(now.minusSeconds(60)))
            .signWith(
                Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET)),
                Jwts.SIG.HS256
            )
            .compact();

        assertThrows(
            ExpiredJwtException.class,
            () -> jwtService.extractUsername(token)
        );
    }

    @Test
    void extractUsername_shouldRejectModifiedToken() {
        String token = jwtService.generateToken("bob", "USER");
        String[] parts = token.split("\\.");

        String payload = new String(
            Base64.getUrlDecoder().decode(parts[1]),
            StandardCharsets.UTF_8
        );

        String modifiedPayload = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                payload.replace("\"bob\"", "\"alice\"")
                    .getBytes(StandardCharsets.UTF_8)
            );

        String modifiedToken =
            parts[0] + "." + modifiedPayload + "." + parts[2];

        assertThrows(
            JwtException.class,
            () -> jwtService.extractUsername(modifiedToken)
        );
    }

    @Test
    void extractUsername_shouldRejectMalformedToken() {
        assertThrows(
            JwtException.class,
            () -> jwtService.extractUsername("invalid-token")
        );
    }

    @Test
    void constructor_shouldRejectInvalidExpiration() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new JwtService(SECRET, 0)
        );
    }
}

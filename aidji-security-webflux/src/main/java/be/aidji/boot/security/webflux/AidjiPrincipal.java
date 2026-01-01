package be.aidji.boot.security.webflux;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;

@AllArgsConstructor
@Getter
public class AidjiPrincipal implements Principal {

    private final String sub;
    private final String ipAddress;
    private final String aud;
    private final String iss;
    private final String sessionId;
    private final Collection<SimpleGrantedAuthority> authorities;
    private final Map<String, Object> extraClaims;

    @Override
    public String getName() {
        return this.sub;
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }
}

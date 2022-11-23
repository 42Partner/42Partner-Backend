package partner42.moduleapi.config.security;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import partner42.moduleapi.dto.user.CustomAuthenticationPrincipal;


@Slf4j
@Component
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().equals("/oauth2/authorization/authclient") ||
            request.getServletPath().equals("login/oauth2/code/authclient")) {
            filterChain.doFilter(request, response);
        } else {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                try {
                    String token = authorizationHeader.substring("Bearer ".length());
                    Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
                    JWTVerifier verifier = JWT.require(algorithm).build();
                    //JWT 토큰 검증 실패하면 JWTVerificationException 발생
                    DecodedJWT decodedJWT = verifier.verify(token);
                    String apiId = decodedJWT.getSubject();
                    String[] authoritiesJWT = decodedJWT.getClaim("authorities").asArray(String.class);
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    Arrays.stream(authoritiesJWT).forEach(authority -> {
                        authorities.add(new SimpleGrantedAuthority(authority));
                    });
                    //만료 되었는지 체크
                    //
                    //SecurityContextHoler에 로그인 인증 정보 저장.
                    UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(apiId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    filterChain.doFilter(request, response);
                } catch (Exception exception) {
                    log.error("Error logging in: {}", exception.getMessage());
                    response.sendError(HttpStatus.UNAUTHORIZED.value());
                    Map<String, String> error = new HashMap<>();
                    error.put("error_message", exception.getMessage());
                    response.setContentType(APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), error);
                }
                //token 자체가 없는 경우. 일단 통과
                //Authentiaation이 없기 때문에 인증해야만 접근 가능한 리소스에 접근하면 401 에러 발생
            } else{
                filterChain.doFilter(request, response);
            }

        }
    }
}

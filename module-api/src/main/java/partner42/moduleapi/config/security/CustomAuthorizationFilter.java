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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
import partner42.modulecommon.domain.model.user.User;


@Slf4j
@Component
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    private final String AUTHORIZATION_HEADER_CUSTOM = "user-token";

//    public static final Set<String> permitAllList = new HashSet<>();
//    static{
//        permitAllList.addAll(Arrays.asList(
//            "/oauth2/authorization/authclient",
//            "login/oauth2/code/authclient",
//            ));
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().equals("/oauth2/authorization/authclient") ||
            request.getServletPath().equals("login/oauth2/code/authclient")) {
            filterChain.doFilter(request, response);

        } else {
//            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER_CUSTOM);

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                try {
                    String token = authorizationHeader.substring("Bearer ".length());
                    Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
                    JWTVerifier verifier = JWT.require(algorithm).build();
                    //JWT ?????? ?????? ???????????? JWTVerificationException ??????
                    DecodedJWT decodedJWT = verifier.verify(token);
                    String username = decodedJWT.getSubject();
                    String[] authoritiesJWT = decodedJWT.getClaim("authorities")
                        .asArray(String.class);
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    Arrays.stream(authoritiesJWT).forEach(authority -> {
                        authorities.add(new SimpleGrantedAuthority(authority));
                    });
                    //?????? ???????????? ??????

                    /**
                     * SecurityContextHoler??? ????????? ?????? ?????? ??????.
                     * SpringSecurity ?????? Authentication??? ???????????? ???????????????
                     * ??????????????? ???????????? ????????? ????????????????????? ????????? ???????????? ?????????.
                     * ????????? ??????????????? ???????????? ????????? ?????? ????????? ???????????? ????????? ??????????????? ???????????? ?????????.
                     * https://www.baeldung.com/spring-security-session
                     * ?????? ????????? ????????? ???????????? ????????? SessionCreationPolicy NEVER->STATELESS??? ???????????? ??????.
                     */
                    UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(CustomAuthenticationPrincipal.of(
                            User.of(username,
                                null, null, null, null, null), null),
                            null,
                            authorities);

                    log.info(authorities.stream()
                        .map((SimpleGrantedAuthority::getAuthority))
                        .collect(Collectors.toList()).toString());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    filterChain.doFilter(request, response);
                } catch (Exception exception) {
                    log.error("Error logging in: {}", exception.getMessage());
//                    response.sendError(HttpStatus.UNAUTHORIZED.value());
//                    Map<String, String> error = new HashMap<>();
//                    error.put("error_message", exception.getMessage());
//                    response.setContentType(APPLICATION_JSON_VALUE);
//                    new ObjectMapper().writeValue(response.getOutputStream(), error);
                    filterChain.doFilter(request, response);

                }
                //token ????????? ?????? ??????. ?????? ??????
                //Authentiaation??? ?????? ????????? ??????????????? ?????? ????????? ???????????? ???????????? 401 ?????? ??????
            } else {
                filterChain.doFilter(request, response);
            }

        }
    }
}

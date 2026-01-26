package com.project.parking.filter;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.project.parking.exceptions.TokenExpiredException;
import com.project.parking.utils.JwtGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.project.parking.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtGenerator jwtGenerator;
    private final UserDetailsService userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${api.v1.prefix}")
    private String apiPrefix;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isNonAuthRequest(request)) {
            filterChain.doFilter(request, response);
            log.info("Non-auth request");
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        log.info("Authorization header: {}", authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authorization header is missing or invalid.");
            return;
        }

        final String token = authorizationHeader.substring(7);

        try {
            if (jwtGenerator.isValidToken(token)) {
                final String username = jwtGenerator.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = (User) userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities());

                    authenticationToken.setDetails(user);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token.");
                return;
            }
        } catch (SignatureVerificationException e) {
            log.error("Signature verification failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token signature.");
            return;
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authentication error: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isNonAuthRequest(HttpServletRequest request) {
        final List<Pair<String, String>> nonAuthRequests = List.of(
                // Swagger
                Pair.of("/swagger-ui/**", "GET"),
                Pair.of("/v3/api-docs/**", "GET"),
                Pair.of("/v3/api-docs", "GET"),
                Pair.of("/swagger-resources", "GET"),
                Pair.of("/swagger-resources/**", "GET"),
                Pair.of("/configuration/ui", "GET"),
                Pair.of("/configuration/security", "GET"),
                Pair.of("/swagger-ui.html", "GET"),
                Pair.of("/v3/api-docs/**", "GET"),
                Pair.of("/actuator", "GET"),
                Pair.of("/actuator/**", "GET"),
                Pair.of("/api-docs/**", "GET"),
                Pair.of("/swagger-resources/**", "GET"),
                Pair.of("/webjars/**", "GET"),

                // Auth
                Pair.of(String.format("%s/users/login", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/create/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/verify", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/resend-verification/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/forgot-password/**", apiPrefix), "POST"),

                // Me
                // Pair.of(String.format("%s/users/me/**", apiPrefix), "GET"),

                Pair.of(String.format("%s/tokens/**", apiPrefix), "GET"),
                Pair.of("/home", "GET"),
                // Pair.of("/", "GET"),

                Pair.of("/login", "GET"),
                Pair.of("/login/oauth2/code/**", "GET"),
                Pair.of("/login/oauth2/code/**", "POST"),

                Pair.of(String.format("%s/oauth/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/oauth/**", apiPrefix), "POST"),

                // Pair.of(String.format("%s/roles", apiPrefix), "GET"),

                // Web-setting
                Pair.of(String.format("%s/web-settings", apiPrefix), "GET"),
                Pair.of(String.format("%s/web-settings/**", apiPrefix), "PUT"),

                // Parking
                Pair.of(String.format("%s/parking/recognize", apiPrefix), "POST"),
                // home
                Pair.of("/home/**", "GET"),

                // device token

                Pair.of(String.format("%s/device-tokens", apiPrefix), "POST"),

                //member
                Pair.of(String.format("%s/members/pricing", apiPrefix), "GET"),
                Pair.of(String.format("%s/members/register", apiPrefix), "POST"),

                // Parking lot
                Pair.of(String.format("%s/parking-lots", apiPrefix), "GET"),
                Pair.of(String.format("%s/parking-lots/**", apiPrefix), "GET"),

                // parking plan
                Pair.of(String.format("%s/parking-plans/**", apiPrefix), "GET")

        );

        // String requestPath = request.getServletPath();
        // String requestMethod = request.getMethod();
        //
        // for (Pair<String, String> nonAuthRequest : nonAuthRequests) {
        // String path = nonAuthRequest.getFirst();
        // String method = nonAuthRequest.getSecond();
        //
        //
        // if (requestPath.matches(path.replace("**", ".*"))
        // && requestMethod.equalsIgnoreCase(method)) {
        // return true;
        // }
        //
        // }
        // String requestPath = request.getServletPath();
        // String requestMethod = request.getMethod();
        //
        // for (Pair<String, String> nonAuthRequest : nonAuthRequests) {
        // String pathPattern = nonAuthRequest.getFirst();
        // String method = nonAuthRequest.getSecond();
        //
        // if (pathMatcher.match(pathPattern, requestPath)
        // && requestMethod.equalsIgnoreCase(method)) {
        // return true;
        // }
        // }
        //
        // return false;
        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();

        return nonAuthRequests.stream()
                .anyMatch(pair -> pathMatcher.match(pair.getFirst(), requestPath)
                        && requestMethod.equalsIgnoreCase(pair.getSecond()));
    }

}

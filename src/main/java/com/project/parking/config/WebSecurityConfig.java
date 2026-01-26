package com.project.parking.config;
import com.project.parking.components.CustomAccessDeniedHandler;
import com.project.parking.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    @Value("${api.v1.prefix}")
    String apiPrefix;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(request -> {
                    request// Yêu cầu xác thực với endpoint /me
                            .anyRequest().permitAll();          // Cho phép các yêu cầu khác
                })
                .authenticationProvider(authenticationProvider);
//                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
//                .oauth2Login(oauth2 -> oauth2
//                        //.loginPage("/login")
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .userService(oauth2UserService()))
//                        .successHandler(customAuthenticationSuccessHandler)
//                );

        return http.build();
    }
    public OAuth2UserService<OAuth2UserRequest,OAuth2User> oauth2UserService() {
        return new DefaultOAuth2UserService();
    }

//    @Bean
//    public AuthenticationSuccessHandler authenticationSuccessHandler( CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
//        return customAuthenticationSuccessHandler;
//    }

}


package com.booking.app.config;

import com.booking.app.security.CustomUserDetailsService;
import com.booking.app.security.RestAuthenticationEntryPoint;
import com.booking.app.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
@Log4j2
public class WebSecurityConfiguration {

    private final CustomUserDetailsService userDetailsService;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String[] PUBLIC_PATHS = {
            "/v3/api-docs/**",
            "/configuration/**",
            "/swagger*/**",
            "/webjars/**",

            "/favicon.ico",
            "/error",
            "/register",
            "/confirm-email",
            "/resend/confirm-token",
            "/resend/reset-token",
            "/confirm-email",
            "/reset",
            "/new-password",
            "/typeAhead",
            "/fail",
            "/login",
            "/logout",
            "/oauth2/authorize/**",
            "/sortedBy",
            "/searchTickets",
            "/get/ticket/**",
            "/selectedTransport",
            "/getReviews"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.cors(Customizer.withDefaults());

//        http.authorizeHttpRequests(request -> request.requestMatchers(PUBLIC_PATHS).permitAll()
        http.authorizeHttpRequests(t -> t.anyRequest().permitAll());

        http.addFilterBefore(jwtAuthenticationFilter, ExceptionTranslationFilter.class);
        http.exceptionHandling(ex -> ex.authenticationEntryPoint(new RestAuthenticationEntryPoint()));

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter(@Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigurationSource) {
        return new CorsFilter(corsConfigurationSource);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(daoAuthenticationProvider()));
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

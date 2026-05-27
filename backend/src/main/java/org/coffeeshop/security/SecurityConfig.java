package org.coffeeshop.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.coffeeshop.security.handlers.RestAccessDeniedHandler;
import org.coffeeshop.security.services.StaffUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Configures authentication, authorization, and JWT filter chain for the API. */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final StaffUserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Value(
            "${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost,https://coffeeshopweb-production-3ca5.up.railway.app}")
    private String allowedOrigins;

    /**
     * Constructs the security configuration with required filter and service dependencies.
     *
     * @param jwtAuthenticationFilter JWT authentication filter
     * @param userDetailsService user details service for authentication
     * @param restAuthenticationEntryPoint entry point for unauthenticated requests
     * @param restAccessDeniedHandler handler for access denied responses
     */
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            StaffUserDetailsService userDetailsService,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }

    /**
     * Password encoder used to hash and verify user passwords.
     *
     * @return bcrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates a DAO authentication provider wired to the custom user details service.
     *
     * @return configured authentication provider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the authentication manager from the Spring Security configuration.
     *
     * @param configuration auto-configured authentication configuration
     * @return the authentication manager
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Builds the CORS configuration source from the comma-separated allowed-origins property.
     *
     * @return CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins =
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isBlank())
                        .collect(Collectors.toList());
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(
                Arrays.asList(
                        "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"
                        // "Access-Control-Allow-Origin",
                        // "Access-Control-Allow-Methods",
                        // "Access-Control-Allow-Headers",
                        // "Access-Control-Allow-Credentials",
                        // "Access-Control-Expose-Headers"
                        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configures the main security filter chain with CORS, CSRF, session management, and endpoint
     * authorization.
     *
     * @param http the HTTP security builder
     * @return the built security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(
                        exceptions ->
                                exceptions
                                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                org.springframework.http.HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers(
                                                org.springframework.http.HttpMethod.GET,
                                                "/api/v1/menu-item/**")
                                        .permitAll()
                                        .requestMatchers(
                                                org.springframework.http.HttpMethod.GET,
                                                "/api/v1/station/**")
                                        .permitAll()
                                        .requestMatchers(
                                                org.springframework.http.HttpMethod.POST,
                                                "/api/v1/order")
                                        .permitAll()
                                        .requestMatchers(
                                                org.springframework.http.HttpMethod.GET,
                                                "/api/v1/order/**")
                                        .permitAll()
                                        .requestMatchers(
                                                org.springframework.http.HttpMethod.POST,
                                                "/api/v1/checkout/**")
                                        .permitAll()
                                        .requestMatchers(
                                                org.springframework.http.HttpMethod.PUT,
                                                "/api/v1/order/**")
                                        .authenticated()
                                        .requestMatchers("/api/v1/auth/**", "/gate", "/error")
                                        .permitAll()
                                        .requestMatchers("/api/v1/customer/**")
                                        .permitAll()
                                        .requestMatchers("/api/v1/staff/**")
                                        .hasRole("ADMIN")
                                        .anyRequest()
                                        .authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

package com.example.plagiarism1.configuration;

import com.example.plagiarism1.repository.RoleRepository;
import com.example.plagiarism1.repository.UtilisateurRepository;
import com.example.plagiarism1.service.CustomOAuth2UserService;
import com.example.plagiarism1.service.JwtFilterService;
import com.example.plagiarism1.service.JwtService;
import com.example.plagiarism1.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class ConfigurationSecurite {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtFilterService jwtFilterService;
    private final JwtService jwtService;
    private final CustomOAuth2UserService customOAuth2UserService;

    public ConfigurationSecurite(
            BCryptPasswordEncoder bCryptPasswordEncoder,
            JwtFilterService jwtFilterService,
            JwtService jwtService,
            CustomOAuth2UserService customOAuth2UserService
    ) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtFilterService = jwtFilterService;
        this.jwtService = jwtService;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        System.out.println("🔐 Configuration Securite initialisée");
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/inscription").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/verification/verifier-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/pending-users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/valider-inscription/{pendingUserId}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/rejeter-inscription/{pendingUserId}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/activation").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/connexion").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/refresh-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reset-password").permitAll()
                        .requestMatchers(HttpMethod.GET, "/oauth2/authorization/google").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/documents/upload").hasAnyAuthority("ROLE_ADMIN", "ROLE_ENSEIGNANT")
                        .requestMatchers(HttpMethod.GET, "/api/documents/select").hasAnyAuthority("ROLE_ADMIN", "ROLE_ENSEIGNANT")
                        .requestMatchers(HttpMethod.POST, "/api/analysis/compare-by-title").hasAnyAuthority("ROLE_ADMIN", "ROLE_ENSEIGNANT")
                        .requestMatchers(HttpMethod.GET, "/api/select").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(jwtFilterService, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // ✅ injection confirmée
                        )
                        .successHandler(oAuth2SuccessHandler())
                        .failureHandler(oAuth2FailureHandler())

                )
                .build();
    }

    // ✅ Handler personnalisé pour retour du JWT après login OAuth2
    @Bean
    public AuthenticationSuccessHandler oAuth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");

            Map<String, String> tokens = jwtService.generate(email);
            String bearer = tokens.get("bearer");
            String refresh = tokens.get("refresh");

            response.sendRedirect("http://localhost:3000/oauth2-success?token=" + bearer + "&refresh=" + refresh);
        };
    }

    @Bean
    public AuthenticationFailureHandler oAuth2FailureHandler() {
        return (request, response, exception) -> {
            response.sendRedirect("http://localhost:3000/login?error=oauth_failed");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return daoAuthenticationProvider;
    }

}




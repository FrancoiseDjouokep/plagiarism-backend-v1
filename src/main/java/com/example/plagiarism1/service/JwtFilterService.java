package com.example.plagiarism1.service;

import com.example.plagiarism1.model.Jwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
public class JwtFilterService extends OncePerRequestFilter {
    private UtilisateurService utilisateurService;
    private JwtService jwtService;

    public JwtFilterService(UtilisateurService utilisateurService, JwtService jwtService) {
        this.utilisateurService = utilisateurService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().startsWith("/connexion") ||
                request.getServletPath().startsWith("/inscription") ||
                request.getServletPath().startsWith("/activation") ||
                request.getServletPath().startsWith("/oauth2") ||
                request.getServletPath().startsWith("/login/oauth2"))  {
            filterChain.doFilter(request, response);
            return;
        }
        String token = null;
    String username = null;
    boolean isTokenExpired = true;
    Jwt tokenBD = null;
       final String authorization = request.getHeader("Authorization");
       if(authorization != null && authorization.startsWith("Bearer")){
           token = authorization.substring(7);
           tokenBD = this.jwtService.tokenByValue(token);
           isTokenExpired = jwtService.isTokenExpired(token);
           username = jwtService.lireusername(token);
       }
       if (!isTokenExpired && username != null
               && tokenBD.getUtilisateur().getEmail().equals(username)
               && SecurityContextHolder.getContext().getAuthentication() == null){
           UserDetails userDetails = utilisateurService.loadUserByUsername(username);
           UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
           SecurityContextHolder.getContext().setAuthentication(authenticationToken);
       }
       filterChain.doFilter(request, response);

    }
}

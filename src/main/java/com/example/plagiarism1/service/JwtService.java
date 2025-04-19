package com.example.plagiarism1.service;

import com.example.plagiarism1.AuthentificationDTO;
import com.example.plagiarism1.model.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private final String Encryption_Key = "7477f0de9d84245e9dbd9cab01ddd403ac70f4ffd57190abf9685d7f34fa6e08";
    private UtilisateurService utilisateurService;

    public JwtService(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    public Map<String, String> generate(String username){
        Utilisateur utilisateur = (Utilisateur) this.utilisateurService.loadUserByUsername(username);
        return this.generateJwt(utilisateur);
    }
    public String lireusername(String token){
        return this.getClaims(token, Claims::getSubject);
    }
    public  boolean isTokenExpired(String token){
        Date expirationDate = getExpirationDateFromToken(token);
        return  expirationDate.before(new Date());
    }
    private Date getExpirationDateFromToken(String token){
        return this.getClaims(token, Claims::getExpiration);
    }
    private <T> T getClaims(String token, Function<Claims, T> function){
        Claims claims = getAllClaims(token);
        return function.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(this.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Map<String, String> generateJwt(Utilisateur utilisateur){
        final long currentTime = System.currentTimeMillis();
        final long expirationTime = currentTime + 30 * 60 * 1000;
        final Map<String, Object> claims = Map.of(
                "nom", utilisateur.getNom(),
                Claims.EXPIRATION, new Date(expirationTime),
                Claims.SUBJECT, utilisateur.getEmail()
        );


        final String bearer = Jwts.builder()
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(expirationTime))
                .setSubject(utilisateur.getEmail())
                .setClaims(claims)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
        return Map.of("bearer", bearer);
    }
    private Key getKey(){
        byte[] decode = Decoders.BASE64.decode(Encryption_Key);
        return Keys.hmacShaKeyFor(decode);
    }

}

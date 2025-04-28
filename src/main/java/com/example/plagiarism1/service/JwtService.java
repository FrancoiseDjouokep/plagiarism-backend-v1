package com.example.plagiarism1.service;

import com.example.plagiarism1.AuthentificationDTO;
import com.example.plagiarism1.model.Jwt;
import com.example.plagiarism1.model.RefreshToken;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.repository.JwtRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional
@Service
public class JwtService {
    public static final String BEARER = "bearer";
    public static final String REFRESH = "refresh";
    private static final String Refresh = "refresh";
    public static final String TOKEN_INVALIDE = "token invalide";
    private final String Encryption_Key = "7477f0de9d84245e9dbd9cab01ddd403ac70f4ffd57190abf9685d7f34fa6e08";
    private UtilisateurService utilisateurService;
    private JwtRepository jwtRepository;

    public JwtService(UtilisateurService utilisateurService, JwtRepository jwtRepository) {
        this.utilisateurService = utilisateurService;
        this.jwtRepository = jwtRepository;
    }

    public Map<String, String> generate(String username){
        Utilisateur utilisateur = (Utilisateur) this.utilisateurService.loadUserByUsername(username);
        this.disableToken(utilisateur);
        final Map<String, String> jwtMap = new HashMap<>(this.generateJwt(utilisateur));
        RefreshToken refreshToken = RefreshToken.builder()
                .valeur(UUID.randomUUID().toString())
                .expire(false)
                .creation(Instant.now())
                .expiration(Instant.now().plusMillis(30*60*1000))
                .build();
        final Jwt jwt = Jwt
                .builder()
                .valeur(jwtMap.get(BEARER))
                .desactive(false)
                .expire(false)
                .refreshToken(refreshToken)
                .utilisateur(utilisateur)
                .build();
        this.jwtRepository.save(jwt);
        jwtMap.put("refresh", refreshToken.getValeur());
        return jwtMap;
    }
    public Map<String, String> refreshToken(Map<String, String> refreshTokenRequest) {
        final Jwt jwt = this.jwtRepository.findByRefreshToken(refreshTokenRequest.get(REFRESH))
                .orElseThrow(() -> new RuntimeException(TOKEN_INVALIDE));

        if(jwt.getRefreshToken().isExpire() ||
                jwt.getRefreshToken().getExpiration().isBefore(Instant.now())) {
            throw new RuntimeException(TOKEN_INVALIDE);
        }

        // Invalidate the old refresh token
        jwt.getRefreshToken().setExpire(true);
        this.jwtRepository.save(jwt);

        // Generate new tokens
        return this.generate(jwt.getUtilisateur().getEmail());
    }
    public void disableToken(Utilisateur utilisateur){
        final List<Jwt> jwtList = this.jwtRepository.findUtilisateur(utilisateur.getEmail()).peek(
                jwt -> {
                    jwt.setDesactive(true);
                    jwt.setExpire(true);
                }
        ).collect(Collectors.toList());
        this.jwtRepository.saveAll(jwtList);
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
        final long expirationTime = currentTime + 1440 * 60 * 1000;
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

    public Jwt tokenByValue(String valeur) {
        return this.jwtRepository.findByValeurAndDesactiveAndExpire(
                        valeur,
                        false,
                        false)
                .orElseThrow(() -> new RuntimeException("Token inconnu"));
    }

    public void deconnexion() {
       Utilisateur utilisateur = (Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       Jwt jwt = this.jwtRepository.findUtilisateurValidToken(
               utilisateur.getEmail(),
               false,
               false)
               .orElseThrow(() -> new RuntimeException("Token invalid"));
       jwt.setExpire(true);
       jwt.setDesactive(true);
       this.jwtRepository.save(jwt);
    }
    @Scheduled(cron = "0 */1 * * * *")
    public void deleteUselessToken(){
    this.jwtRepository.deleteAllByExpireAndDesactive(true, true);
    }


}


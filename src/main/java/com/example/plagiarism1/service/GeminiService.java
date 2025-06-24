package com.example.plagiarism1.service;

import com.example.plagiarism1.model.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final String API_KEY = "AIzaSyD8OnMvfL2NAXFKrWtDAayJiozxQxdOqNE"; // Remplacez par votre vraie clé
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public String detectAi(String texte) {
        RestTemplate restTemplate = new RestTemplate();

        String prompt = """
    Analyse le texte fourni pour déterminer s'il a été généré par une intelligence artificielle (comme GPT, Gemini, etc.) ou s'il a été écrit par un humain.
    Tu dois effectuer une double analyse :
         1. **Donner un pourcentage approximatif** indiquant la proportion du texte qui semble générée par une IA.
         2. **Justifier ta reponse avec des examples que tu prendra du texte fourni**.

    Pour t'aider dans cette analyse, voici des exemples clairs :

    ---
    Exemple 1 (Humain)
    Texte: "Franchement, j'ai eu une journée de dingue aujourd'hui. Mon chat a renversé le café sur mon clavier, j'ai failli rater mon bus, et après j'ai passé deux heures à essayer de comprendre ce nouveau logiciel. Je suis crevée mais contente d'être rentrée !"
    Analyse: Utilise un langage informel, inclut des erreurs mineures naturelles (potentiellement "crevée" sans genre précis si l'auteur est un homme, ou une tournure légèrement maladroite), exprime des émotions personnelles et une narration spontanée.
    Résultat attendu: Humain

    ---
    Exemple 2 (IA)
    Texte: "L'optimisation des flux logistiques est cruciale pour l'efficience opérationnelle des chaînes d'approvisionnement modernes. L'intégration de systèmes de gestion intelligents permet de rationaliser les processus et de maximiser la rentabilité."
    Analyse: Vocabulaire technique et formel, structure de phrase parfaite, absence de toute imperfection ou touche personnelle, sonne très "rapport d'entreprise".
    Résultat attendu: IA

    ---
    Exemple 3 (Humain - avec des erreurs/particularités)
    Texte: "Le christianisme est une religion dont le fondateur est Jésus Christ. Le livre Saint que nous utilisons dans cette religion est la Sainte Bible. En tant que chrétien, nous avons pour obligation de : • Aimer son prochain comme soit même • Aimer Dieu de tout notre être. Ces deux commandement nous ont été laisser par Jésus Christ notre Seigneur et Sauveur. Il nous prouve son amour inconditionnelle en donnant sa vie sur la croix a fin que nous soyons libéré de toutes chaine qui nous relie a l’enferre. Pour cela, nous lui devons une reconnaissance infini. Mais Lui ne nous demande que de Lui laisser entrer dans nos vies pour temoigner la gloire de Sont nom."
    Analyse: Contient des fautes d'orthographe ("soit même", "commandement", "l’enferre", "temoigner", "Sont"), des tournures de phrases qui sonnent très personnelles et directes ("nous lui devons une reconnaissance infini"), et exprime une conviction forte de manière simple. Ces imperfections et cette subjectivité sont des marques humaines.
    Résultat attendu: Humain

    ---
    Exemple 4 (IA - sur un sujet similaire, mais générique)
    Texte: "Le christianisme, fondé sur les enseignements de Jésus de Nazareth, est une religion monothéiste globale. Son texte sacré principal est la Bible, qui guide les fidèles dans leurs obligations éthiques et spirituelles. Le sacrifice du Christ sur la croix est perçu comme l'apogée de l'amour divin, offrant la rédemption à l'humanité. Les croyants sont invités à accepter cette grâce pour manifester la puissance de la foi."
    Analyse: Langage fluide et impeccable, vocabulaire riche mais générique, structure parfaite sans aucune faute, sonne comme un article encyclopédique ou une synthèse parfaite.
    Résultat attendu: IA

    ---

    **Critères d'analyse pour le texte à évaluer :**
    * **Signes d'un texte humain :** Présence de petites imperfections naturelles (fautes de frappe, erreurs grammaticales non systématiques, répétitions involontaires), utilisation d'un langage plus idiomatique, personnel, ou avec des variations inattendues, expressions de sentiments ou opinions subjectives de manière nuancée, un flux de pensée qui peut parfois sembler moins linéaire.
    * **Signes d'un texte généré par IA :** Cohérence grammaticale et orthographique quasi-parfaite, structure de phrase et de paragraphe très régulière et prévisible, absence de "bruit" humain typique (hésitations, reformulations évidentes), utilisation d'un vocabulaire riche mais souvent générique et optimisé pour la clarté/concision, manque de profondeur émotionnelle ou de personnalité distincte.

    Format de réponse attendu (respecte scrupuleusement la structure suivante) :
    Pourcentage IA : 
    Justification :

    Texte à analyser :
    """ + texte;

        // Construction de la requête
        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> message = Map.of(
                "role", "user",
                "parts", List.of(part)
        );

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(message));
        body.put("generationConfig", Map.of(
                "temperature", 0.0,
                "topP", 0.1,
                "maxOutputTokens", 512
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_URL, request, Map.class);

            System.out.println("Réponse brute Gemini : " + response.getBody());

            return extractResponse(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur : " + e.getMessage();
        }
    }

    private String extractResponse(Map<String, Object> responseBody) {
        if (responseBody == null) return "Erreur : réponse vide";

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
        if (candidates == null || candidates.isEmpty()) return "Aucune réponse générée";

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) return "Erreur : contenu manquant";

        List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
        if (parts == null || parts.isEmpty()) return "Erreur : texte manquant";

        String response = parts.get(0).get("text").trim();

        // Retour strict
        return response;
    }
}

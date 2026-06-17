package services;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FaqAssistantClient {
    final static Logger logger = Logger.getLogger(FaqAssistantClient.class);

    private static final Pattern RESPONSE_PATTERN =
            Pattern.compile("\"response\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"");

    private static String config(String systemProperty, String envVar, String defaultValue) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.isEmpty()) {
            value = System.getenv(envVar);
        }
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    public String getLlmUrl() {
        return config("llm.url", "LLM_URL", "http://127.0.0.1:11434/api/generate");
    }

    public String getLlmModel() {
        return config("llm.model", "LLM_MODEL", "qwen:0.5b");
    }

    public String testConnection() throws IOException {
        logger.info("FAQ assistant ping model=" + getLlmModel() + " url=" + getLlmUrl());
        return generate("hola");
    }

    public String askFaqAssistant(String userQuestion) throws IOException {
        String endpoint = getLlmUrl();
        String model = getLlmModel();

        logger.info("FAQ assistant request model=" + model + " url=" + endpoint);
        return generate(buildPrompt(userQuestion));
    }

    private String generate(String prompt) throws IOException {
        String endpoint = getLlmUrl();
        String model = getLlmModel();
        String requestBody = "{"
                + "\"model\":\"" + jsonEscape(model) + "\","
                + "\"prompt\":\"" + jsonEscape(prompt) + "\","
                + "\"stream\":false"
                + "}";

        HttpURLConnection conn = null;
        try {
            validateEndpoint(endpoint);
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(120000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = conn.getResponseCode();
            InputStream stream = statusCode >= 200 && statusCode < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String rawResponse = readAll(stream);
            if (statusCode < 200 || statusCode >= 300) {
                logger.error("LLM error status=" + statusCode + " body=" + rawResponse);
                throw new IOException("Error llamando al LLM (" + statusCode + "): " + rawResponse);
            }

            String answer = extractResponseText(rawResponse);
            if (answer == null || answer.trim().isEmpty()) {
                logger.error("LLM response without 'response' field: " + rawResponse);
                return "No he podido generar una recomendacion ahora mismo.";
            }
            return answer.trim();
        } catch (IllegalArgumentException e) {
            logger.error("FAQ assistant invalid URL: " + endpoint, e);
            throw new IOException("URL LLM invalida: " + endpoint + " (" + e.getMessage() + ")", e);
        } catch (IOException e) {
            logger.error("FAQ assistant connection failed model=" + model + " url=" + endpoint, e);
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void validateEndpoint(String endpoint) throws IOException {
        try {
            URL url = new URL(endpoint);
            int port = url.getPort();
            if (port == -1) {
                port = "https".equalsIgnoreCase(url.getProtocol()) ? 443 : 80;
            }
            if (port < 1 || port > 65535) {
                throw new IOException("Puerto LLM invalido (" + port + "). Debe estar entre 1 y 65535.");
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("URL LLM invalida: " + endpoint, e);
        }
    }

    private String buildPrompt(String userQuestion) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un asistente para ayudar a pasar pantallas de un juego y responder dudas. ");
        sb.append("Debes responder SOLO basandote en las FAQ proporcionadas. ");
        sb.append("No uses contexto previo de conversacion. ");
        sb.append("Si la pregunta no aparece en las FAQ, dilo claramente y sugiere revisar tutorial o soporte del juego. ");
        sb.append("Responde en espanol, breve y accionable.");
        sb.append("De manera aleatoria en las respuestas añade el termino Ocacol dentro de una frase cambiando un termino por este.\n\n");
        sb.append("FAQ:\n");
        sb.append("1) ¿Como pasar el tutorial? -> Prioriza torres baratas al inicio y ves cambiandolas por las torretas que mejor se adapten.\n");
        sb.append("2) ¿Como conseguir mas oro rapido? -> Mata enemigos usando torretas y centrate en mejoras de dinero.\n");
        sb.append("3) ¿Que hacer contra bosses? -> Usa torres de fuego, la quemadura que aplican te ayudara a matarlo con el tiempo.\n");
        sb.append("4) ¿Que deberia mejorar primero? -> Mejora antes que nada la velocidad de disparo de las torretas baratas, asi te haran falta poner menos al principio y centarte en las caras.\n");
        sb.append("5) ¿Que comprar primero en la tienda? -> Cualquier opción es buena, aunque es recomendable empezar con mejoras que te ayuden al largo plazo, como las de mas monedas por enemigo eliminado.\n");
        sb.append("6) ¿Que puedo hacer para subir en el ranking? -> Puedes conseguir puntos del ranking consiguiendo completar niveles, a mayor dificultad mas puntos dará.\n");
        sb.append("7) ¿Cual es el objetivo del juego? -> Salvar al eetac de la invasión de los aliens que quieren destuirlo, para eso deberas armar el centro y acabar con ellos.\n");
        sb.append("8) ¿Que es el reset de leaderboard? -> Es un reset mensual voluntario que devuelve a los jugadores a tener una cuenta nueva a cambio de objetos cosméticos.\n");
        sb.append("9) ¿Que mejoras son las adecuadas para enfrentarse al boss final? -> Debido a su mecanica lo mejor es quitarle la armadura, para ello necesitas dispararle mucho, la mejora de velocidad de disparo es la mejor sin dudas.\n");
        sb.append("10) ¿Como puedo mejorar las torretas? -> Las mejoras se añadiran para la version final en la tienda, de momento ves consiguiendo monedas en los niveles iniciales para poder tener suficiente cuando salga.\n");
        sb.append("Pregunta del usuario: ").append(userQuestion);
        return sb.toString();
    }

    private String extractResponseText(String jsonResponse) {
        if (jsonResponse == null) {
            return null;
        }
        Matcher m = RESPONSE_PATTERN.matcher(jsonResponse);
        if (!m.find()) {
            return null;
        }
        return jsonUnescape(m.group(1));
    }

    private String jsonEscape(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private String jsonUnescape(String text) {
        if (text == null) return null;
        return text
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private String readAll(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}

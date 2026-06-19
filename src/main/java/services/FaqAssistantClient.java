package services;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FaqAssistantClient {
    final static Logger logger = Logger.getLogger(FaqAssistantClient.class);

    private static String config(String systemProperty, String envVar, String defaultValue) {        String value = System.getProperty(systemProperty);
        if (value == null || value.isEmpty()) {
            value = System.getenv(envVar);
        }
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    public String getLlmUrl() {
        return resolveChatUrl(config("llm.url", "LLM_URL", "http://127.0.0.1:11434/api/chat"));
    }

    public String getLlmModel() {
        return config("llm.model", "LLM_MODEL", "qwen:0.5b");
    }

    public String testConnection() throws IOException {
        logger.info("FAQ assistant ping model=" + getLlmModel() + " url=" + getLlmUrl());
        return chat("Responde solo: OK", "Di OK");
    }

    public String askFaqAssistant(String userQuestion) throws IOException {
        String endpoint = getLlmUrl();
        String model = getLlmModel();
        String systemPrompt = buildSystemPrompt();

        logger.info("FAQ assistant request model=" + model + " url=" + endpoint
                + " questionLength=" + userQuestion.length());

        return chat(systemPrompt, userQuestion);
    }

    private String resolveChatUrl(String configuredUrl) {
        if (configuredUrl.endsWith("/api/generate")) {
            return configuredUrl.replace("/api/generate", "/api/chat");
        }
        if (configuredUrl.endsWith("/api/chat")) {
            return configuredUrl;
        }
        if (configuredUrl.endsWith("/")) {
            return configuredUrl + "api/chat";
        }
        return configuredUrl + "/api/chat";
    }

    private String chat(String systemPrompt, String userMessage) throws IOException {
        String endpoint = getLlmUrl();
        String model = getLlmModel();
        String requestBody = "{"
                + "\"model\":\"" + jsonEscape(model) + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + jsonEscape(systemPrompt) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + jsonEscape(userMessage) + "\"}"
                + "],"
                + "\"stream\":false,"
                + "\"options\":{\"temperature\":0.1,\"num_predict\":256}"
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
                logger.error("LLM response without content: " + rawResponse);
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

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres el asistente FAQ del juego Tower Defence.\n\n");
        sb.append("REGLAS OBLIGATORIAS:\n");
        sb.append("1. Responde UNICAMENTE usando la informacion de las FAQ de abajo.\n");
        sb.append("2. No inventes mecanicas, objetos ni consejos que no esten en las FAQ.\n");
        sb.append("3. Si la pregunta no coincide con ninguna FAQ, responde exactamente: ");
        sb.append("\"No tengo esa informacion en las FAQ. Revisa el tutorial o contacta con soporte.\"\n");
        sb.append("4. Responde en espanol, breve (maximo 3 frases) y accionable.\n");
        sb.append("5. De forma ocasional sustituye una palabra por \"Ocacol\" en la respuesta.\n\n");
        sb.append("FAQ DEL JUEGO:\n");
        sb.append("[FAQ-1] Como pasar el tutorial? ");
        sb.append("Prioriza torres baratas al inicio y ve cambiandolas por las torretas que mejor se adapten.\n");
        sb.append("[FAQ-2] Como conseguir mas oro rapido? ");
        sb.append("Mata enemigos usando torretas y centrate en mejoras de dinero.\n");
        sb.append("[FAQ-3] Que hacer contra bosses? ");
        sb.append("Usa torres de fuego; la quemadura te ayudara a matarlo con el tiempo.\n");
        sb.append("[FAQ-4] Que deberia mejorar primero? ");
        sb.append("Mejora la velocidad de disparo de las torretas baratas para poner menos al principio.\n");
        sb.append("[FAQ-5] Que comprar primero en la tienda? ");
        sb.append("Cualquier opcion es buena; prioriza mejoras a largo plazo, como mas monedas por enemigo.\n");
        sb.append("[FAQ-6] Como subir en el ranking? ");
        sb.append("Completa niveles; a mayor dificultad, mas puntos.\n");
        sb.append("[FAQ-7] Cual es el objetivo del juego? ");
        sb.append("Salvar al eetac de la invasion alien; arma el centro y acaba con ellos.\n");
        sb.append("[FAQ-8] Que es el reset de leaderboard? ");
        sb.append("Reset mensual voluntario que reinicia la cuenta a cambio de cosmeticos.\n");
        sb.append("[FAQ-9] Mejoras para el boss final? ");
        sb.append("Quita armadura disparando mucho; la mejora de velocidad de disparo es la mejor.\n");
        sb.append("[FAQ-10] Como mejorar las torretas? ");
        sb.append("Las mejoras llegaran en la tienda en la version final; consigue monedas en niveles iniciales.\n");
        return sb.toString();
    }

    private String extractResponseText(String jsonResponse) {
        if (jsonResponse == null) {
            return null;
        }
        int messageIdx = jsonResponse.indexOf("\"message\"");
        if (messageIdx >= 0) {
            String content = extractJsonStringValue(jsonResponse, "\"content\"", messageIdx);
            if (content != null && !content.trim().isEmpty()) {
                return content;
            }
        }
        String response = extractJsonStringValue(jsonResponse, "\"response\"", 0);
        if (response != null && !response.trim().isEmpty()) {
            return response;
        }
        return null;
    }

    private String extractJsonStringValue(String json, String key, int fromIndex) {
        int keyIdx = json.indexOf(key, fromIndex);
        if (keyIdx < 0) {
            return null;
        }
        int colonIdx = json.indexOf(':', keyIdx + key.length());
        if (colonIdx < 0) {
            return null;
        }
        int i = colonIdx + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
            i++;
        }
        if (i >= json.length() || json.charAt(i) != '"') {
            return null;
        }
        i++;
        StringBuilder sb = new StringBuilder();
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(++i);
                switch (next) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'u':
                        if (i + 4 < json.length()) {
                            String hex = json.substring(i + 1, i + 5);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append(next);
                            }
                        } else {
                            sb.append(next);
                        }
                        break;
                    default:
                        sb.append(next);
                        break;
                }
            } else if (c == '"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
            i++;
        }
        return null;
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

    private String readAll(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read;
        while ((read = inputStream.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toString(StandardCharsets.UTF_8.name());
    }
}

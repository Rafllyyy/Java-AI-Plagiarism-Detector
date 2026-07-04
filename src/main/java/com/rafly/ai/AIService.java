package com.rafly.ai;

import com.rafly.model.HighlightResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AIService {

    private static final String BASE_URL    = "http://localhost:8000";
    private static final int    TIMEOUT_SEC = 120;

    private final HttpClient httpClient;

    public AIService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .build();
    }

    public static class AIResult {
        public final double                overallSimilarity;
        public final List<HighlightResult> matchedSentences;
        public final int                   sentenceCountA;
        public final int                   sentenceCountB;
        public final double                processingTimeMs;

        public AIResult(double overallSimilarity, List<HighlightResult> matchedSentences,
                        int sentenceCountA, int sentenceCountB, double processingTimeMs) {
            this.overallSimilarity = overallSimilarity;
            this.matchedSentences  = matchedSentences;
            this.sentenceCountA    = sentenceCountA;
            this.sentenceCountB    = sentenceCountB;
            this.processingTimeMs  = processingTimeMs;
        }
    }

    public AIResult compare(String documentA, String documentB) throws IOException {
        String body = buildRequestJson(documentA, documentB);
        byte[] bodyBytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/compare"))
                .timeout(Duration.ofSeconds(TIMEOUT_SEC))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Server error: HTTP " + response.statusCode()
                        + "\nBody: " + response.body().substring(0, Math.min(200, response.body().length())));
            }

            return parseResponse(response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> resp =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // ── JSON Builder ──────────────────────────────────────────────────

    private String buildRequestJson(String docA, String docB) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"documentA\":").append(jsonEscape(docA));
        sb.append(",");
        sb.append("\"documentB\":").append(jsonEscape(docB));
        sb.append("}");
        return sb.toString();
    }

    private String jsonEscape(String text) {
        if (text == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    // ── JSON Parser ───────────────────────────────────────────────────

    private AIResult parseResponse(String json) {
        double overall        = extractDouble(json, "overallSimilarity");
        int    countA         = (int) extractDouble(json, "sentenceCountA");
        int    countB         = (int) extractDouble(json, "sentenceCountB");
        double processingTime = extractDouble(json, "processingTimeMs");

        List<HighlightResult> matches = parseMatchedSentences(json);

        return new AIResult(overall, matches, countA, countB, processingTime);
    }

    private List<HighlightResult> parseMatchedSentences(String json) {
        List<HighlightResult> result = new ArrayList<>();

        int arrStart = json.indexOf("\"matchedSentences\"");
        if (arrStart < 0) return result;
        arrStart = json.indexOf("[", arrStart);
        int arrEnd = json.lastIndexOf("]");
        if (arrStart < 0 || arrEnd < 0) return result;

        String array = json.substring(arrStart + 1, arrEnd);

        int depth = 0, start = -1;
        for (int i = 0; i < array.length(); i++) {
            char c = array.charAt(i);
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}') {
                if (--depth == 0 && start >= 0) {
                    String obj   = array.substring(start, i + 1);
                    String sentA = extractString(obj, "sentenceA");
                    String sentB = extractString(obj, "sentenceB");
                    double sim   = extractDouble(obj, "similarity");
                    result.add(new HighlightResult(sentA, sentB, sim / 100.0));
                    start = -1;
                }
            }
        }
        return result;
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return "";
        start += search.length();
        StringBuilder sb = new StringBuilder();
        int i = start;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case 'n':  sb.append('\n'); i += 2; continue;
                    case 'r':  sb.append('\r'); i += 2; continue;
                    case 't':  sb.append('\t'); i += 2; continue;
                    case '"':  sb.append('"');  i += 2; continue;
                    case '\\': sb.append('\\'); i += 2; continue;
                }
            }
            if (c == '"') break;
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private double extractDouble(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) return 0;
        start += search.length();
        // Skip spasi
        while (start < json.length() && json.charAt(start) == ' ') start++;
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (!Character.isDigit(c) && c != '.' && c != '-') break;
            end++;
        }
        try { return Double.parseDouble(json.substring(start, end)); }
        catch (NumberFormatException e) { return 0; }
    }
}
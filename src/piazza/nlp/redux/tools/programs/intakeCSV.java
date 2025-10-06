package piazza.nlp.redux.tools.programs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class intakeCSV {

    public static class QAEntry {
        public final String answer, score, feedback;
        public QAEntry(String a, String s, String f) { this.answer = a; this.score = s; this.feedback = f; }
    }

    public static class QuestionBlock {
        public String questionType; // "MCQ" or "FreeResponse"
        public final Map<String, QAEntry> studentSubmissions = new LinkedHashMap<>();
    }

    static class QCols {
        final String key; final int ans, score, fb;
        QCols(String key, int ans, int score, int fb) { this.key = key; this.ans = ans; this.score = score; this.fb = fb; }
    }

    public static void main(String[] args) throws Exception {
        String path = "/resources/Basic Genetics Quiz (Anonymized).csv";

        // Load all rows
        List<String[]> rows;
        try (InputStream is = intakeCSV.class.getResourceAsStream(path);
             CSVReader reader = new CSVReader(new InputStreamReader(is))) {
            rows = reader.readAll();
        }
        if (rows.isEmpty()) return;

        String[] header = rows.get(0);
        int nCols = header.length;

        // Locate Anonymous ID column
        int anonIdx = -1;
        for (int i = 0; i < nCols; i++) if ("Anonymous ID".equals(header[i])) { anonIdx = i; break; }
        if (anonIdx == -1) throw new IllegalStateException("No 'Anonymous ID' column.");

        // Discover question triplets
        List<QCols> questions = new ArrayList<>();
        String lastBaseQ = null;
        for (int i = 0; i + 2 < nCols; i++) {
            String q = header[i], s = header[i+1], f = header[i+2];
            if (s.equals(q + " [Score]") && f.equals(q + " [Feedback]")) {
                String baseKey = q;
                if (q.startsWith("Explain your reason")) {
                    if (lastBaseQ != null) baseKey = lastBaseQ + " — " + q;
                } else {
                    lastBaseQ = q;
                }
                questions.add(new QCols(baseKey, i, i+1, i+2));
                i += 2;
            }
        }

        // Build: Question -> QuestionBlock (with submissions)
        Map<String, QuestionBlock> byQuestion = new LinkedHashMap<>();
        // Also collect answers per question for type inference
        Map<String, List<String>> answersPerQuestion = new HashMap<>();

        for (int r = 1; r < rows.size(); r++) {
            String[] row = rows.get(r);
            if (row.length == 0) continue;
            String anon = safe(row, anonIdx).trim();

            for (QCols qc : questions) {
                String ans = safe(row, qc.ans).trim();
                String score = safe(row, qc.score).trim();
                String fb = safe(row, qc.fb).trim();

                QuestionBlock qb = byQuestion.computeIfAbsent(qc.key, k -> new QuestionBlock());
                qb.studentSubmissions.put(anon, new QAEntry(ans, score, fb));

                if (!ans.isEmpty()) {
                    answersPerQuestion.computeIfAbsent(qc.key, k -> new ArrayList<>()).add(normalize(ans));
                }
            }
        }

        // Infer questionType
        for (Map.Entry<String, QuestionBlock> e : byQuestion.entrySet()) {
            List<String> answers = answersPerQuestion.getOrDefault(e.getKey(), Collections.emptyList());
            int total = answers.size();
            int unique = new HashSet<>(answers).size();
            e.getValue().questionType = (total > 0 && unique == total) ? "FreeResponse" : "MCQ";
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(byQuestion);
        // write to file
        Files.write(Path.of("file_systems_quiz_output.json"), json.getBytes(StandardCharsets.UTF_8));
    }

    private static String safe(String[] row, int idx) {
        return (idx >= 0 && idx < row.length) ? row[idx] : "";
    }

    // Light normalization so minor spacing/case differences don’t fake uniqueness
    private static String normalize(String s) {
        return s.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
    }
}
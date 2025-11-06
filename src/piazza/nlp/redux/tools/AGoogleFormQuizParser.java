package piazza.nlp.redux.tools;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.exceptions.CsvValidationException;

//TODO: interface?

public class AGoogleFormQuizParser {
	
	/* HELPER CLASSES */
	
    public static class QAEntry {
        public final String answer;
        public final String score;   // keep as string to preserve "--" or partials
        public final String feedback;
        public QAEntry(String a, String s, String f) {
            this.answer = a; this.score = s; this.feedback = f;
        }
    }

    public static class QuestionBlock {
        public String questionType; // "MCQ" or "FreeResponse"
        public Double maxScore;     // lifted to question level
        public boolean extraCredit;
        public final Map<String, QAEntry> studentSubmissions = new LinkedHashMap<>();
    }

    
    
    public JSONObject readQuizGrades(String path, boolean parseIDFromEmail) throws Exception {
		
        // load all rows
        List<String[]> rows = new ArrayList<String[]>();
        try (FileReader reader = new FileReader(path)) { // Use try-with-resources for safety
        	RFC4180Parser rfc4180Parser = new RFC4180Parser();
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(rfc4180Parser) // Attach the powerful parser
                    .build();
            rows = csvReader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (rows.isEmpty()) return new JSONObject();

        String[] header = rows.get(0);
        int nCols = header.length;

        // if parseIDFromEmail, use email column as identifier (and later extract onyen), otherwise use onyen column as identifier
        int idIdx = -1;
    	for (int i = 0; i < nCols; i++) {
        	if ((!parseIDFromEmail && header[i].toLowerCase().equals("onyen")) || (parseIDFromEmail && header[i].toLowerCase().equals("username"))) {
        		idIdx = i;
        		break;
        	}
    	}
        if (idIdx == -1) throw new IllegalStateException("No ID column found.");     

        // Discover question triplets
        List<QCols> questions = new ArrayList<>();
        String lastBaseQ = null;
        for (int i = 0; i + 2 < nCols; i++) {
            String q = header[i], s = header[i+1], f = header[i+2];
        
            // skip the non-graded questions
            if (q.toLowerCase().equals("onyen") || q.toLowerCase().equals("anonymous id"))
            	continue;
            
            // parse the graded questions
            if (s.equals(q + " [Score]") && f.equals(q + " [Feedback]")) {
                String baseKey = q;
                if (q.startsWith("Explain your") || q.startsWith("Justify your")) {
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
            
            String user;
            if (parseIDFromEmail)
            	user = safe(row, idIdx).split("@")[0].trim();
        	else
        		user = safe(row, idIdx).trim();

            for (QCols qc : questions) {
                String ans = safe(row, qc.ans).trim();
                String rawScore = safe(row, qc.score).trim();
                String fb = safe(row, qc.fb).trim();

                // Ensure QuestionBlock exists
                QuestionBlock qb = byQuestion.computeIfAbsent(qc.key, k -> new QuestionBlock());

                // Parse "score / maxScore"
                ParsedScore ps = parseScore(rawScore);
                // Set/lock maxScore at the question level if present
                if (ps.maxScore != null) {
                    if (qb.maxScore == null) {
                        qb.maxScore = ps.maxScore;
                    } else {
                        // If a different maxScore appears later, choose what to do:
                        // Option A (current): keep the first seen; ignore mismatches
                        // Option B: pick the maximum: qb.maxScore = Math.max(qb.maxScore, ps.maxScore);
                        // Option C: throw to surface data issues
                        // if (!qb.maxScore.equals(ps.maxScore)) {
                        //     throw new IllegalStateException("Conflicting maxScore for question: " + qc.key);
                        // }
                    }
                }

                // Store student's visible score (left side), preserving "--" if ungraded
                qb.studentSubmissions.put(user, new QAEntry(ans, ps.leftScore, fb));

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
        return new JSONObject(json);
 
	}
    
    
    
    public JSONObject readInstructorSolutions(String path) throws Exception {
    	
    	// This is the main JSON object that will hold all key-value pairs
        JSONObject questionsAndSolutions = new JSONObject();

        // 1. Instantiate the RFC4180Parser as requested
        RFC4180Parser rfc4180Parser = new RFC4180Parser();

        // 2. Use try-with-resources to automatically close the readers
        try (
            FileReader fileReader = new FileReader(path);
            
            // 3. Build the CSVReader using CSVReaderBuilder to inject the RFC4180Parser
            CSVReader csvReader = new CSVReaderBuilder(fileReader)
                                    .withCSVParser(rfc4180Parser)
                                    .build()
        ) {
            
            // 4. Skip the header row ("Question", "Solution")
            csvReader.readNext();

            // 5. Read all remaining lines one by one
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                
                // Ensure the line has at least two columns
                if (nextLine.length >= 2) {
                    String question = nextLine[0];
                    String solution = nextLine[1];
                    
                    // 6. Add the question and solution to the JSONObject
                    questionsAndSolutions.put(question, solution);
                }
            }
        }

        return questionsAndSolutions;
    	
    }
    
    
    
    
    
    /* HELPER METHODS */
    
    static class QCols {
        final String key; final int ans, score, fb;
        QCols(String key, int ans, int score, int fb) { this.key = key; this.ans = ans; this.score = score; this.fb = fb; }
    }
	
    private static String safe(String[] row, int idx) {
        return (idx >= 0 && idx < row.length) ? row[idx] : "";
    }

    // Light normalization so minor spacing/case differences don’t fake uniqueness
    private static String normalize(String s) {
        return s.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
    }

    // ---------- score parsing helpers ----------

    private static class ParsedScore {
        final String leftScore; // as shown to the grader (e.g., "--" or "3")
        final Double maxScore;  // numeric, lifted to question level
        ParsedScore(String leftScore, Double maxScore) {
            this.leftScore = leftScore;
            this.maxScore = maxScore;
        }
    }

    /**
     * Accepts formats like:
     *   "-- / 5", "3/5", " 4  / 10 ", " / 5", "3 / "
     * Returns:
     *   leftScore: left side trimmed (may be empty or "--")
     *   maxScore: right side parsed as Double (or null if missing/unparseable)
     */
    private static ParsedScore parseScore(String raw) {
        if (raw == null) return new ParsedScore("", null);
        String s = raw.trim();
        int slash = s.indexOf('/');
        if (slash < 0) {
            // No slash -> keep entire thing as left score, no maxScore
            return new ParsedScore(s, null);
        }
        String left = s.substring(0, slash).trim();
        String right = s.substring(slash + 1).trim();

        Double max = tryParseDouble(right);
        return new ParsedScore(left, max);
    }

    private static Double tryParseDouble(String x) {
        if (x == null || x.isEmpty()) return null;
        try {
            return Double.parseDouble(x);
        } catch (NumberFormatException e) {
            return null;
        }
    }
	

}

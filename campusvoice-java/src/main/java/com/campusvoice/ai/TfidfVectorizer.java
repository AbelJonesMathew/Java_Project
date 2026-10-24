package com.campusvoice.ai;

import java.io.Serializable;
import java.util.*;

public class TfidfVectorizer implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are",
        "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
        "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from", "further",
        "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "him", "himself", "his",
        "how", "i", "if", "in", "into", "is", "it", "its", "itself", "me", "more", "most", "my", "myself",
        "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours",
        "ourselves", "out", "over", "own", "same", "she", "should", "so", "some", "such", "than", "that",
        "the", "their", "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those",
        "through", "to", "too", "under", "until", "up", "very", "was", "we", "were", "what", "when",
        "where", "which", "while", "who", "whom", "why", "with", "would", "you", "your", "yours", "yourself"
    ));

    private Map<String, Integer> vocabulary = new HashMap<>();
    private double[] idf;
    private int numDocs = 0;

    public void fit(List<String> documents) {
        vocabulary.clear();
        numDocs = documents.size();
        Map<String, Integer> docFreq = new HashMap<>();

        for (String doc : documents) {
            Set<String> uniqueTermsInDoc = new HashSet<>(extractNGrams(doc));
            for (String term : uniqueTermsInDoc) {
                docFreq.put(term, docFreq.getOrDefault(term, 0) + 1);
            }
        }

        // Build indexed vocabulary
        int idx = 0;
        List<String> sortedTerms = new ArrayList<>(docFreq.keySet());
        Collections.sort(sortedTerms);

        for (String term : sortedTerms) {
            vocabulary.put(term, idx++);
        }

        // Compute scikit-learn standard smooth IDF: ln((1 + N) / (1 + df)) + 1
        idf = new double[vocabulary.size()];
        for (Map.Entry<String, Integer> entry : vocabulary.entrySet()) {
            String term = entry.getKey();
            int termIdx = entry.getValue();
            int df = docFreq.getOrDefault(term, 0);
            idf[termIdx] = Math.log((1.0 + numDocs) / (1.0 + df)) + 1.0;
        }
    }

    public double[] transform(String document) {
        if (vocabulary.isEmpty() || idf == null) {
            return new double[0];
        }

        double[] vec = new double[vocabulary.size()];
        List<String> terms = extractNGrams(document);

        for (String term : terms) {
            Integer termIdx = vocabulary.get(term);
            if (termIdx != null) {
                vec[termIdx] += 1.0; // Raw Term Frequency
            }
        }

        // Apply IDF weights
        double sumSq = 0.0;
        for (int i = 0; i < vec.length; i++) {
            vec[i] *= idf[i];
            sumSq += vec[i] * vec[i];
        }

        // L2 Normalization
        if (sumSq > 0.0) {
            double norm = Math.sqrt(sumSq);
            for (int i = 0; i < vec.length; i++) {
                vec[i] /= norm;
            }
        }

        return vec;
    }

    private List<String> extractNGrams(String text) {
        List<String> ngrams = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return ngrams;
        }

        // Clean and tokenize words
        String clean = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] rawTokens = clean.split("\\s+");

        List<String> tokens = new ArrayList<>();
        for (String token : rawTokens) {
            if (token.length() >= 2 && !STOP_WORDS.contains(token)) {
                tokens.add(token);
            }
        }

        // 1. Unigrams
        ngrams.addAll(tokens);

        // 2. Bigrams
        for (int i = 0; i < tokens.size() - 1; i++) {
            ngrams.add(tokens.get(i) + " " + tokens.get(i + 1));
        }

        return ngrams;
    }

    public int getVocabularySize() {
        return vocabulary.size();
    }

    public Map<String, Integer> getVocabulary() {
        return Collections.unmodifiableMap(vocabulary);
    }
}

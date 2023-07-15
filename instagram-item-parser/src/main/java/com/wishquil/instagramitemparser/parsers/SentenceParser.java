package com.wishquil.instagramitemparser.parsers;

public final class SentenceParser {

    private SentenceParser() {
    }

    public static String parseFirstSentence(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String firstSentence = "";
        int endIndex = text.length();

        // Search for sentence ending punctuation marks: . ? !
        endIndex = getIndexOfEndPunctuation(text, endIndex);

        // Extract the first sentence
        if (endIndex > 0) {
            firstSentence = text.substring(0, endIndex).trim();
        }

        return firstSentence;
    }

    private static int getIndexOfEndPunctuation(String text, int startIndex) {
        for (int i = 0; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            if (currentChar == '.' || currentChar == '?' || currentChar == '!' || currentChar == '\n') {
                startIndex = i + 1;
                break;
            }
        }
        return startIndex;
    }

}

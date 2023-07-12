package com.wishquil.instagramitemparser.util;

public class SentenceParser {
    public static String getFirstSentence(String text) {
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

    public static String getAllSentencesExceptFirst(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String allSentencesExceptFirst = "";
        int startIndex = 0;

        // Search for sentence ending punctuation marks: . ? !
        startIndex = getIndexOfEndPunctuation(text, startIndex);

        if (startIndex < text.length()) {
            allSentencesExceptFirst = text.substring(startIndex);
        }
        return allSentencesExceptFirst;
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

    public static void main(String[] args) {
        String text = "Hello! How are you? I hope you're doing well.";
        String firstSentence = getFirstSentence(text);
        System.out.println("First sentence: " + firstSentence);
    }
}

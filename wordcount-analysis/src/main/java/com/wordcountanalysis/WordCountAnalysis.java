package com.wordcountanalysis;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordCountAnalysis {

    public static void main(String[] args) throws Exception {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a PDF or DOCX file");
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();

            String content = "";
            if (filePath.endsWith(".pdf")) {
                content = extractTextFromPDF(filePath);
            } else if (filePath.endsWith(".docx")) {
                content = extractTextFromDOCX(filePath);
            } else {
                JOptionPane.showMessageDialog(null, "Unsupported file format. Please select a PDF or DOCX file.");
                return;
            }

            performWordAnalysis(content);
        } else {
            JOptionPane.showMessageDialog(null, "No file selected.");
        }
    }

    
    private static String extractTextFromPDF(String filePath) throws Exception {
        PDDocument document = PDDocument.load(new File(filePath));
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(document);
        document.close();
        return text;
    }

   
    private static String extractTextFromDOCX(String filePath) throws Exception {
        FileInputStream fis = new FileInputStream(new File(filePath));
        XWPFDocument document = new XWPFDocument(fis);
        StringBuilder content = new StringBuilder();
        document.getParagraphs().forEach(p -> content.append(p.getText()).append(" "));
        document.close();
        return content.toString();
    }

    private static void performWordAnalysis(String content) {
        StringTokenizer tokenizer = new StringTokenizer(content, " \t\n\r\f,.:;?![]'");
        Map<String, Integer> wordCount = new HashMap<>();
        int totalWords = 0, totalLength = 0, totalSyllables = 0, totalSentences = 0, totalParagraphs = 0;
        String mostFrequentWord = "";
        int maxFrequency = 0;

        String longestWord = "";
        String shortestWord = null;
        boolean hasInteger = false;

       
        Pattern sentencePattern = Pattern.compile("[.!?]+");
        Matcher sentenceMatcher = sentencePattern.matcher(content);
        while (sentenceMatcher.find()) totalSentences++;

        Pattern paragraphPattern = Pattern.compile("(?m)^.*[a-zA-Z]+.*$");
        Matcher paragraphMatcher = paragraphPattern.matcher(content);
        while (paragraphMatcher.find()) totalParagraphs++;

        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken().toLowerCase();
            totalWords++;
            totalLength += word.length();
            totalSyllables += countSyllables(word);

            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            if (wordCount.get(word) > maxFrequency) {
                mostFrequentWord = word;
                maxFrequency = wordCount.get(word);
            }

            
            if (word.length() > longestWord.length()) longestWord = word;
            if (shortestWord == null || word.length() < shortestWord.length()) shortestWord = word;

            if (word.matches("\\d+")) hasInteger = true;
        }

        double avgWordLength = (double) totalLength / totalWords;
        double avgSentenceLength = totalWords / (double) totalSentences;
        double avgSyllablesPerWord = (double) totalSyllables / totalWords;

       
        JFrame frame = new JFrame("Word Count Analysis Results");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 0, 5, 0);

        gbc.gridy = 0;
        panel.add(new JLabel("Total Words: " + totalWords), gbc);
        gbc.gridy++;
        panel.add(new JLabel("Unique Words: " + wordCount.size()), gbc);
        gbc.gridy++;
        panel.add(new JLabel("Average Word Length: " + String.format("%.2f", avgWordLength)), gbc);
        gbc.gridy++;
        panel.add(new JLabel("Total Sentences: " + totalSentences), gbc);
        gbc.gridy++;
        panel.add(new JLabel("Average Sentence Length (in words): " + String.format("%.2f", avgSentenceLength)), gbc);
        gbc.gridy++;
        panel.add(new JLabel("Total Paragraphs: " + totalParagraphs), gbc);
        gbc.gridy++;
        panel.add(new JLabel("Average Syllables Per Word: " + String.format("%.2f", avgSyllablesPerWord)), gbc);
        gbc.gridy++;
        panel.add(new JLabel("Most Frequent Word: '" + mostFrequentWord + "' occurred " + maxFrequency + " times"), gbc);
        gbc.gridy++;
        panel.add(new JLabel("Longest Word: " + longestWord), gbc);
        gbc.gridy++;
        panel.add(new JLabel("Shortest Word: " + shortestWord), gbc);
        gbc.gridy++;
        panel.add(new JLabel("Contains Integer Value: " + (hasInteger ? "Yes" : "No")), gbc);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    
    private static int countSyllables(String word) {
        String vowels = "aeiouy";
        int syllableCount = 0;
        boolean lastWasVowel = false;

        for (char wc : word.toCharArray()) {
            boolean isVowel = vowels.indexOf(wc) != -1;
            if (isVowel && !lastWasVowel) syllableCount++;
            lastWasVowel = isVowel;
        }

        if (word.endsWith("e")) syllableCount = Math.max(syllableCount - 1, 1);
        return syllableCount;
    }
}


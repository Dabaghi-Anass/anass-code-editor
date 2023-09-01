package com.anass.anass_code_editor;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.awt.Color;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {

    private CodeArea codeArea;
    private String language;
    private Map<String, Color> themeColors;
    private HashMap<String, String[]> languageKeywords;
    private final Map<String, Integer> groupNames = new HashMap<>();
    Pattern PATTERN;
    CodeCompleter completer;
    String VARIABLE_PATTERN;
    String METHODS_PATTERN;
    SyntaxHighlighter(CodeArea codeArea,CodeCompleter comp, String language) {
        completer = comp;
        this.codeArea = codeArea;
        this.languageKeywords = new HashMap<>();
        JsonReader<LanguageKeywords> reader = new JsonReader<>("assets/keywords.json", LanguageKeywords.class);
        List<LanguageKeywords> languages = reader.getData();
        if (languages == null) {
            System.out.println("try again");
            return;
        }
        for (LanguageKeywords l : languages) {
            languageKeywords.put(l.name.toLowerCase(), l.keywords);
        }
        this.language = language.toLowerCase().trim();
        String[] keys;
        keys = languageKeywords.get(language);
        if (keys == null) keys = new String[]{"true","false","null","undefined"};
        getPattern(keys);
        highlight();
    }
    public void getPattern(String[] keys){
        String KEYWORD_PATTERN = "\\b(" + String.join("|", keys) + ")\\b";
        groupNames.put("KEYWORD", 1);
        groupNames.put("PAREN", 2);
        groupNames.put("BRACE", 3);
        groupNames.put("BRACKET",4);
        groupNames.put("STRING", 5);
        groupNames.put("COMMENT", 6);
        groupNames.put("SYMBOL", 7);
        groupNames.put("NUMBER", 8);
        groupNames.put("VARIABLE", 9);
        groupNames.put("METHOD", 10);
        groupNames.put("DEFAULT", 11);
        String PAREN_PATTERN = "\\(|\\)";
        VARIABLE_PATTERN = "\\b("+String.join("|",completer.getVariables())+")\\b";
        METHODS_PATTERN = "\\b("+String.join("|",completer.getFunctions())+")\\b";
        String BRACE_PATTERN = "\\{|\\}";
        String BRACKET_PATTERN = "\\[|\\]";
        String STRING_PATTERN= "(\\\"(?:[^\\\"]|\\\"\\\")*\\\"|\\'(?:[^\\']|\\'\\')*\\'|\\`(?:[^\\`]|\\'\\')*\\`)";
        String NUMBER_PATTERN = "(?<![\\w.])(?:0x[\\da-fA-F]+|[-+]?(?:(?:\\d*\\.\\d+)|(?:\\d+\\.\\d*)|(?:\\d+))(?:[eE][-+]?\\d+)?)(?![\\w.])";
        String COMMENT_PATTERN = "//[^\n]*|/\\*(.|\\R)*?\\*/";
        String SYMBOL_PATTERN = "[=\\-+.*/<>,%;!?&|:^,$]";
        String DEFAULT_PATTERN = ".";
        String ANNOTATION_PATTERN = "@\\w+";
        String DIRECTIVE_PATTERN = "#\\w+";
        PATTERN = Pattern.compile(
                "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                        + "|(?<PAREN>" + PAREN_PATTERN + ")"
                        + "|(?<BRACE>" + BRACE_PATTERN + ")"
                        + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                        + "|(?<STRING>" + STRING_PATTERN + ")"
                        + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                        + "|(?<SYMBOL>" + SYMBOL_PATTERN + ")"
                        + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                        + "|(?<VARIABLE>" + VARIABLE_PATTERN + ")"
                        + "|(?<METHOD>" + METHODS_PATTERN + ")"
                        + "|(?<ANNOTATION>" + ANNOTATION_PATTERN + ")"
                        + "|(?<DIRECTIVE>" + DIRECTIVE_PATTERN + ")"
                        + "|(?<DEFAULT>" + DEFAULT_PATTERN + ")"
        );
        if(VARIABLE_PATTERN.equals("\\b()\\b") && METHODS_PATTERN.equals("\\b()\\b")){
            groupNames.clear();
            groupNames.put("KEYWORD", 1);
            groupNames.put("PAREN", 2);
            groupNames.put("BRACE", 3);
            groupNames.put("BRACKET",4);
            groupNames.put("STRING", 5);
            groupNames.put("COMMENT", 6);
            groupNames.put("SYMBOL", 7);
            groupNames.put("NUMBER", 8);
            groupNames.put("DEFAULT", 9);
            PATTERN = Pattern.compile(
                    "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                            + "|(?<PAREN>" + PAREN_PATTERN + ")"
                            + "|(?<BRACE>" + BRACE_PATTERN + ")"
                            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                            + "|(?<STRING>" + STRING_PATTERN + ")"
                            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                            + "|(?<SYMBOL>" + SYMBOL_PATTERN + ")"
                            + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                            + "|(?<ANNOTATION>" + ANNOTATION_PATTERN + ")"
                            + "|(?<DIRECTIVE>" + DIRECTIVE_PATTERN + ")"
                            + "|(?<DEFAULT>" + DEFAULT_PATTERN + ")"
            );
        }
        else if(VARIABLE_PATTERN.equals("\\b()\\b")){
            groupNames.clear();
            groupNames.put("KEYWORD", 1);
            groupNames.put("PAREN", 2);
            groupNames.put("BRACE", 3);
            groupNames.put("BRACKET",4);
            groupNames.put("STRING", 5);
            groupNames.put("COMMENT", 6);
            groupNames.put("SYMBOL", 7);
            groupNames.put("NUMBER", 8);
            groupNames.put("METHOD", 9);
            groupNames.put("DEFAULT", 10);
            PATTERN = Pattern.compile(
                    "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                            + "|(?<PAREN>" + PAREN_PATTERN + ")"
                            + "|(?<BRACE>" + BRACE_PATTERN + ")"
                            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                            + "|(?<STRING>" + STRING_PATTERN + ")"
                            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                            + "|(?<SYMBOL>" + SYMBOL_PATTERN + ")"
                            + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                            + "|(?<ANNOTATION>" + ANNOTATION_PATTERN + ")"
                            + "|(?<DIRECTIVE>" + DIRECTIVE_PATTERN + ")"
                            + "|(?<METHOD>" + METHODS_PATTERN + ")"
                            + "|(?<DEFAULT>" + DEFAULT_PATTERN + ")"
            );
        }
        else if(METHODS_PATTERN.equals("\\b()\\b")){
            groupNames.clear();
            groupNames.put("KEYWORD", 1);
            groupNames.put("PAREN", 2);
            groupNames.put("BRACE", 3);
            groupNames.put("BRACKET",4);
            groupNames.put("STRING", 5);
            groupNames.put("COMMENT", 6);
            groupNames.put("SYMBOL", 7);
            groupNames.put("NUMBER", 8);
            groupNames.put("VARIABLE", 9);
            groupNames.put("DEFAULT", 10);
            PATTERN = Pattern.compile(
                    "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                            + "|(?<PAREN>" + PAREN_PATTERN + ")"
                            + "|(?<BRACE>" + BRACE_PATTERN + ")"
                            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                            + "|(?<STRING>" + STRING_PATTERN + ")"
                            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                            + "|(?<SYMBOL>" + SYMBOL_PATTERN + ")"
                            + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                            + "|(?<ANNOTATION>" + ANNOTATION_PATTERN + ")"
                            + "|(?<DIRECTIVE>" + DIRECTIVE_PATTERN + ")"
                            + "|(?<VARIABLE>" + VARIABLE_PATTERN + ")"
                            + "|(?<DEFAULT>" + DEFAULT_PATTERN + ")"
            );
        }

    }
    public void highlight() {
        try {
            if (languageKeywords == null) {
                System.out.println("failed to load json");
                return;
            }
            String[] keywords = languageKeywords.get(language);
            if (keywords == null) {
                keywords = new String[]{"true","false","undefined","null"};
            }
            getPattern(keywords);
            String text = codeArea.getText();
            Matcher matcher = PATTERN.matcher(text);
            String th = App.getSettings().theme.replace(" ","_").toLowerCase();
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            int lastEnd = 0;
            while (matcher.find()) {
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastEnd);
                String className = "default";
                if (matcher.group("KEYWORD") != null) {
                   className = "keyword";
                } else if (matcher.group("PAREN") != null) {
                    className = "paren";
                } else if (matcher.group("BRACE") != null) {
                    className = "brace";
                } else if (matcher.group("BRACKET") != null) {
                    className = "bracket";
                } else if (matcher.group("STRING") != null) {
                    className = "string";
                } else if (matcher.group("COMMENT") != null) {
                    className = "comment";
                } else if (matcher.group("NUMBER") != null) {
                    className = "number";
                }else if (matcher.group("SYMBOL") != null) {
                    className = "symbol";
                }
                else if (matcher.group("ANNOTATION") != null) {
                    className = "annotation";
                } else if (matcher.group("DIRECTIVE") != null) {
                    className = "directive";
                }
                else{
                    className = "default";
                }
                if(!VARIABLE_PATTERN.equals("\\b()\\b") && !METHODS_PATTERN.equals("\\b()\\b")){
                    if (matcher.group("VARIABLE") != null) {
                        if(languageKeywords.get(language) != null){
                            className = "variable";
                        }
                    }
                    if (matcher.group("METHOD") != null) {
                        if(languageKeywords.get(language) != null){
                            className = "method";
                        }
                    }
                }
                if(!VARIABLE_PATTERN.equals("\\b()\\b")){
                    if (matcher.group("VARIABLE") != null) {
                        if(languageKeywords.get(language) != null){
                            className = "variable";
                        }
                    }
                }
                if(!METHODS_PATTERN.equals("\\b()\\b")){
                    if (matcher.group("METHOD") != null) {
                        if(languageKeywords.get(language) != null){
                            className = "method";
                        }
                    }
                }

                spansBuilder.add(Collections.singleton(th+"_"+className), matcher.end() - matcher.start());
                lastEnd = matcher.end();
            }
            spansBuilder.add(Collections.emptyList(), text.length() - lastEnd);
            StyleSpans<Collection<String>> styleSpans = spansBuilder.create();
            codeArea.setStyleSpans(0, styleSpans);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

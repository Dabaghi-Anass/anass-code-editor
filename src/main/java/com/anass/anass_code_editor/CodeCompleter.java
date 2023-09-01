package com.anass.anass_code_editor;

import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.fxmisc.richtext.Caret;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeCompleter {
    String language;
    CodeArea codeArea;
    HashMap<String,String> patterns = new HashMap<>();
    HashMap<String,String[]> languages = new HashMap<>();
    CodeCompleter(CodeArea ca,String fileName){
        codeArea = ca;
        language = App.getLanguageNameOfExtension(App.getExtension(fileName));
        JsonReader<LanguageDeclaration> reader =  new JsonReader<>("assets/intellisense.json", LanguageDeclaration.class);
        JsonReader<LanguageKeywords> keysReader =  new JsonReader<>("assets/keywords.json", LanguageKeywords.class);
        if(reader == null || keysReader == null || reader.getData() == null || keysReader.getData() == null) return;
        for (LanguageDeclaration ld : reader.getData()){
            patterns.put(ld.language.toLowerCase(),ld.variablePattern+"&_&"+ld.functionPattern);
        }
        for (LanguageKeywords ld : keysReader.getData()){
            languages.put(ld.name.toLowerCase(),ld.keywords);
        }
    }
    ListView<String> suggestionListView;
    public void completeText(){
        if(!App.isIntellisense())
            return;
        int firstVisibleLine = codeArea.firstVisibleParToAllParIndex();
        double lineHeight = (double) App.getSettings().fontSize;
            double caretX = (codeArea.getCaretColumn() + 6) * App.getSettings().fontSize * 9/16;
        double caretY = (codeArea.getCurrentParagraph() - firstVisibleLine) * (lineHeight);
        if (suggestionListView != null) {
            App.getCodeAreaStackPane().getChildren().remove(suggestionListView);
        }
        suggestionListView = new ListView<>();
        suggestionListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    private final ImageView imageView = new ImageView();
                    File varF;
                    {
                        try {
                            varF = new File(getClass().getResource("assets/var.png").toURI());
                        } catch (URISyntaxException e) {
                            System.out.println(e.getMessage());
                            throw new RuntimeException(e);

                        }
                    }

                    File methF;
                    {
                        try {
                            methF = new File(getClass().getResource("assets/method.png").toURI());
                        } catch (URISyntaxException e) {
                            System.out.println(e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }

                    File keyF;

                    {
                        try {
                            keyF = new File(getClass().getResource("assets/key.png").toURI());
                        } catch (URISyntaxException e) {
                            System.out.println(e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }

                    String varUrl;
                    {
                        try {
                            varUrl = varF.toURI().toURL().toExternalForm();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    String methUrl;
                    String keyUrl;

                    {
                        try {
                            methUrl = methF.toURI().toURL().toExternalForm();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    {
                        try {
                            keyUrl = keyF.toURI().toURL().toExternalForm();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        String th = App.getSettings().theme.replace(" ","_").toLowerCase()+"_";
                        if (item == null || empty) {
                            setText(null);
                            setGraphic(null);
                            setVisible(false);
                            getStyleClass().add(th+"list_view_cell");
                        } else {
                            Image icon;
                            if (item.startsWith("k-")) {
                                icon = new Image(keyUrl);
                                setText(item.replace("k-", ""));
                            }
                            else if (item.startsWith("v-")) {
                                icon = new Image(varUrl);
                                setText(item.replace("v-", ""));
                            } else {
                                icon = new Image(methUrl);
                                setText(item.replace("m-", ""));
                                getStyleClass().add(th+"list_view_method_cell");
                            }
                            imageView.setImage(icon);
                            imageView.setFitWidth(16);
                            imageView.setFitHeight(16);
                            setGraphic(imageView);
                            getStyleClass().add(th+"list_view_cell");
                        }
                    }
                };
            }
        });

        ArrayList<String> vars = getVariables();
        ArrayList<String> methods = getFunctions();
        int currentParagraph = Math.max(0,codeArea.getCurrentParagraph());
        String currentLineText = codeArea.getParagraph(currentParagraph).getText();
        int cursorPos = Math.min(codeArea.getCaretColumn()+1,currentLineText.length());
        String[] words = currentLineText.substring(0,cursorPos).split(" ");
        if(words.length == 0)return;
        String text = words[words.length -1];
        ArrayList<String> sugg = new ArrayList<>();
        if (!text.trim().isEmpty()) {
            for (String var : vars) {
                String varName = "v-" + var.trim();
                if (var.toLowerCase().startsWith(text.trim().toLowerCase())) {
                     Optional<String> exist =sugg.stream().filter(e -> e.equals(varName)).findAny();
                    if(exist.isEmpty()){
                        sugg.add(varName);
                    }
                }
                if(var.toLowerCase().equals(text.toLowerCase())){
                    sugg.remove(varName);
                }
            }
            for (String var : methods) {
                String varName = "m-" + var.trim();
                if (var.toLowerCase().startsWith(text.trim().toLowerCase())) {
                    Optional<String> exist =sugg.stream().filter(e -> e.equals(varName)).findAny();
                    if(exist.isEmpty()){
                        sugg.add(varName);
                    }
                }
                if(var.toLowerCase().equals(text.toLowerCase())){
                    sugg.remove(varName);
                }
            }
            if(languages.get(language) != null && languages.get(language).length > 0){
                String[] keywords = languages.get(language);
                for (String var : keywords) {
                    String varName = "k-" + var.trim();
                    if (var.toLowerCase().startsWith(text.trim().toLowerCase())) {
                        Optional<String> exist =sugg.stream().filter(e -> e.equals(varName)).findAny();
                        if(exist.isEmpty()){
                            sugg.add(varName);
                        }
                    }
                    if(var.toLowerCase().equals(text.toLowerCase())){
                        sugg.remove(varName);
                    }
                }
            }
        }
        suggestionListView.getItems().addAll(sugg);
        suggestionListView.getStyleClass().add("suggestions_list");
        suggestionListView.setMaxSize(250, 250);
        suggestionListView.setVisible(true);
        codeArea.setOnMouseClicked(e -> {
            if(App.getCodeAreaStackPane().getChildren().size()>1){
                App.getCodeAreaStackPane().getChildren().remove(suggestionListView);
            }
        });
        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (e.getCode().equals(KeyCode.ENTER)) {
                    if(App.getCodeAreaStackPane().getChildren().size()>1){
                        e.consume();
                        if(words.length >= 0){
                            suggestionListView.requestFocus();
                            if(suggestionListView.getItems().size()>0){
                            String selected = suggestionListView.getItems().get(0);
                                if(selected == null) return;
                                selected = selected.replace("k-","").replace("v-","").replace("m-","");
                                codeArea.insertText(codeArea.getCaretPosition(),selected);
                                App.getHighlighter().highlight();
                            }
                        }
                        codeArea.requestFocus();
                        codeArea.setShowCaret(Caret.CaretVisibility.ON);
                        App.getCodeAreaStackPane().getChildren().remove(suggestionListView);
                    }
                }
                else if(e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.UP){
                    if(App.getCodeAreaStackPane().getChildren().size()>1){
                        e.consume();
                        suggestionListView.requestFocus();
                    }
                }else{
                    codeArea.requestFocus();
                }
            }
        });
        suggestionListView.setOnMouseClicked(e ->{
            String selected = suggestionListView.getSelectionModel().getSelectedItem();
            selected = selected.replace(text,"").replace("k-","").replace("v-","").replace("m-","");
            codeArea.insertText(codeArea.getCaretPosition(),selected);
            App.getHighlighter().highlight();
            codeArea.requestFocus();
            codeArea.setShowCaret(Caret.CaretVisibility.ON);
            App.getCodeAreaStackPane().getChildren().remove(suggestionListView);

        });
        suggestionListView.setOnKeyPressed((e)->{
            if(e.getCode() == KeyCode.ENTER){
                e.consume();
                String selected = suggestionListView.getSelectionModel().getSelectedItem();
                selected = selected.replace(text,"").replace("k-","").replace("v-","").replace("m-","");
                codeArea.insertText(codeArea.getCaretPosition(),selected);
                App.getHighlighter().highlight();
                codeArea.requestFocus();
                codeArea.setShowCaret(Caret.CaretVisibility.ON);
                App.getCodeAreaStackPane().getChildren().remove(suggestionListView);
            }
            else if(e.getCode()!=KeyCode.DOWN&&e.getCode()!=KeyCode.UP){
                codeArea.requestFocus();
            }
        });
        if(sugg.size() > 0){
            App.getCodeAreaStackPane().getChildren().add(suggestionListView);
            double si = suggestionListView.getItems().size() * 24;
            suggestionListView.setMaxHeight(si);
            double suggestionX = codeArea.getLayoutX() + caretX;
            double suggestionY = codeArea.getLayoutY() + caretY;

            suggestionListView.setTranslateX(suggestionX);
            suggestionListView.setTranslateY(suggestionY);
        }

    }
    public ArrayList<String> getVariables(){
        ArrayList<String> vars = new ArrayList<>();
        if(patterns.get(language) == null) return vars;
        Pattern pattern = Pattern.compile(patterns.get(language).split("&_&")[0]);
        Matcher matcher = pattern.matcher(codeArea.getText());
        while(matcher.find()){
            String variable = codeArea.getText().substring(matcher.start(), matcher.end()).split("=")[0].trim();
            variable = variable.split(":")[0].trim();
            int l = variable.split(" ").length;
            variable = variable.split(" ")[l-1].trim();
            String finalVariable = variable;
            if(vars.stream().noneMatch(t -> t.equals(finalVariable)))
                vars.add(variable.trim());
        }
        return vars;
    }
    public ArrayList<String> getFunctions(){
        ArrayList<String> vars = new ArrayList<>();
        if(patterns.get(language) == null) return vars;
        Pattern pattern = Pattern.compile(patterns.get(language).split("&_&")[1]);
        Matcher matcher = pattern.matcher(codeArea.getText());
        while(matcher.find()){
            String variable = codeArea.getText().substring(matcher.start(), matcher.end());
            String[] keywords = new String[]{};
            if(languages.get(language) != null){
                keywords = languages.get(language);
            }
            if(keywords == null) return vars;
            for (String key : keywords){
                for (String k : variable.split(" ")){
                    if(key.trim().equals(k.trim())) variable = variable.replaceFirst(k,"");
                }
            }
            variable = variable.split("\\(")[0].trim();
            int l = variable.split(" ").length;
            variable = variable.split(" ")[l-1].trim();
            String finalVariable = variable;
            if(vars.stream().noneMatch(t -> t.equals(finalVariable)))
                vars.add(variable.trim());
        }
        return vars;
    }
}

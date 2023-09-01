package com.anass.anass_code_editor;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.fxmisc.richtext.CodeArea;

public class MainController implements Initializable  {

    private double xOffset = 0;
    private double yOffset = 0;
    @FXML
    HBox codeAreaHolder,tabsPane;
    CodeArea codeArea;
    @FXML
    Label fileLanguagePlaceHolder,currentLinePlaceHolder,fileSavedPlaceHolder,encodingPlaceHolder;
    @FXML
    VBox displayPane,terminalContainer;
    @FXML
    ImageView settingsIcon;
    @FXML
    BorderPane codePane;
    @FXML
    SplitPane upSplitPane,downSplitPane;

    @FXML
    AnchorPane containerPane;
    @FXML
    ScrollPane promptsContainer;
    @FXML
    MenuBar appMenuBar;
    @FXML
    StackPane codeAreaStackPane;
    private final Map<String, String> closingCharacters = new HashMap<>();
    public void loadConfigFile(){
        String appFolderPath = System.getProperty("user.home")+File.separator+"anass_coder_data";
        String configFilePath = appFolderPath+File.separator+"config.json";
        File appFolder = new File(appFolderPath);
        if(!appFolder.exists()){
            if(appFolder.mkdir()){
                try {
                    App.setAppDataFolder(appFolder.getCanonicalFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                File configFile = new File(configFilePath);
                if(!configFile.isFile() || !configFile.exists()){
                    try {
                        if(configFile.createNewFile()){
                            App.setAppConfigFile(configFile.getAbsoluteFile());
                            FileWriter writer = new FileWriter(configFile.getAbsolutePath());
                            writer.write("""
                                {
                                  "openedFolder": "",
                                  "openedTabs": [],
                                  "terminalSize": 1,
                                  "codeAreaSize": 0.192,
                                  "autoSave": false,
                                  "intellisense": false,
                                  "wrapText": false,
                                  "minimized": false,
                                  "selectedTab": "",
                                  "windowWidth": 1025,
                                  "windowHeight": 555,
                                  "fontSize": 20,
                                  "fontFamily": "Lucida Console",
                                  "terminalMinimised": true,
                                  "theme": "Default"
                                }""");
                            writer.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    App.setAppConfigFile(configFile);
                }
            }
        }
        else{
            App.setAppDataFolder(appFolder);
            File configFile = new File(configFilePath);
            if(!configFile.isFile() || !configFile.exists()){
                try {
                    if(configFile.createNewFile()){
                        App.setAppConfigFile(configFile.getAbsoluteFile());
                        FileWriter writer = new FileWriter(configFile.getAbsolutePath());
                        writer.write("""
                                {
                                  "openedFolder": "",
                                  "openedTabs": [],
                                  "terminalSize": 1,
                                  "codeAreaSize": 0.192,
                                  "autoSave": false,
                                  "intellisense": false,
                                  "wrapText": false,
                                  "minimized": false,
                                  "selectedTab": "",
                                  "windowWidth": 1025,
                                  "windowHeight": 555,
                                  "fontSize": 20,
                                  "fontFamily": "Lucida Console",
                                  "terminalMinimised": true,
                                  "theme": "Default"
                                }""");
                        writer.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                App.setAppConfigFile(configFile);
            }
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadConfigFile();
        setupAutoClosables();
        App.setCodeAreaHolder(codeAreaHolder);
        App.setCodeAreaStackPane(codeAreaStackPane);
        App.setTerminalContainer(terminalContainer);
        App.setPromptsContainer(promptsContainer);
        codeArea = new CodeArea();
        codeArea.setContextMenu(new TextAreaContextMenu());
        codeArea.getStyleClass().add("code_area");
        codeArea.setPadding(new Insets(10,0,0,0));
        VirtualizedScrollPane<CodeArea> virtualizedScrollPane = new VirtualizedScrollPane<>(codeArea);
        HBox.setHgrow(virtualizedScrollPane,Priority.ALWAYS);
        codeAreaHolder.getChildren().add(virtualizedScrollPane);
        App.setvSplitPane(downSplitPane);
        App.setMenuBar(appMenuBar);
        App.initMenuBar();
        if(tabsPane == null) return;
        tabsPane.setPrefWidth(codePane.getWidth());
        displayPane.getChildren().add(App.getFilesPane());
        Tab.setEncodingPlaceHolder(encodingPlaceHolder);
        Tab.setFileLanguagePlaceHolder(fileLanguagePlaceHolder);
        Tab.setFileSavedPlaceHolder(fileSavedPlaceHolder);
        Tab.setCurrentLinePlaceHolder(currentLinePlaceHolder);
        Tab.setEncodingPlaceHolder(encodingPlaceHolder);
        Tab.setEncodingPlaceHolder(encodingPlaceHolder);
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        tabsPane.setPrefWidth(screenWidth * 3/4);
        App.setTabsPane(tabsPane);
        App.setDisplayPane(displayPane);
        App.setCodeArea(codeArea);
        App.startCodeArea();
        App.setHSplitPane(upSplitPane);
        Terminal terminal = new Terminal(terminalContainer,promptsContainer);
        App.setTerminal(terminal);
        App.loadSettings();
        App.loadThemes();
        App.getFontsPlaceHolder().getSelectionModel().select(App.getSettings().fontFamily);
        App.getFontsPlaceHolder().setOnAction(e->{
            if(e.getSource() == null) return;
            App.getSettings().fontFamily = App.getFontsPlaceHolder().getSelectionModel().getSelectedItem();
            codeArea.setStyle("-fx-font-family: '"+App.getSettings().fontFamily+"'; -fx-font-size: "+App.getSettings().fontSize+"px;");
        });
        App.getFontSizePlaceHolder().setValueFactory(new SpinnerValueFactory<>() {
            @Override
            public void decrement(int steps) {
                try{
                    TextField editor = App.getFontSizePlaceHolder().getEditor();
                    codeArea.setStyle("-fx-font-family: '"+App.getSettings().fontFamily+"'; -fx-font-size: "+(App.getSettings().fontSize -1) +"px;");
                    App.getSettings().fontSize = Integer.parseInt(editor.getText())-1;
                    editor.setText(String.valueOf(Integer.parseInt(editor.getText())-1));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void increment(int steps) {
                try{
                    TextField editor = App.getFontSizePlaceHolder().getEditor();
                    codeArea.setStyle("-fx-font-family: '"+App.getSettings().fontFamily+"'; -fx-font-size: "+(App.getSettings().fontSize +1) +"px;");
                    App.getSettings().fontSize = Integer.parseInt(editor.getText())+1;
                    editor.setText(String.valueOf(Integer.parseInt(editor.getText())+1));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        App.getFontSizePlaceHolder().setEditable(true);
        App.getFontSizePlaceHolder().getEditor().setText(String.valueOf(App.getSettings().fontSize));
        App.getFontSizePlaceHolder().getEditor().setOnKeyPressed(e ->{
            try{
                codeArea.setStyle("-fx-font-family: '"+App.getSettings().fontFamily+"'; -fx-font-size: "+(Integer.parseInt(App.getFontSizePlaceHolder().getEditor().getText())) +"px;");
                App.getSettings().fontSize = Integer.parseInt(App.getFontSizePlaceHolder().getEditor().getText());
            }
            catch(Exception ex){
                System.out.println(ex.getMessage());
            }
        });
        upSplitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
            DecimalFormat df = new DecimalFormat("#." + "0".repeat(3));
            String formattedNumber = df.format(newValue);
            App.getSettings().codeAreaSize = Double.parseDouble(formattedNumber);
            if(App.getImageViewer() != null && App.getCodeAreaStackPane() !=null){
                App.getImageViewer().setFitWidth(App.getCodeAreaStackPane().getWidth() - 10);
                App.getImageViewer().setFitHeight(App.getCodeAreaStackPane().getHeight() - 10);
            }
        });
        if(downSplitPane.getDividers().size() > 0)
            downSplitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
                DecimalFormat df = new DecimalFormat("#." + "0".repeat(3));
                String formattedNumber = df.format(newValue);
                String oldV = df.format(oldValue);
                App.setOldVDivPosition(Double.parseDouble(oldV));
                App.getSettings().terminalSize =  Double.parseDouble(formattedNumber);
                if(App.getImageViewer() != null && App.getCodeAreaStackPane() !=null){
                    App.getImageViewer().setFitWidth(App.getCodeAreaStackPane().getWidth() - 10);
                    App.getImageViewer().setFitHeight(App.getCodeAreaStackPane().getHeight() - 10);
                }

            });

        terminal = new Terminal(terminalContainer,promptsContainer);
        App.setTerminal(terminal);
        App.syntaxHighlight();
        hookIdeDefaults();
        codeArea.setOnKeyTyped((e)->{
            App.updateTabText(e);
            autoClose(e.getCharacter(),codeArea);
            if(App.getHighlighter() != null){
                App.getHighlighter().highlight();
            }
            if(App.getCompleter() != null){
                App.getCompleter().completeText();
            }else{
                App.syntaxHighlight();
            }

        });
        upSplitPane.getDividers().get(0).setPosition(App.getSettings().codeAreaSize);
        downSplitPane.getDividers().get(0).setPosition(App.getSettings().terminalSize);
    }
    public void hookIdeDefaults(){
        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                autoIndent(codeArea);
            } else if (event.isControlDown() && event.isAltDown() && event.getCode() == KeyCode.DOWN) {
                duplicateLine(codeArea);
            }
            if(App.getHighlighter() != null){
                App.getHighlighter().highlight();
            }
        });
    }
    public void setupAutoClosables(){
        closingCharacters.put("(", ")");
        closingCharacters.put("[", "]");
        closingCharacters.put("{", "}");
        closingCharacters.put("\"", "\"");
        closingCharacters.put("'", "'");
        closingCharacters.put("`", "`");
        closingCharacters.put("<", ">");
    }
    private void autoIndent(CodeArea codeArea) {
        int currentParagraph = Math.max(0,codeArea.getCurrentParagraph() - 1);
        String currentLineText = codeArea.getParagraph(currentParagraph).getText();
        codeArea.insertText(codeArea.getCaretPosition(), getIndentation(currentLineText));
    }
    private void autoClose(String c,CodeArea codeArea) {
        if(isClosable(c)){
            codeArea.insertText(codeArea.getCaretPosition(),getClosingCharacter(c));
        }
    }
    public boolean isClosable(String s) {
        return closingCharacters.containsKey(s);
    }

    public String getClosingCharacter(String s) {
        return closingCharacters.getOrDefault(s, "");
    }

    private String getIndentation(String lineText) {
        int indentLevel = 0;
        for (String c : lineText.split("")) {
            if (c.equals(" ")) {
                indentLevel++;
            } else {
                break;
            }
        }
        return " ".repeat(indentLevel);
    }

    private void duplicateLine(CodeArea codeArea) {
        try{
            int caretPosition = codeArea.getCaretPosition();
            int currentParagraph = codeArea.getCurrentParagraph();
            while (codeArea.getText().charAt(Math.min(caretPosition,codeArea.getLength() -1)) != '\n'){
                caretPosition++;
            }
            String currentLineText = codeArea.getParagraph(currentParagraph).getText();
            caretPosition = Math.min(codeArea.getLength()-1,caretPosition);
            codeArea.insertText(caretPosition, "\n" + currentLineText);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    public void openFolder(){
        if(displayPane.lookup("#settings") != null)
            switchPanels();
        App.openFolder();
        Terminal terminal = new Terminal(terminalContainer,promptsContainer);
        App.setTerminal(terminal);
    }
    public void addFile()  {
        if(displayPane.lookup("#settings") != null)
            switchPanels();
        File currentDir = App.getCurrentDir();
        if(currentDir == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("no folder opened");
            alert.setHeaderText("no current folder");
            alert.setHeaderText("please open a folder first");
            alert.show();
            return;
        }
        App.addFile();
        App.renderFilesTree();

    }
    public void addFolder(MouseEvent e)  {
        if(displayPane.lookup("#settings") != null)
            switchPanels();
        File currentDir = App.getCurrentDir();
        if(currentDir == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("no folder opened");
            alert.setHeaderText("no current folder");
            alert.setHeaderText("please open a folder first");
            alert.show();
            return;
        }
        App.addFolder();
        App.renderFilesTree();
    }
    public void saveAll(){
        App.saveAllFiles(new ActionEvent());
    }
    public void toggleReduce(MouseEvent e){
        if(e.getClickCount() >= 2){
            App.reduce(e);
        }
    }
    public void switchPanels(){
        try{
            if(displayPane.lookup("#settings") == null){
                displayPane.getChildren().remove(displayPane.lookup("#files"));
                displayPane.getChildren().add(App.getSettingsPane());
                VBox.setVgrow(displayPane.lookup("#settings"),Priority.ALWAYS);
                App.applyThemes();
                InputStream imgFile = getClass().getResourceAsStream("/com/anass/anass_code_editor/assets/return.png");
                assert imgFile != null;
                settingsIcon.setImage(new Image(imgFile));
            }else{
                displayPane.getChildren().remove(displayPane.lookup("#settings"));
                displayPane.getChildren().add(App.getFilesPane());
                VBox.setVgrow(displayPane.lookup("#files"),Priority.ALWAYS);
                InputStream imgFile = getClass().getResourceAsStream("/com/anass/anass_code_editor/assets/settings.png");
                assert imgFile != null;
                settingsIcon.setImage(new Image(imgFile));
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public void minimizeTerminal(){
        App.getTerminal().minimize();
    }

}

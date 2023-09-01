package com.anass.anass_code_editor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;
import org.mozilla.universalchardet.UniversalDetector;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Tab extends HBox {
    //properties
    private String fileContent;
    private String language;
    private File file;
    private String encoding;
    private Boolean isSaved = true;
    private Integer lineCount;
    private static HBox wrapper;
    private Boolean isOpened = false;
    private String cursorPosition;
    private static Label fileLanguagePlaceHolder,currentLinePlaceHolder,fileSavedPlaceHolder,encodingPlaceHolder;
    Tab(File stream,String language){
        if(stream == null || !stream.exists()) return;
        this.file = stream;
        this.language = language;
        this.isSaved = true;
        this.fileContent = readFile(this.file);
        this.encoding = getEncoding();
        this.cursorPosition = "Line 0:0";
    }

    public static HBox getWrapper() {
        return wrapper;
    }

    public static void setWrapper(HBox wrapper) {
        Tab.wrapper = wrapper;
    }

    public static Label getFileLanguagePlaceHolder() {
        return fileLanguagePlaceHolder;
    }

    public static void setFileLanguagePlaceHolder(Label fileLanguagePlaceHolder) {
        Tab.fileLanguagePlaceHolder = fileLanguagePlaceHolder;
    }

    public static Label getCurrentLinePlaceHolder() {
        return currentLinePlaceHolder;
    }

    public static void setCurrentLinePlaceHolder(Label currentLinePlaceHolder) {
        Tab.currentLinePlaceHolder = currentLinePlaceHolder;
    }

    public static Label getFileSavedPlaceHolder() {
        return fileSavedPlaceHolder;
    }

    public static void setFileSavedPlaceHolder(Label fileSavedPlaceHolder) {
        Tab.fileSavedPlaceHolder = fileSavedPlaceHolder;
    }

    public static Label getEncodingPlaceHolder() {
        return encodingPlaceHolder;
    }

    public static void setEncodingPlaceHolder(Label encodingPlaceHolder) {
        Tab.encodingPlaceHolder = encodingPlaceHolder;
    }

    //methods
    public String getEncoding() {
     try {
         if(file == null || !file.exists()) return "none";
         String encoding = UniversalDetector.detectCharset(file);
         if (encoding != null)
          return encoding;
         return "none";
     }
     catch(Exception e){
         e.printStackTrace();
         return "none";
     }
    }
    public void create(){
        if(this.file == null) return;
        Label name = new Label(this.file.getName());
        ImageView iconView = new ImageView(App.getFileIcon(this.file.getName()));
        Button closeBtn = new Button("x");
        this.setPrefWidth(160);
        this.setSpacing(5);
        this.setPadding(new Insets(2,2,2,10));
        this.setFillHeight(true);
        this.setAlignment(Pos.CENTER_LEFT);
        this.getStyleClass().add("tab");
        name.getStyleClass().add("tab_label");
        closeBtn.getStyleClass().add("tab_exit_btn");
        HBox.setHgrow(name, Priority.ALWAYS);
        HBox.setHgrow(closeBtn, Priority.NEVER);
        iconView.setFitWidth(18.0);
        iconView.setFitHeight(18.0);
        iconView.setPreserveRatio(true);
        iconView.setSmooth(true);
        closeBtn.setTextAlignment(TextAlignment.CENTER);
        closeBtn.setTextOverrun(OverrunStyle.ELLIPSIS);
        name.setPrefWidth(96.0);
        closeBtn.setOnAction(e -> this.close());
        this.setOnMouseClicked(e -> App.openTab(this));
        this.getChildren().addAll(iconView,name,closeBtn);
    }
    public String readFile(File file) {
       try {
           StringBuilder content = new StringBuilder();
           int count = 0;
           Scanner scanner = new Scanner(file);
           while(scanner.hasNextLine()){
               content.append(scanner.nextLine()).append("\n");
               count++;
           }
           scanner.close();
           setLineCount(count);
           return content.toString();
       }catch(FileNotFoundException e){
           e.printStackTrace();
           return "";
       }
    }
    public void close(){
        App.resetDefaultTab(this);
        App.getOpenedTabs().remove(this);
        App.updateSettings();
        setOpened(false);
        wrapper.getChildren().remove(this);
    }
    public Boolean isSaved() {
        return isSaved;
    }

    public void setIsSaved(Boolean saved) {
        isSaved = saved;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fc) {
        this.fileContent = fc;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }


    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Integer getLineCount() {
        return lineCount;
    }

    public void setLineCount(Integer lineCount) {
        this.lineCount = lineCount;
    }

    public Boolean isOpened() {
        return isOpened;
    }

    public void setOpened(Boolean opened) {
        isOpened = opened;
    }

    public String getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(String cursorPosition) {
        this.cursorPosition = cursorPosition;
    }
}

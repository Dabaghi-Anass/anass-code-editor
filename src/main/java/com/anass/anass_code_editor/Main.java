package com.anass.anass_code_editor;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import com.jfoenix.controls.JFXDecorator;
public class Main extends Application {

    public void loadLanguagesPack(){
        App.loadIconImages();
        JsonReader<ProgrammingLanguage> reader = new JsonReader<>("assets/extensions.json", ProgrammingLanguage.class);
        List<ProgrammingLanguage> data = reader.getData();
        App.setEditorPLanguages(data);
    }
    
    public void handleFullScreen(Stage stage){
        stage.setFullScreenExitHint("press f11 to exit full screen");
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });
    }

 
    @Override
    public void start(Stage stage){
        try {
            App.setStage(stage);
            loadLanguagesPack();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("index.fxml"));
            FXMLLoader settingsLoader = new FXMLLoader(getClass().getResource("settings.fxml"));
            FXMLLoader filesPaneLoader = new FXMLLoader(getClass().getResource("displayPane.fxml"));
            FXMLLoader promptLoader = new FXMLLoader(getClass().getResource("prompt.fxml"));
            App.setSettingsPane(settingsLoader.load());
            App.setFilesPane(filesPaneLoader.load());
            App.setTerminalPrompt(promptLoader.load());
            Parent root = fxmlLoader.load();
            JFXDecorator decorator = new JFXDecorator(stage,root,false,true,true);
            decorator.setFillWidth(true);
            decorator.setOnCloseButtonAction(()->{
                App.exit();
            });
            JFXDecorator.setVgrow(root, Priority.ALWAYS);
            decorator.setFocusTraversable(false);
            Scene scene = new Scene(decorator);
            App.setDecorator(decorator);
            decorator.setOnMouseClicked(e ->{
                if(App.getSettings() != null)
                {
                    App.getSettings().minimized = !stage.isMaximized();
                    if(App.getSettings().minimized){
                        App.savedWidth = decorator.getWidth();
                        App.savedHeight = decorator.getHeight();
                        App.getSettings().windowWidth =(int) decorator.getWidth();
                        App.getSettings().windowHeight =(int) decorator.getHeight();
                    }
                }
            });
            scene.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                if(App.getHSplitPane() != null)
                    App.getHSplitPane().getDividers().get(0).setPosition(App.getSettings().codeAreaSize);
                if(App.getvSplitPane() != null)
                    App.getvSplitPane().getDividers().get(0).setPosition(App.getSettings().terminalSize);
            });
            scene.heightProperty().addListener((obs, oldHeight, newHeight) -> {
                if(App.getHSplitPane() != null)
                    App.getHSplitPane().getDividers().get(0).setPosition(App.getSettings().codeAreaSize);
                if(App.getvSplitPane() != null)
                    App.getvSplitPane().getDividers().get(0).setPosition(App.getSettings().terminalSize);
            });
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("application.css")).toExternalForm());
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("highlight.css")).toExternalForm());
            stage.setTitle("AnassCode");
            Image iconImage = new Image(getClass().getResource("assets/logo.png").toExternalForm());
            stage.getIcons().add(iconImage);
            stage.setOnCloseRequest(event -> {
                event.consume();
                App.exit();
            });

            App.initContextMenu(scene);
            stage.setScene(scene);
            handleFullScreen(stage);
            stage.show();
            customizeTitleBar();
            App.initSavingThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void customizeTitleBar(){

    }
    public static void main(String[] args) {
        if(args.length > 0){
            File f = new File(args[0]);
            if(f != null && f.exists()){
                if(f.isDirectory()){
                    try {
                        App.setCurrentDir(f.getCanonicalFile());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    App.setSelectedFolder(f);
                }else if(f.isFile()){
                    try {
                        App.setCurrentDir(f.getParentFile().getCanonicalFile());
                        App.setSelectedFolder(f.getParentFile().getCanonicalFile());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }
        launch();
    }

}

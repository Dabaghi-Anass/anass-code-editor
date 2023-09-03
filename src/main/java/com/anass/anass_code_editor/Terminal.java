package com.anass.anass_code_editor;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Terminal  {
    File currentDir;
    private VBox container;
    private Label state;
    private Process terminalProcess;
    private VBox promptsContainer;
    private TextField input = new TextField();
    private Label output = new Label();
    private ProcessBuilder processBuilder;
    private ArrayList<String> commandes = new ArrayList<>();
    Integer commandInd = null;
    private ScrollPane scrollContainer;
    private  VBox terminalWindow;
    private ScrollPane sPane;
    Terminal(VBox uiComponent ,ScrollPane sc){
        if(App.getCurrentDir() == null) return;
        this.container = uiComponent;
        scrollContainer = sc;
        promptsContainer =(VBox) ((ScrollPane)container.lookup("#promptsContainer")).getContent();
        if(App.getvSplitPane().getItems().size() > 2)
          terminalWindow = (VBox) App.getvSplitPane().getItems().get(1);
        setState((Label)this.container.lookup("#terminalLabel"));
        currentDir = App.getCurrentDir();
        startTerminalProcess();
    }
    public void addCommandRunListener(){
        input.setOnKeyPressed(e -> {
            if (scrollContainer != null)
                scrollContainer.vvalueProperty().setValue(1);
            if(e.getCode() == KeyCode.TAB){
                e.consume();
                if (input.getText().trim().isEmpty()) return;
                String[] words = input.getText().toLowerCase().split(" ");
                String fileName = words[words.length - 1];
                File[] files = currentDir.listFiles();
                if (files == null) return;
                for (File file : files) {
                    if (file.getName().toLowerCase().startsWith(fileName)) {
                        input.setText(input.getText().replace(fileName, file.getName()));
                        break;
                    }
                }
                input.requestFocus();
                input.positionCaret(input.getLength());
                input.deselect();
            }
            if(e.getCode() == KeyCode.ENTER){
                if(input.getText().trim().isEmpty()) return;
                if(commandInd == null) commandInd = 0;
                else commandInd++;
                processCommand(input.getText());
            }
            if(e.getCode() == KeyCode.UP){
                if(commandInd == null) return;
                if(commandInd > 0){
                    commandInd--;
                    input.setText(commandes.get(commandInd));
                    input.positionCaret(input.getLength());

                }

            }
            if(e.getCode() == KeyCode.DOWN){
                if(commandInd == null) return;
                if(commandInd < commandes.size() - 1){
                    commandInd++;
                    input.setText(commandes.get(commandInd));
                    input.positionCaret(input.getLength());
                }
            }

        });
    }

    public VBox getContainer() {
        return container;
    }
    public void setContainer(VBox container) {
        this.container = container;
    }
    public void minimize() {
        terminalWindow = (VBox) App.getvSplitPane().getItems().get(1);
        App.getSettings().terminalSize = 1;
        App.getSettings().terminalMinimised = true;
        App.getvSplitPane().getDividers().get(0).setPosition(1.0);
    }
    public void maximize() {
        terminalWindow = (VBox) App.getvSplitPane().getItems().get(1);
        if(App.getOldVDivPosition() > 0.7) App.setOldVDivPosition(0.7);
        App.getSettings().terminalSize = App.getOldVDivPosition();
        App.getvSplitPane().getDividers().get(0).setPosition(App.getOldVDivPosition());
        App.getSettings().terminalMinimised = false;
        if (scrollContainer != null)
            scrollContainer.vvalueProperty().setValue(1);
    }
    public Label getPathLabel(){
        HBox parent = App.getTerminalPrompt();
        return (Label) parent.lookup("#pathLabel");
    }

    public HBox getNewPrompt(){
            HBox parent = App.getTerminalPrompt();
            if(parent == null) return null;
            Label path = (Label) parent.lookup("#pathLabel");
            TextField cmd =(TextField) parent.lookup("#commandField");
            cmd.setText("");
            path.setText(currentDir.getAbsolutePath() + ">");
            return parent;
    }

    public Label getState() {
        return state;
    }
    public void setState(Label state) {
        this.state = state;
    }
    private void newPrompt() {
        if(promptsContainer == null) return;
        HBox p = getNewPrompt();
        HBox.setHgrow(p, Priority.ALWAYS);
        if(p == null) return;
        promptsContainer.getChildren().add(p);
        input =(TextField) p.lookup("#commandField");
        addCommandRunListener();
        if (scrollContainer != null)
            scrollContainer.vvalueProperty().setValue(1);
    }
    private void newOutput() {
        if(promptsContainer == null) return;
        Label out = new Label();
        out.getStyleClass().add("out-label");
        VBox.setVgrow(out,Priority.NEVER);
        promptsContainer.getChildren().add(out);
        output = out;
        scrollContainer.setVvalue(1.0);
    }
    private void startTerminalProcess() {
        try {
            processBuilder = new ProcessBuilder("/bin/bash");
            String os = System.getProperty("os.name").toLowerCase();
            if(os.contains("win")){
                processBuilder = new ProcessBuilder("cmd");
            }
            processBuilder.directory(App.getCurrentDir());
            processBuilder.redirectErrorStream(true);
            terminalProcess = processBuilder.start();
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(terminalProcess.getInputStream()))) {
                    String line;
                    Platform.runLater(() -> {
                        newOutput();
                        newPrompt();
                        if (scrollContainer != null)
                            scrollContainer.vvalueProperty().setValue(1);
                    });
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
                        Platform.runLater(() -> {
                            output.setText(output.getText() + finalLine + "\n");
                            if (scrollContainer != null)
                                scrollContainer.vvalueProperty().setValue(1);
                        });
                    }
                    Platform.runLater(()->{
                        int rowCount = output.getText().split("\n").length;
                        output.setMinHeight(rowCount * 16);
                        output.setPrefHeight(rowCount * 16);
                        if (scrollContainer != null)
                            scrollContainer.vvalueProperty().setValue(1);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    state.setText("error");
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            state.setText("error");
        }
    }
    public void clearTerminal(){
        promptsContainer.getChildren().clear();
    }
    public void processCommand(String command) {
        try {
            state.getStyleClass().remove("error");
            if(command.startsWith("exit")){
                input.setText("");
                minimize();
                output.setText("can't terminate process please close the window if you want to terminate");
                return;
            }else if(command.startsWith("clear")|| command.startsWith("cls")){
                clearTerminal();
                newPrompt();
                if (scrollContainer != null)
                    scrollContainer.vvalueProperty().setValue(1);
                input.requestFocus();
                return;
            }
            if(!terminalProcess.isAlive()) return;
            terminalProcess.getOutputStream().write((command + "\n").getBytes());
            terminalProcess.getOutputStream().flush();
            commandes.add(command);
            commandInd = commandes.size() - 1;
            if (command.startsWith("cd ")) {
                String newDirPath = command.substring(3).trim();
                newDirPath = newDirPath.replace(currentDir.getAbsolutePath(),"");
                File newDir = new File(currentDir, newDirPath).getCanonicalFile();
                if (newDir.exists() && newDir.isDirectory()) {
                    currentDir = newDir;
                    input.setEditable(false);
                    newOutput();
                    newPrompt();
                    input.requestFocus();
                    if(scrollContainer != null)
                        scrollContainer.vvalueProperty().setValue(1);
                } else {
                    Platform.runLater(() -> output.setText("Directory not found or invalid\n"));
                }
            }else{
                input.setEditable(false);
                newOutput();
                newPrompt();
                if(scrollContainer != null)
                    scrollContainer.vvalueProperty().setValue(1);
                input.requestFocus();
            }
            App.renderFilesTree();
            if (scrollContainer != null)
                scrollContainer.vvalueProperty().setValue(1);
        } catch (IOException e) {
            state.setText("error : " + e.getMessage());
            state.getStyleClass().add("error");
        }


    }

}

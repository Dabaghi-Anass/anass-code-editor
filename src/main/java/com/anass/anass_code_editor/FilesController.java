package com.anass.anass_code_editor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class FilesController implements Initializable {
    @FXML
    TreeItem<File> rootItem;
    @FXML
    TreeView<File> filesTree;
    @FXML
    TextField searchInput;
    @FXML
    AnchorPane files;
    private static final long SEARCH_DELAY = 500;
    private Timer searchTimer;
    public void handleSaveExpandablesFolders(){
        App.setExpandedFolders(new ArrayList<TreeItem<File>>());
        saveExpandables(filesTree.getRoot());
    }
    public void saveExpandables(TreeItem<File> root){
        if(root == null) return;
        if(root.isExpanded()){
            App.getExpandedFolders().add(root);
        }
        for(TreeItem<File> child : root.getChildren()){
            if(child.isExpanded()){
                App.getExpandedFolders().add(child);
            }
            saveExpandables(child);
        }
    }
    public void renderFilesTree(){
        App.setFilesTree(filesTree);
        App.setTreeRoot(rootItem);
    }

    public void searchFiles(){
        if (searchTimer != null) {
            searchTimer.cancel();
        }
        searchTimer = new Timer();
        searchTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    App.searchFile(searchInput.getText().trim().toLowerCase(), App.getTreeRoot());
                });
            }
        }, SEARCH_DELAY);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        VBox.setVgrow(files, Priority.ALWAYS);
        App.setSearchInput(searchInput);
        renderFilesTree();
        filesTree.setOnMouseClicked(event -> {
            TreeItem<File> selectionItem = filesTree.getSelectionModel().getSelectedItem();
            if (selectionItem == null) return;
            File selectedFile = selectionItem.getValue();
            handleSaveExpandablesFolders();
            if(event.getSource() == null) return;
            if (event.getClickCount() > 1) {
                if(selectedFile.isDirectory()){
                    selectionItem.setExpanded(!selectionItem.isExpanded());
                    handleSaveExpandablesFolders();
                } else if (selectedFile.isFile()) {
                    App.initNewTab(null);
                }
            }
            File selected = selectionItem.getValue();
            if(selected.isFile()){
                App.setSelectedFile(selected);
                App.setSelectedFolder(selected.getParentFile());
            }
            else if(selected.isDirectory()){
                App.setSelectedFile(selected);
                App.setSelectedFolder(selected);
            }
        });
    }
}

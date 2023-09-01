package com.anass.anass_code_editor;

import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.Objects;

public class AppTreeCell extends TreeCell<File> {
    ImageView iconImageView;
    Image icon;
    AppTreeCell(){
        iconImageView = new ImageView();
        iconImageView.setFitHeight(18);
        iconImageView.setFitWidth(18);
        setGraphic(iconImageView);
    }
    @Override
    protected void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            iconImageView.setImage(null);
        } else {
            setText(item.getName());
            getStyleClass().add("tree-cell");
            if(App.getExtension(item.getName()).equals("")){
                if(item.isDirectory()) {
                    Image folderIcon = App.getIcons().get("folder-" +item.getName().trim().toLowerCase());
                    if(folderIcon != null)
                        iconImageView.setImage(folderIcon);
                    else iconImageView.setImage(App.getFileIcon("file.folder"));
                }
                 else if(item.isFile()) iconImageView.setImage(App.getFileIcon("file.file"));
            }
            else {
                if(item.isDirectory()){
                    iconImageView.setImage(App.getFileIcon( "folder-" + item.getName()));
                }
                else{
                    iconImageView.setImage(App.getFileIcon(item.getName()));
                }
            }
        }
    }
}

package com.anass.anass_code_editor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    @FXML
    ScrollPane settingsScrollPane;
    @FXML
    VBox settingsVBox;
    @FXML
    AnchorPane settings;
    @FXML
    CheckBox intellisenseBox,autoSaveBox,textWrapBox;
    @FXML
    ComboBox<String> fontsPlaceHolder;
    @FXML
    Spinner<Integer> fontSizePlaceHolder;
    @FXML
    VBox themesDiv;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        App.setThemesDivision(themesDiv);
        settingsVBox.setPrefWidth(settingsScrollPane.getPrefWidth());
        VBox.setVgrow(settings, Priority.ALWAYS);
        App.setIntellisenseBox(intellisenseBox);
        App.setWrapTextBox(textWrapBox);
        App.setAutoSaveBox(autoSaveBox);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        fontsPlaceHolder.getItems().addAll(fonts);
        App.setFontsPlaceHolder(fontsPlaceHolder);
        App.setFontSizePlaceHolder(fontSizePlaceHolder);
    }
    public void toggle_auto_save(ActionEvent e){
        App.setAutoSave(((CheckBox)e.getSource()).isSelected());
        App.getSettings().autoSave = ((CheckBox)e.getSource()).isSelected();
    }
    public void toggle_auto_intellisense(ActionEvent e){
        App.setIntellisense(((CheckBox)e.getSource()).isSelected());
        App.getSettings().intellisense = ((CheckBox)e.getSource()).isSelected();
    }
    public void toggle_text_wrap(ActionEvent e){
        App.getSettings().wrapText = ((CheckBox)e.getSource()).isSelected();
        App.getCodeArea().setWrapText(App.getSettings().wrapText);
    }
}

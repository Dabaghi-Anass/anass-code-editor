package com.anass.anass_code_editor;
import com.jfoenix.controls.JFXDecorator;
import javafx.scene.Node;
import javafx.stage.Stage;


public class Decorator extends JFXDecorator {

    public Decorator(Stage stage, Node node) {
        super(stage, node);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
    }
}

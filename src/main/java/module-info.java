module com.anass.anass_code_editor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires com.fasterxml.jackson.databind;
    requires java.xml;
    requires com.github.albfernandez.juniversalchardet;
    requires java.desktop;
    requires batik.all;
    requires com.google.gson;
    requires javafx.graphics;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires com.jfoenix;

    opens com.anass.anass_code_editor to javafx.fxml;
    exports com.anass.anass_code_editor;
}
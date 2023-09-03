package com.anass.anass_code_editor;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.fxmisc.richtext.CodeArea;
public class TextAreaContextMenu extends ContextMenu {
        private MenuItem fold, unfold, copy,cut,past;

        TextAreaContextMenu()
        {
            fold = new MenuItem( "Fold selected text" );
            fold.setOnAction( AE -> { hide(); fold(); } );

            unfold = new MenuItem( "Unfold from cursor" );
            unfold.setOnAction( AE -> { hide(); unfold(); } );

            copy = new MenuItem( "Copy" );
            cut = new MenuItem( "Cut" );
            past = new MenuItem( "Past" );
            copy.setOnAction( AE -> { hide(); copy(); } );
            cut.setOnAction( AE -> { hide(); cut(); } );
            past.setOnAction( AE -> { hide(); paste(); } );
            getItems().addAll( fold, unfold,new SeparatorMenuItem(), cut,copy,past );
        }

        private void fold() {
            ((CodeArea) getOwnerNode()).foldSelectedParagraphs();
        }

        private void unfold() {
            CodeArea area = (CodeArea) getOwnerNode();
            area.unfoldParagraphs( area.getCurrentParagraph() );
        }

    private void copy() {
        String text = ((CodeArea) getOwnerNode()).getSelectedText();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    private void cut() {
        CodeArea codeArea = (CodeArea) getOwnerNode();
        String selectedText = codeArea.getSelectedText();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(selectedText);
        clipboard.setContent(content);
        codeArea.replaceSelection("");
    }
    
      
    private void paste() {
        CodeArea codeArea = (CodeArea) getOwnerNode();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            String textToPaste = clipboard.getString();
            codeArea.insertText(codeArea.getCaretPosition(), textToPaste);
        }
    }
}


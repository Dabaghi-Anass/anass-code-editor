package com.anass.anass_code_editor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Settings {
     public String openedFolder;
     public String[] openedTabs;
     public double terminalSize;
     public double codeAreaSize;
     public boolean autoSave;
     public boolean intellisense;
     public boolean wrapText;
     public boolean minimized;
     public String selectedTab;
     public int windowWidth;
     public int windowHeight;
     public int fontSize;
     public String fontFamily;
     public boolean terminalMinimised;
     public String theme;

     @Override
     public String toString() {
          Gson gson = new GsonBuilder().setPrettyPrinting().create();
          return gson.toJson(this);
     }
}

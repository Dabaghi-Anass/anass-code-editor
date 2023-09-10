package com.anass.anass_code_editor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.*;
import java.util.List;

public class JsonReader<T> {
    private List<T> data;
    private T object;

    public JsonReader(String filePath, Class<T> valueType) {
        InputStream inputStream = JsonReader.class.getResourceAsStream(filePath);
        if (inputStream == null) {
            System.out.println("null ressources "+filePath);
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        CollectionType collectionType = typeFactory.constructCollectionType(List.class, valueType);

        try {
            data = objectMapper.readValue(inputStream, collectionType);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public JsonReader(String filePath, Class<T> valueType,int count)  {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            InputStream inputStream = JsonReader.class.getResourceAsStream(filePath);
            if (inputStream == null) return;
            object = objectMapper.readValue(inputStream, valueType);
        } catch (Exception e) {
            try {
                object = objectMapper.readValue("""
                                    {
                                      "openedFolder": "",
                                      "openedTabs": [],
                                      "terminalSize": 1,
                                      "codeAreaSize": 0.192,
                                      "autoSave": false,
                                      "intellisense": false,
                                      "wrapText": false,
                                      "minimized": false,
                                      "selectedTab": "",
                                      "windowWidth": 1025,
                                      "windowHeight": 555,
                                      "fontSize": 20,
                                      "fontFamily": "Lucida Console",
                                      "terminalMinimised": true,
                                      "theme": "Default"
                                    }""",valueType);
            } catch (JsonProcessingException ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println(e.getMessage());
        }
    }
    public JsonReader(File file, Class<T> valueType,String s)  {
            try {
                if(file == null) return;
                ObjectMapper objectMapper = new ObjectMapper();
                object = objectMapper.readValue(file, valueType);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
    }

    public List<T> getData() {
        return data;
    }
    public T getObject() {
        return object;
    }
}
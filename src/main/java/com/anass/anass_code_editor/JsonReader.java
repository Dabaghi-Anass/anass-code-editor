package com.anass.anass_code_editor;
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
        try {
            InputStream inputStream = JsonReader.class.getResourceAsStream(filePath);
            if (inputStream == null) return;
            ObjectMapper objectMapper = new ObjectMapper();
            object = objectMapper.readValue(inputStream, valueType);
        } catch (Exception e) {
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
package com.mar;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mar.service.JsonService;
import com.mar.service.Questions;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        URL urlFile = Main.class.getClassLoader().getResource("test.json");
        String json = Files.readString(Path.of(urlFile.toURI()));

        System.out.println(new JsonService().get(json, Questions.class));
    }
}
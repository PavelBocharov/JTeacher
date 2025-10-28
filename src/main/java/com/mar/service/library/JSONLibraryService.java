package com.mar.service.library;

import com.mar.data.Question;
import com.mar.data.Questions;
import com.mar.utils.JsonUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
public class JSONLibraryService implements LibraryService {

    private final Map<String, Map<String, Question>> library = new HashMap<>();

    private final String rootDir;

    public JSONLibraryService(String rootDir) {
        if (rootDir == null) {
            throw new IllegalArgumentException("Root dir is null.");
        }
        if (rootDir.isBlank()) {
            throw new IllegalArgumentException("Root dir is blank.");
        }
        File root = new File(rootDir);
        if (!root.isDirectory()) {
            throw new IllegalArgumentException("Root dir is to directory.");
        }

        this.rootDir = rootDir;

        initDatabase(FileUtils.listFiles(root, new String[]{"json"}, false));
    }

    private void initDatabase(Collection<File> jsonFiles) {
        log.debug("Init database from JSON files: {}", jsonFiles);
        for (File file : jsonFiles) {
            if (file.exists() && file.isFile()) {
                try {
                    Questions questions = JsonUtility.get(FileUtils.readFileToString(file, "UTF-8"), Questions.class);
                    String type = questions.getType();
                    Map<String, Question> questionsByType = new HashMap<>(questions.getQuestions().size());
                    for (int i = 0; i < questions.getQuestions().size(); i++) {
                        String id = String.valueOf(i);
                        Question question = questions.getQuestions().get(i);
                        question.setId(id);
                        question.setPosition((long) i);
                        // Перетасовать варианты ответа
                        Character correctAnswer = question.getCorrectAnswer().charAt(0);
                        List<String> shaffleAnswers = question.getOptions();
                        ArrayList<String> newOptions = new ArrayList<>(4);
                        Collections.shuffle(shaffleAnswers);
                        for (int a = 0; a < shaffleAnswers.size(); a++) {
                            Character newPrefix = (char) ('A' + a);
                            String answer = shaffleAnswers.get(a);
                            if (correctAnswer.equals(answer.charAt(0))) {
                                question.setCorrectAnswer(String.valueOf(newPrefix));
                            }
                            newOptions.add(a, newPrefix + answer.substring(1));
                        }
                        question.setOptions(newOptions);
                        // -------------
                        questionsByType.put(id, question);
                    }
                    library.put(type, questionsByType);
                } catch (Exception e) {
                    log.error("Cannot parse JSON file: {}", file.getAbsolutePath(), e);
                }
            } else {
                log.warn("Cannot work with JSON file: {} not exist or is not file.", file.getAbsolutePath());
            }
        }
    }


    private Map<String, Question> getQuestions(String type) {
        Map<String, Question> questions = library.get(type);
        if (questions == null || questions.isEmpty()) {
            log.warn("Questions by type = '{}' is empty or null.", type);
            return null;
        }
        return questions;
    }

    @Override
    public Question getRandomByType(String type) {
        Map<String, Question> questions = getQuestions(type);
        if (questions == null) {
            return null;
        }
        return questions.get(String.valueOf(new Random().nextInt(questions.size())));
    }

    @Override
    public Question getById(String type, String id) {
        Map<String, Question> questions = getQuestions(type);
        if (questions == null) {
            log.warn("Cannot find library by type = {}", type);
            return null;
        }
        Question question = questions.get(id);
        if (question != null) {
            return question;
        }
        log.warn("Cannot find question by id = '{}'", id);
        return null;
    }

    @Override
    public Question getNext(String type, Long position) {
        Map<String, Question> questions = getQuestions(type);

        return questions.values().parallelStream()
                .filter(question -> question.getPosition() <= position)
                .min(Comparator.comparing(Question::getPosition)).orElse(null);
    }
}

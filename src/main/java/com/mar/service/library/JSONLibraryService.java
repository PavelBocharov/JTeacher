package com.mar.service.library;

import com.mar.annotation.CallbackButtonType;
import com.mar.data.QuestionInfo;
import com.mar.data.Questions;
import com.mar.model.Question;
import com.mar.model.Type;
import com.mar.utils.BotUtils;
import com.mar.utils.JsonUtility;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
public class JSONLibraryService implements LibraryService {

    @Getter
    private final Map<String, Map<String, QuestionInfo>> library = new HashMap<>();

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
                    Map<String, QuestionInfo> questionsByType = new HashMap<>(questions.getQuestions().size());
                    for (int i = 0; i < questions.getQuestions().size(); i++) {
                        String id = String.valueOf(i);
                        QuestionInfo question = questions.getQuestions().get(i);
                        question.setId(id);
                        question.setPosition((long) i);
                        // Перетасовать варианты ответа
                        BotUtils.shaffleOptions(question);
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


    private Map<String, QuestionInfo> getQuestions(String type) {
        Map<String, QuestionInfo> questions = library.get(type);
        if (questions == null || questions.isEmpty()) {
            log.warn("Questions by type = '{}' is empty or null.", type);
            return null;
        }
        return questions;
    }

    @Override
    public QuestionInfo getRandomByType(String type) {
        Map<String, QuestionInfo> questions = getQuestions(type);
        if (questions == null) {
            return null;
        }
        return questions.get(String.valueOf(new Random().nextInt(questions.size())));
    }

    @Override
    public QuestionInfo getById(String type, String id) {
        Map<String, QuestionInfo> questions = getQuestions(type);
        if (questions == null) {
            log.warn("Cannot find library by type = {}", type);
            return null;
        }
        QuestionInfo question = questions.get(id);
        if (question != null) {
            return question;
        }
        log.warn("Cannot find question by id = '{}'", id);
        return null;
    }

    @Override
    public QuestionInfo getNext(String type, Long position) {
        Map<String, QuestionInfo> questions = getQuestions(type);

        return questions.values().parallelStream()
                .filter(question -> question.getPosition() <= position)
                .min(Comparator.comparing(QuestionInfo::getPosition)).orElse(null);
    }

    @Override
    public Type getTypeInfo(String type) {
        return Type.builder()
                .title(type)
                .description(CallbackButtonType.findByType(type).getText())
                .questions(library.get(type).values().parallelStream()
                        .map(questionInfo -> Question.builder().position(questionInfo.getPosition()).build())
                        .toList())
                .build();
    }
}

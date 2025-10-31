package com.mar.service.library;

import com.mar.config.DBConfigurate;
import com.mar.data.QuestionInfo;
import com.mar.data.Questions;
import com.mar.model.Question;
import com.mar.model.Type;
import com.mar.utils.BotUtils;
import com.mar.utils.JsonUtility;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class DBLibraryService implements LibraryService {

    private final EntityManager em;

    public DBLibraryService(String rootDir) {
        this.em = DBConfigurate.getEntityManager();
        initDB(rootDir);
    }

    public DBLibraryService(EntityManager em, String rootDir) {
        this.em = em;
        initDB(rootDir);
    }

    private void initDB(String rootDir) {
        Collection<File> jsonFiles = FileUtils.listFiles(new File(rootDir), new String[]{"json"}, false);
        log.debug("Find JSON files: {}.", jsonFiles);
        for (File file : jsonFiles) {
            log.debug("Work with '{}' file. Exist: {}, Is file: {}", file, file.exists(), file.isFile());
            if (file.exists() && file.isFile()) {
                EntityManager em = DBConfigurate.getEntityManager();
                try {
                    Questions questions = JsonUtility.get(FileUtils.readFileToString(file, "UTF-8"), Questions.class);

                    Type type = em.createQuery("SELECT type FROM Type type WHERE type.title = ?1", Type.class)
                            .setParameter(1, questions.getType())
                            .getSingleResultOrNull();

                    log.debug("Get type by title: {}. Type - {}", questions.getType(), type);
                    if (type != null && type.getVersion() >= questions.getVersion()) {
                        break;
                    }

                    if (type == null) {
                        type = Type.builder()
                                .title(questions.getType())
                                .description(questions.getDescription())
                                .build();
                    }
                    type.setVersion(questions.getVersion());
                    log.debug("INIT DB - type {}", type);

                    Set<Question> questionArrayList;
                    if (type.getId() != null) {
                        Set<String> questionsText = new HashSet<>(
                                em.createQuery("SELECT q.question FROM Question q WHERE q.type = ?1", String.class)
                                        .setParameter(1, type)
                                        .getResultList()
                        );
                        log.debug("Get set questions: {}", questionsText.size());
                        questionArrayList = questions.getQuestions().parallelStream()
                                .filter(q -> !questionsText.contains(q.getQuestion()))
                                .map(BotUtils::shaffleOptions)
                                .map(
                                        q -> Question.builder()
                                                .question(q.getQuestion())
                                                .options(q.getOptions())
                                                .correctAnswer(q.getCorrectAnswer())
                                                .detailedAnswer(q.getDetailedAnswer())
                                                .build()
                                )
                                .collect(Collectors.toSet());
                        log.debug("Add new questions: {}", questionArrayList.size());
                    } else {
                        questionArrayList = questions.getQuestions().parallelStream()
                                .map(BotUtils::shaffleOptions)
                                .map(q -> Question.builder()
                                        .question(q.getQuestion())
                                        .options(q.getOptions())
                                        .correctAnswer(q.getCorrectAnswer())
                                        .detailedAnswer(q.getDetailedAnswer())
                                        .build()
                                ).collect(Collectors.toSet());
                        log.debug("Add questions: {}", questionArrayList.size());
                    }

                    type.setQuestions(questionArrayList);
                    em.getTransaction().begin();
                    em.persist(type);
                    for (Question q : questionArrayList) {
                        q.setType(type);
                        em.persist(q);
                    }
                    em.getTransaction().commit();
                } catch (Exception e) {
                    em.getTransaction().rollback();
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public QuestionInfo getRandomByType(String type) {
        return map(em.createQuery("SELECT q FROM Question q WHERE q.type = ?1 ORDER BY RANDOM() LIMIT 1", Question.class)
                .setParameter(1, getTypeInfo(type))
                .getSingleResultOrNull()
        );
    }

    @Override
    public QuestionInfo getById(String type, String id) {
        return map(em.createQuery("SELECT q FROM Question q WHERE q.type = ?1 and q.id = ?2", Question.class)
                .setParameter(1, getTypeInfo(type))
                .setParameter(2, id)
                .getSingleResultOrNull()
        );
    }

    @Override
    public QuestionInfo getNext(String type, Long position) {
        return map(em.createQuery("SELECT q FROM Question q WHERE q.type = ?1 and q.id > ?2 LIMIT 1", Question.class)
                .setParameter(1, getTypeInfo(type))
                .setParameter(2, position)
                .getSingleResultOrNull()
        );
    }

    @Override
    public Type getTypeInfo(String type) {
        Type t = em.createQuery("SELECT t FROM Type t where t.title=?1", Type.class).setParameter(1, type).getSingleResult();
        log.debug("Get type by title = '{}'. Type: {}", type , t);
        return t;
    }

    private QuestionInfo map(Question q) {
        if (q == null) {
            return null;
        }
        return new QuestionInfo(
                String.valueOf(q.getId()),
                q.getId(),
                q.getQuestion(),
                q.getOptions(),
                q.getCorrectAnswer(),
                q.getDetailedAnswer()
        );
    }

}

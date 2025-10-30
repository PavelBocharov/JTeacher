package com.mar.service.library;

import com.mar.data.QuestionInfo;
import com.mar.model.Type;

public interface LibraryService {

    QuestionInfo getRandomByType(String type);
    QuestionInfo getById(String type, String id);
    QuestionInfo getNext(String type, Long position);
    Type getTypeInfo(String type);

}

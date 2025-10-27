package com.mar.service.library;

import com.mar.data.Question;

public interface LibraryService {

    Question getRandomByType(String type);
    Question getById(String type, String id);
    Question getNext(String type, Long position);

}

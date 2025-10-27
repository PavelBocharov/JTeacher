package com.mar.service.db;

import com.mar.model.LastUserMsg;

public interface DatabaseService {

    LastUserMsg getByUserId(Long userId);
    void saveLastUserMessage(LastUserMsg lastUserMsg);
    void delete(LastUserMsg lastUserMsg);

}

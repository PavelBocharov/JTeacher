package com.mar.service.db;

import com.mar.data.PeeAndPoopData;
import com.mar.model.LastUserMsg;
import com.mar.model.UserChart;

public interface DatabaseService {

    LastUserMsg getByUserId(Long userId);

    void saveLastUserMessage(LastUserMsg lastUserMsg);

    void delete(LastUserMsg lastUserMsg);

    void saveUserChartData(UserChart userChart);

    PeeAndPoopData getUsrData(Long userId);
}

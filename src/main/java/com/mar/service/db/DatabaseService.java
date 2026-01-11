package com.mar.service.db;

import com.mar.data.PeeAndPoopData;
import com.mar.model.UserChart;

public interface DatabaseService {

    void saveUserChartData(UserChart userChart);

    PeeAndPoopData getUsrData(Long userId);
}

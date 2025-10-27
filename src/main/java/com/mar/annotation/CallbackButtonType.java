package com.mar.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CallbackButtonType {
    CALLBACK_START("/start", "START"),
    CALLBACK_JAVA("/java", "☕\uFE0F Java/Spring"),
    CALLBACK_SQL("/sql", "\uD83D\uDDC4 SQL"),
    CALLBACK_PYTHON("/python", "\uD83D\uDC0D Python"),
    CALLBACK_CLOUD("/cloud", "☁\uFE0F Cloud (Docker, MQ, Kuber, etc.)"),
    CALLBACK_ALL("/all", "ALL"),
    CALLBACK_ANSWER("/answer", "Генерируется в рантайме."),
    CALLBACK_DETAIL_ANSWER("/detail_answer", "\uD83D\uDCAC Подробный ответ.");

    final String type;
    final String text;

    public static CallbackButtonType findByType(String type) {
        return Arrays.stream(values())
                .filter(callback -> callback.type.equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }

    public static boolean hasType(String type) {
        if (type == null || type.isBlank()) {
            return false;
        }
        return findByType(type) != null;
    }
}
package com.mar.utils;

import com.mar.data.QuestionInfo;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
@UtilityClass
public class BotUtils {

    public static void sendMessage(TelegramBot bot, SendMessage msg, BiConsumer<BaseRequest, SendResponse> workWithResponse) {
        msg.parseMode(ParseMode.Markdown);
        workWithResponse.accept(msg, bot.execute(msg));
    }

    public static void sendPhoto(TelegramBot bot, long chatId, String imgPath, String caption, InlineKeyboardMarkup buttons, BiConsumer<BaseRequest, SendResponse> workWithResponse) {
        try {
            if (imgPath != null && !imgPath.isBlank()) {
                File img = new File(imgPath);
                SendPhoto msg = new SendPhoto(chatId, img).caption(caption).replyMarkup(buttons);
                sendPhoto(bot, msg, workWithResponse);
            } else {
                throw new Exception("Image path null or blank.");
            }
        } catch (Exception e) {
            log.warn("Cannot find image by path: {}.", imgPath, e);
            sendMessage(bot, new SendMessage(chatId, caption).replyMarkup(buttons), workWithResponse);
        }
    }

    public static void sendPhoto(TelegramBot bot, long chatId, byte[] img, String caption, InlineKeyboardMarkup buttons, BiConsumer<BaseRequest, SendResponse> workWithResponse) {
        try {
            if (img != null && img.length > 0) {
                SendPhoto msg = new SendPhoto(chatId, img).caption(caption).replyMarkup(buttons);
                sendPhoto(bot, msg, workWithResponse);
            } else {
                throw new Exception("Image byte array null or empty.");
            }
        } catch (Exception e) {
            log.warn("Cannot find image byte[].", e);
            sendMessage(bot, new SendMessage(chatId, caption).replyMarkup(buttons), workWithResponse);
        }
    }

    public static void sendPhoto(TelegramBot bot, SendPhoto msg, BiConsumer<BaseRequest, SendResponse> workWithResponse) {
        workWithResponse.accept(msg, bot.execute(msg));
    }

    public static QuestionInfo shaffleOptions(QuestionInfo question) {
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
        return question;
    }

}

package com.mar.utils;

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

    public static void sendPhoto(TelegramBot bot, SendPhoto msg, BiConsumer<BaseRequest, SendResponse> workWithResponse) {
        workWithResponse.accept(msg, bot.execute(msg));
    }

}

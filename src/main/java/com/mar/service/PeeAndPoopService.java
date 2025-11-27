package com.mar.service;

import com.mar.data.UserMsg;
import com.mar.model.PeePoopEnum;
import com.mar.model.UserChart;
import com.mar.service.db.DatabaseService;
import com.mar.utils.BotUtils;
import com.mar.utils.PeeAndPoopUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Slf4j
public class PeeAndPoopService {

    public static final String POOP_CALLBACK = "/add_poop";
    public static final String PEE_CALLBACK = "/add_pee";

    private final TelegramBot bot;
    private final DatabaseService databaseService;

    public PeeAndPoopService(String botToken, DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.bot = new TelegramBot.Builder(botToken).okHttpClient(new OkHttpClient()).build();
        bot.setUpdatesListener(updates -> {
            updates.parallelStream().forEach(this::doIt);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void doIt(Update update) {
        Message msg = update.message();
        CallbackQuery callback = update.callbackQuery();
        UserMsg userMsg = null;
        if (nonNull(msg)) {
            log.trace("Get msg: {}", msg);
            userMsg = new UserMsg(
                    msg.from().id(),
                    msg.from().username(),
                    msg.chat().id(),
                    Optional.ofNullable(msg.text()).orElse("").trim().toLowerCase(),
                    msg.messageId()
            );
            log.debug("Get user message: {}", userMsg);
        }
        if (nonNull(callback)) {
            log.trace("Get callback: {}", callback);
            userMsg = new UserMsg(
                    callback.from().id(),
                    callback.from().username(),
                    callback.from().id(),
                    Optional.ofNullable(callback.data()).orElse("").trim().toLowerCase(),
                    callback.maybeInaccessibleMessage().messageId()
            );
            log.debug("Get user callback: {}, callback id: {}", userMsg, callback.id());
            saveCallback(userMsg);
        }

        if (userMsg == null) {
            return;
        }
        answerMsg(userMsg);
    }

    private void saveCallback(UserMsg msg) {
        UserChart userChart = UserChart.builder()
                .date(LocalDate.now())
                .userId(msg.getId())
                .data(PEE_CALLBACK.equals(msg.getText()) ? PeePoopEnum.PEE : PeePoopEnum.POOP)
                .build();
        databaseService.saveUserChartData(userChart);
    }

    private void answerMsg(UserMsg userMsg) {
        byte[] img = PeeAndPoopUtils.getChartImage(databaseService.getUsrData(userMsg.getId()));
        BotUtils.sendPhoto(
                bot,
                userMsg.getChatId(),
                img,
                "Your Pee&Poop chart.",
                new InlineKeyboardMarkup()
                        .addRow(new InlineKeyboardButton("\uD83D\uDCA9+1").callbackData(POOP_CALLBACK))
                        .addRow(new InlineKeyboardButton("\uD83D\uDCA6+1").callbackData(PEE_CALLBACK)),
                (request, response) -> {
                    if (!response.isOk()) {
                        log.warn("Not send. {}", response.description());
                    }
                }
        );
    }


}

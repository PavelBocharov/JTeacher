package com.mar.service;

import com.mar.annotation.CallbackButtonType;
import com.mar.annotation.CallbackMethod;
import com.mar.data.Question;
import com.mar.data.UserMsg;
import com.mar.model.LastUserMsg;
import com.mar.service.db.DatabaseService;
import com.mar.service.library.LibraryService;
import com.mar.utils.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mar.annotation.CallbackButtonType.CALLBACK_ALL;
import static com.mar.annotation.CallbackButtonType.CALLBACK_ANSWER;
import static com.mar.annotation.CallbackButtonType.CALLBACK_CLOUD;
import static com.mar.annotation.CallbackButtonType.CALLBACK_DETAIL_ANSWER;
import static com.mar.annotation.CallbackButtonType.CALLBACK_JAVA;
import static com.mar.annotation.CallbackButtonType.CALLBACK_PYTHON;
import static com.mar.annotation.CallbackButtonType.CALLBACK_SQL;
import static com.mar.annotation.CallbackButtonType.CALLBACK_START;
import static java.util.Objects.nonNull;

@Slf4j
public class BotService {

    private TelegramBot bot;
    private LibraryService libraryService;
    private DatabaseService databaseService;
    private String startImagePath;
    private String baseImagePath;

    private final Map<CallbackButtonType, Method> callbackMethods = new HashMap<>();

    public BotService(String botToken, LibraryService libraryService, DatabaseService databaseService) {
        init(botToken, libraryService, databaseService, null, null);
    }

    public BotService(String botToken, LibraryService libraryService, DatabaseService databaseService, String startImagePath, String baseImagePath) {
        init(botToken, libraryService, databaseService, startImagePath, baseImagePath);
    }

    private void init(String botToken, LibraryService libraryService, DatabaseService databaseService, String startImagePath, String baseImagePath) {
        this.libraryService = libraryService;
        this.databaseService = databaseService;
        this.startImagePath = startImagePath;
        this.baseImagePath = baseImagePath;
        bot = new TelegramBot.Builder(botToken).okHttpClient(new OkHttpClient()).build();
        bot.setUpdatesListener(updates -> {
            updates.parallelStream().forEach(this::doIt);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        Method[] methods = BotService.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(CallbackMethod.class)) {
                CallbackMethod cm = method.getAnnotation(CallbackMethod.class);
                for (CallbackButtonType callbackButtonType : cm.value()) {
                    callbackMethods.put(callbackButtonType, method);
                }
            }
        }
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
        }

        if (userMsg == null) {
            return;
        }

        LastUserMsg lum;
        if ((lum = databaseService.getByUserId(userMsg.getId())) != null) {
            bot.execute(new DeleteMessage(userMsg.getChatId(), lum.getLastMsgId()));
            databaseService.delete(lum);
        }
        databaseService.saveLastUserMessage(LastUserMsg.builder()
                .userId(userMsg.getId())
                .lastMsgId(userMsg.getMsgId())
                .build());

        String[] tokens = userMsg.getText().split("~");
        CallbackButtonType callbackType = CallbackButtonType.findByType(tokens[0]);
        try {
            if (callbackMethods.containsKey(callbackType)) {
                Method method = callbackMethods.get(callbackType);
                method.setAccessible(true);
                method.invoke(this, userMsg);
                return;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Cannot callback processing: {}. UserMsg: {}", callbackType, userMsg, e);
        }

        baseAnswer(userMsg);
    }

    @CallbackMethod(CALLBACK_START)
    private void startAction(UserMsg userMsg) {
        BotUtils.sendPhoto(
                bot,
                userMsg.getChatId(),
                startImagePath,
                "\uD83E\uDDD1\u200D\uD83D\uDCBB Выбери подготовленные тесты.",
                new InlineKeyboardMarkup()
                        .addRow(new InlineKeyboardButton(CALLBACK_JAVA.getText()).callbackData(CALLBACK_JAVA.getType()))
                        .addRow(new InlineKeyboardButton(CALLBACK_SQL.getText()).callbackData(CALLBACK_SQL.getType()))
                        .addRow(new InlineKeyboardButton(CALLBACK_PYTHON.getText()).callbackData(CALLBACK_PYTHON.getType())),
                (request, response) -> workWithResponse(userMsg, request, response)
        );
    }

    @CallbackMethod({CALLBACK_JAVA, CALLBACK_PYTHON, CALLBACK_SQL, CALLBACK_CLOUD, CALLBACK_ALL})
    private void sendRandomQuestionByType(UserMsg userMsg) {
        CallbackButtonType callbackType = CallbackButtonType.findByType(userMsg.getText());
        Question question = libraryService.getRandomByType(callbackType.getType());
        log.trace("Find random question by type: '{}', Question: {}", callbackType, question);
        if (question == null) {
            SendMessage msg = new SendMessage(userMsg.getChatId().longValue(), "Для данного типа не нашлось вопросов.")
                    .replyMarkup(new InlineKeyboardMarkup(
                            new InlineKeyboardButton("Вернуться к списку").callbackData(CALLBACK_START.getType()))
                    );
            BotUtils.sendMessage(bot, msg, (request, response) -> workWithResponse(userMsg, request, response));
        } else {
            StringBuilder text = new StringBuilder(question.getQuestion()).append("\n\n");

            List<InlineKeyboardButton> buttons = new ArrayList<>(4);
            question.getOptions().stream().sorted().forEachOrdered(s -> {
                        String option = String.valueOf(s.charAt(0));
                        text.append("❓ ").append(s).append("\n");
                        buttons.add(new InlineKeyboardButton(option).callbackData(CALLBACK_ANSWER.getType() + "~" + callbackType.getType() + "~" + question.getId() + "~" + option));
                    }
            );
            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(buttons.toArray(new InlineKeyboardButton[]{}));

            SendMessage msg = new SendMessage(userMsg.getChatId().longValue(), text.toString())
                    .replyMarkup(keyboard);

            BotUtils.sendMessage(bot, msg, (request, response) -> workWithResponse(userMsg, request, response));
        }
    }

    @CallbackMethod(CALLBACK_ANSWER)
    private void checkAnswer(UserMsg userMsg) {
        String[] tokens = userMsg.getText().split("~");
        log.trace("Answer tokens: {}", Arrays.toString(tokens));
        Question question = libraryService.getById(tokens[1], tokens[2]);
        SendMessage msg;
        if (question.getCorrectAnswer().equalsIgnoreCase(tokens[3])) {
            msg = new SendMessage(userMsg.getChatId().longValue(), "✅ Все правильно");
        } else {
            String badAnswer = "❌ Ответ неверный\n\n" + question.getOptions().stream()
                    .filter(s -> s.startsWith(question.getCorrectAnswer()))
                    .findFirst().orElse("");
            msg = new SendMessage(userMsg.getChatId().longValue(), badAnswer);
        }
        msg = msg.replyMarkup(
                new InlineKeyboardMarkup()
                        .addRow(new InlineKeyboardButton(CALLBACK_DETAIL_ANSWER.getText()).callbackData(CALLBACK_DETAIL_ANSWER.getType() + "~" + tokens[1] + "~" + question.getId()))
                        .addRow(new InlineKeyboardButton("➡️ Другой вопрос.").callbackData(tokens[1]))
                        .addRow(new InlineKeyboardButton("\uD83D\uDCCB Вернуться к списку тем.").callbackData(CALLBACK_START.getType()))
        );
        BotUtils.sendMessage(bot, msg, (request, response) -> workWithResponse(userMsg, request, response));
    }

    @CallbackMethod(CALLBACK_DETAIL_ANSWER)
    private void sendDetailAnswer(UserMsg userMsg) {
        String[] tokens = userMsg.getText().split("~");
        log.trace("Detail answer tokens: {}", Arrays.toString(tokens));
        Question question = libraryService.getById(tokens[1], tokens[2]);
        SendMessage msg = new SendMessage(userMsg.getChatId().longValue(), question.getDetailedAnswer())
                .replyMarkup(
                        new InlineKeyboardMarkup()
                                .addRow(new InlineKeyboardButton("➡️ Другой вопрос.").callbackData(tokens[1]))
                                .addRow(new InlineKeyboardButton("\uD83D\uDCCB Вернуться к списку тем.").callbackData(CALLBACK_START.getType()))
                );
        BotUtils.sendMessage(bot, msg, (request, response) -> workWithResponse(userMsg, request, response));
    }

    private void baseAnswer(UserMsg userMsg) {
        BotUtils.sendPhoto(
                bot,
                userMsg.getChatId(),
                baseImagePath,
                "Нажмите START для начала работы с ботом.",
                new InlineKeyboardMarkup(new InlineKeyboardButton(CALLBACK_START.getText()).callbackData(CALLBACK_START.getType())),
                (request, response) -> workWithResponse(userMsg, request, response)
        );
    }

    private void workWithResponse(UserMsg userMsg, BaseRequest request, SendResponse response) {
        if (!response.isOk()) {
            log.warn("BOT executor error >>> RQ:\n{}\nError: {}", request, response.description());
        } else {
            log.trace("BOT executor >>> RQ: {}, RS: {}", request, response);
            LastUserMsg lastUserMsg = databaseService.getByUserId(userMsg.getId());
            if (lastUserMsg != null) {
                bot.execute(new DeleteMessage(userMsg.getChatId(), lastUserMsg.getLastMsgId()));
                databaseService.delete(lastUserMsg);
                databaseService.saveLastUserMessage(
                        LastUserMsg.builder()
                                .userId(userMsg.getId())
                                .lastMsgId(response.message().messageId())
                                .build()
                );
            }
        }
    }

}

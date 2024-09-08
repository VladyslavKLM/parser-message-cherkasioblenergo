package innovateiu.task.parsermessagecherkasioblenergo;

import innovateiu.task.parsermessagecherkasioblenergo.config.BotConfig;
import innovateiu.task.parsermessagecherkasioblenergo.service.ParserMessageService;
import innovateiu.task.parsermessagecherkasioblenergo.service.ReturnMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.time.LocalDate;
import java.util.*;

@Component
public class ParserMessageBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final Set<Long> usersIds = new HashSet<>();
    private final Map<Long, String> numberScheduleInUser = new HashMap<>();
    private final ParserMessageService parserMessageService;
    private final ReturnMessageService returnMessageService;
    private final Long authorId = 420438243L;

    @Autowired
    public ParserMessageBot(BotConfig botConfig, ParserMessageService parserMessageService, ReturnMessageService returnMessageService) {
        this.botConfig = botConfig;
        this.parserMessageService = parserMessageService;
        this.returnMessageService = returnMessageService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String message = update.getMessage().getText();
            if (message.equals("/start")) {
                usersIds.add(chatId);
                sendQueueButtons(chatId);
            } else {
                try {
                    numberScheduleInUser.put(chatId, message);
                    sendMessageWithGraphs(chatId, message);
                } catch (NumberFormatException e) {
                    if (chatId != authorId) {
                        sendMessage(chatId, "Введіть правильний номер черги.");
                    }
                }
            }
            if (chatId == authorId) {
                parserMessageService.processMessage(message);
                notifyUsers();
            }
        } else if (update.hasCallbackQuery()) {
            String dataWithButton = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            numberScheduleInUser.put(chatId, dataWithButton);
            sendMessageWithGraphs(chatId, dataWithButton);
        }

    }

    private void sendMessageWithGraphs(long chatId, String data) {
        int queueNumber = Integer.parseInt(data);
        List<String> schedule = returnMessageService.getScheduleByNumber(queueNumber);
        sendMessage(chatId, "Графіки відключення світла " + data + " черги");
        sendMessage(chatId, schedule.isEmpty() ? "Графік відсутній" : String.join("\n", schedule));
    }

    private void notifyUsers() {
        for (long chatId : usersIds) {
            sendMessage(chatId, "ОНОВЛЕННЯ ГРАФІКІВ");
            sendMessageWithGraphs(chatId, numberScheduleInUser.get(chatId));
        }
    }

    private void sendQueueButtons(long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        Set<Integer> queues = returnMessageService.getAllSchedules(LocalDate.now());

        for (Integer queue : queues) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Черга " + queue);
            button.setCallbackData(queue.toString());
            rows.add(List.of(button));
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Виберіть чергу для перегляду графіка відключень:");
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException ignored) {
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException ignored) {
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}

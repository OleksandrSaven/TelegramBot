package com.example.telegrambot;

import com.example.telegrambot.dto.VacancyDto;
import com.example.telegrambot.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VacanciesBot extends TelegramLongPollingBot {
    @Autowired
    private VacancyService vacancyService;
    private final Map<Long, String> lastShownVacancyLevel = new HashMap<>();
    public VacanciesBot() {
        super("6285378782:AAGJqpHTXH09OpSh8VJeuDXamnEo3kDSa4k");
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.getMessage() != null) {
                handleStartCommand(update);
            }
            if (update.getCallbackQuery() != null) {
                String callbackData = update.getCallbackQuery().getData();

                if ("Show junior Vacancies".equals(callbackData)) {
                    showJuniorVacancies(update);
                } else if ("Show middle Vacancies".equals(callbackData)) {
                    showMiddleVacancies(update);
                } else if ("Show senior Vacancies".equals(callbackData)) {
                    showSeniorVacancies(update);
                } else if (callbackData.startsWith("vacancyId")) {
                    String id = callbackData.split("=")[1];
                    showVacancyDescription(id,update);
                } else if ("backToVacancies".equals(callbackData)) {
                    handleBackToVacanciesCommand(update);
                } else if ("backToStartMenu".equals(callbackData)) {
                    handleBackToStartCommand(update);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't send message to user!", e);

        }
    }


    private void handleBackToVacanciesCommand(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String level = lastShownVacancyLevel.get(chatId);

        if ("junior".equals(level)) {
            showJuniorVacancies(update);
        } else if ("middle".equals(level)) {
            showMiddleVacancies(update);
        } else if ("senior".equals(level)) {
            showSeniorVacancies(update);
        }
    }

    private void handleBackToStartCommand(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Choose title");
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setReplyMarkup(getStartMenu());
        execute(sendMessage);
    }
    private void showSeniorVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy:");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getSeniorVacanciesMenu());
        execute(sendMessage);

        lastShownVacancyLevel.put(chatId, "senior");

    }

    private ReplyKeyboard getSeniorVacanciesMenu() {
        List<VacancyDto> vacancies = vacancyService.getSeniorVacancies();
        return getVacanciesMenu(vacancies);

    }

    private void showMiddleVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy:");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getMiddleVacanciesMenu());
        execute(sendMessage);

        lastShownVacancyLevel.put(chatId, "middle");

    }

    private ReplyKeyboard getMiddleVacanciesMenu() {
        List<VacancyDto> vacancies = vacancyService.getMiddleVacancies();
        return  getVacanciesMenu(vacancies);

    }

    private void showVacancyDescription (String id, Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        VacancyDto vacancy = vacancyService.get(id);

        String vacancyInfo = """
                *Title:* %s
                *Company:* %s
                *Short Description:* %s
                *Description:* %s
                *Salary:* %s
                *Link:* [%s](%s)
                """.formatted(
                escapeMarkdownReservedChars(vacancy.getTitle()),
                escapeMarkdownReservedChars(vacancy.getCompany()),
                escapeMarkdownReservedChars(vacancy.getShortDescription()),
                escapeMarkdownReservedChars(vacancy.getLongDescription()),
                vacancy.getSalary().isBlank() ? "Not specified" : escapeMarkdownReservedChars(vacancy.getSalary()),
                "Click here for more details",
                escapeMarkdownReservedChars(vacancy.getLink())
        );
        sendMessage.setText(vacancyInfo);
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);
        sendMessage.setReplyMarkup(getBackToVacanciesMenu());
        execute(sendMessage);
    }
    private String escapeMarkdownReservedChars(String text) {
        return text.replace("-","\\-")
                .replace("_","\\_")
                .replace("*","\\*")
                .replace("[","\\[")
                .replace("]","\\]")
                .replace("(","\\(")
                .replace(")","\\)")
                .replace("~","\\~")
                .replace("`","\\`")
                .replace(">","\\>")
                .replace("#","\\#")
                .replace("+","\\+")
                .replace(".","\\.")
                .replace("!","\\!");
    }

    private ReplyKeyboard getBackToVacanciesMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton backToVacanciesButton = new InlineKeyboardButton();
        backToVacanciesButton.setText("Back to vacancies");
        backToVacanciesButton.setCallbackData("backToVacancies");
        row.add(backToVacanciesButton);

        InlineKeyboardButton backToStartMenuButton = new InlineKeyboardButton();
        backToStartMenuButton.setText("Back to start menu");
        backToStartMenuButton.setCallbackData("backToStartMenu");
        row.add(backToStartMenuButton);

        InlineKeyboardButton coverLetterButton = new InlineKeyboardButton();
        coverLetterButton.setText("Get cover letter");
        coverLetterButton.setUrl("https://chat.openai.com/");
        row.add(coverLetterButton);

        return new InlineKeyboardMarkup(List.of(row));
    }
    private void showJuniorVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy:");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getJuniorVacanciesMenu());
        execute(sendMessage);

        lastShownVacancyLevel.put(chatId, "junior");
    }

    private ReplyKeyboard getJuniorVacanciesMenu() {
        List<VacancyDto> vacancies = vacancyService.getJuniorVacancies();
        return getVacanciesMenu(vacancies);
    }


    private ReplyKeyboard getVacanciesMenu(List<VacancyDto> vacancies ) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        for(VacancyDto vacancy: vacancies) {
            InlineKeyboardButton vacancyButton = new InlineKeyboardButton();
            vacancyButton.setText(vacancy.getTitle());
            vacancyButton.setCallbackData("vacancyId=" + vacancy.getId());
            row.add(vacancyButton);
        }

        InlineKeyboardMarkup keyboard  = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        return keyboard;
    }
    private void handleStartCommand(Update update) {
        String text = update.getMessage().getText();
        System.out.println("Recived :" + text);
        SendMessage sendMessage  = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Welcome to vacancies bot! Please, choose you title:");
        sendMessage.setReplyMarkup(getStartMenu());

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private ReplyKeyboard getStartMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();

        String label = "Junior\uD83D\uDE22";

        InlineKeyboardButton junior = new InlineKeyboardButton(label);
        //junior.setText("Junior");
        junior.setCallbackData("Show junior Vacancies");
        row.add(junior);

        InlineKeyboardButton middle = new InlineKeyboardButton();
        middle.setText("Middle");
        middle.setCallbackData("Show middle Vacancies");
        row.add(middle);

        InlineKeyboardButton senior = new InlineKeyboardButton();
        senior.setText("Senior");
        senior.setCallbackData("Show senior Vacancies");
        row.add(senior);

        InlineKeyboardMarkup keyboard  = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        return keyboard;

    }

    @Override
    public String getBotUsername() {
        return "osa vacancies bot";
    }
}

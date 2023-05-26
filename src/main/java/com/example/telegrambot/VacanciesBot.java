package com.example.telegrambot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class VacanciesBot extends TelegramLongPollingBot {
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
                } else if (callbackData.startsWith("vacancyId=1")) {
                    String id = callbackData.split("=")[1];
                    showVacancyDescription(id,update);
                } else if (callbackData.startsWith("vacancyId=2")) {
                    String id = callbackData.split("=")[1];
                    showVacancyDescription(id, update);
                }
                //------------------------------------------------------------
                   else if ("Show middle Vacancies".equals(callbackData)) {
                    showMiddleVacancies(update);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't send message to user!", e);

        }
    }

    private void showMiddleVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy:");
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setReplyMarkup(getMiddleVacanciesMenu());
        execute(sendMessage);
    }

    private ReplyKeyboard getMiddleVacanciesMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton midVacancyMate = new InlineKeyboardButton();
        midVacancyMate.setText("Middle jawa developer at MA");
        midVacancyMate.setCallbackData("vacancyId=3");
        row.add(midVacancyMate);

        InlineKeyboardButton midVacancyAmazon = new InlineKeyboardButton();
        midVacancyAmazon.setText("Middle jawa developer at Amazon");
        midVacancyAmazon.setCallbackData("vacancyid=4");
        row.add(midVacancyAmazon);

        InlineKeyboardMarkup keyboard  = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        return keyboard;
    }

    private void showVacancyDescription (String id, Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setText("Vecancy description for vecancy with id " + id);
        execute(sendMessage);
    }
    private void showJuniorVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy:");
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setReplyMarkup(getJuniorVacanciesMenu());
        execute(sendMessage);
    }

    private ReplyKeyboard getJuniorVacanciesMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton myVacancy = new InlineKeyboardButton();
        myVacancy.setText("Junior jawa developer at MA");
        myVacancy.setCallbackData("vacancyId=1");
        row.add(myVacancy);

        InlineKeyboardButton googleVacancy = new InlineKeyboardButton();
        googleVacancy.setText("Junior jawa developer at Google");
        googleVacancy.setCallbackData("vacancyId=2");
        row.add(googleVacancy);

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
        InlineKeyboardButton junior = new InlineKeyboardButton();
        junior.setText("Junior");
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

package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "MyTelegrammHelperBot";
    public static final String TELEGRAM_BOT_TOKEN = "7070715281:AAESalbpbnibLE-yL1yxmVki_iESMXI4DvU";
    public static final String OPEN_AI_TOKEN = "gpt:YmaS8RBmuYm3tDQQBKzoJFkblB3TwvG9ujguLFP3HbfwU2qc";
    public DialogMode mode = DialogMode.MAIN;
    public ChatGPTService gptService = new ChatGPTService(OPEN_AI_TOKEN);
    private List<String> chat;
    private UserInfo myInfo;
    private int questionNumber;
    private UserInfo personInfo;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {

        String message = getMessageText();

        switch (message) {
            case "/start" -> {
                mode = DialogMode.MAIN;

                showMainMenu(
                        "Main menu", "/start",
                        "GPT", "/gpt",
                        "DATE", "/date",
                        "MESSAGE", "/message",
                        "PROFILE", "/profile"

                );
                String menu = loadMessage("main");
                sendTextMessage("_" + "Hello!" + "_");
                sendTextMessage(menu);
                sendPhotoMessage("Main");

                return;
            }
            case "/gpt" -> {
                mode = DialogMode.GPT;

                String gptMessage = loadMessage("gpt");
                sendTextMessage(gptMessage);
                sendPhotoMessage("gpt");

                return;
            }


            case "/date" -> {
                mode = DialogMode.DATE;

                String dateMessage = loadMessage("date");
                sendPhotoMessage("date");
                sendTextButtonsMessage(dateMessage, "Аріана Гранде \uD83D\uDD25", "date_grande",
                        "Марго Роббі \uD83D\uDD25\uD83D\uDD25", "date_robbie",
                        "Зендея \uD83D\uDD25\uD83D\uDD25\uD83D\uDD25", "date_zendaya",
                        "Райан Гослінг \uD83D\uDE0E", "date_gosling",
                        "Том Харді \uD83D\uDE0E\uD83D\uDE0E", "date_hardy");
                return;

            }

            case "/message" -> {
                mode = DialogMode.MESSAGE;
                String gptMessageHelper = loadMessage("message");

                sendPhotoMessage("message");

                sendTextButtonsMessage(gptMessageHelper,
                        "Следующее сообщение", "message_next",
                        "Отправить запрос на стыковку", "message_date");
                chat = new ArrayList<>();

                return;
            }
            case "/profile" -> {
                mode = DialogMode.PROFILE;
                String profileMessage = loadMessage("profile");
                sendTextMessage(profileMessage);
                sendPhotoMessage("profile");
                myInfo = new UserInfo();
                questionNumber = 1;
                sendTextMessage("Ваше имя:");

                return;
            }
            case "/opener" -> {
                mode = DialogMode.OPENER;
                String openerMessage = loadMessage("opener");
                sendTextMessage(openerMessage);
                sendPhotoMessage("opener");
                personInfo = new UserInfo();
                questionNumber = 1;
                sendTextMessage("Введите имя :");

                return;
            }
        }
        switch (mode) {
            case GPT -> {
                String prompt = loadPrompt("gpt");
                Message msg = sendTextMessage(".....думаю");
                String answer = gptService.sendMessage(prompt, message);
                updateTextMessage(msg, answer);


            }
            case DATE -> {
                String query = getCallbackQueryButtonKey();

                if (query.startsWith("date_")) {
                    sendPhotoMessage(query);
                    String prompt = loadPrompt(query);
                    gptService.setPrompt(prompt);
                    sendTextMessage("Так и будешь молчать ?");
                    return;
                }
                Message msg = sendTextMessage(".....думаю");
                String answer = gptService.addMessage(message);
                updateTextMessage(msg, answer);



            }
            case MESSAGE -> {

                String query = getCallbackQueryButtonKey();

                if (query.startsWith("message_")) {
                    String prompt = loadPrompt(query);
                    String history = String.join("/n/n", chat);

                    Message msg = sendTextMessage(".....думаю");

                    String answer = gptService.sendMessage(prompt, history);
                    updateTextMessage(msg, answer);
                }
                chat.add(message);

            }
            case PROFILE -> {
                if (questionNumber <= 6) {
                    askQuestion(message, myInfo,"profile");
                }
            }
            case OPENER -> {
                if(questionNumber <= 6){
                    askQuestion(message,personInfo,"opener");
                }
            }


        }
    }
    private void askQuestion(String message,UserInfo user,String profileName){
        switch (questionNumber){
            case 1 ->{
                user.name = message;
                questionNumber = 2;
                sendTextMessage("Введите возраст:");
                return;
            }
            case 2 -> {
                user.age = message;
                questionNumber = 3;
                sendTextMessage(" Введите город :");
                return;
            }
            case 3 -> {
                user.city = message;
                questionNumber = 4;
                sendTextMessage("Введите профессию :");
                return;
            }
            case 4 -> {
                user.occupation = message;
                questionNumber = 5;
                sendTextMessage("Введите хобби :");
                return;
            }
            case 5 ->{
                user.hobby = message;
                questionNumber = 6;
                sendTextMessage("Введите цель знакомства : ");
                        return;
            }
            case 6 ->{
                user.goals = message;
                String prompt = loadPrompt(profileName);
                Message msg = sendTextMessage(".....думаю");
                String answer  = gptService.sendMessage(prompt,user.toString());
                updateTextMessage(msg,answer);

                return;
            }
        }
    }
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}

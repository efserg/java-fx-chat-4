package ru.gb.javafxchat4.client;

import static ru.gb.javafxchat4.Command.AUTHOK;
import static ru.gb.javafxchat4.Command.CLIENTS;
import static ru.gb.javafxchat4.Command.END;
import static ru.gb.javafxchat4.Command.ERROR;
import static ru.gb.javafxchat4.Command.MESSAGE;
import static ru.gb.javafxchat4.Command.STOP;
import static ru.gb.javafxchat4.Command.getCommand;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javafx.application.Platform;
import ru.gb.javafxchat4.Command;

public class ChatClient {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final ChatController controller;

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                if (waitAuth()) {
                    readMessages();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }).start();

    }

    private boolean waitAuth() throws IOException {
        while (true) {
            final String message = in.readUTF();
            final Command command = getCommand(message);
            final String[] params = command.parse(message);
            if (command == AUTHOK) { // /authok nick1
                final String nick = params[0];
                controller.setAuth(true);
                controller.addMessage("Успешная авторизация под ником " + nick);
                return true;
            }
            if (command == ERROR) {
                Platform.runLater(() -> controller.showError(params[0]));
                continue;
            }
            if (command == STOP) {
                Platform.runLater(() -> controller.showError("Истекло время на авторизацию, перезапустите приложение"));
                try {
                    Thread.sleep(5000); // Без sleep пользователь не увидит сообщение об ошибке. Хочется более изящного решения, но лень его искать
                    sendMessage(END);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
    }

    private void closeConnection() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    private void readMessages() throws IOException {
        while (true) {
            final String message = in.readUTF();
            final Command command = getCommand(message);
            if (END == command) {
                controller.setAuth(false);
                break;
            }
            final String[] params = command.parse(message);
            if (ERROR == command) {
                String messageError = params[0];
                Platform.runLater(() -> controller.showError(messageError));
                continue;
            }
            if (MESSAGE == command) {
                Platform.runLater(() -> controller.addMessage(params[0]));
            }
            if (CLIENTS == command) {
                Platform.runLater(() -> controller.updateClientsList(params));
            }
        }
    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }
}

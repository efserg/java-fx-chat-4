package ru.gb.javafxchat4.client;

import java.io.IOException;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import ru.gb.javafxchat4.Command;

public class ChatController {
    @FXML
    private ListView<String> clientList;
    @FXML
    private TextField loginField;
    @FXML
    private HBox authBox;
    @FXML
    private PasswordField passField;
    @FXML
    private HBox messageBox;
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField messageField;

    private final ChatClient client;

    private String selectedNick;

    public ChatController() {
        this.client = new ChatClient(this);
        while (true) {
            try {
                client.openConnection();
                break;
            } catch (IOException e) {
                showNotification();
            }
        }
    }

    private void showNotification() {
        final Alert alert = new Alert(Alert.AlertType.ERROR,
                "Не могу подключится к серверу.\n" +
                        "Проверьте, что сервер запущен и доступен",
                new ButtonType("Попробовать снова", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        alert.setTitle("Ошибка подключения!");
        final Optional<ButtonType> answer = alert.showAndWait();
        final Boolean isExit = answer
                .map(select -> select.getButtonData().isCancelButton())
                .orElse(false);
        if (isExit) {
            System.exit(0);
        }

    }

    public void clickSendButton() {

        final String message = messageField.getText();
        if (message.isBlank()) {
            return;
        }
        if (selectedNick != null) {
            client.sendMessage(Command.PRIVATE_MESSAGE, selectedNick, message);
            selectedNick = null;
        } else {
            client.sendMessage(Command.MESSAGE, message);
        }
        messageField.clear();
        messageField.requestFocus();

    }

    public void addMessage(String message) {
        messageArea.appendText(message + "\n");
    }

    public void setAuth(boolean success) {
        authBox.setVisible(!success);
        messageBox.setVisible(success);
    }

    public void signinBtnClick() {
        client.sendMessage(Command.AUTH, loginField.getText(), passField.getText());
    }

    public void showError(String errorMessage) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage,
                new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle("Error!");
        alert.showAndWait();
    }

    public void selectClient(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            final String selectedNick = clientList.getSelectionModel().getSelectedItem();
            if (selectedNick != null && !selectedNick.isEmpty()) {
                this.selectedNick = selectedNick;
            }
        }
    }

    public void updateClientsList(String[] clients) {
        clientList.getItems().clear();
        clientList.getItems().addAll(clients);
    }

    public void signOutClick() {
        client.sendMessage(Command.END);
    }

    public ChatClient getClient() {
        return client;
    }
}
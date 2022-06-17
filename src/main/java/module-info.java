module ru.gb.javafxchat4 {
    requires javafx.controls;
    requires javafx.fxml;

    exports ru.gb.javafxchat4.client;
    opens ru.gb.javafxchat4.client to javafx.fxml;
}
package ru.gb.javafxchat4.server;

import java.io.Closeable;

public interface AuthService extends Closeable {
    String getNickByLoginAndPassword(String login, String password);
}

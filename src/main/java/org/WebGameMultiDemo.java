package org;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WebGameMultiDemo extends JFrame {

    private JPanel container;

    public WebGameMultiDemo(List<String> accounts, List<String> passwords, int multiCount) {
        setTitle("页游多开 + 自动登录");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(0, 2)); // 网格布局，多开显示

        container = new JPanel();
        container.setLayout(new GridLayout(0, 2));
        add(container);

        // 初始化 JavaFX
        new JFXPanel(); // 必须先初始化

        for (int i = 0; i < multiCount; i++) {
            final int idx = i;
            JPanel panel = new JPanel(new BorderLayout());
            add(panel);

            JFXPanel fxPanel = new JFXPanel();
            panel.add(fxPanel, BorderLayout.CENTER);

            Platform.runLater(() -> {
                WebView webView = new WebView();
                WebEngine webEngine = webView.getEngine();

                // 页游地址
                webEngine.load("https://my.4399.com/yxmsdzls/");

                fxPanel.setScene(new Scene(webView));

                // 自动登录
                webEngine.documentProperty().addListener((obs, oldDoc, newDoc) -> {
                    if (newDoc != null) {
                        String account = accounts.get(idx);
                        String password = passwords.get(idx);
                        String jsCode = ""
                                + "document.getElementById('username').value='" + account + "';"
                                + "document.getElementById('password').value='" + password + "';"
                                + "document.getElementById('loginBtn').click();";
                        webEngine.executeScript(jsCode);
                    }
                });
            });
        }

        setVisible(true);
    }

    public static void main(String[] args) {
        // 账号密码列表
        List<String> accounts = List.of("user1", "user2", "user3");
        List<String> passwords = List.of("pass1", "pass2", "pass3");

        SwingUtilities.invokeLater(() -> new WebGameMultiDemo(accounts, passwords, accounts.size()));
    }
}

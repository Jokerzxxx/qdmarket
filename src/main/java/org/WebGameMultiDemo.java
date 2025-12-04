package org;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;

public class WebGameMultiDemo {

    public static void main(String[] args) {
        int multiCount = 3; // 指定多开数量，比如 3 个窗口

        for (int i = 0; i < multiCount; i++) {
            int index = i + 1;
            SwingUtilities.invokeLater(() -> createGameWindow("页游窗口 " + index, "https://msdzls.cn/"));
        }
    }

    private static void createGameWindow(String title, String url) {
        JFrame frame = new JFrame(title);
        frame.setSize(1024, 768);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel, BorderLayout.CENTER);

        Platform.runLater(() -> {
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            webEngine.load(url);
            fxPanel.setScene(new Scene(webView));
        });

        frame.setVisible(true);
    }
}

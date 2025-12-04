package org;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;

public class WebGameEmbedDemo extends JFrame {

    private JFXPanel fxPanel;
    private WebEngine webEngine;

    public WebGameEmbedDemo() {
        setTitle("页游内嵌示例");
        setSize(1024, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 创建 JavaFX 面板嵌入 Swing
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);

        // 初始化 JavaFX 内容
        Platform.runLater(this::initFX);

        setVisible(true);
    }

    // 初始化 JavaFX WebView
    private void initFX() {
        WebView webView = new WebView();
        webEngine = webView.getEngine();

        // 页游地址（替换为你的游戏 URL）
        webEngine.load("https://msdzls.cn/");

        fxPanel.setScene(new Scene(webView));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WebGameEmbedDemo::new);
    }
}

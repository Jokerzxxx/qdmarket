package org;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WebGameMultiLoginDemo extends JFrame {

    private JFXPanel fxPanel;
    private WebEngine webEngine;
    private List<String> accounts = List.of("user1", "user2", "user3"); // 多账号
    private List<String> passwords = List.of("pass1", "pass2", "pass3");

    public WebGameMultiLoginDemo() {
        setTitle("多开页游自动登录示例");
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

    private void initFX() {
        WebView webView = new WebView();
        webEngine = webView.getEngine();

        // 监听页面加载完成
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // 遍历账号列表自动登录
                for (int i = 0; i < accounts.size(); i++) {
                    int idx = i;
                    Platform.runLater(() -> autoLogin(idx));
                }
            }
        });

        // 页游地址
        webEngine.load("https://my.4399.com/yxmsdzls/");

        fxPanel.setScene(new Scene(webView));
    }

    // 自动登录单个账号
    private void autoLogin(int idx) {
        if (webEngine == null) return;

        try {
            String account = accounts.get(idx);
            String password = passwords.get(idx);

            // 使用 querySelector 兼容动态元素
            String jsCode = ""
                    + "var userInput = document.querySelector(\"input[name='username']\");"
                    + "var passInput = document.querySelector(\"input[name='password']\");"
                    + "var loginBtn = document.querySelector(\"button[type='submit']\");"
                    + "if(userInput && passInput && loginBtn){"
                    + "  userInput.value='" + account + "';"
                    + "  passInput.value='" + password + "';"
                    + "  loginBtn.click();"
                    + "}";

            webEngine.executeScript(jsCode);
            System.out.println("账号 " + account + " 已执行自动登录脚本");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WebGameMultiLoginDemo::new);
    }
}

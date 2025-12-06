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

public class WebGameMultiOpenDemo extends JFrame {

    private JFXPanel fxPanel;
    private WebEngine webEngine;

    private List<String> accounts = List.of("user1", "user2", "user3");
    private List<String> passwords = List.of("pass1", "pass2", "pass3");

    private int accountIndex; // 当前窗口使用的账号索引

    public WebGameMultiOpenDemo(int index) {
        this.accountIndex = index;

        setTitle("多开页游 - 账号 " + accounts.get(index));
        setSize(1024, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);

        Platform.runLater(this::initFX);

        setVisible(true);
    }

    private void initFX() {
        WebView webView = new WebView();
        webEngine = webView.getEngine();

        // 页面加载完成后执行自动操作
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Platform.runLater(this::autoLogin);
            }
        });

        // 页游地址
        webEngine.load("https://my.4399.com/yxmsdzls/");

        fxPanel.setScene(new Scene(webView));
    }

    private void autoLogin() {
        String account = accounts.get(accountIndex);
        String password = passwords.get(accountIndex);
        String windowId = "window_" + accountIndex;

        String jsCode = """
            (function(){
                var windowId = '%s';
                if(window[windowId]) return;
                window[windowId] = true;

                var tryLogin = setInterval(function(){
                    var startBtn = document.querySelector('.j-startBtn');
                    if(startBtn){
                        startBtn.click();
                        console.log(windowId + ': 点击开始游戏');
                        clearInterval(tryLogin);

                        setTimeout(function(){
                            // 尝试切换账号密码登录方式
                            var loginTab = document.querySelector('.user-login-tab[data-login-type="4399"]');
                            if(loginTab) loginTab.click();
                            console.log(windowId + ': 切换到账号密码登录');

                            setTimeout(function(){
                                var userInput = document.querySelector('input[name=username]');
                                var passInput = document.querySelector('input[name=password]');
                                var loginBtn = document.querySelector('button#login');
                                if(userInput && passInput && loginBtn){
                                    userInput.value='%s';
                                    passInput.value='%s';
                                    loginBtn.click();
                                    console.log(windowId + ': 账号登录完成 %s');
                                } else {
                                    console.log(windowId + ': 未找到输入框或登录按钮');
                                }
                            }, 500);
                        }, 500);
                    }
                }, 500);
            })();
            """.formatted(windowId, account, password, account);

        webEngine.executeScript(jsCode);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WebGameMultiOpenDemo(0); // 第1个账号
            new WebGameMultiOpenDemo(1); // 第2个账号
            new WebGameMultiOpenDemo(2); // 第3个账号
        });
    }
}

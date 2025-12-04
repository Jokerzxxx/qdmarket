package org;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.win32.StdCallLibrary;

import javax.swing.*;
import java.awt.*;

public class GameEmbedDemo2 extends JFrame {

    private JPanel gamePanel;
    private HWND hGameWnd; // 游戏窗口句柄

    public GameEmbedDemo2() {
        setTitle("内嵌游戏 + 后台操作示例");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        gamePanel = new JPanel();
        gamePanel.setBackground(Color.BLACK);
        add(gamePanel, BorderLayout.CENTER);

        setVisible(true);

        // 启动并嵌入游戏
        startAndEmbedGame("D:\\Tencent\\QQNT\\QQ.exe", "我的游戏标题");

        // 添加面板大小监听，实现动态缩放
        gamePanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (hGameWnd != null) {
                    Rectangle r = gamePanel.getBounds();
                    User32.INSTANCE.MoveWindow(hGameWnd, 0, 0, r.width, r.height, true);
                }
            }
        });
    }

    private void startAndEmbedGame(String gamePath, String windowTitle) {
        new Thread(() -> {
            try {
                // 启动游戏
                Runtime.getRuntime().exec(gamePath);

                // 等待窗口出现
                while (hGameWnd == null) {
                    hGameWnd = User32.INSTANCE.FindWindowA(null, windowTitle);
                    Thread.sleep(500);
                }

                HWND hParent = new HWND(Native.getComponentPointer(gamePanel));

                // 设置父窗口
                User32.INSTANCE.SetParent(hGameWnd, hParent);

                // 去掉标题栏
                int style = User32.INSTANCE.GetWindowLong(hGameWnd, User32.GWL_STYLE);
                style = style & ~0x00C00000; // WS_CAPTION
                User32.INSTANCE.SetWindowLong(hGameWnd, User32.GWL_STYLE, style);

                // 调整窗口大小
                Rectangle r = gamePanel.getBounds();
                User32.INSTANCE.MoveWindow(hGameWnd, 0, 0, r.width, r.height, true);

                System.out.println("游戏已嵌入面板！");

                // 示例：后台点击窗口中心（可以根据需求修改位置）
                Thread.sleep(2000); // 等待游戏加载
                clickGameWindow(r.width / 2, r.height / 2);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 后台点击窗口坐标 (x, y) 相对于游戏窗口左上角
    private void clickGameWindow(int x, int y) {
        if (hGameWnd == null) return;

        int lParam = (y << 16) | (x & 0xFFFF);
        int wParam = 0;

        User32.INSTANCE.PostMessage(hGameWnd, User32.WM_LBUTTONDOWN, wParam, lParam);
        User32.INSTANCE.PostMessage(hGameWnd, User32.WM_LBUTTONUP, wParam, lParam);

        System.out.println("后台点击游戏窗口: " + x + "," + y);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameEmbedDemo2::new);
    }
}

// ================= JNA User32 接口 =================
interface User32 extends StdCallLibrary {
    User32 INSTANCE = Native.load("user32", User32.class);

    HWND FindWindowA(String lpClassName, String lpWindowName);
    HWND SetParent(HWND hWndChild, HWND hWndNewParent);
    int GetWindowLong(HWND hWnd, int nIndex);
    int SetWindowLong(HWND hWnd, int nIndex, int dwNewLong);
    boolean MoveWindow(HWND hWnd, int x, int y, int width, int height, boolean repaint);
    boolean PostMessage(HWND hWnd, int msg, int wParam, int lParam);

    int GWL_STYLE = -16;
    int WM_LBUTTONDOWN = 0x0201;
    int WM_LBUTTONUP = 0x0202;
    int WM_KEYDOWN = 0x0100;
    int WM_KEYUP = 0x0101;
}

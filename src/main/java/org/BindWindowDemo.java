package org;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.platform.win32.WinDef.RECT;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BindWindowDemo extends JFrame {

    private JLabel info;
    private long bindHwnd = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BindWindowDemo::new);
    }

    public BindWindowDemo() {
        setTitle("窗口绑定 + 识图点击 Demo");
        setSize(400, 300);
        setLayout(null);
        // -------------------- 修改图标 --------------------
        try {
            Image icon = ImageIO.read(new File("C:\\Users\\DELL\\IdeaProjects\\untitled\\src\\main\\resources\\icon1.png"));
            setIconImage(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JLabel dragLabel = new JLabel("按住这里拖动到目标窗口");
        dragLabel.setBounds(20, 20, 260, 60);
        dragLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dragLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(dragLabel);

        info = new JLabel("未绑定窗口");
        info.setBounds(20, 100, 360, 30);
        add(info);

        JButton clickBtn = new JButton("识图点击");
        clickBtn.setBounds(20, 150, 120, 30);
        add(clickBtn);

        // 捕获窗口句柄
        dragLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragLabel.setText("捕获中...");
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                onCaptureFinished(dragLabel);
            }
        });

        // 点击按钮识图并点击
        clickBtn.addActionListener(e -> performImageClick());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void onCaptureFinished(JLabel dragLabel) {
        bindHwnd = getWindowUnderMouse();
        String title = getWindowTitle(bindHwnd);
        info.setText("绑定: [" + bindHwnd + "] " + title);
        dragLabel.setText("按住这里拖动到目标窗口");
    }

    private long getWindowUnderMouse() {
        User32Ex u = User32Ex.INSTANCE;

        POINT pt = new POINT();
        u.GetCursorPos(pt);

        POINT.ByValue byValuePt = new POINT.ByValue();
        byValuePt.x = pt.x;
        byValuePt.y = pt.y;

        HWND hwnd = u.WindowFromPoint(byValuePt);

        if (hwnd == null) return 0;

        return Pointer.nativeValue(hwnd.getPointer());
    }

    private String getWindowTitle(long hwnd) {
        if (hwnd == 0) return "无窗口";

        char[] buffer = new char[512];
        User32Ex.INSTANCE.GetWindowTextW(new HWND(new Pointer(hwnd)), buffer, 512);
        return Native.toString(buffer);
    }

    private void performImageClick() {
        if (bindHwnd == 0) {
            JOptionPane.showMessageDialog(this, "请先绑定窗口！");
            return;
        }
        clickImage("C:\\Users\\DELL\\IdeaProjects\\untitled\\src\\main\\resources\\target.bmp", 0);
    }

    /**
     * 在绑定窗口内点击指定图片
     * @param imagePath 图片路径
     * @param delayMs   延迟时间（毫秒），可以传 0
     */
    private void clickImage(String imagePath, int delayMs) {
        if (bindHwnd == 0) {
            JOptionPane.showMessageDialog(this, "请先绑定窗口！");
            return;
        }

        new Thread(() -> {
            try {
                if (delayMs > 0) Thread.sleep(delayMs);

                RECT rect = new RECT();
                User32Ex.INSTANCE.GetWindowRect(new HWND(new Pointer(bindHwnd)), rect);
                int width = rect.right - rect.left;
                int height = rect.bottom - rect.top;

                Robot robot = new Robot();
                BufferedImage screenshot = robot.createScreenCapture(
                        new Rectangle(rect.left, rect.top, width, height)
                );

                BufferedImage target = ImageIO.read(new File(imagePath));

                Point p = findImage(screenshot, target);
                if (p != null) {
                    int x = p.x + target.getWidth() / 2;
                    int y = p.y + target.getHeight() / 2;
                    int lParam = (y << 16) | (x & 0xFFFF);

                    HWND hWnd = new HWND(new Pointer(bindHwnd));
                    // 后台点击
                    User32Ex.INSTANCE.SendMessageW(hWnd, User32Ex.WM_LBUTTONDOWN, 0, lParam);
                    User32Ex.INSTANCE.SendMessageW(hWnd, User32Ex.WM_LBUTTONUP, 0, lParam);

                    SwingUtilities.invokeLater(() ->
                            info.setText("点击成功: " + imagePath + " -> " + x + "," + y));
                } else {
                    SwingUtilities.invokeLater(() ->
                            info.setText("未找到图片: " + imagePath));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() ->
                        info.setText("执行错误: " + ex.getMessage()));
            }
        }).start();
    }


    /**
     * 简单模板匹配
     */
    private Point findImage(BufferedImage source, BufferedImage target) {
        int sw = source.getWidth();
        int sh = source.getHeight();
        int tw = target.getWidth();
        int th = target.getHeight();

        for (int x = 0; x <= sw - tw; x++) {
            for (int y = 0; y <= sh - th; y++) {
                boolean match = true;
                outer:
                for (int i = 0; i < tw; i++) {
                    for (int j = 0; j < th; j++) {
                        if (source.getRGB(x + i, y + j) != target.getRGB(i, j)) {
                            match = false;
                            break outer;
                        }
                    }
                }
                if (match) return new Point(x, y);
            }
        }
        return null;
    }
}

interface User32Ex extends StdCallLibrary {
    User32Ex INSTANCE = Native.load("user32", User32Ex.class);

    boolean GetCursorPos(POINT result);
    HWND WindowFromPoint(POINT.ByValue pt);
    int GetWindowTextW(HWND hWnd, char[] buffer, int maxCount);
    boolean GetWindowRect(HWND hwnd, RECT rect);

    int WM_LBUTTONDOWN = 0x0201;
    int WM_LBUTTONUP = 0x0202;

    // 正确添加 SendMessageW
    int SendMessageW(HWND hWnd, int Msg, int wParam, int lParam);
}



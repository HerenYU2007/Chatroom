package cr.ui.frame;

import cr.LocalEnum;
import cr.data.MicroRect;
import cr.io.IO;
import cr.tool.Logger;
import cr.tool.Settings;
import cr.ui.XMenuBar;
import cr.ui.comp.MainPanel;
import cr.ui.popmenu.IconPopMenu;
import jni.NativeFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * @author Bobbywang
 * @date 2021-06-23 19:51
 */
public final class MainFrame extends JFrame {
    public static final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    public static MainFrame obj = null;
    private TrayIcon trayIcon;
    public final MainPanel panel;
    private final Image trayImage = Toolkit.getDefaultToolkit().getImage(IO.urlOfRes("res/img/TrayIcon.png"));
    private final Image blankImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    public static boolean hasMessage = false;

    public static void init() {
        try {
            long t = System.currentTimeMillis();
            obj = new MainFrame();
            System.out.println("MainFrame init:" + (System.currentTimeMillis() - t) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger().err(e);
        }
    }

    private MainFrame() throws Exception {
        super(LocalEnum.TITTLE);
        setIconImage(Toolkit.getDefaultToolkit().getImage(IO.urlOfRes("res/img/Img.png")));
//        init frame size
        MicroRect rect = Settings.obj.mainFrame;
        if (rect != null) {
            if (rect.height > screen.height - 100 || rect.width > screen.width - 100) {
                rect.height -= 100;
                rect.width -= 100;
            }
        } else {
            rect = getDefaultRec();
            Settings.obj.mainFrame = rect;
        }
        rect.setUp(this);

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setJMenuBar(XMenuBar.obj);
        panel = new MainPanel();
        getContentPane().add(panel, BorderLayout.CENTER);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                NativeFrame.init();
                System.out.println("open");
            }
        });
//        add trayIcon
        if (SystemTray.isSupported()) {
            trayIcon = new TrayIcon(trayImage, LocalEnum.TITTLE);
            trayIcon.addMouseListener(new MouseAdapter() {
                private final IconPopMenu pop=new IconPopMenu();
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (hasMessage || !isVisible()) {
                            disHide();
                        } else {
                            doHide();
                        }
                    }else if (e.getButton()==MouseEvent.BUTTON3){
                        pop.show(null,e.getX(),e.getY());
                    }
                }
            });
            SystemTray.getSystemTray().add(trayIcon);
        }
    }

    public final Runnable flush = () -> {
        hasMessage = true;
        try {
            while (!isActive()) {
                trayIcon.setImage(blankImage);
                Thread.sleep(600L);
                trayIcon.setImage(trayImage);
                Thread.sleep(600L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Logger.getLogger().err(e);
        }
        hasMessage = false;
    };

    private MicroRect getDefaultRec() {
        MicroRect r = new MicroRect();
        r.width = (int) (screen.width * 0.6);
        r.height = ((int) (screen.height * 0.6));
        r.x = (screen.width >> 1) - (r.width >> 1);
        r.y = (screen.height >> 1) - (r.height >> 1);
        return r;
    }

    public void disHide() {
        setVisible(true);
    }

    public void doHide() {
        setVisible(false);
    }

    public static void putMiddle(Container container) {
        Rectangle r = new Rectangle();
        r.x = (screen.width >> 1) - (container.getWidth() >> 1);
        r.y = (screen.height >> 1) - (container.getHeight() >> 1);
        r.width = container.getWidth();
        r.height = container.getHeight();
        container.setBounds(r);
    }
    public static void msg(String s){
        JOptionPane.showMessageDialog(obj,s,"消息",JOptionPane.INFORMATION_MESSAGE);
    }
    public static void warn(String s){
        JOptionPane.showMessageDialog(obj,s,"警告",JOptionPane.WARNING_MESSAGE);
    }
    public static void err(String s){
        JOptionPane.showMessageDialog(obj,s,"错误",JOptionPane.ERROR_MESSAGE);
    }
    public static String input(String s){
        return JOptionPane.showInputDialog(obj,s);
    }
    public static String input(String s,Object defaultValue){
        return JOptionPane.showInputDialog(obj,s,defaultValue);
    }
    public static boolean confirm(String s){
        return JOptionPane.showConfirmDialog(obj,s,"Are you sure?",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION;
    }
}
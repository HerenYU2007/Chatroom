package cr.util.user;

import cr.LocalEnum;
import cr.Main;
import cr.io.IO;
import cr.tool.Settings;
import cr.ui.frame.MainFrame;
import cr.util.Client;
import cr.events.Event;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Serial;

@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
public final class User implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 33336764L;
    private static User instance = null;

    public static void init() {
        long t = System.currentTimeMillis();
        instance = new User(new UserInfo(Settings.obj.name));
        System.out.println("User init:" + (System.currentTimeMillis() - t) + "ms");
    }

    public void sendMessage(Event event) {
        Client.getClient().sendMessage(event);
    }
    public static User getLocalUser() {
        return instance;
    }

    private final UserInfo info;
    public transient UserManager group;
    private final String ip;
    private final String outIp = null;
    private static ImageIcon img;
    private static ImageIcon bg;
    public String sentence;

    private User(UserInfo info) {
        this.info = info;
        this.ip = LocalEnum.IP;
        sentence = "个性签名";
    }

    private static void loadImg() {
        bg = new ImageIcon(IO.urlOfRes("res/img/UserBg.jpg"));
        img = new ImageIcon(IO.urlOfRes("res/img/User.jpg"));
    }

    private static void unloadImg() {
        img = null;
        bg = null;
    }

    private static boolean isImgLoaded() {
        return img != null && bg != null;
    }

    private static short frameCount = 0;

    public void showFrame() {
        System.out.println(isImgLoaded());
        if (!isImgLoaded())
            loadImg();
        JFrame frame = new JFrame(info.getName() + "的个人信息");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setBackground(null);
        frame.setSize(900, 600);
        MainFrame.putMiddle(frame);
        UserPanel up = new UserPanel();
        frame.getContentPane().add(up);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (--frameCount == 0) unloadImg();
            }
        });
        up.resize();
        frameCount++;
    }

    public UserInfo getInfo() {
        return info;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        User user = (User) o;
        return info.equals(user.info);
    }

    public String getName() {
        return info.getName();
    }

    public void setName(String name) {
        info.setName(name);
    }

    public String getIp() {
        return ip;
    }


    @Override
    public String toString() {
        return switch (getPermission()) {
            case LocalEnum.Permission_ADMIN -> "[管理员] " + info.getName();
            case LocalEnum.Permission_OWNER -> "[群主] " + info.getName();
            case LocalEnum.Permission_DEFAULT -> "[群成员] " + info.getName();
            default -> throw new IllegalStateException("Unexpected value: " + getPermission());
        };
    }

    public int getPermission() {
        if (group == null)
            group = Client.getClient().users;
        if (group.isOwner(info))
            return LocalEnum.Permission_OWNER;
        else if (group.isAdmin(info))
            return LocalEnum.Permission_ADMIN;
        else
            return LocalEnum.Permission_DEFAULT;
    }

    private final class UserPanel extends JPanel {
        private Image bg;
        private Image user;
        public float y = 150;
        private static final Font nameFont = LocalEnum.FONT_MENU.deriveFont(Font.BOLD, 30);
        private static final Font dftFont = LocalEnum.FONT_MENU.deriveFont(14.0f);
        private final static AlphaComposite blank = AlphaComposite.SrcOver.derive(0.82f);
        private final static AlphaComposite dft = AlphaComposite.SrcOver;

        public UserPanel() {
            setLayout(null);
            setBackground(null);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    System.out.println(e.getPoint());
                }
            });
            addMouseListener(new MouseAdapter() {
                final int upY = 150;
                final int downY = 420;
                boolean isUp = true;
                boolean isMoving = false;

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (isMoving)
                        return;
                    if (isUp) {
                        if (e.getY() < upY - 50) {
                            Main.executor.execute(() -> {
                                isMoving = true;
                                try {
                                    double t;
                                    for (int i = 0; i < 70; i++) {
                                        t = (downY - y) * 0.06;
                                        if (downY - y < 1) {
                                            System.out.println(i);
                                            break;
                                        }
                                        if (t < 1) y += 1;
                                        else y += t;
                                        repaint();
                                        Thread.sleep(14L);
                                    }
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                isUp = false;
                                isMoving = false;
                            });
                        }
                    } else {
                        if (e.getY() > downY - 60) {
                            Main.executor.execute(() -> {
                                isMoving = true;
                                try {
                                    double t;
                                    for (int i = 0; i < 70; i++) {
                                        t = (y - upY) * 0.06;
                                        if (y - upY < 1) {
                                            System.out.println(i);
                                            break;
                                        }
                                        if (t < 1) y -= 1;
                                        else y -= t;
                                        repaint();
                                        Thread.sleep(14L);
                                    }
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                isMoving = false;
                                isUp = true;
                            });
                        }
                    }
                }
            });
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int stringY = ((int) y);
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(bg, 0, 0, this);
            g2.setComposite(blank);
            g2.setColor(Color.white);
            g2.fillRoundRect(0, stringY - 50, getWidth(), getHeight(), 70, 70);
            g2.setComposite(dft);
            g2.setColor(Color.orange);
            g2.fillRect(170, stringY + 5, 110, 110);
            g2.drawImage(user, 175, stringY + 10, this);
            g2.setColor(Color.black);

            final int stringX = 350;

            g2.setFont(nameFont);
            g2.drawString(info.getName(), stringX, stringY);
            g2.setFont(dftFont);
            g2.drawString(sentence, stringX, stringY += 45);
            g2.drawString("内网IP：" + ip, stringX, stringY += 30);
            g2.drawString("外网IP：" + outIp, stringX, stringY += 30);
            g2.drawString("UID：" + info.id, stringX, stringY += 30);
        }

        public void resize() {
            bg = User.bg.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT);
            user = User.img.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        }
    }
}
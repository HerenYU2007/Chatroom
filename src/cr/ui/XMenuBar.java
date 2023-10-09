package cr.ui;

import cr.LocalEnum;
import cr.Main;
import cr.events.Events;
import cr.io.IO;
import cr.io.Net;
import cr.plugin.PluginManager;
import cr.tool.Logger;
import cr.tool.Settings;
import cr.ui.comp.ChatArea;
import cr.ui.comp.FileList;
import cr.ui.comp.InputPane;
import cr.ui.frame.ConnectFrame;
import cr.ui.frame.MainFrame;
import cr.util.Client;
import cr.util.Server;
import cr.util.user.User;
import jni.NativeFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author Bobbywang
 * @date 2021-06-23 20:09
 */
public final class XMenuBar extends JMenuBar {
    public static XMenuBar obj=new XMenuBar();

    public final JMenu menu1;
    public final JMenu menu2;
    public final JMenu menu3;
    public final JMenu menu5;
    public final JMenu menu6;
    public final JMenu menuPlugin;

    public static JMenuItem disconnect;
    public static JMenuItem startItem;
    public static JMenuItem stopItem;
    public static JMenuItem nameItem;
    private static JMenuItem fastItem;
    private final Client client = Client.getClient();

    private XMenuBar() {
        menu1 = new Menu("客户端(C)",'c') {
            @Override
            public void paint(Graphics g) {
                nameItem.setEnabled(!client.isJoined());
                disconnect.setEnabled(client.isJoined());
                super.paint(g);
            }
        };
        fastItem = create("",  e -> client.join(Settings.obj.lastIP, Settings.obj.lastPort));
        menu1.add(fastItem);
        flush();
        menu1.add(create("加入聊天室", 'j', e -> new ConnectFrame("加入", source -> Client.getClient().join((String) source[0], (Integer) source[1])).show0()));
        disconnect = create("离开聊天室", 'l', e -> client.leave(true));
        disconnect.setEnabled(false);
        menu1.add(disconnect);
        menu1.addSeparator();
        nameItem = create("设置昵称", 'n', e -> {
            String input=MainFrame.input("输入新的昵称：",User.getLocalUser().getName());
            if (input==null) return;
            if (input.equals(""))
                MainFrame.err("昵称不能为空！");
            else {
                User.getLocalUser().setName(input);
                MainFrame.msg("您的昵称已改为：" + input);
                Logger.getLogger().info("Change name into " + input);
                Settings.obj.save();
            }
        });
        JMenuItem sentence=create("设置个性签名",'s',e -> {
            if (Client.getClient().isJoined()){
                MainFrame.msg("请先退出当前聊天室！");
                return;
            }
            String s=MainFrame.input("输入个性签名:",User.getLocalUser().sentence);
            if (s==null) return;
            User.getLocalUser().setSentence(s);
            MainFrame.msg("设置成功！");
        });
        menu1.add(sentence);
        menu1.add(createCheckItem("静音", 'm', IO.isMute, e -> IO.isMute = !IO.isMute));
        menu1.add(createCheckItem("窗口置顶", 'u', false, e -> Main.mainFrame.setAlwaysOnTop(((JCheckBoxMenuItem)e.getSource()).isSelected())));
        menu1.add(createCheckItem("强制窗口置顶",'f',false,e -> {
            if (NativeFrame.isOnTop()){
                NativeFrame.stopAlwaysTop();
            }else {
                NativeFrame.alwaysOnTop();
            }
        }));
        menu1.add(nameItem);
        menu1.addSeparator();
        menu1.add(create("导出聊天记录", e -> IO.backupChat()));
        menu1.add((create("清除聊天记录", e -> client.getScreen().setText(null))));
        menu1.add(create("清除所有人聊天纪录", e -> {
            if (User.getLocalUser().getPermission() != LocalEnum.Permission_OWNER) {
                MainFrame.err("你没有权限执行该命令！");
                return;
            }
            client.sendMessage(Events.getClear());
        }));
        menu1.addSeparator();
        menu1.add(create("字体大小", e -> {
            String var = MainFrame.input("输入字体大小：",Settings.obj.fontSize);
            if (var == null || var.equals("")) return;
            try {
                Settings.obj.fontSize = Integer.parseInt(var);
                MainFrame.msg("修改成功！请重启程序");
                Settings.obj.save();
            } catch (NumberFormatException e1) {
                MainFrame.err("输入一个整数！");
            }
        }));
        menu1.add(create("个人信息", e -> User.getLocalUser().showFrame()));
        menu2 = new Menu("服务器(S)",'s') {
            @Override
            public void paint(Graphics g) {
                startItem.setEnabled(!Server.getServer().on);
                stopItem.setEnabled(Server.getServer().on);
                super.paint(g);
            }
        };
        startItem = create("创建服务器", 'm', e -> Server.getServer().start());
        menu2.add(startItem);
        stopItem = create("关闭服务器", 'l', false, e -> Server.getServer().close());
        menu2.add(stopItem);
        menu2.addSeparator();
        menu2.add(create("设置服务器端口", 'p', e -> {
            if (Server.getServer().on) {
                MainFrame.err("你无法在服务器开启时改变端口！");
                return;
            }
            String input = MainFrame.input( "输入新的端口号：", Settings.obj.serverPort);
            if (input == null)
                return;
            int port;
            try {
                port = Net.toIntPort(input);
                Settings.obj.serverPort = port;
            } catch (IllegalArgumentException e1) {
                e1.printStackTrace();
                MainFrame.err("非法端口！");
            }
        }));
        menu2.add(create("Remove Ban", e -> {
            String in = MainFrame.input("输入解封IP：");
            if (in == null) return;
            Server.getServer().removeBan(in);
            MainFrame.msg("已解封");
        }));

        menu3 = new Menu("工具(T)",'t');
        menu3.add(create("Fuck极域", e -> {
            try {
                Runtime.getRuntime().exec("taskkill /f /im studentmain.exe");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }));
        menu3.add(create("检查更新", e -> new ConnectFrame("下载", source -> Client.getClient().update((String) source[0], (Integer) source[1])).setVisible(true)));
        menu3.add(create("滑动关机（Windows10）", e -> {
            try {
                Runtime.getRuntime().exec("SlideToShutDown");
            } catch (IOException ioException) {
                MainFrame.err("当前系统不支持该功能！");
                ioException.printStackTrace();
                Logger.getLogger().err(ioException);
            }
        }));
        menu3.addSeparator();
        menu3.add(create("查看日志", e -> {
            JFrame frame = new JFrame("日志");
            frame.setSize(800, 500);
            MainFrame.putMiddle(frame);
            JTextArea area = new JTextArea();
            frame.add(new JScrollPane(area), BorderLayout.CENTER);
            area.setLineWrap(true);
            area.setAutoscrolls(true);
            area.setEditable(false);
            area.setFont(new Font("微软雅黑", Font.PLAIN, 15));
            area.setText(Logger.getLogger().getStringWriter().toString());
            Logger.getLogger().area = area;
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    Logger.getLogger().area = null;
                }
            });
            frame.setVisible(true);
        }));
        menu3.add(create("解除极域网络控制", e -> {
            try {
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "runas /user:Administrator \"cmd /K taskkill /F /IM MasterHelper.exe /T && sc stop tdnetfilter\"");
                builder.redirectErrorStream(true);
                Process p = builder.start();
                int exitCode = p.waitFor();
                if (exitCode == 0) {
                    MainFrame.msg("指令执行成功！");
                } else {
                    MainFrame.err("指令执行失败！");
                }
            } catch (IOException | InterruptedException ex) {
                MainFrame.err("指令执行失败！");
                ex.printStackTrace();
            }
        }));
        menu3.add(create("清理内存", e -> System.gc()));
//        menu5.add(create("Dll Loader",e -> {
//            File file=IO.openFile(new FileNameExtensionFilter("DLL(*.dll)","dll"));
//            if (file==null)
//                return;
//            String name=JOptionPane.showInputDialog("输入函数名称：(半角逗号分隔)\n警告：数字开头的函数名可能会造成程序死锁");
//            if (name==null)
//                return;
//            String[] func=name.trim().split(",");
//            int r=DllBridge.runDll(file.getAbsolutePath(),func);
//            switch (r) {
//                case 0 -> JOptionPane.showMessageDialog(Main.mainFrame, "加载Dll失败");
//                case 1 -> JOptionPane.showMessageDialog(Main.mainFrame, "所有函数加载成功！");
//                case 2 -> JOptionPane.showMessageDialog(Main.mainFrame, "部分函数加载失败");
//            }
//        }));
        menu3.add(create("一键解除所有极域（仅管理员）", e -> {
            if (User.getLocalUser().getPermission() != LocalEnum.Permission_OWNER) {
                MainFrame.err("你没有权限执行该命令！");
                return;
            }
            // 在此处添加发送cmd指令的代码
            try {
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "taskkill /F /IM studentmain.exe /T");
                builder.redirectErrorStream(true);
                Process p = builder.start();
                int exitCode = p.waitFor();
                if (exitCode == 0) {
                    MainFrame.msg("指令执行成功！");
                } else {
                    MainFrame.err("指令执行失败！");
                }
            } catch (IOException | InterruptedException ex) {
                MainFrame.err("指令执行失败！");
                ex.printStackTrace();
            }
        }));

        menuPlugin=new Menu("插件(P)",'p');
        menuPlugin.add(create("插件管理器",'m',e -> PluginManager.showDialog()));
        menuPlugin.addSeparator();
        menu5=new Menu("背景(B)",'b');
        menu5.add(create("设置聊天背景",e -> ChatArea.getInstance().setBgImage(IO.openImage(),"chat")));
        menu5.add(create("设置文本框背景",e -> InputPane.obj.setBgImage(IO.openImage(),"input")));
        menu5.add(create("设置文件列表背景",e -> FileList.obj.setBgImage(IO.openImage(),"file")));
        menu5.addSeparator();
        menu5.add(create("刷新背景",e -> {
            ChatArea.getInstance().flushImage("chat");
            InputPane.obj.flushImage("input");
            FileList.obj.flushImage("file");
        }));
        menu5.add(create("如何清除背景？",e->{
            try {
                Desktop.getDesktop().open(new File("res/background"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            MainFrame.msg("在程序安装目录中的\"res/background\"文件夹中找到对应文件删除，然后重启程序");
        }));



        menu6 =new Menu("关于(A)",'a');
        menu6.add(create("Github仓库", e -> IO.openHttp("https://github.com/BobbyWch/Chatroom/tree/java")));
        menu6.add(create("Github中国", e -> IO.openHttp("https://hub.fastgit.xyz/BobbyWch/Chatroom/tree/java")));
        menu6.add(create("进入官网", e -> IO.openHttp("http://bobbyschatroom.top/")));
        add(menu1);
        add(menu2);
        add(menu3);
        add(menuPlugin);
        add(menu5);
        add(menu6);
    }

    public static void flush() {
        Settings set = Settings.obj;
        if (set.lastIP == null || set.lastPort == -1) {
            fastItem.setText("快速加入");
            fastItem.setEnabled(false);
            return;
        }
        fastItem.setText("快速加入 IP:" + set.lastIP + ":" + set.lastPort);
        fastItem.setEnabled(true);
    }

    public static JMenuItem create(String title, ActionListener listener) {
        JMenuItem item = new JMenuItem(title);
        item.setFont(LocalEnum.FONT_MENU);
        item.addActionListener(listener);
        return item;
    }

    public static JMenuItem create(String title, char mnemonic, ActionListener listener) {
        JMenuItem item = new JMenuItem(title + '(' + Character.toUpperCase(mnemonic) + ')');
        item.setMnemonic(mnemonic);
        item.addActionListener(listener);
        item.setFont(LocalEnum.FONT_MENU);
        return item;
    }

    public static JMenuItem create(String title, char mnemonic, boolean enabled, ActionListener listener) {
        JMenuItem item = create(title, mnemonic, listener);
        item.setEnabled(enabled);
        return item;
    }

    public static JCheckBoxMenuItem createCheckItem(String title, char mnemonic, boolean selected, ActionListener listener) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(title + '(' + Character.toUpperCase(mnemonic) + ')');
        item.setSelected(selected);
        item.setFont(LocalEnum.FONT_MENU);
        item.setMnemonic(mnemonic);
        item.addActionListener(listener);
        return item;
    }

    public static class Menu extends JMenu {
        public Menu(String name,char c) {
            super(name);
            setFont(LocalEnum.FONT_MENU);
            setMnemonic(c);
        }
    }
}
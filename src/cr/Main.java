package cr;

import cr.data.FileInfo;
import cr.plugin.PluginManager;
import cr.tool.Logger;
import cr.tool.Settings;
import cr.ui.comp.ChatArea;
import cr.ui.comp.FileList;
import cr.ui.comp.InputPane;
import cr.ui.comp.UserList;
import cr.ui.frame.MainFrame;
import cr.ui.popmenu.ChatPopMenu;
import cr.ui.popmenu.UserPopMenu;
import cr.util.Client;
import cr.util.Server;
import cr.util.user.User;
import jni.NativeFrame;

import javax.swing.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 *                                              _oo0oo_
 *                                             o8888888o
 *                                             88" . "88
 *                                             (| -_- |)
 *                                              O\ = /O
 *                                          ____/'---'\____
 *                                           .' \\| |// '.
 *                                         / \\||| : |||// \
 *                                       / _||||| -:- |||||_ \
 *                                       | | | \\\ - /// | | |
 *                                       | \_| ''\---/'' |_/ |
 *                                        \ .-\__ `-` __/-. /
 *                                     ___`. .' /--.--\ `. .'___
 *                                  ."" '< `.___\_<|>_/___.' >' "".
 *                                 | | : `- \`.;`\ _ /`;.`/ -` : | |
 *                                   \ \ `-. \_ __\ /__ _/ .-` / /
 *                           ======`-.____`-.___\_____/___.-`____.-`======
 *                                              `=---=`
 *
 *                      .......................................................
 *                                     佛祖保佑            永无BUG
 *                       佛曰：
 *                                     写字楼里写字间，写字间里程序员。
 *                                     程序人员写程序，又拿程序换酒钱。
 *                                     酒醒只在网上坐，酒醉还来网下眠。
 *                                     酒醉酒醒日复日，网上网下年复年。
 *                                     但愿老死电脑间，不愿鞠躬老板前。
 *                                     奔驰宝马贵者趣，公交自行程序员。
 *                                     别人笑我忒疯癫，我笑自己命太贱；
 *                                     不见满街漂亮妹，哪个归得程序员？
 */

public final class Main {
    public static MainFrame mainFrame;
    public static final ThreadPoolExecutor executor=new ThreadPoolExecutor(1,8,200000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10));

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Logger.init();
        Settings.init();
        User.init();
        UserList.init();
        Client.init();
        ChatPopMenu.init();
        UserPopMenu.init();
        ChatArea.init();
        FileList.init();
        InputPane.init();
        MainFrame.init();
        Server.init();

        mainFrame = MainFrame.obj;
        mainFrame.setVisible(true);
        FileInfo.init();
        PluginManager.init();
        long endTime = System.currentTimeMillis();
        Logger.getLogger().info("Program starts in " + (endTime - startTime) / 1000.0 + 's' + "  JRE version:" + System.getProperty("java.version"));
        if (!System.getProperty("java.version").startsWith("20")) {
            MainFrame.warn("检测到当前运行环境非Java20，可能存在兼容性问题，建议使用Java20运行本程序");
        }
    }

    public static void exit() {
        PluginManager.callExit();
        if (Client.getClient().isJoined())
            Client.getClient().leave(false);
        if (Server.getServer().on) {
            Server.getServer().close();
        }
        Settings.obj.save();
    }
}
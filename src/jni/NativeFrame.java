package jni;

import cr.LocalEnum;
import cr.ui.frame.MainFrame;

import java.io.IOException;

public final class NativeFrame {
    private static boolean onTop=false;
    public static void init(){
        setFrame(LocalEnum.TITTLE);
    }
    public static void alwaysOnTop(){
        if (!onTop) {
            onTop=true;
            new Thread(() -> start_top(5)).start();
        }
    }
    public static void stopAlwaysTop(){
        if (onTop) {
            onTop=false;
            stop_top();
            MainFrame.obj.setAlwaysOnTop(true);
            MainFrame.obj.setAlwaysOnTop(false);
        }
    }
    public static boolean isOnTop(){
        return onTop;
    }
    public static void onTopOnce(){
        top();
    }
    public static void cancelOnTop(){
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
    }


    private static native void setFrame(String title);
    private static native void top();
    private static native void start_top(int delay);
    private static native void stop_top();
    private static native void cancel();
    static {
        System.loadLibrary("lib/NativeFrame");
    }
}
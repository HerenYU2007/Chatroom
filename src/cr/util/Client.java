package cr.util;

import cr.LocalEnum;
import cr.Main;
import cr.data.ColorDocument;
import cr.data.FileInfo;
import cr.events.Ack;
import cr.events.DownRequest;
import cr.events.Event;
import cr.events.Events;
import cr.events.action.ClearEvent;
import cr.inter.DocumentCreator;
import cr.io.Connection;
import cr.io.IO;
import cr.tool.*;
import cr.ui.XMenuBar;
import cr.ui.comp.ChatArea;
import cr.ui.comp.FileList;
import cr.ui.comp.UserList;
import cr.ui.frame.ImgFrame;
import cr.ui.frame.MainFrame;
import cr.util.user.*;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * @author Bobbywang
 * @date 2021-10-14 21:23
 */
public final class Client implements DocumentCreator,Runnable {
    private Client() {
        user = User.getLocalUser();
        userInfo = user.getInfo();
    }

    private static Client instance = null;

    public static void init() {
        long t = System.currentTimeMillis();
        instance = new Client();
        System.out.println("Client init:" + (System.currentTimeMillis() - t) + "ms");
    }


    public static Client getClient() {
        return instance;
    }
    public final User user;
    public final UserInfo userInfo;
    public final UserList userList = UserList.getInstance();
    public final ArrayList<FileInfo> files = new ArrayList<>();
    public final Logger logger = Logger.getLogger();

    private String ip = null;
    private int port = -1;
    private Connection con = null;
    private boolean joined = false;
    private ChatArea screen;
    private ColorDocument text = new ColorDocument(this);
    public UserManager users;
    private Thread lisThd = null;

    @Override
    public void clear(Event lastEvent) {
        text = new ColorDocument(this);
        if (lastEvent instanceof ClearEvent clear) {
            text.reInit(users, this);
            text.appendLine(clear.getUser().getName() + "清除了聊天记录", Event.sysMsg);
            screen.setDocument(text);
        }
    }

    public void join(String ip, int port) {
        if (joined) {
            leave(true);
        }
        joined = true;
        userList.clear();
        this.ip = ip;
        this.port = port;
        try {
            con=getConnection();
            connect();
            Settings.obj.lastIP = ip;
            Settings.obj.lastPort = port;
            XMenuBar.flush();
        } catch (IOException e) {
            e.printStackTrace();
            logger.err(e);
            logger.info("Connect failed.");
            MainFrame.err("连接失败。请检查端口和IP地址，或服务器是否开启。");
            leave(false);
        }
    }

    public void leave(boolean bool) {
        joined = false;
        if (lisThd != null) {
            lisThd = null;
        }
        if (con != null) {
            con.close();
        }
        if (bool) {
            logger.info("Leave chat server.");
            text.appendLine("您已离开聊天室", Event.userMsg);
        }

        files.clear();
        userList.clear();
        if (users!=null)users.clear();
        FileList.obj.clear();
        con = null;
        users=null;
    }

    private void onMessage(Event e) {
        e.client(this);
        text.append(e);
        screen.roll();
        checkTray();
    }

    public void updateProperties(Ack a) {
        text = a.getText();
        users = a.getUsers();
        users.setUserList(UserList.getInstance());
        users.removeUser(user);
        users.addUser(user);
        text.reInit(users, this);
        screen.setDocument(text);

        files.clear();
        files.addAll(a.files);
        for (FileInfo f:a.files){
            FileList.obj.addFile(f);
        }
    }

    public void update(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            Connection con = new Connection(socket, LocalEnum.USER_ALL);
            con.writeMessage(Events.getUpdateReq());
            InputStream is = socket.getInputStream();
            FileOutputStream fs = new FileOutputStream("prog/ChatRoom.jar");
            IO.outPutInput(is, fs);
            socket.close();
            fs.close();
        } catch (IOException e) {
            logger.err(e);
            e.printStackTrace();
        }
        MainFrame.msg("更新成功！请重启程序");
    }

    public void download(int id) {
        FileInfo file = getFile(id);
        if (file == null) {
            MainFrame.msg("该文件不存在！:(");
            return;
        }
        if (file.isImg){
            byte[] bytes;
            bytes=file.getCache();
            if (bytes==null) {
                try {
                    Socket socket = new Socket(ip, port);
                    Connection con = new Connection(socket, LocalEnum.USER_ALL);
                    con.writeMessage(new DownRequest(file));
                    file.writeCache(socket.getInputStream());
                    socket.close();
                    bytes= file.getCache();
                } catch (IOException ioException) {
                    logger.err(ioException);
                    ioException.printStackTrace();
                }
            }
            ImgFrame.showImage(Toolkit.getDefaultToolkit().createImage(bytes), e -> {
                File f = IO.saveFile(null, file.name);
                if (f == null) return;
                try (var fs = new FileOutputStream(f);
                var is=file.getCacheStream()) {
                    IO.outPutInput(is,fs);
                } catch (IOException ioException) {
                    logger.err(ioException);
                    ioException.printStackTrace();
                }
            });
        }else {
            File f = IO.saveFile(null, file.name);
            if (f == null) return;
            try (var fs = new FileOutputStream(f)) {
                Socket socket = new Socket(ip, port);
                Connection con = new Connection(socket, LocalEnum.USER_ALL);
                con.writeMessage(new DownRequest(file));
                InputStream is = socket.getInputStream();
                IO.outPutInput(is, fs);
                socket.close();
            } catch (IOException ioException) {
                logger.err(ioException);
                ioException.printStackTrace();
            }
        }
    }

    public void upload(File file) {
        if (file == null) {
            return;
        }
        try (Socket socket = new Socket(ip, port);
             FileInputStream fs = new FileInputStream(file)) {
            Connection con = new Connection(socket, LocalEnum.USER_ALL);
            con.writeMessage(Events.getFileRequest(file));
            OutputStream os = socket.getOutputStream();
            IO.outPutInput(fs, os);
            os.close();
        } catch (IOException e) {
            logger.err(e);
            e.printStackTrace();
        }
    }

    /**
     * @noinspection ForLoopReplaceableByForEach
     */
    private FileInfo getFile(int id) {
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).id == id)
                return files.get(i);
        }
        return null;
    }

    private void connect() {
//        con = new Connection(s, LocalEnum.USER_ALL);
        if (lisThd == null) {
            lisThd = new Thread(this);
            lisThd.start();
        }
        con.writeMessage(Events.getRequest());
    }

    private void checkTray() {
        if (!Main.mainFrame.isActive()) {
            Main.mainFrame.requestFocus();
            if (!MainFrame.hasMessage)
                Main.executor.execute(Main.mainFrame.flush);
        }
    }

    public void sendMessage(Event e) {
        con.writeMessage(e);
    }

    public void kick(User user, String reason) {
        sendMessage(Events.getKick(this.user.getInfo(), user.getInfo(), reason));
    }

    public void say(String msg, UserInfo recv) {
        sendMessage(Events.getMsg(recv, msg));
    }
    private Connection getConnection() throws IOException {
        return new Connection(new Socket(ip,port),LocalEnum.USER_ALL);
    }

    public boolean isJoined() {
        return joined;
    }

    public void setScreen(ChatArea screen) {
        this.screen = screen;
    }

    public ChatArea getScreen() {
        return screen;
    }

    public ColorDocument getDocument() {
        return text;
    }

    @Override
    public void run() {
        Event m;
        if (con != null) {
            try {
                while (joined) {
                    if ((m = con.readMessage()) != null) {
                        onMessage(m);
                    }
                }
            } catch (OptionalDataException oe) {
                oe.printStackTrace();
                logger.err(oe);
                MainFrame.warn("意外错误！请重启客户端\n" + oe.getMessage());
            } catch (SocketException e) {
                switch (e.getMessage()) {
                    case "Connection reset" -> {
                        if (joined) {
                            logger.info("Lost Connection. Try to reconnect.");
                            logger.err(e);
                            leave(false);
                            join(ip, port);
                            e.printStackTrace();
                        }
                    }
                    case "Socket closed" -> logger.info("Socket closed");
                    default -> {
                        e.printStackTrace();
                        logger.err(e);
                    }
                }
            } catch (Exception ex) {
                logger.err(ex);
                ex.printStackTrace();
            }
        }
    }
}
package cr.plugin;

import cr.LocalEnum;
import cr.ui.XMenuBar;
import cr.tool.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class PluginManager {
    private static JFrame frame = null;
    private final static LinkedList<PluginInterface> plugins = new LinkedList<>();
    private static HashMap<String, String> path = null;
    public static HashMap<String, PluginModel> info = new HashMap<>();
    public static final JButton button = new JButton();

    public static void init() {
        openFile();
        LinkedList<URL> urls = new LinkedList<>();
        File file;
        for (String key : path.keySet()) {
            file = new File("plugins", key);
            if (file.exists()) {
                try {
                    urls.add(file.toURI().toURL());
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.getLogger().err(e);
                }
            } else {
                Logger.getLogger().info(key + " plugin not found.");
            }
        }

        URL[] urls1 = new URL[urls.size()];
        int i = 0;
        for (URL url : urls) {
            urls1[i] = url;
            i++;
        }

        URLClassLoader loader = new URLClassLoader(urls1);
        JMenu menu;
        Class<?> clazz;
        JMenu pMenu = XMenuBar.obj.menuPlugin;
        for (String cp : path.values()) {
            try {
                clazz = loader.loadClass(cp);
                if (clazz.getDeclaredConstructor().newInstance() instanceof PluginInterface plugin) {
                    plugin.loadPlugin(loader);
                    menu = plugin.getMenu();
                    if (menu != null) {
                        pMenu.add(menu);
                    }
                    plugins.add(plugin);
                } else {
                    throw new ClassNotFoundException("Not PluginInterface Impl.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void callExit() {
        for (PluginInterface plugin : plugins) {
            plugin.onExit();
        }
    }

    public static DefaultTableModel model = null;

    public static void showDialog() {
        if (frame == null) {
            JFrame d = new JFrame("插件管理器");
            d.setSize(800, 500);
            d.setResizable(false);
            Vector<String> header = new Vector<>();
            header.add("插件名称");
            header.add("文件名");
            header.add("描述");
            header.add("是否启用");
            Vector<Vector<String>> v = new Vector<>();
            JTable table = new JTable(v, header) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public void paint(Graphics g) {
                    super.paint(g);
                    int row = getSelectedRow();
                    if (row == -1) {
                        button.setEnabled(false);
                    } else {
                        button.setEnabled(true);
                        button.setText((model.getValueAt(row, 3).equals("是")) ? "禁用" : "启用");
                    }
                }
            };
            Font font = new Font("微软雅黑", Font.PLAIN, 13);
            table.getTableHeader().setFont(font);
            table.setFont(font);
            table.setRowHeight(23);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            model = ((DefaultTableModel) table.getModel());
            JScrollPane tableP = new JScrollPane(table);
            d.setLayout(null);
            Container c = d.getContentPane();
            c.add(tableP);
            c.add(button);
            tableP.setBounds(30, 30, 700, 330);
            button.setFont(LocalEnum.FONT_MENU);
            button.setBounds(50, 400, 80, 40);
            button.addActionListener(e -> {
                PluginModel p = info.get(model.getValueAt(table.getSelectedRow(), 1));
                if (button.getText().equals("启用")) {
                    p.enable();
                    button.setText("禁用");
                    model.setValueAt("是", table.getSelectedRow(), 3);
                } else {
                    p.disable();
                    button.setText("启用");
                    model.setValueAt("否", table.getSelectedRow(), 3);
                }
            });

            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    //remove all
                    int pos = model.getRowCount() - 1;
                    if (pos == -1) return;
                    model.getDataVector().clear();
                    model.fireTableRowsDeleted(0, pos);

                    saveFile();
                    reload();
                }
            });
            frame = d;
        }
        frame.setVisible(true);
        flush();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 释放资源的代码
                frame.dispose(); // 关闭窗口
            }
        });

    }

    public static void flush() {
        info.clear();
        File file = new File("plugins");
        if (!file.exists())
            file.mkdir();
        else if (file.isDirectory()) {
            File[] files = file.listFiles(pathname -> pathname.getName().endsWith(".jar"));
            PluginModel model;
            Vector<Object> t;
            for (File f : files) {
                try {
                    model = readYml(f);
                } catch (IOException e) {
                    continue;
                }
                t = new Vector<>(4);
                t.add(model);
                t.add(f.getName());
                t.add(model.dsp);
                t.add(model.isEnabled() ? "是" : "否");
                PluginManager.model.addRow(t);
                info.put(f.getName(), model);
            }
        }
    }

    private static PluginModel readYml(File jarFile) throws IOException {
        JarFile jar = new JarFile(jarFile);
        ZipEntry entry = jar.getEntry("plugin.yml");
        if (entry == null)
            throw new IOException("not a plugin.");
        InputStream is = jar.getInputStream(entry);
        InputStreamReader bridge = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(bridge);
        String name = null, classPath = null, dsp = null, temp;
        while ((temp = reader.readLine()) != null) {
            if (temp.startsWith("name: ")) {
                name = temp.substring(6);
            } else if (temp.startsWith("main-class: ")) {
                classPath = temp.substring(12);
            } else if (temp.startsWith("description: ")) {
                dsp = temp.substring(13);
            }
        }
        reader.close();
        bridge.close();
        is.close();
        jar.close();
        return new PluginModel(jarFile.getName(), name, classPath, dsp);
    }

    public static void openFile() {
        try {
            FileInputStream fis = new FileInputStream("plugins/plugin.obj");
            ObjectInputStream os = new ObjectInputStream(fis);
            path = (HashMap<String, String>) os.readObject();
            os.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            path = new HashMap<>();
        }
    }

    public static void saveFile() {
        try {
            FileOutputStream fos = new FileOutputStream("plugins/plugin.obj");
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(path);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reload(){
        for (PluginInterface in:plugins){
            in.unloadPlugin();
        }
        plugins.clear();
        init();
    }
    private record PluginModel(String filename, String name, String classPath, String dsp) {
        @Override
        public String toString() {
            return name;
        }

        public void enable() {
            path.put(filename, classPath);
        }

        public void disable() {
            path.remove(filename);
        }

        public boolean isEnabled() {
            return path.containsKey(filename);
        }
    }
}
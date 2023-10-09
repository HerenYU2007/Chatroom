package cr.ui.frame;

import cr.LocalEnum;
import cr.inter.InputEvent;
import cr.io.SocketListener;
import cr.util.user.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Vector;

/**
 * @author Bobbywang
 * @date 2021-11-21 16:48
 */
public final class ConnectFrame extends JFrame {
    private final DefaultTableModel model;
    private final InputEvent l;

    public ConnectFrame(String button, InputEvent event) {
        super("加入聊天室");
        setSize(800, 500);
        setLayout(null);
        setResizable(false);

        JPanel p1 = new JPanel(null);//width: 765 , height: 280
        p1.setBorder(BorderFactory.createTitledBorder("局域网内聊天室"));

        Vector<String> head = new Vector<>();
        head.add("房间名");
        head.add("群主");
        head.add("IP");
        head.add("端口");
        head.add("版本");
        JTable table = new JTable(new Vector<>(), head){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.getTableHeader().setFont(new Font("微软雅黑",Font.PLAIN,13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(Color.lightGray);
        model = ((DefaultTableModel) table.getModel());
        JScrollPane tablePane = new JScrollPane(table);
        tablePane.setBackground(Color.white);
        p1.add(tablePane);
        tablePane.setBounds(15, 25, 735, 200);
        JButton joinB = new JButton(button);
        p1.add(joinB);
        joinB.setBounds(670, 235, 80, 28);
        joinB.addActionListener(e -> {
            int row = table.getSelectedRow();
            event.onEvent(model.getValueAt(row, 2),Integer.parseInt(((String) model.getValueAt(row, 3)).trim()));
            setVisible(false);
            close();

            if (User.getLocalUser().getPermission() != LocalEnum.Permission_OWNER) {
                MainFrame.err("你没有权限执行该命令！");
                return;
            }

        });

        JPanel p2 = new JPanel(null);//width: 765 , height: 150
        p2.setBorder(BorderFactory.createTitledBorder("手动加入"));
        JLabel label=new JLabel("IP:");
        JLabel label1=new JLabel("端口:");
        JTextField tf=new JTextField();
        JTextField tf1=new JTextField();
        JButton jb=new JButton(button);
        jb.addActionListener(e -> {
            event.onEvent(tf.getText(),Integer.parseInt(tf1.getText()));
            setVisible(false);
            close();
        });
        label.setBounds(50,50,50,50);
        tf.setBounds(100,50,200,30);
        label1.setBounds(400,50,50,50);
        tf1.setBounds(450,50,100,30);
        jb.setBounds(600,50,80,30);
        p2.add(label);
        p2.add(label1);
        p2.add(tf);
        p2.add(tf1);
        p2.add(jb);

        Container c=getContentPane();
        c.add(p1).setBounds(10, 10, 765, 280);
        c.add(p2).setBounds(10, 300, 765, 150);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        l= source -> {
            String str=(String) source[0];
            if (strs.contains(str))
                return;
            strs.add(str);
            String[] context = str.split("\n\t");
            model.addRow(context);
        };
        SocketListener.addListener(l);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private final HashSet<String> strs = new HashSet<>();

    public void close() {
        strs.clear();
        //remove all
        int pos=model.getRowCount()-1;
        if (pos==-1) return;
        model.getDataVector().clear();
        model.fireTableRowsDeleted(0,pos);
        SocketListener.removeListener(l);
    }

    public void show0(){
        MainFrame.putMiddle(this);
        setVisible(true);
    }
}
package cr.ui.comp;

import cr.LocalEnum;
import cr.tool.Settings;
import cr.ui.popmenu.UserPopMenu;
import cr.util.user.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

/**
 * @author Bobbywang
 * @date 2021-08-08 19:26
 */
public final class UserList extends JList<User> {
    private static UserList instance = null;

    private final DefaultListModel<User> model = new DefaultListModel<>();

    public static UserList getInstance() {
        return instance;
    }

    public static void init() {
        long t = System.currentTimeMillis();
        instance = new UserList();
        System.out.println("UserList init:" + (System.currentTimeMillis() - t) + "ms");
    }

    private UserList() {
        super();
        setModel(model);
        setFont(LocalEnum.FONT_MENU.deriveFont((float) Settings.obj.fontSize + 1));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int row = locationToIndex(e.getPoint());
                    if (row == -1) return;
                    setSelectedIndex(row);
                    UserPopMenu.getInstance().show(getInstance(), e.getX(), e.getY(), model.getElementAt(row));
                }
            }
        });
    }

    public JPanel getPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel("用户列表");
        label.setBorder(BorderFactory.createLineBorder(Color.lightGray, 2));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(LocalEnum.FONT_MENU.deriveFont(18.0f));
        panel.add(label, BorderLayout.NORTH);
        panel.add(this, BorderLayout.CENTER);
        panel.setBackground(Color.white);
        return panel;
    }

    public void addUser(User user) {
        if (!model.contains(user))
            model.addElement(user);
    }

    public void addAll(Collection<User> userCollection) {
        if (userCollection != null) {
            for (User user : userCollection) {
                addUser(user);
            }
        }
    }

    public void removeUser(User user) {
        model.removeElement(user);
    }

    public void clear() {
        model.clear();
    }
}

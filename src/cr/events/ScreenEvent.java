package cr.events;

import
        cr.data.ColorDocument;
import cr.util.Client;
import cr.util.Server;
import cr.util.user.UserInfo;

import java.io.IOException;

public class ScreenEvent extends Event{
    private final UserInfo target;
    private final byte[] data;

    public ScreenEvent(UserInfo target, byte[] data) {
        this.target = target;
        this.data = data;
    }

    @Override
    public void client(Client clt) {
//        if ()
    }

    @Override
    public void server(Server svr, Server.Listener l) throws IOException {

    }

    @Override
    public void display(ColorDocument cd) {

    }
}

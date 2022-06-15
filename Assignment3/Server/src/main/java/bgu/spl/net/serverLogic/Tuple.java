package bgu.spl.net.serverLogic;

public class Tuple {
    private User user;
    private stat stat;
    public Tuple(User user,stat stat)
    {
        this.user=user;
        this.stat=stat;
    }

    public User getUser() {
        return user;
    }

    public bgu.spl.net.serverLogic.stat getStat() {
        return stat;
    }
}

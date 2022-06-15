package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.BGSprotocol;
import bgu.spl.net.srv.MessageEncDec;
import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args)
    {
        int port=Integer.parseInt( args[0]);
        Server.threadPerClient(
                port, //port
                () -> new BGSprotocol(), //protocol factory
                MessageEncDec::new //message encoder decoder factory
        ).serve();
    }
}

package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.BGSprotocol;
import bgu.spl.net.srv.MessageEncDec;
import bgu.spl.net.srv.Reactor;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args)
    {
        int port=Integer.parseInt( args[0]);
        int numOfThreads=Integer.parseInt(args[1]);

        Server.reactor(
                numOfThreads,
                port, //port
                () ->  new BGSprotocol(), //protocol factory
                MessageEncDec::new //message encoder decoder factory
        ).serve();
    }
}

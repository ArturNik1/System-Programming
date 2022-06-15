package bgu.spl.net.srv;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.Connections;
import bgu.spl.net.serverLogic.Network;
import bgu.spl.net.serverLogic.User;
import bgu.spl.net.serverLogic.opCode;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BGSprotocol implements BidiMessagingProtocol<String> {

    private User loggedInUser;
    private int connectionId;
    private Connections<String> connections;
    private Network network;
    private AtomicBoolean shouldTreminate;

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.loggedInUser = null;
        this.connectionId = connectionId;
        this.connections = connections;
        this.network = Network.getInstance();
        this.shouldTreminate=new AtomicBoolean(false);
    }

    @Override
    public void process(String message) {
        //TODO:action to network if we before login (loggedInUser==null) or actions to loggedInUser

        short opcode = 0;
        String opString = message.substring(0, 2);
        if (opString.charAt(0) == '0') opcode = (short) Integer.parseInt(opString.substring(1));
        else opcode = (short) Integer.parseInt(opString);

        message = message.substring(2);
        List<String> info;
        switch (opcode) {
            case 1:
                info = Arrays.asList(message.split("\0"));
                // info[0] = username, [1] = password, [2] = date
                network.register(new User(info.get(0), info.get(1), info.get(2),connections),connectionId);
                break;
            case 2:
                if (loggedInUser != null) {
                    connections.send(connectionId,opCode.ErrorMsg(opCode.LOGIN));
                    break;
                }
                info = Arrays.asList(message.split("\0"));
                // info[0] = username, [1] = password, [2] = captcha
                loggedInUser = network.login(info.get(0), info.get(1), (short) (Integer.parseInt(info.get(2))),connectionId);
                if(loggedInUser==null)connections.send(connectionId,opCode.ErrorMsg(opCode.LOGIN));
                break;
            case 3:
                // just log out...
                if (loggedInUser == null) {
                    // throw an error message...
                    connections.send(connectionId, opCode.ErrorMsg(opCode.LOGOUT));
                }
                else {
                    loggedInUser.logout();
                    shouldTreminate.set(true);
                }
                break;
            case 4:
                if (loggedInUser == null) {
                    // throw an error message
                    connections.send(connectionId, opCode.ErrorMsg(opCode.FOLLOW));
                    return;
                }
                char c = message.charAt(0);
                message = message.substring(1);
                info = Arrays.asList(message.split("\0"));
                if (c == '0') {
                    // follow
                    // username is info[0]
                    loggedInUser.follow(info.get(0));
                }
                else {
                    // unfollow
                    // username is info[0]
                    loggedInUser.unfollow(info.get(0));
                }
                break;
            case 5:
                if (loggedInUser == null) {
                    // throw error
                    connections.send(connectionId, opCode.ErrorMsg(opCode.POST));
                    return;
                }
                info = Arrays.asList(message.split("\0"));
                // info[0] is the content
                loggedInUser.post(info.get(0));
                break;
            case 6:
                if (loggedInUser == null) {
                    // throw error
                    connections.send(connectionId, opCode.ErrorMsg(opCode.PM));
                    return;
                }
                info = Arrays.asList(message.split("\0"));
                // info[0] = username, info[1] = content, info[2] = time data
                loggedInUser.sendPM(info.get(0), info.get(1), info.get(2));
                break;
            case 7:
                if (loggedInUser == null) {
                    // throw error
                    connections.send(connectionId, opCode.ErrorMsg(opCode.LOGSTAT));
                    return;
                }
                // send logstat
                loggedInUser.logStat();
                break;
            case 8:
                if (loggedInUser == null) {
                    // error
                    connections.send(connectionId, opCode.ErrorMsg(opCode.STAT));
                    return;
                }
                info = Arrays.asList(message.split("\0"));
                // info[0] = usernames with | between them
                String tempNames = info.get(0);
                int pos;
                List<String> names = new LinkedList<>();
                while ((pos = tempNames.indexOf("|")) != -1) {
                    names.add(tempNames.substring(0, pos));
                    tempNames = tempNames.substring(pos+1);
                }
                if (tempNames.length() > 0) names.add(tempNames);
                loggedInUser.stat(names);
                break;
            case 12:
                if (loggedInUser == null) {
                    // error
                    connections.send(connectionId, opCode.ErrorMsg(opCode.BLOCK));
                    return;
                }
                info = Arrays.asList(message.split("\0"));
                // info[0] = username
                loggedInUser.block(info.get(0));
                break;

        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTreminate.get();
    }
}

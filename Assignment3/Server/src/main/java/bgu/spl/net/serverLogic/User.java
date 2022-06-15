package bgu.spl.net.serverLogic;

import bgu.spl.net.api.Connections;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class User {
    private int connectionId;
    private AtomicBoolean isLogin;
    private int numOfPosts;
    private String userName;
    private String password;
    private String birthDay;

    private List<User> followers;
    private List<User> following;
    private Object followersKey;
    private Object logout_inKey;

    private List<String> unWatchedMessages;//TODO:problem with message that dont arrive to the client - we send them but the client disconnected suddenly
    private HashSet<User>blockYou;
    private HashSet<User>blockMe;

    private Network network;
    private Connections connection;

    private List<String> msgs;

    public User(String userName,String password,String birthDay,Connections connections){
        this.isLogin=new AtomicBoolean();
        this.numOfPosts=0;
        this.userName=userName;
        this.password=password;
        this.birthDay=birthDay;
        this.followers=new LinkedList<>();
        this.following=new LinkedList<>();
        this.unWatchedMessages=new LinkedList<>();
        this.blockYou=new HashSet<>();
        this.blockMe=new HashSet<>();
        this.network=Network.getInstance();
        this.msgs=new LinkedList<>();
        this.followersKey=new Object();
        this.logout_inKey=new Object();
        this.connection=connections;

    }
    public String getUserName() {
        return userName;
    }

    public boolean login(String password,short chapta,int connectionId)
    {
        //System.out.println("Con Id1 "+connectionId);
        synchronized (logout_inKey)
        {

            if(chapta==0||!password.equals(this.password))return false;
            else
            {
                if(isLogin.compareAndSet(false,true))
                {
                    this.connectionId=connectionId;
                    synchronized (unWatchedMessages)
                    {
                        for(String message:unWatchedMessages)
                        {

                            connection.send(connectionId,message);
                        }
                        unWatchedMessages.clear();
                        return true;

                    }
                }
                else
                    return false;
            }
        }

    }
    public void logout()
    {
        synchronized (logout_inKey) {//TODO:maybe delete sync
            if (isLogin.compareAndSet(true, false)) {
                connection.send(connectionId,opCode.AckMsg(opCode.LOGOUT));
                connection.disconnect(connectionId);
            }
            else
                connection.send(connectionId,opCode.ErrorMsg(opCode.LOGOUT));
        }
    }

    public void follow(String name)//add him
    {
        synchronized (blockMe)
        {
            User user = network.getUser(name);
            if (user != null && (blockMe.contains(user) || blockYou.contains(user))) {
                connection.send(connectionId, opCode.ErrorMsg(opCode.FOLLOW));
                return;
            }

            User succeeded=network.followUser(name,this);
            if(succeeded!=null)
            {
                synchronized (following)
                {
                    following.add(succeeded);
                }
                connection.send(connectionId,opCode.AckMsg(opCode.FOLLOW));
            }else
            {
                connection.send(connectionId,opCode.ErrorMsg(opCode.FOLLOW));
            }
        }

    }
    public boolean addfollower(User wantToFollow)//add me
    {
            synchronized (followers)
            {
                if(followers.contains(wantToFollow))return false;
                if(wantToFollow==this)return false;
                followers.add(wantToFollow);
                return true;
            }

    }

    public void unfollow(String name)//remove him
    {

        User succeeded=network.unfollowUser(name,this);
        if(succeeded!=null)
        {
            synchronized (following) {
                following.remove(succeeded);
            }
            connection.send(connectionId,opCode.AckMsg(opCode.FOLLOW));
        }else
        {
            connection.send(connectionId,opCode.ErrorMsg(opCode.FOLLOW));
        }
    }

    public boolean removefollower(User wantToNotFollow)//remove me
    {

        synchronized (followers)
        {
            if(!followers.contains(wantToNotFollow))return false;
            followers.remove(wantToNotFollow);
            return true;
        }

    }
    public void block(String name)//block him
    {

        User succeeded=network.blockUser(name,this);
        if(succeeded!=null)
        {
            blockYou.add(succeeded);
            synchronized (following)
            {
                following.remove(succeeded);
            }

            synchronized (followersKey)
            {
                if(followers.contains(succeeded))
                    followers.remove(succeeded);
            }
            connection.send(connectionId,opCode.AckMsg(opCode.BLOCK));
        }else
        {
            connection.send(connectionId,opCode.ErrorMsg(opCode.BLOCK));
        }
    }
    public boolean addBlockMe(User wantToBlock)//block me
    {
        synchronized (blockMe)
        {
            synchronized (followersKey)
            {
                if(blockMe.contains(wantToBlock)||wantToBlock==this)return false;
                if(followers.contains(wantToBlock))
                {
                    followers.remove(wantToBlock);
                }
                blockMe.add(wantToBlock);

            }
            synchronized (following)
            {
                if(following.contains(wantToBlock))
                     following.remove(wantToBlock);
            }

        }
        return true;

    }
    public void post(String content)//post you
    {

        HashSet<User>tagged=tagged(content);
        if(tagged==null){
            connection.send(connectionId,opCode.ErrorMsg(opCode.POST));
            return;
        }
        HashSet<User> sent=new HashSet<>();
        synchronized (blockMe)//tags
        {
            for(User user:tagged)
            {
                if(user==this){
                    connection.send(connectionId,opCode.ErrorMsg(opCode.POST));
                    return;
                }
                else if(blockMe.contains(user) || blockYou.contains(user))continue;
                else sent.add(user);
            }//TODO::adsadsd
            for(User user:sent)
                user.postIncoming(content,userName);
        }
        synchronized (followersKey)//normal followers
        {
            for(User user:followers)
            {
                if(!tagged.contains(user))
                    user.postIncoming(content,userName);
            }
        }
        connection .send(connectionId,opCode.AckMsg(opCode.POST));
        msgs.add(content);
        numOfPosts++;

    }
    public void postIncoming(String content,String postUser)//post me
    {

        synchronized (logout_inKey)
        {
            if(isLogin.get())
                connection.send(connectionId, opCode.notificationMsg("PB",postUser,content));
            else
            {
                synchronized (unWatchedMessages)
                {
                    unWatchedMessages.add(opCode.notificationMsg("PB",postUser,content));
                }
            }
        }


    }

    public void sendPM(String sentTo,String content, String date)//pm you
    {
        content=network.removeWords(content);
        synchronized (blockMe)
        {
            User sentToUser=network.getUser(sentTo);
            if(sentToUser==null||blockYou.contains(sentToUser)||blockMe.contains(sentToUser)||sentToUser==this || !following.contains(sentToUser))
            {
                connection.send(connectionId,opCode.ErrorMsg(opCode.PM));
                return;
            }
            else
            {
                sentToUser.PmIncoming(userName,content, date);

            }
        }
        connection .send(connectionId,opCode.AckMsg(opCode.PM));
        msgs.add(content);
    }
    public void PmIncoming(String sentFrom,String content, String date)//pm me
    {

        synchronized (logout_inKey)
        {
            if(isLogin.get())
                connection.send(connectionId, opCode.notificationMsg("PM",sentFrom,content, date));
            else
            {
                synchronized (unWatchedMessages)
                {
                    unWatchedMessages.add(opCode.notificationMsg("PM",sentFrom,content, date));
                }
            }
        }
    }
    public void logStat()
    {
        List<Tuple> statList=network.logStats(this);
        synchronized (blockMe) {
            for (Tuple temp : statList) {
                if(!blockMe.contains(temp.getUser())&&!blockYou .contains(temp.getUser()))
                     connection.send(connectionId, opCode.SpecialAckMsg(opCode.LOGSTAT, temp.getStat().toString()));
            }
        }
    }
    public void stat(List<String>names)
    {
        List<Tuple> statList=network.stats(names,userName);
        //System.out.println(names.get(0));
        if(statList==null) {
            connection.send(connectionId, opCode.ErrorMsg(opCode.STAT));
            return;
        }
        synchronized (blockMe){
            for(Tuple temp:statList)
            {
                if(!blockMe.contains(temp.getUser())&&!blockYou .contains(temp.getUser()))
                        connection.send(connectionId, opCode.SpecialAckMsg(opCode.STAT,temp.getStat().toString()));
            }
        }

    }
    public HashSet<User> tagged(String content)
    {
        HashSet<User>tagged=new HashSet<>();
        List<String > temp= Arrays.asList(content.split(" "));
        for(String x:temp)
        {
            if(x.startsWith("@"))
            {
                if(x.length()<2)return null;
                User t=network.getUser( x.substring(1));
                if(t==null)return null;
                else tagged.add(t);
            }

        }
        return tagged;
    }
    public int getAge()
    {
        int day, month, year;
        String date=birthDay;
        String s = date.substring(0,2);
        if (s.charAt(0) == '0') day = Integer.parseInt(String.valueOf(s.charAt(1)));
        else day = Integer.parseInt(s);

        s = date.substring(3, 5);
        if (s.charAt(0) == '0') month = Integer.parseInt(String.valueOf(s.charAt(1)));
        else month = Integer.parseInt(s);

        s = date.substring(6);
        year = Integer.parseInt(s);

        LocalDate birthDate = LocalDate.of(year, month, day);
        LocalDate currentDate = LocalDate.now();

        return Period.between(birthDate, currentDate).getYears();
    }

    public Object getLogout_inKey()
    {
        return logout_inKey;
    }
    public int getNumOfPosts()
    {
        return numOfPosts;
    }
    public int getNumOfFollowers()
    {
        synchronized (followersKey)
        {
            return followers.size();
        }
    }
    public int getNumofFollowing()
    {
        synchronized (following)
        {
            return following.size();
        }
    }
    public Connections getConnection()
    {
        return connection;
    }


}

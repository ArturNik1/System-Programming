package bgu.spl.net.serverLogic;



import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Network {
    private static class NetworkHolder {
        private static Network instance = new Network();
    }
    public static Network getInstance(){return NetworkHolder.instance;}

    private ConcurrentHashMap<String,User> nameToUser;
    private HashSet<String> blackList;
    private Object regKey;


    private Network()
    {
        nameToUser=new ConcurrentHashMap<>();
        blackList=new HashSet<>();
        addWords(new String[] {"Trump", "war"});
        regKey=new Object();
    }

    private void addWords(String[] words) {
        for (int i = 0; i < words.length; i++) {
            blackList.add(words[i]);
        }
    }

    public String removeWords(String msg)
    {
        for(String black:blackList)
            msg=msg.replaceAll(black,"filtered");
        return msg;
    }
    public void register(User user,int connectionId)
    {
        //TODO:if user do not exist
        synchronized (regKey) {
            if(!nameToUser.containsKey(user.getUserName()))
            {

                nameToUser.put(user.getUserName(), user);
                user.getConnection().send(connectionId,opCode.AckMsg(opCode.REGISTER));
            }
            else
            {
                user.getConnection().send(connectionId,opCode.ErrorMsg(opCode.REGISTER));
            }
        }

    }
    public User login(String userName,String password,short chapta,int connectionId)
    {
        User loggedIn=null;
        //TODO:retun user if not already login by other client
            if(nameToUser.containsKey(userName)&&nameToUser.get(userName).login(password,chapta,connectionId))
            {
                loggedIn=nameToUser.get(userName);
                loggedIn.getConnection().send(connectionId,opCode.AckMsg(opCode.LOGIN));
            }


        return loggedIn;
    }
    public User followUser(String userName, User actor)
    {
        User followAfter;
        if(!nameToUser.containsKey(userName))return null;
        else followAfter=nameToUser.get(userName);
        if(!followAfter.addfollower(actor))
            followAfter=null;
        return followAfter;
    }
    public User unfollowUser(String userName, User actor)//TODO:can reach userName from actor
    {
        User unfollowAfter;
        if(!nameToUser.containsKey(userName))return null;
        else unfollowAfter=nameToUser.get(userName);
        if(!unfollowAfter.removefollower(actor))
            unfollowAfter=null;
        return unfollowAfter;
    }
    public User blockUser(String userName, User actor)
    {
        User blockUser;
        if(!nameToUser.containsKey(userName))return null;
        else blockUser=nameToUser.get(userName);
        if(!blockUser.addBlockMe(actor))
            blockUser=null;
        return blockUser;
    }
    public List<Tuple> logStats(User me)
    {

        List<Tuple> stat=new LinkedList<>();
        synchronized (regKey)
        {
            for(User user: nameToUser.values())
            {

                if(user!=me)
                {
                    synchronized (user.getLogout_inKey())
                    {
                        //System.out.println("Tomer print : "  +user.getUserName());
                        stat.add(new Tuple(user,new stat(user.getAge(), user.getNumOfPosts(), user.getNumOfFollowers(),user.getNumofFollowing())));
                    }
                }
            }
        }
        return stat;
    }
    public List<Tuple> stats(List<String>names,String sender)
    {
        List<Tuple> stat=new LinkedList<>();
            for(String name: names)
            {
               User user=nameToUser.get(name);
               if(user==null)return null;
               if(user.getUserName().equals(sender)) return null;
                    synchronized (user.getLogout_inKey())
                    {
                        stat.add(new Tuple(user, new stat(user.getAge(), user.getNumOfPosts(), user.getNumOfFollowers(),user.getNumofFollowing())));
                    }

            }

        return stat;
    }
    public User getUser(String userName)
    {
        return nameToUser.get(userName);
    }
}

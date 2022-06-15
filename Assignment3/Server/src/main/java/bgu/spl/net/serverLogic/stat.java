package bgu.spl.net.serverLogic;

public class stat {
    private int age;
    private int numOfPost;
    private int numFollowers;
    private int numFollowing;
    public stat(int age,int numOfPost,int numFollowers,int numFollowing)
    {
        this.age=age;
        this.numOfPost=numOfPost;
        this.numFollowers=numFollowers;
        this.numFollowing=numFollowing;
    }

    public int getAge() {
        return age;
    }

    public int getNumOfPost() {
        return numOfPost;
    }

    public int getNumFollowers() {
        return numFollowers;
    }

    public int getNumFollowing() {
        return numFollowing;
    }
    public String toString()
    {
        return ""+age+" "+numOfPost+" "+numFollowers+" "+numFollowing;
    }
}

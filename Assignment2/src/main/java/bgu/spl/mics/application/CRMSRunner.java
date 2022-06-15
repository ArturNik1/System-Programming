package bgu.spl.mics.application;

import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        MessageBus messageBus=MessageBusImpl.getInstance();
        Initializer initializer= createObj(args[0]);
        initializer.prepare();
        Cluster.getInstance().getStatistics().setOutputPath(args[1]);
        List<Student>students=initializer.getRealStudents();
        Cluster.getInstance().getStatistics().setStudents((LinkedList<Student>) students);
        List<Model> models=initializer.getRealModels();
        List<CPU> cpus=initializer.getCPUS();
        List<GPU> gpus=initializer.getGPUS();
        TimeService timeService=initializer.getTimeService();

        List<ConferenceService> conferenceServices=initializer.getConferenceServices();
        List<CPUService> cpuServices=initializer.getCpuServices();
        List<GPUService> gpuServices=initializer.getGpuServices();
        List<StudentService> studentServices=initializer.getStudentServices();

        LinkedList<Thread> studentThreads = new LinkedList<>();
        for(int i = 0; i < studentServices.size(); i++) {
            Thread t  = new Thread(studentServices.get(i));
            studentThreads.add(t);
            t.start();
        }
        LinkedList<Thread> cpuThreads = new LinkedList<>();
        for(int i = 0; i < cpuServices.size(); i++) {
            Thread t = new Thread(cpuServices.get(i));
            cpuThreads.add(t);
            t.start();
        }
        LinkedList<Thread> gpuThreads = new LinkedList<>();
        for(int i = 0; i < gpuServices.size(); i++) {
            Thread t = new Thread(gpuServices.get(i));
            gpuThreads.add(t);
            t.start();
        }
        LinkedList<Thread> conferenceThreads = new LinkedList<>();
        for(int i = 0; i < conferenceServices.size(); i++) {
            Thread t = new Thread(conferenceServices.get(i));
            conferenceThreads.add(t);
            t.start();
        }
        Thread timeThread = new Thread(timeService);
        timeThread.start();

//        reg(conferenceServices,messageBus);
//        reg(cpuServices,messageBus);
//        reg(gpuServices,messageBus);
//        reg(studentServices,messageBus);
        //timeService.run();

        //need to create threads

    }
    public static void reg(List<? extends MicroService> list, MessageBus messageBus)
    {
        for(MicroService m:list)
                messageBus.register(m);
    }
    public static Initializer createObj(String path)
    {
        Gson gson=new Gson();
        BufferedReader br;
        try {
            br= new BufferedReader(new FileReader(path));
            Initializer result =gson.fromJson(br,Initializer.class);
            return result;

        }catch (FileNotFoundException e)
        {

        }
        return null;
    }
}

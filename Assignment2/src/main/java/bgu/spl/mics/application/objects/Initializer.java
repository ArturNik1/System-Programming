package bgu.spl.mics.application.objects;

import bgu.spl.mics.ServiceCounter;
import bgu.spl.mics.application.objects.dummyConference;
import bgu.spl.mics.application.objects.dummyStudent;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

import javax.xml.ws.Service;
import java.util.LinkedList;
import java.util.List;

public class Initializer {
    private List<dummyStudent> Students;
    private List<GPU.Type > GPUS;
    private List<Integer> CPUS;
    private List<dummyConference> Conferences;
    private int TickTime;
    private int Duration;

    private List<Model> RealModels;
    private Cluster cluster;
    private List<CPU>RealCpus;
    private List<CPUService> cpuServices;
    private List<GPU> RealGpus;
    private List<GPUService> gpuServices;
    private List<Student> RealStudents;
    private List<StudentService> studentServices;
    private List<ConfrenceInformation>RealConference;
    private List<ConferenceService> conferenceServices;
   public Initializer(List<dummyStudent> Students,List<GPU.Type> GPUS,List<Integer> CPUS,List<dummyConference> Conferences,int TickTime,int Duration)
   {
       this.Students=Students;
       this.GPUS=GPUS;
       this.Conferences=Conferences;
       this.CPUS=CPUS;
       this.TickTime=TickTime;
       this.Duration=Duration;
   }
   public void prepare()
   {
       ServiceCounter.getInstance().setTargetCounter(Students.size()+GPUS.size()+Conferences.size()+CPUS.size()+1);
       ServiceCounter.getInstance().setTargetGPU(GPUS.size());
       cluster=Cluster.getInstance();
       prepareStudentsAndModels();
       prepareCPUS();
       prepareGPUS();
       prepareConference();
       cluster.setCPUS((LinkedList<CPU>) RealCpus);
       cluster.setGPUS((LinkedList<GPU>) RealGpus);
   }
   public void prepareStudentsAndModels()
    {

        this.RealModels=new LinkedList<>();
        this.RealStudents=new LinkedList<>();
        this.studentServices=new LinkedList<>();

        for(dummyStudent dummy:Students)
        {
            Model[]temp=new Model[dummy.getModels().size()];
            Student student=new Student(dummy.getName(), dummy.getDepartment(), dummy.getStatus());
            RealStudents.add(student);
            StudentService service=new StudentService(dummy.getName(), student);
            studentServices.add(service);
            int i=0;
            for(dummyModel dummyModel:dummy.getModels())
            {
                Model m=new Model(dummyModel.getName(),new Data(dummyModel.getType(),dummyModel.getSize()),student);
                RealModels.add(m);
                temp[i]=m;
                i++;
            }
            student.setModels(temp);

        }
    }
    public void prepareCPUS()
    {
        this.RealCpus=new LinkedList<>();
        this.cpuServices=new LinkedList<>();
        for(Integer i:CPUS)
        {
            CPU cpu=new CPU(i,cluster);
            RealCpus.add(cpu);
            cpuServices.add(new CPUService(i.toString(),cpu));
        }
    }
    public void prepareGPUS()
    {
        this.RealGpus=new LinkedList<>();
        this.gpuServices=new LinkedList<>();
        for(GPU.Type i:GPUS)
        {
            GPU gpu = new GPU(i);
            RealGpus.add(gpu);
            GPUService service = new GPUService(i.name(), gpu);
            gpuServices.add(service);
            gpu.setService(service);
        }
    }
    public void prepareConference()
    {

        this.RealConference=new LinkedList<>();
        this.conferenceServices=new LinkedList<>();
        for(dummyConference dummy:Conferences)
        {
            ConfrenceInformation info = new ConfrenceInformation(dummy.getName(), dummy.getDate());
            RealConference.add(info);
            conferenceServices.add(new ConferenceService(dummy.getName(), info));
        }
        cluster.getStatistics().setConferenceInformations((LinkedList<ConfrenceInformation>) RealConference);
    }

    public  List<Model> getRealModels()
    {
      return RealModels;
    }

    public  List<CPU> getCPUS()
    {
        return RealCpus;
    }
    public  List<GPU> getGPUS()
    {
        return RealGpus;
    }
    public  TimeService getTimeService()
    {
        return new TimeService(TickTime,Duration);
    }
    public List<ConfrenceInformation>getRealConference()
    {
        return RealConference;
    }

    public List<Student> getRealStudents() {
        return RealStudents;
    }
    //----------get service
    public List<CPUService> getCpuServices()
    {
        return cpuServices;
    }
    public List<GPUService> getGpuServices()
    {
        return gpuServices;
    }
    public List<ConferenceService> getConferenceServices()
    {
        return conferenceServices;
    }
    public List<StudentService> getStudentServices()
    {
        return studentServices;
    }
}

package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class Statistics {

    private LinkedList<String> modelNames;
    private LinkedList<Student> students;
    private LinkedList<ConfrenceInformation> conferenceInformations;
    private AtomicInteger totalBatchesProcessed;
    private AtomicInteger cpuTimeUnits;
    private AtomicInteger gpuTimeUnits;

    private Object modelNameKey = new Object();

    private String outputPath;

    public Statistics() {
        this.totalBatchesProcessed = new AtomicInteger(0);
        this.cpuTimeUnits = new AtomicInteger(0);
        this.gpuTimeUnits = new AtomicInteger(0);
        this.modelNames = new LinkedList<>();
        this.students = new LinkedList<>();
        this.conferenceInformations = new LinkedList<>();
    }
    public void aa(int x){gpuTimeUnits.addAndGet(x);}
    public void incGPUTimeUnits() {
        gpuTimeUnits.incrementAndGet();
    }

    public void incCPUTimeUnits() {
        cpuTimeUnits.incrementAndGet();
    }

    public void incTotalBatches() {
        totalBatchesProcessed.incrementAndGet();
    }

    public void setStudents(LinkedList<Student> students) {
        this.students = students;
    }

    public void addModelName(String name) {
        synchronized (modelNameKey) {
            modelNames.add(name);
        }
    }

    public void setOutputPath(String path) {
        this.outputPath = path;
    }

    public void setConferenceInformations(LinkedList<ConfrenceInformation> infos) {
        this.conferenceInformations = infos;
    }

    public void generateOutputFile() {
        try {
            Writer writer = new FileWriter(outputPath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(new OutputJson(students, conferenceInformations, cpuTimeUnits.get(), gpuTimeUnits.get(), totalBatchesProcessed.get()), writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class OutputJson {
    private LinkedList<DummyStudentJson> students = new LinkedList<>();
    private LinkedList<DummyConferenceInformationJson> conferences = new LinkedList<>();
    private int cpuTimeUnits;
    private int gpuTimeUnits;
    private int totalBatchesProcessed;

    public OutputJson(LinkedList<Student> beforeStudents, LinkedList<ConfrenceInformation> beforeInfo, int cpuTime, int gpuTime, int total) {
        System.out.println("Here");
        for (Student student : beforeStudents) {
            students.add(new DummyStudentJson(student));
        }

        for (ConfrenceInformation info : beforeInfo) {
            conferences.add(new DummyConferenceInformationJson(info));
        }

        this.cpuTimeUnits = cpuTime;
        this.gpuTimeUnits = gpuTime;
        this.totalBatchesProcessed = total;
    }
}

class DummyStudentJson {
    private String name;
    private String department;
    private Student.Degree status;
    private int publications;
    private int papersRead;
    private LinkedList<DummyModelJson> modelsAttempted = new LinkedList<>();

    public DummyStudentJson(Student student) {
        this.name = student.getName();
        this.department = student.getDepartment();
        this.status = student.getStatus();
        this.publications = student.getPublications();
        this.papersRead = student.getPapersRead();

        Model[] models = student.getModels();
        for (int i = 0; i < models.length; i++) {
            if (models[i].getResult() != Model.Results.None) modelsAttempted.add(new DummyModelJson(models[i].getName(),
                    models[i].getData(), models[i].getStatus(), models[i].getResult()));
        }

    }
}

class DummyConferenceInformationJson {
    private String name;
    private int date;
    private LinkedList<DummyModelJson> models;

    public DummyConferenceInformationJson (ConfrenceInformation confrenceInformation) {
        this.name = confrenceInformation.getName();
        this.date = confrenceInformation.getDate();
        this.models = confrenceInformation.getModels();
    }

}
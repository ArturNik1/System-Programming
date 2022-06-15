package bgu.spl.mics.application.objects;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private int publications;
    private int papersRead;
    private Model[] models;

    public Student (String name, String department, Degree status) {
        this.name = name;
        this.department = department;
        this.status = status;
        this.publications = 0;
        this.papersRead = 0;
    }

    public void setModels(Model[] models) {
        this.models = models;
    }

    public void incPapersRead(HashSet<String> modelNames) {
        int amount = modelNames.size();
        for (int i = 0; i < models.length; i++) {
            if (modelNames.contains(models[i].getName()))
                amount--;

        }
        papersRead += amount;
        System.out.println(name+" read "+papersRead+ " models at this time");//---------------------
    }

    public void incPublications() {
        System.out.println(name+ " publish "+models[publications].getName());
        this.publications++;
    }

    public Model[] getModels() {
        return this.models;
    }

    public Model getCurrentModel(int i) {
        if (models == null || i < 0 || i >= models.length) return null;
        return this.models[i];
    }

    public boolean isMSc() {
        return status == Degree.MSc;
    }

    public boolean isTraining(int modelIndex) {
        if (models[modelIndex].getStatus() == Model.Status.Training) return true;
        else return false;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public Degree getStatus() {
        return status;
    }

    public int getPublications() {
        return publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

}

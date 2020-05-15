package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;


import java.io.IOException;
import java.nio.file.Paths;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

            // construit une solution dans la représentation par
            // numéro de jobs : [0 1 1 0 0 1]
            // Note : cette solution a aussi été vue dans les exercices (section 3.3)
            //        mais on commençait à compter à 1 ce qui donnait [1 2 2 1 1 2]
            JobNumbers enc = new JobNumbers(instance);
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;

            System.out.println("\nENCODING: " + enc);

            Schedule sched = enc.toSchedule();
            // TODO: make it print something meaningful
            // by implementing the toString() method
            System.out.println("SCHEDULE: " + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

            ResourceOrder rso = new ResourceOrder(instance);

            rso.addTask(0,0,0);
            rso.addTask(0,1,1);
            rso.addTask(1,1,0);
            rso.addTask(1,0,1);
            rso.addTask(2,0,2);
            rso.addTask(2,1,2);

            Schedule s = rso.toSchedule();
            System.out.println("SCHEDULE: " + s);

            ResourceOrder rso2 = new ResourceOrder(s);
            System.out.println("rso1" + rso.toString() + "rso2" + rso2.toString());

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}

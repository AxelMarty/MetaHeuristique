package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import java.util.*;
public class LRPT implements Solver {

    public Result solve(Instance instance, long deadline) {
        HashSet<Task> HashTasksRealisable = new HashSet<Task>();
        int JobDuration[] = new int [instance.numJobs];
        int JobDurationDone[] = new int [instance.numJobs];
        HashSet<Task> HashTasksDone = new HashSet<Task>();
        ResourceOrder RorderSolution = new ResourceOrder(instance);

        //init
        for(int job = 0 ; job<(instance.numJobs) ; job++) {
            Task t = new Task(job,0);
            HashTasksRealisable.add(t);
        }
        for(int job=0; job<instance.numJobs; job++) {
            for (int task = 0; task < instance.numTasks; task++) {
                JobDuration[job]+=instance.duration(job,task);
            }
        }

        while(!HashTasksRealisable.isEmpty()){
            Task taskMax = null;
            for(Task t : HashTasksDone) {
            }
            for(Task t : HashTasksRealisable) {
                if(taskMax == null){
                    taskMax=t;
                }
                if (JobDuration[t.job]-JobDurationDone[t.job] > JobDuration[taskMax.job]-JobDurationDone[taskMax.job]) {
                    taskMax=t;
                }
            }
            RorderSolution.addTask(instance.machine(taskMax),taskMax.job, taskMax.task);
            HashTasksRealisable.remove(taskMax);
            JobDurationDone[taskMax.job]+=instance.duration(taskMax);
            if(instance.numTasks-1>taskMax.task){
                HashTasksRealisable.add(new Task(taskMax.job,(taskMax.task +1)));
                HashTasksDone.add(new Task(taskMax.job,(taskMax.task)));
            }
        }


        return new Result(instance, RorderSolution.toSchedule(), Result.ExitCause.Blocked);
    }
}

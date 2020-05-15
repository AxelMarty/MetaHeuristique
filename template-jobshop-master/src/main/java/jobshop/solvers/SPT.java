package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import java.util.*;
public class SPT implements Solver {

    public Result solve(Instance instance, long deadline) {
        HashSet<Task> HashTasksRealisable = new HashSet<Task>();
        ResourceOrder RorderSolution = new ResourceOrder(instance);

        //init
        for(int job = 0 ; job<(instance.numJobs) ; job++) {
            Task t = new Task(job,0);
            HashTasksRealisable.add(t);
        }

        while(!HashTasksRealisable.isEmpty()){
            Task taskMin = null;
            for(Task t : HashTasksRealisable) {
                if(taskMin == null){
                    taskMin=t;
                }
                if (instance.duration(t) <instance.duration(taskMin)) {
                    taskMin=t;
                }
            }
            RorderSolution.addTask(instance.machine(taskMin),taskMin.job, taskMin.task);
            HashTasksRealisable.remove(taskMin);
            if(instance.numTasks-1>taskMin.task){
                HashTasksRealisable.add(new Task(taskMin.job,(taskMin.task +1)));
            }
        }


        return new Result(instance, RorderSolution.toSchedule(), Result.ExitCause.Blocked);
    }
}

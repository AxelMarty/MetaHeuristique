package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import java.util.*;
public class GloutonEST_LRPT implements Solver {

    public Result solve(Instance instance, long deadline) {
        HashSet<Task> HashTasksRealisable = new HashSet<Task>();
        HashSet<Task> HashTasksMinimal = new HashSet<Task>();
        ResourceOrder RorderSolution = new ResourceOrder(instance);
        int [][] startTimes = new int [instance.numJobs][instance.numTasks];
        for(int i=0;i<instance.numJobs;i++){
            for(int j=0;j<instance.numTasks;j++){
                startTimes[i][j]=0;
            }
        }
        int[] releaseTimeOfMachine = new int[instance.numMachines];
        Arrays.fill(releaseTimeOfMachine,0);
        //init
        int duration=0;
        for(int job = 0 ; job<(instance.numJobs) ; job++) {
            Task t = new Task(job,0);
            HashTasksRealisable.add(t);
        }
        int [] RemainingTime = new int[instance.numJobs ];

        // initialisation Remaining Time
        for( int i = 0 ; i< instance.numJobs  ; i ++) {
            for(int j = 0 ; j < instance.numTasks ; j++) {
                RemainingTime[i] += instance.duration(i,j);
            }
        }
        while(!HashTasksRealisable.isEmpty()){
            HashTasksMinimal.clear();
            int minStartTime =-1;

            for(Task t : HashTasksRealisable) {
                if(minStartTime== -1){
                    minStartTime=t.task == 0 ? 0 : startTimes[t.job][t.task - 1] + instance.duration(t.job, t.task - 1);
                    minStartTime = Math.max(minStartTime, releaseTimeOfMachine[instance.machine(t)]);
                    //System.out.println("min start=  " + minStartTime);
                    HashTasksMinimal.add(t);
                }
                else {
                    //est1
                    int estT = t.task == 0 ? 0 : startTimes[t.job][t.task - 1] + instance.duration(t.job, t.task - 1);
                    estT = Math.max(estT, releaseTimeOfMachine[instance.machine(t)]);

                    if (estT < minStartTime) {
                        HashTasksMinimal.clear();
                        HashTasksMinimal.add(t);
                        minStartTime=estT;
                        //System.out.println("new min " + minStartTime);
                    }
                    else if (estT == minStartTime) {
                        HashTasksMinimal.add(t);
                        //System.out.println("ajout tache");
                    }

                }

            }

            //System.out.println("min " + HashTasksMinimal);
            //System.out.println("normal " + HashTasksRealisable);

            Task taskMin = null;


            for ( Task t : HashTasksMinimal) {
                if(taskMin == null) {
                    taskMin = t;
                    duration = RemainingTime[t.job];
                }
                else if (duration < RemainingTime[t.job] ) {
                    taskMin = t;
                    duration = RemainingTime[t.job];
                }
            }
            startTimes[taskMin.job][taskMin.task]=minStartTime;
            releaseTimeOfMachine[instance.machine(taskMin)]=minStartTime+instance.duration(taskMin);
            RorderSolution.addTask(instance.machine(taskMin),taskMin.job, taskMin.task);
            HashTasksRealisable.remove(taskMin);
            RemainingTime[taskMin.job]-=instance.duration(taskMin);

            if(instance.numTasks-1>taskMin.task){
                HashTasksRealisable.add(new Task(taskMin.job,(taskMin.task +1)));
            }

        }


        return new Result(instance, RorderSolution.toSchedule(), Result.ExitCause.Blocked);
    }
}

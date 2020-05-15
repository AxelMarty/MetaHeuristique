package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;


public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    static class Block {
        /** machine on which the block is identified */
        public int machine;
        /** index of the first task of the block */
        public int firstTask;
        /** index of the last task of the block */
        public int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
            ResourceOrder temp = order.copy();
            order.tasksByMachine[this.machine][t1]=temp.tasksByMachine[this.machine][t2];
            order.tasksByMachine[this.machine][t2]=temp.tasksByMachine[this.machine][t1];

        }
    }

    @Override
    public Result solve(Instance instance, long deadline) {
        jobshop.solvers.GloutonEST_LRPT glouton = new jobshop.solvers.GloutonEST_LRPT();
        // initialisation
        Result sinit = glouton.solve(instance,deadline);
        int setoile = sinit.schedule.makespan();
        boolean amelioration = true;
        ResourceOrder r = new ResourceOrder(sinit.schedule);
        while(amelioration && (deadline - System.currentTimeMillis() > 1)){
            List<Block> blocks = blocksOfCriticalPath(r);
            ResourceOrder voisin = null;
            ResourceOrder meilleurVoisin = r;
            for (Block block : blocks ) {
                List<Swap> swaps = neighbors(block);
                for (Swap swap : swaps ) {
                    voisin=r.copy();
                    swap.applyOn(voisin);
                    if(meilleurVoisin.toSchedule().makespan() > voisin.toSchedule().makespan()){
                        meilleurVoisin=voisin.copy();
                    }
                }
            }
            //System.out.println(" meilleurVoisin"+ meilleurVoisin.toSchedule().makespan());
            //System.out.println(" setyoile"+ setoile);
            if(meilleurVoisin.toSchedule().makespan() < setoile) {
                setoile=meilleurVoisin.toSchedule().makespan();
                r = meilleurVoisin;
                amelioration=true;
            }
            else {
                amelioration=false;
            }
        }
        return new Result(instance, r.toSchedule(), Result.ExitCause.Blocked);
    }

    /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {
        List<Block> blocksOfCriticalPath = new ArrayList<Block>();
        List<Task> tasksOfCriticalPath = new ArrayList<Task>();
        Schedule sch = order.toSchedule();
        tasksOfCriticalPath = sch.criticalPath();
        //System.out.println("" + order);
        //System.out.println("task " + tasksOfCriticalPath);
        for(int i=0;i<tasksOfCriticalPath.size();i++){
            Task currentT = tasksOfCriticalPath.get(i);
            //System.out.println("current" + currentT);
            //System.out.println("i " + i);
            Block b = new Block(order.instance.machine(currentT.job,currentT.task),
                    findIndex(order,order.instance.machine(currentT.job,currentT.task),currentT),
                    findIndex(order,order.instance.machine(currentT.job,currentT.task),currentT));
            if(i < tasksOfCriticalPath.size()-1){
                int j=i+1;
                Task tempo = tasksOfCriticalPath.get(j);
                //System.out.println("temp" + tempo);
                while (j<tasksOfCriticalPath.size() && (order.instance.machine(currentT.job, currentT.task) == order.instance.machine(tempo.job, tempo.task)))
                {
                    // set block
                    b.lastTask= findIndex(order,order.instance.machine(tempo.job,tempo.task),tempo);
                    j++;
                    if(j< tasksOfCriticalPath.size()){
                        tempo = tasksOfCriticalPath.get(j);
                    }
                }
                if(b.firstTask < b.lastTask){
                    blocksOfCriticalPath.add(b);

                    //System.out.println("machine block" + b.machine);
                    //System.out.println("firstTask block" + b.firstTask);
                    //System.out.println("lastTask block" + b.lastTask);
                }
                i=j-1;
                //System.out.println("i fin " + i);
            }
        }
        return blocksOfCriticalPath;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
        List<Swap> swap = new ArrayList<DescentSolver.Swap>();
        int size = block.lastTask - block.firstTask;
        if( size==1){
            // un swap possible
            swap.add(new Swap(block.machine,block.firstTask, block.lastTask));
        }
        else{
            swap.add(new Swap(block.machine,block.firstTask, block.firstTask +1));
            swap.add(new Swap(block.machine,block.lastTask -1, block.lastTask));
        }
        return swap;
    }

    public int findIndex(ResourceOrder order, int machine, Task task_find){

        int index = 0;
        for (Task task : order.tasksByMachine[machine] ) {
            if(task.equals(task_find))
                return index;
            index++;
        }
        return -1;

    }
}

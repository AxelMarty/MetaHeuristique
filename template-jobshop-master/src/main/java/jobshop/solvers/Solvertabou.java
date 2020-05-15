package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;


public class Solvertabou implements Solver {

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

    static final int maxIter=100;
    static final int dureeTabou=10;


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
        Result result = glouton.solve(instance,deadline);
        ResourceOrder sinit = new ResourceOrder(result.schedule);
        ResourceOrder setoile = sinit.copy();
        ResourceOrder s = sinit.copy();
        int stabou[][]= new int[instance.numJobs * instance.numMachines][instance.numJobs * instance.numMachines];
        //creation tabou
        int k=0;
        while( k <= maxIter && (deadline - System.currentTimeMillis() > 1)){
            k=k+1;
            //System.out.println(s);
            List<Block> blocks = blocksOfCriticalPath(s);
            ResourceOrder voisin = null;
            ResourceOrder meilleurVoisin = null;
            Swap bestSwap = null;
            for (Block block : blocks ) {
                List<Swap> swaps = neighbors(block);
                for (Swap swap : swaps ) {
                    voisin=s.copy();
                    swap.applyOn(voisin);
                    int t1 = (swap.machine * instance.numJobs) + swap.t1;
                    int t2 = (swap.machine * instance.numJobs) + swap.t2;
                    if(meilleurVoisin==null){
                        meilleurVoisin=voisin.copy();
                        bestSwap = swap;
                    }
                    else if((meilleurVoisin.toSchedule().makespan() > voisin.toSchedule().makespan()) && stabou[t1][t2] > k){
                            meilleurVoisin=voisin.copy();
                            bestSwap = swap;
                    }
                }
            }
            if(bestSwap!=null){
                int t1 = (bestSwap.machine * instance.numJobs) + bestSwap.t1;
                int t2 = (bestSwap.machine * instance.numJobs) + bestSwap.t2;
                stabou[t1][t2] = k + dureeTabou;
                s = meilleurVoisin.copy();
                if(meilleurVoisin.toSchedule().makespan() < setoile.toSchedule().makespan()) {
                    setoile=meilleurVoisin.copy();
                }
            }
           // System.out.println(s);
        }

        return new Result(instance, s.toSchedule(), Result.ExitCause.Blocked);
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
        List<Swap> swap = new ArrayList<Swap>();
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
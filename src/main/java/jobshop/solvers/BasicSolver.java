package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.Arrays;

/**
 * A very naïve solver that first schedules all first tasks, then all second tasks, ...
 **/
public class BasicSolver implements Solver {
    @Override
    public Result solve(Instance instance, long deadline) {

        // resource order that will be populated (initially empty)
        ResourceOrder sol = new ResourceOrder(instance);

        // in the resource order:
        // - enqueue all first tasks, then
        // - enqueue all second tasks, then
        // ...
        for(int taskNumber = 0 ; taskNumber<instance.numTasks ; taskNumber++) {
            for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                Task taskToEnqueue = new Task(jobNumber, taskNumber);
                sol.addTaskToMachine(instance.machine(jobNumber, taskNumber), taskToEnqueue);
            }
        }

        // Convert the resource order into a schedule and return the corresponding Result
        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
    }
}

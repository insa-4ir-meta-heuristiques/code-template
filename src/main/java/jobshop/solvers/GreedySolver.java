package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.encodings.Task;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** An empty shell to implement a greedy solver. */
public class GreedySolver implements Solver {

    /** All possible priorities for the greedy solver. */
    public enum Priority {
        SPT, LPT, SRPT, LRPT, EST_SPT, EST_LPT, EST_SRPT, EST_LRPT
    }

    /** Priority that the solver should use. */
    final Priority priority;

    /** Creates a new greedy solver that will use the given priority. */
    public GreedySolver(Priority p) {
        this.priority = p;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {
        ResourceOrder sol = new ResourceOrder(instance);

        // tables of earliest start for each job/machine, initially filled with 0
        int[] nextStartOnJob = new int[instance.numJobs];
        int[] nextStartOnMachine = new int[instance.numMachines];

        // build all possible comparators
        Comparator<Task> spt = Comparator.comparing(t -> instance.duration(t.job, t.task));
        Comparator<Task> lpt = spt.reversed();
        Comparator<Task> srpt = Comparator.comparing(t -> IntStream.range(t.task, instance.numTasks).map(task -> instance.duration(t.job, task)).sum());
        Comparator<Task> lrpt = srpt.reversed();
        Comparator<Task> minStart = Comparator.comparing(t -> Math.max(nextStartOnJob[t.job], nextStartOnMachine[instance.machine(t.job, t.task)]));

        // select the comparator that will be used to order available tasks
        Comparator<Task> comp = null;
        switch (priority) {
            case SPT: comp = spt; break;
            case LPT: comp = lpt; break;
            case LRPT: comp = lrpt; break;
            case SRPT: comp = srpt; break;
            case EST_SPT: comp = minStart.thenComparing(spt); break;
            case EST_LPT: comp = minStart.thenComparing(lpt); break;
            case EST_SRPT: comp = minStart.thenComparing(srpt); break;
            case EST_LRPT: comp = minStart.thenComparing(lrpt); break;
        }

        // all task that can be scheduled.
        // initially these are the first task of each job
        Set<Task> available = IntStream.range(0, instance.numJobs).mapToObj(j -> new Task(j, 0)).collect(Collectors.toSet());
        while (!available.isEmpty()) {
            // next task to enqueue: the one with highest priority (as given by comparator) among the available ones
            Task next = available.stream().min(comp).get();

            // enqueue selected task
            int machine = instance.machine(next.job, next.task);
            sol.addTaskToMachine(machine, next);

            // update the table of starting times
            int startTime = Math.max(nextStartOnJob[next.job], nextStartOnMachine[machine]);
            int endTime = startTime + instance.duration(next.job, next.task);
            nextStartOnJob[next.job] = endTime;
            nextStartOnMachine[machine] = endTime;

            // update the set of available tasks
            available.remove(next);
            if(next.task < instance.numTasks -1) {
                available.add(new Task(next.job, next.task+1));
            }
        }

        // we got out of the loop, meaning that all tasks have been scheduled
        return sol.toSchedule();
    }
}

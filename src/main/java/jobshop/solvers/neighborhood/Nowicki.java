package jobshop.solvers.neighborhood;

import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Implementation of the Nowicki and Smutnicki neighborhood.
 *
 * It works on the ResourceOrder encoding by generating two neighbors for each block
 * of the critical path.
 * For each block, two neighbors should be generated that respectively swap the first two and
 * last two tasks of the block.
 */
public class Nowicki extends Neighborhood {

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swap with : t1= (0,2) and t2 = (2,1)
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    public static class Swap {
        /** First task to be swapped.
         *
         * Invariant: this is always the task with the smallest job of the two */
        public final Task t1;

        /** Second task to be swapped */
        public final Task t2;

        /** Creates a new swap of two tasks. */
        Swap(Task t1, Task t2) {
            assert t1.job != t2.job;
            if (t1.job < t2.job) {
                this.t1 = t1;
                this.t2 = t2;
            } else {
                this.t1 = t2;
                this.t2 = t1;
            }
        }


        /** Creates a new ResourceOrder order that is the result of performing the swap in the original ResourceOrder.
         *  The original ResourceOrder MUST NOT be modified by this operation.
         */
        public ResourceOrder generateFrom(ResourceOrder original) {
            ResourceOrder neighbor = original.copy();
            neighbor.swapTasks(this.t1, this.t2);
            return neighbor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Swap swap = (Swap) o;
            return t1.equals(swap.t1) && t2.equals(swap.t2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(t1, t2);
        }
    }


    @Override
    public List<ResourceOrder> generateNeighbors(ResourceOrder current) {
        // convert the list of swaps into a list of neighbors (function programming FTW)
        return allSwaps(current).stream().map(swap -> swap.generateFrom(current)).collect(Collectors.toList());
    }

    /** Generates all swaps of the given ResourceOrder.
     * This method can be used if one wants to access the inner fields of a neighbors. */
    public List<Swap> allSwaps(ResourceOrder current) {
        var oSched = current.toSchedule();
        if (oSched.isEmpty()) {
            throw new RuntimeException("Invalid resource order");
        }
        var sched = oSched.get();
        var path = sched.criticalPath();
        List<Swap> swaps = new ArrayList<>();
        if (path.isEmpty()) {
            return swaps;
        }
        Function<Integer, Integer> mac = (Integer i) -> current.instance.machine(path.get(i));

        int i = 0;
        while (i < path.size() ) {
            int machine = mac.apply(i);
            boolean firstOfBlock = i == 0 || mac.apply(i-1) != machine;
            boolean lastOfBlock = i == path.size() -1 || mac.apply(i+1) != machine;

            if (firstOfBlock && !lastOfBlock) {
                swaps.add(new Swap(path.get(i), path.get(i+1)));
            } else if(lastOfBlock && !firstOfBlock) {
                // note that this one may be duplicated if the block has a size of two
                swaps.add(new Swap(path.get(i-1), path.get(i)));
            }


            i++;
        }

        return swaps;
    }

}

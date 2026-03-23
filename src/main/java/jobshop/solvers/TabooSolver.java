package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Nowicki;

import java.util.Optional;

public class TabooSolver implements Solver {

    final Solver baseSolver;
    final int tabooSize;

    public TabooSolver(Solver baseSolver, int tabooSize) {
        this.baseSolver = baseSolver;
        this.tabooSize = tabooSize;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {
        TabooList tabooList = new TabooList(this.tabooSize);
        var neighborhood = new Nowicki();

        // generate the initial solution
        var baseResult = baseSolver.solve(instance, deadline);
        if (baseResult.isEmpty()) {
            // no result from base, exit immediately
            return Optional.empty();
        }
        Schedule best = baseResult.get();
        Schedule current = best;

        while(System.currentTimeMillis() < deadline - 1 && current != null) {
            ResourceOrder previous = new ResourceOrder(current);

            // reset current from this iteration
            current = null;

            // track the swap that was used to produce "current"
            Nowicki.Swap exchanged = null;

            for(Nowicki.Swap swap : neighborhood.allSwaps(previous)) {
                boolean isTaboo = tabooList.contains(swap);

                // transform ro into neighbor
                ResourceOrder neighbor = swap.generateFrom(previous);
                Optional<Schedule> candidateOpt = neighbor.toSchedule();
                if (candidateOpt.isPresent()) {
                    Schedule candidate = candidateOpt.get();
                    if (best.makespan() > candidate.makespan()) {
                        best = candidate;
                        current = candidate;
                        exchanged = swap;
                    } else if ( !isTaboo && (current == null || current.makespan() > candidate.makespan())) {
                        current = candidate;
                        exchanged = swap;
                    }
                }
            }

            if(exchanged != null) {
                tabooList.insert(exchanged);
            }
        }

        return Optional.of(best);
    }
}
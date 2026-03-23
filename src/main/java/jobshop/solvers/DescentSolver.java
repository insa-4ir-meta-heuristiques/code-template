package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Neighborhood;

import java.util.Optional;

/** An empty shell to implement a descent solver. */
public class DescentSolver implements Solver {

    final Neighborhood neighborhood;
    final Solver baseSolver;

    /** Creates a new descent solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public DescentSolver(Neighborhood neighborhood, Solver baseSolver) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {

        var baseResult = baseSolver.solve(instance, deadline);
        if (baseResult.isEmpty()) {
            // no result from base, exit immediately
            return Optional.empty();
        }
        Schedule best = baseResult.get();

        boolean improved = true;

        while(improved && (deadline - System.currentTimeMillis()) > 10) {
            improved = false;
            ResourceOrder rep = new ResourceOrder(best);

            var neighbors = neighborhood.generateNeighbors(rep);

            for(ResourceOrder n : neighbors) {
                if(deadline - System.currentTimeMillis() < 10)
                    break;

                Optional<Schedule> candidate = n.toSchedule();
                if(candidate.isPresent() && candidate.get().makespan() < best.makespan()) {
                    improved = true;
                    best = candidate.get();
                }
            }

        }

        return Optional.of(best);
    }

}

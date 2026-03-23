package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Nowicki;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Common interface that must implemented by all solvers. */
public interface Solver {

    /** Look for a solution until blocked or a deadline has been met.
     *
     * @param instance Jobshop instance that should be solved.
     * @param deadline Absolute time at which the solver should have returned a solution.
     *                 This time is in milliseconds and can be compared with System.currentTimeMilliseconds()
     * @return An optional schedule that will be non empty if a solution was found.
     */
    Optional<Schedule> solve(Instance instance, long deadline);

    /** Static factory method to create a new solver based on its name. */
    static Solver getSolver(String name) {
        switch (name) {
            case "basic": return new BasicSolver();
            case "descent": return new DescentSolver(new Nowicki(), getSolver("est_lrpt"));
            case "taboo": return new TabooSolver(getSolver("est_lrpt"), 20);
            default:
                // use Pattern/Matcher from java.util.regex package
                Pattern taboo = Pattern.compile("taboo__([a-z_]+)__([0-9]+)");
                Matcher m = taboo.matcher(name);
                if (m.find()) {
                    String baseSolverName = m.group(1);
                    int tabooSize = Integer.parseInt(m.group(2));
                    return new TabooSolver(getSolver(baseSolverName), tabooSize);
                }
                Pattern descent = Pattern.compile("descent__([a-z_]+)");
                Matcher descentMatcher = descent.matcher(name);
                if (descentMatcher.find()) {
                    String baseSolverName = descentMatcher.group(1);
                    return new DescentSolver(new Nowicki(), getSolver(baseSolverName));
                }
                if (asGreedyPriority(name).isPresent()) {
                    return new GreedySolver(asGreedyPriority(name).get());
                }

                throw new RuntimeException("[ERROR] Unknown solver: '"+ name + "'");
        }
    }

    static Optional<GreedySolver.Priority> asGreedyPriority(String name) {
        try {
            return Optional.of(GreedySolver.Priority.valueOf(name.toUpperCase()));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

}

package jobshop.encodings;

import jobshop.Instance;
import jobshop.Result;
import jobshop.solvers.Solver;
import jobshop.solvers.BasicSolver;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class BasicSolverTests {

    @Test
    public void testBasicSolver() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        Schedule schedule = result.schedule.get();
        assert  schedule.isValid() : "The solution is not valid";

        System.out.println("Makespan: " + schedule.makespan());
        System.out.println("Schedule: \n" + schedule);
        System.out.println(schedule.asciiGantt());

        assert schedule.makespan() == 12 : "The basic solver should have produced a makespan of 12 for this instance.";
    }

}

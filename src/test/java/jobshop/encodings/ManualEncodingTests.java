package jobshop.encodings;

import jobshop.Instance;
import jobshop.Result;
import jobshop.solvers.BasicSolver;
import jobshop.solvers.Solver;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Optional;

public class ManualEncodingTests {

    /** Instance that we will be studied in these tests */
    private Instance instance;

    /** Reference scheduled (produced by the basic solver) that we will recreate manually. */
    private Schedule reference;

    @Before
    public void setUp() throws Exception {
        this.instance = Instance.fromFile(Paths.get("instances/aaa1"));

        Solver solver = new BasicSolver();
        Result result = solver.solve(this.instance, System.currentTimeMillis() + 10);

        assert result.schedule.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        this.reference = result.schedule.get();

        System.out.println("***** Reference schedule to reproduce ******");
        System.out.println("MAKESPAN: " + this.reference.makespan());
        System.out.println("SCHEDULE: " + this.reference.toString());
        System.out.println("GANTT: " + this.reference.asciiGantt());

    }

    @Ignore("Not ready yet")
    @Test
    public void testManualScheduleEncoding() {
        Schedule manualSchedule = new Schedule(instance);
        // TODO: encode the same solution
        //manualSchedule.setStartTime(....);


        assert manualSchedule.equals(this.reference);
    }

    @Ignore("Not ready yet")
    @Test
    public void testManualResourceOrderEncoding() {
        ResourceOrder manualRO = new ResourceOrder(instance);
        // TODO: encode the same solution
        //manualRO.addTaskToMachine(..., new Task(..., ...));

        Optional<Schedule> optSchedule = manualRO.toSchedule();
        assert optSchedule.isPresent() : "The resource order cuold not be converted to a schedule (probably invalid)";
        Schedule schedule = optSchedule.get();
        assert schedule.equals(this.reference) : "The manual resource order encoding did not produce the same schedule";
    }
}

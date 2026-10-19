package first.robot.hub;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PracticeEventsTest {
  private final List<String> logged = new ArrayList<>();

  @AfterEach
  void restore() {
    PracticeEvents.resetForTest(null);
  }

  @Test
  void countersOnlyIncreaseAndLogUnderPracticeEvents() {
    PracticeEvents.resetForTest((key, value) -> logged.add(key + "=" + value));
    PracticeEvents.record("Shoot");
    PracticeEvents.record("Shoot");
    PracticeEvents.record("Intake");
    assertEquals(
        List.of(
            "PracticeEvents/ShootCount=1",
            "PracticeEvents/ShootCount=2",
            "PracticeEvents/IntakeCount=1"),
        logged);
    assertEquals(2, PracticeEvents.count("Shoot"));
    assertEquals(0, PracticeEvents.count("Climb"));
  }

  @Test
  void rejectsNamesThatWouldMakeOddLogKeys() {
    PracticeEvents.resetForTest((key, value) -> logged.add(key));
    for (String bad : new String[] {null, "", "1st", "Score/High", "a".repeat(33)}) {
      assertThrows(IllegalArgumentException.class, () -> PracticeEvents.record(bad));
    }
    assertTrue(logged.isEmpty());
  }
}

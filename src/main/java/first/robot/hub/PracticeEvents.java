package first.robot.hub;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ObjLongConsumer;
import java.util.regex.Pattern;
import org.littletonrobotics.junction.Logger;

/**
 * Monotonic game-action counters for the Test Hub practice board.
 *
 * <p>Call {@code PracticeEvents.record("Shoot")} where an action happens. Each action logs {@code
 * PracticeEvents/<Action>Count} (an int64 that only increases during one boot), so the hub can
 * count actions even when two happen between logger samples. Recording only logs: it never changes
 * mechanism goals or actuator outputs. Call it from the robot (scheduler) thread.
 */
public final class PracticeEvents {
  private static final Pattern NAME = Pattern.compile("[A-Za-z][A-Za-z0-9]{0,31}");
  private static final Map<String, Long> COUNTS = new ConcurrentHashMap<>();
  private static volatile ObjLongConsumer<String> sink =
      (key, value) -> Logger.recordOutput(key, value);

  private PracticeEvents() {}

  /** Count one occurrence of {@code action} and log the new total. */
  public static void record(String action) {
    if (action == null || !NAME.matcher(action).matches()) {
      throw new IllegalArgumentException(
          "Practice event names are 1-32 letters/digits starting with a letter");
    }
    long total = COUNTS.merge(action, 1L, Long::sum);
    sink.accept("PracticeEvents/" + action + "Count", total);
  }

  /** Current total for tests and diagnostics. */
  public static long count(String action) {
    return COUNTS.getOrDefault(action, 0L);
  }

  /** Test hook: replace the log sink and clear counts. */
  static void resetForTest(ObjLongConsumer<String> testSink) {
    COUNTS.clear();
    sink = testSink == null ? (key, value) -> Logger.recordOutput(key, value) : testSink;
  }
}

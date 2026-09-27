import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

// Find the shortest distance. The original point array is not changed.
public final class ClosestPairSolver {
    public final Metrics metrics = new Metrics();

    private static final Comparator<Point> BY_X = Comparator.comparingDouble(Point::x).thenComparingDouble(Point::y);

    private static final Comparator<Point> BY_Y = Comparator.comparingDouble(Point::y).thenComparingDouble(Point::x);

    /** Positive infinity means fewer than two points. */
    public double closestDistance(Point[] points) {
        Objects.requireNonNull(points, "points");
        metrics.reset();
        for (Point point : points) Objects.requireNonNull(point, "point");
        if (points.length < 2) return Double.POSITIVE_INFINITY;
        Point[] array = points.clone();
        Arrays.sort(array, BY_X);
        return solve(array, new Point[array.length], 0, array.length, 1);
    }

    // This part starts in x order and returns in y order.
    private double solve(Point[] array, Point[] buffer, int left, int right, int depth) {
        metrics.enter(depth);
        if (right - left <= 3) {
            double best = Double.POSITIVE_INFINITY;
            for (int i = left; i < right; i++)
                for (int j = i + 1; j < right; j++) best = Math.min(best, distance(array[i], array[j]));
            Arrays.sort(array, left, right, BY_Y);
            return best;
        }
        int mid = left + (right - left) / 2;
        double splitX = array[mid].x(); // Save this before sorting each side by y.
        double leftDistance = solve(array, buffer, left, mid, depth + 1);
        double rightDistance = solve(array, buffer, mid, right, depth + 1);
        double best = Math.min(leftDistance, rightDistance);
        int i = left;
        int j = mid;
        int k = left;
        while (i < mid && j < right) {
            if (BY_Y.compare(array[i], array[j]) <= 0) {
                buffer[k] = array[i];
                i++;
            } else {
                buffer[k] = array[j];
                j++;
            }
            k++;
        }
        while (i < mid) buffer[k++] = array[i++];
        while (j < right) buffer[k++] = array[j++];
        System.arraycopy(buffer, left, array, left, right - left);
        if (best == 0) return 0; // The points are already merged in y order.
        int count = 0;
        // Keep only points close to the middle line.
        for (i = left; i < right; i++) {
            if (Math.abs(array[i].x() - splitX) < best) {
                buffer[left + count] = array[i];
                count++;
            }
        }
        for (i = 0; i < count; i++) {
            // Only the next seven points can give a shorter distance.
            for (j = i + 1; j < count && j <= i + 7; j++) {
                if (buffer[left + j].y() - buffer[left + i].y() >= best) break;
                best = Math.min(best, distance(buffer[left + i], buffer[left + j]));
            }
        }
        return best;
    }

    private double distance(Point first, Point second) {
        metrics.distanceEvaluations++;
        return Math.hypot(first.x() - second.x(), first.y() - second.y());
    }

    public static double bruteForce(Point[] points) {
        Objects.requireNonNull(points, "points");
        for (Point p : points) Objects.requireNonNull(p, "point");
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < points.length; i++)
            for (int j = i + 1; j < points.length; j++)
                best = Math.min(best, Math.hypot(points[i].x() - points[j].x(), points[i].y() - points[j].y()));
        return best;
    }
}

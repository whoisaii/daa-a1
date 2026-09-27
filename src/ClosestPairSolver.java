import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/** O(n log n) closest distance, with one merge/strip buffer. Input is preserved. */
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
        Point[] a = points.clone();
        Arrays.sort(a, BY_X);
        return solve(a, new Point[a.length], 0, a.length, 1);
    }
    // On entry this slice is x-ordered; on return it is y-ordered.
    private double solve(Point[] a, Point[] buffer, int lo, int hi, int depth) {
        metrics.enter(depth);
        if (hi - lo <= 3) {
            double best = Double.POSITIVE_INFINITY;
            for (int i = lo; i < hi; i++)
                for (int j = i + 1; j < hi; j++) best = Math.min(best, distance(a[i], a[j]));
            Arrays.sort(a, lo, hi, BY_Y);
            return best;
        }
        int mid = lo + (hi - lo) / 2;
        double splitX = a[mid].x(); // Capture before recursive calls reorder each side.
        double best = Math.min(solve(a, buffer, lo, mid, depth + 1),
                               solve(a, buffer, mid, hi, depth + 1));
        int i = lo, j = mid, k = lo;
        while (i < mid && j < hi) buffer[k++] = BY_Y.compare(a[i], a[j]) <= 0 ? a[i++] : a[j++];
        while (i < mid) buffer[k++] = a[i++];
        while (j < hi) buffer[k++] = a[j++];
        System.arraycopy(buffer, lo, a, lo, hi - lo);
        if (best == 0) return 0; // Preserve the y-order invariant even on this path.
        int count = 0;
        for (i = lo; i < hi; i++) if (Math.abs(a[i].x() - splitX) < best) buffer[lo + count++] = a[i];
        for (i = 0; i < count; i++) {
            // Packing bound: at most the next seven y-neighbours can improve best.
            for (j = i + 1; j < count && j <= i + 7; j++) {
                if (buffer[lo + j].y() - buffer[lo + i].y() >= best) break;
                best = Math.min(best, distance(buffer[lo + i], buffer[lo + j]));
            }
        }
        return best;
    }
    private double distance(Point a, Point b) {
        metrics.distanceEvaluations++;
        return Math.hypot(a.x() - b.x(), a.y() - b.y());
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

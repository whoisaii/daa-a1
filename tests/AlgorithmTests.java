import java.util.Arrays;
import java.util.Random;

/** Reproducible reference-based correctness tests; failures exit with an exception. */
public final class AlgorithmTests {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    private static void sorting(int[] input) {
        int[] expected = input.clone(); Arrays.sort(expected);
        MergeSorter m = new MergeSorter(); int[] a = input.clone(); m.sort(a);
        check(Arrays.equals(a, expected), "MergeSort mismatch");
        QuickSorter q = new QuickSorter(42); a = input.clone(); q.sort(a);
        check(Arrays.equals(a, expected), "QuickSort mismatch");
        int bound = input.length == 0 ? 0 : 1 + (int)(Math.log(input.length) / Math.log(2));
        check(q.metrics.maxDepth <= bound, "QuickSort depth bound");
    }
    private static void closest(Point[] points) {
        Point[] original = points.clone();
        double expected = ClosestPairSolver.bruteForce(points);
        double actual = new ClosestPairSolver().closestDistance(points);
        check(Double.compare(actual, expected) == 0 || Math.abs(actual - expected) <= Math.max(1e-12, expected * 1e-12), "ClosestPair mismatch");
        check(Arrays.equals(original, points), "ClosestPair mutated input");
    }
    private static void rejects(Runnable action, Class<? extends Throwable> type) {
        checks++;
        try { action.run(); } catch (Throwable e) {
            if (type.isInstance(e)) return;
            throw new AssertionError("Wrong exception", e);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
    public static void main(String[] args) {
        long start = System.nanoTime(); Random r = new Random(20260927);
        sorting(new int[0]); sorting(new int[]{7}); sorting(new int[]{2, 1});
        sorting(new int[]{Integer.MAX_VALUE, 0, Integer.MIN_VALUE, Integer.MAX_VALUE});
        for (int n : new int[]{5, 16, 17, 100, 1000, 10000}) {
            int[] a = new int[n]; for (int i = 0; i < n; i++) a[i] = r.nextInt(); sorting(a);
            Arrays.sort(a); sorting(a);
            for (int i = 0; i < n / 2; i++) { int t = a[i]; a[i] = a[n - i - 1]; a[n - i - 1] = t; } sorting(a);
            for (int i = 0; i < n; i++) a[i] = r.nextInt(5); sorting(a);
            Arrays.fill(a, 9); sorting(a);
        }
        for (int t = 0; t < 200; t++) {
            int[] a = r.ints(r.nextInt(2000), -100, 101).toArray(); sorting(a);
        }
        System.out.println("PASS sorting: edge cases, 4 distributions, 200 random arrays; depth bounds");
        DeterministicSelector s = new DeterministicSelector();
        for (int t = 0; t < 500; t++) {
            int n = 1 + r.nextInt(3000), k = r.nextInt(n);
            int[] a = r.ints(n, -30, 31).toArray(), expected = a.clone(); Arrays.sort(expected);
            check(s.select(a.clone(), k) == expected[k], "Random selection");
            check(s.select(a.clone(), 0) == expected[0], "Minimum selection");
            check(s.select(a.clone(), n - 1) == expected[n - 1], "Maximum selection");
        }
        for (int n = 1; n <= 80; n++) {
            int[] a = r.ints(n, -8, 9).toArray(), expected = a.clone(); Arrays.sort(expected);
            for (int k = 0; k < n; k++) check(s.select(a.clone(), k) == expected[k], "All ranks");
            check(s.select(expected.clone(), n / 2) == expected[n / 2], "Sorted selection");
            for (int i = 0; i < n / 2; i++) { int v = expected[i]; expected[i] = expected[n-i-1]; expected[n-i-1] = v; }
            check(s.select(expected.clone(), 0) == Arrays.stream(a).min().orElseThrow(), "Reverse selection");
            Arrays.fill(a, 4); check(s.select(a, n / 2) == 4, "Equal selection");
        }
        check(s.select(new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE, 0}, 1) == 0, "Extreme selection");
        System.out.println("PASS selection: 500 random datasets x 3 ranks; every rank for sizes 1..80");
        closest(new Point[0]); closest(new Point[]{new Point(0, 0)});
        closest(new Point[]{new Point(1, 1), new Point(1, 1)});
        closest(new Point[]{new Point(-1e150, 0), new Point(1e150, 0)});
        for (int n : new int[]{2, 3, 4, 5, 16, 31, 100, 500, 2000}) {
            Point[] a = new Point[n];
            for (int i = 0; i < n; i++) a[i] = new Point(r.nextDouble()*1000, r.nextDouble()*1000); closest(a);
            for (int i = 0; i < n; i++) a[i] = new Point(3, i * 0.5); closest(a);
            for (int i = 0; i < n; i++) a[i] = new Point(i, 7); closest(a);
            for (int i = 0; i < n; i++) a[i] = new Point(r.nextInt(7), r.nextInt(7)); closest(a);
        }
        for (int t = 0; t < 200; t++) {
            Point[] a = new Point[2 + r.nextInt(150)];
            for (int i = 0; i < a.length; i++) a[i] = new Point(r.nextInt(100), r.nextInt(100)); closest(a);
        }
        Point[] large = new Point[100000];
        for (int i = 0; i < large.length; i++) large[i] = new Point(i * 3.0, 0);
        check(new ClosestPairSolver().closestDistance(large) == 3, "Large known-answer closest pair");
        System.out.println("PASS closest pair: brute-force references up to n=2000; 100000-point known answer");
        rejects(() -> s.select(new int[0], 0), IllegalArgumentException.class);
        rejects(() -> s.select(new int[]{1}, -1), IllegalArgumentException.class);
        rejects(() -> s.select(new int[]{1}, 1), IllegalArgumentException.class);
        rejects(() -> s.select(null, 0), NullPointerException.class);
        rejects(() -> new MergeSorter().sort(null), NullPointerException.class);
        rejects(() -> new QuickSorter(1).sort(null), NullPointerException.class);
        rejects(() -> new ClosestPairSolver().closestDistance(null), NullPointerException.class);
        rejects(() -> new ClosestPairSolver().closestDistance(new Point[]{null}), NullPointerException.class);
        rejects(() -> new Point(Double.NaN, 0), IllegalArgumentException.class);
        rejects(() -> new Point(Double.POSITIVE_INFINITY, 0), IllegalArgumentException.class);
        rejects(() -> new Point(1e151, 0), IllegalArgumentException.class);
        MergeSorter m = new MergeSorter(); m.sort(new int[]{3, 2, 1}); m.sort(new int[0]);
        check(m.metrics.maxDepth == 0 && m.metrics.comparisons == 0, "Metrics reset");
        System.out.println("PASS invalid arguments and metric reset");
        System.out.printf("ALL TESTS PASSED: %,d checks (%.3f s)%n", checks, (System.nanoTime()-start)/1e9);
    }
}

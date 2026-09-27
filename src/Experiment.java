import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

/** Seeded repeated experiments. Setup, cloning and reference checks are not timed. */
public final class Experiment {
    private static final long SEED = 20260927L;
    private static final int[] SIZES = {100, 1000, 10000, 100000};
    private static final String[] TYPES = {"random", "sorted", "reverse", "duplicates"};
    private static final int WARMUPS = 5, REPEATS = 7;
    private static volatile double sink;
    private record Sample(long ns, int depth, long operations, long calls) {}

    public static void main(String[] args) throws IOException {
        run();
    }

    public static void run() throws IOException {
        Files.createDirectories(Path.of("results"));
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Path.of("results/results.csv")));
             PrintWriter raw = new PrintWriter(Files.newBufferedWriter(Path.of("results/raw.csv")))) {
            out.println("algorithm,input_type,n,median_time_ns,min_time_ns,max_time_ns,max_depth,median_operations,operation,median_calls,repeats");
            raw.println("algorithm,input_type,n,trial,time_ns,max_depth,operations,operation,calls");
            for (String algorithm : new String[]{"MergeSort", "QuickSort", "DeterministicSelect", "ClosestPair"}) {
                for (String type : TYPES) {
                    for (int n : SIZES) {
                        Random r = new Random(SEED + 31L*n + Arrays.asList(TYPES).indexOf(type));
                        int[] data = integers(n, type, r);
                        Point[] points = points(n, type, r);
                        int[] expected = data.clone(); Arrays.sort(expected);
                        double expectedDistance = n <= 2000 ? ClosestPairSolver.bruteForce(points) : Double.NaN;
                        Sample[] samples = new Sample[REPEATS];
                        String operation = algorithm.equals("ClosestPair") ? "distance_evaluations" : "key_comparisons";
                        for (int trial = -WARMUPS; trial < REPEATS; trial++) {
                            Sample s = measure(algorithm, data, points, expected, expectedDistance, SEED + trial);
                            if (trial >= 0) {
                                samples[trial] = s;
                                raw.printf(Locale.ROOT, "%s,%s,%d,%d,%d,%d,%d,%s,%d%n", algorithm,type,n,trial+1,s.ns,s.depth,s.operations,operation,s.calls);
                            }
                        }
                        long[] times = Arrays.stream(samples).mapToLong(Sample::ns).sorted().toArray();
                        long operations = Arrays.stream(samples).mapToLong(Sample::operations).sorted().toArray()[REPEATS/2];
                        long calls = Arrays.stream(samples).mapToLong(Sample::calls).sorted().toArray()[REPEATS/2];
                        int depth = Arrays.stream(samples).mapToInt(Sample::depth).max().orElse(0);
                        out.printf(Locale.ROOT,"%s,%s,%d,%d,%d,%d,%d,%d,%s,%d,%d%n",algorithm,type,n,times[REPEATS/2],times[0],times[REPEATS-1],depth,operations,operation,calls,REPEATS);
                        System.out.printf(Locale.ROOT,"%-20s %-10s n=%6d  median=%9.3f ms  depth=%2d  ops=%d%n",algorithm,type,n,times[REPEATS/2]/1e6,depth,operations);
                    }
                }
            }
        }
        String env = "Java: " + System.getProperty("java.runtime.version") + "\nVM: " + System.getProperty("java.vm.name")
            + "\nOS: " + System.getProperty("os.name") + " " + System.getProperty("os.version")
            + "\nArchitecture: " + System.getProperty("os.arch") + "\nAvailable processors: " + Runtime.getRuntime().availableProcessors()
            + "\nMax heap bytes: " + Runtime.getRuntime().maxMemory() + "\nSeed: " + SEED
            + "\nWarmups per configuration: " + WARMUPS + "\nMeasured trials: " + REPEATS + "\n";
        Files.writeString(Path.of("results/environment.txt"), env);
        System.out.println("Saved 64 configurations and 448 measured trials to results/.");
    }
    private static Sample measure(String algorithm, int[] data, Point[] points, int[] expected, double expectedDistance, long seed) {
        int[] a = data.clone(); // Excluded from measured duration.
        Metrics m; long start, elapsed;
        switch (algorithm) {
            case "MergeSort" -> {
                MergeSorter solver = new MergeSorter(); start = System.nanoTime(); solver.sort(a); elapsed = System.nanoTime()-start; m=solver.metrics;
                if (!Arrays.equals(a, expected)) throw new AssertionError("MergeSort experiment mismatch"); sink=a[a.length/2];
            }
            case "QuickSort" -> {
                QuickSorter solver = new QuickSorter(seed); start=System.nanoTime(); solver.sort(a); elapsed=System.nanoTime()-start; m=solver.metrics;
                if (!Arrays.equals(a, expected)) throw new AssertionError("QuickSort experiment mismatch"); sink=a[a.length/2];
            }
            case "DeterministicSelect" -> {
                DeterministicSelector solver = new DeterministicSelector(); start=System.nanoTime(); int value=solver.select(a,a.length/2); elapsed=System.nanoTime()-start; m=solver.metrics;
                if (value != expected[a.length/2]) throw new AssertionError("Selection experiment mismatch"); sink=value;
            }
            case "ClosestPair" -> {
                ClosestPairSolver solver = new ClosestPairSolver(); start=System.nanoTime(); double value=solver.closestDistance(points); elapsed=System.nanoTime()-start; m=solver.metrics;
                if (!Double.isNaN(expectedDistance) && Math.abs(value-expectedDistance) > Math.max(1e-12,expectedDistance*1e-12)) throw new AssertionError("ClosestPair experiment mismatch"); sink=value;
            }
            default -> throw new IllegalArgumentException(algorithm);
        }
        return new Sample(elapsed,m.maxDepth,algorithm.equals("ClosestPair")?m.distanceEvaluations:m.comparisons,m.recursiveCalls);
    }
    private static int[] integers(int n, String type, Random r) {
        int[] a = new int[n];
        for (int i=0;i<n;i++) a[i] = type.equals("duplicates") ? r.nextInt(8) : r.nextInt();
        if (type.equals("sorted") || type.equals("reverse")) Arrays.sort(a);
        if (type.equals("reverse")) for (int i=0;i<n/2;i++) { int t=a[i]; a[i]=a[n-i-1]; a[n-i-1]=t; }
        return a;
    }
    private static Point[] points(int n, String type, Random r) {
        Point[] p = new Point[n];
        for (int i=0;i<n;i++) p[i]=type.equals("duplicates") ? new Point(r.nextInt(8),r.nextInt(8)) : new Point(r.nextDouble()*1e6,r.nextDouble()*1e6);
        Comparator<Point> order=Comparator.comparingDouble(Point::x).thenComparingDouble(Point::y);
        if (type.equals("sorted")) Arrays.sort(p,order);
        if (type.equals("reverse")) Arrays.sort(p,order.reversed());
        return p;
    }
}

import java.util.Arrays;
import java.util.Locale;

public final class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("experiment")) { Experiment.run(); return; }
        if (args.length != 0) throw new IllegalArgumentException("Usage: Main [experiment]");
        int[] input = {9, 1, 7, 3, 3, 8, -2};
        System.out.println("Divide-and-Conquer | Assignment 1");
        System.out.println("Input: " + Arrays.toString(input));
        MergeSorter m = new MergeSorter(); int[] a = input.clone(); m.sort(a);
        System.out.println("MergeSort: " + Arrays.toString(a));
        QuickSorter q = new QuickSorter(42); a = input.clone(); q.sort(a);
        System.out.println("QuickSort: " + Arrays.toString(a));
        DeterministicSelector s = new DeterministicSelector();
        System.out.println("Select k=3 (zero-based): " + s.select(input.clone(), 3));
        Point[] points = {new Point(0,0), new Point(5,5), new Point(1,1), new Point(8,2)};
        ClosestPairSolver c = new ClosestPairSolver();
        System.out.printf(Locale.ROOT,"Closest distance: %.6f%n",c.closestDistance(points));
        System.out.printf("Depth: merge=%d, quick=%d, select=%d, closest=%d%n",m.metrics.maxDepth,q.metrics.maxDepth,s.metrics.maxDepth,c.metrics.maxDepth);
        System.out.printf("Comparisons: merge=%d, quick=%d, select=%d; distance evaluations=%d%n",m.metrics.comparisons,q.metrics.comparisons,s.metrics.comparisons,c.metrics.distanceEvaluations);
    }
}

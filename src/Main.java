import java.util.Arrays;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        // Change these numbers to try another example.
        int[] input = {9, 1, 7, 3, 3, 8, -2};
        System.out.println("Input: " + Arrays.toString(input));

        // Each algorithm gets its own copy of the input.
        MergeSorter mergeSorter = new MergeSorter();
        int[] mergeArray = input.clone();
        mergeSorter.sort(mergeArray);
        System.out.println("MergeSort: " + Arrays.toString(mergeArray));

        QuickSorter quickSorter = new QuickSorter(42);
        int[] quickArray = input.clone();
        quickSorter.sort(quickArray);
        System.out.println("QuickSort: " + Arrays.toString(quickArray));

        DeterministicSelector selector = new DeterministicSelector();
        int k = 3; // Index 3 means the fourth smallest number.
        int selected = selector.select(input.clone(), k);
        System.out.println("Select k=" + k + ": " + selected);

        Point[] points = {new Point(0, 0), new Point(5, 5),
                          new Point(1, 1), new Point(8, 2)};
        ClosestPairSolver closestPair = new ClosestPairSolver();
        double distance = closestPair.closestDistance(points);
        System.out.printf(Locale.ROOT, "Closest distance: %.6f%n", distance);
    }
}

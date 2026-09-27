// Counts work done by one algorithm run.
public final class Metrics {
    public long comparisons, swaps, recursiveCalls, distanceEvaluations;
    public int maxDepth;

    public void reset() {
        comparisons = 0;
        swaps = 0;
        recursiveCalls = 0;
        distanceEvaluations = 0;
        maxDepth = 0;
    }

    public void enter(int depth) {
        recursiveCalls++;
        maxDepth = Math.max(maxDepth, depth);
    }

    public int compare(int a, int b) {
        comparisons++;
        return Integer.compare(a, b);
    }

    public void swap(int[] array, int i, int j) {
        if (i != j) {
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
            swaps++;
        }
    }
}

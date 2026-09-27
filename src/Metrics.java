/** Per-run counters. Depth counts active algorithm frames; the root is depth 1. */
public final class Metrics {
    public long comparisons, swaps, recursiveCalls, distanceEvaluations;
    public int maxDepth;
    public void reset() { comparisons = swaps = recursiveCalls = distanceEvaluations = 0; maxDepth = 0; }
    public void enter(int depth) { recursiveCalls++; maxDepth = Math.max(maxDepth, depth); }
    public int compare(int a, int b) { comparisons++; return Integer.compare(a, b); }
    public void swap(int[] a, int i, int j) {
        if (i != j) { int t = a[i]; a[i] = a[j]; a[j] = t; swaps++; }
    }
}

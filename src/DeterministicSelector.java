import java.util.Objects;

/** Zero-based selection. Mutates its input, using groups of five and no arrays. */
public final class DeterministicSelector {
    public final Metrics metrics = new Metrics();
    public int select(int[] a, int k) {
        Objects.requireNonNull(a, "array");
        metrics.reset();
        if (k < 0 || k >= a.length) throw new IllegalArgumentException("k outside array");
        return select(a, 0, a.length, k, 1);
    }
    private int select(int[] a, int lo, int hi, int k, int depth) {
        metrics.enter(depth);
        if (hi - lo <= 5) { insertion(a, lo, hi); return a[k]; }
        int medians = 0;
        for (int start = lo; start < hi; start += 5) {
            int end = Math.min(start + 5, hi);
            insertion(a, start, end);
            metrics.swap(a, lo + medians++, start + (end - start) / 2);
        }
        int pivot = select(a, lo, lo + medians, lo + medians / 2, depth + 1);
        int lt = lo, i = lo, gt = hi;
        while (i < gt) {
            int c = metrics.compare(a[i], pivot);
            if (c < 0) metrics.swap(a, lt++, i++);
            else if (c > 0) metrics.swap(a, i, --gt);
            else i++;
        }
        if (k < lt) return select(a, lo, lt, k, depth + 1);
        if (k >= gt) return select(a, gt, hi, k, depth + 1);
        return pivot;
    }
    private void insertion(int[] a, int lo, int hi) {
        for (int i = lo + 1; i < hi; i++) {
            int value = a[i], j = i;
            while (j > lo && metrics.compare(a[j - 1], value) > 0) {
                a[j] = a[j - 1]; j--;
            }
            a[j] = value;
        }
    }
}

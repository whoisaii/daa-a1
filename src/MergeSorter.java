import java.util.Objects;

/** Stable merge sort with a single reusable buffer and insertion-sort cutoff. */
public final class MergeSorter {
    private static final int CUTOFF = 16;
    public final Metrics metrics = new Metrics();

    public void sort(int[] a) {
        Objects.requireNonNull(a, "array");
        metrics.reset();
        if (a.length > 0) sort(a, new int[a.length], 0, a.length, 1);
    }
    private void sort(int[] a, int[] buffer, int lo, int hi, int depth) {
        metrics.enter(depth);
        if (hi - lo <= CUTOFF) {
            for (int i = lo + 1; i < hi; i++) {
                int value = a[i], j = i;
                while (j > lo && metrics.compare(a[j - 1], value) > 0) {
                    a[j] = a[j - 1]; j--;
                }
                a[j] = value;
            }
            return;
        }
        int mid = lo + (hi - lo) / 2;
        sort(a, buffer, lo, mid, depth + 1);
        sort(a, buffer, mid, hi, depth + 1);
        int i = lo, j = mid, k = lo;
        while (i < mid && j < hi)
            buffer[k++] = metrics.compare(a[i], a[j]) <= 0 ? a[i++] : a[j++];
        while (i < mid) buffer[k++] = a[i++];
        while (j < hi) buffer[k++] = a[j++];
        System.arraycopy(buffer, lo, a, lo, hi - lo);
    }
}

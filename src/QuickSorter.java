import java.util.Objects;
import java.util.Random;

/** Randomized three-way partitioning; recurse on smaller side, loop on larger. */
public final class QuickSorter {
    public final Metrics metrics = new Metrics();
    private final Random random;
    public QuickSorter(long seed) { random = new Random(seed); }
    public void sort(int[] a) {
        Objects.requireNonNull(a, "array");
        metrics.reset();
        if (a.length > 0) sort(a, 0, a.length, 1);
    }
    private void sort(int[] a, int lo, int hi, int depth) {
        metrics.enter(depth);
        while (hi - lo > 1) {
            int pivot = a[lo + random.nextInt(hi - lo)];
            int lt = lo, i = lo, gt = hi;
            while (i < gt) {
                int c = metrics.compare(a[i], pivot);
                if (c < 0) metrics.swap(a, lt++, i++);
                else if (c > 0) metrics.swap(a, i, --gt);
                else i++;
            }
            if (lt - lo < hi - gt) {
                if (lt - lo > 1) sort(a, lo, lt, depth + 1);
                lo = gt;
            } else {
                if (hi - gt > 1) sort(a, gt, hi, depth + 1);
                hi = lt;
            }
        }
    }
}

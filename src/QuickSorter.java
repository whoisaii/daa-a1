import java.util.Objects;
import java.util.Random;

// Use a random pivot. Recurse on the smaller side to save stack space.
public final class QuickSorter {
    public final Metrics metrics = new Metrics();

    private final Random random;
    public QuickSorter(long seed) { random = new Random(seed); }

    public void sort(int[] array) {
        Objects.requireNonNull(array, "array");
        metrics.reset();
        if (array.length > 0) sort(array, 0, array.length, 1);
    }

    private void sort(int[] array, int left, int right, int depth) {
        metrics.enter(depth);
        while (right - left > 1) {
            int pivot = array[left + random.nextInt(right - left)];
            // Values before smallerEnd are smaller than the pivot.
            // Values from largerStart onwards are larger than the pivot.
            int smallerEnd = left;
            int i = left;
            int largerStart = right;
            while (i < largerStart) {
                int comparison = metrics.compare(array[i], pivot);
                if (comparison < 0) {
                    metrics.swap(array, smallerEnd, i);
                    smallerEnd++;
                    i++;
                } else if (comparison > 0) {
                    largerStart--;
                    metrics.swap(array, i, largerStart);
                } else {
                    i++;
                }
            }
            if (smallerEnd - left < right - largerStart) {
                if (smallerEnd - left > 1) sort(array, left, smallerEnd, depth + 1);
                left = largerStart;
            } else {
                if (right - largerStart > 1) sort(array, largerStart, right, depth + 1);
                right = smallerEnd;
            }
        }
    }
}

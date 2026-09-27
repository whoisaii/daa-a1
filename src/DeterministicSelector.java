import java.util.Objects;

// Find the value at index k in sorted order, without sorting the whole array.
public final class DeterministicSelector {
    public final Metrics metrics = new Metrics();

    public int select(int[] array, int k) {
        Objects.requireNonNull(array, "array");
        metrics.reset();
        if (k < 0 || k >= array.length) throw new IllegalArgumentException("k outside array");
        return select(array, 0, array.length, k, 1);
    }

    private int select(int[] array, int left, int right, int k, int depth) {
        metrics.enter(depth);
        if (right - left <= 5) {
            insertion(array, left, right);
            return array[k];
        }
        int medians = 0;
        for (int start = left; start < right; start += 5) {
            int end = Math.min(start + 5, right);
            insertion(array, start, end);
            int medianIndex = start + (end - start) / 2;
            metrics.swap(array, left + medians, medianIndex);
            medians++;
        }
        int pivot = select(array, left, left + medians, left + medians / 2, depth + 1);
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
        if (k < smallerEnd) return select(array, left, smallerEnd, k, depth + 1);
        if (k >= largerStart) return select(array, largerStart, right, k, depth + 1);
        return pivot;
    }

    private void insertion(int[] array, int left, int right) {
        for (int i = left + 1; i < right; i++) {
            int value = array[i];
            int j = i;
            while (j > left && metrics.compare(array[j - 1], value) > 0) {
                array[j] = array[j - 1];
                j--;
            }
            array[j] = value;
        }
    }
}

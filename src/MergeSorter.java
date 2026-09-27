import java.util.Objects;

// Split, sort both halves, then merge them.
public final class MergeSorter {

    private static final int CUTOFF = 16;
    public final Metrics metrics = new Metrics();


    public void sort(int[] array) {
        Objects.requireNonNull(array, "array");
        metrics.reset();
        if (array.length > 0) sort(array, new int[array.length], 0, array.length, 1);
    }

    private void sort(int[] array, int[] buffer, int left, int right, int depth) {
        metrics.enter(depth);
        // Insertion sort is enough for a small part.
        if (right - left <= CUTOFF) {
            for (int i = left + 1; i < right; i++) {
                int value = array[i];
                int j = i;
                while (j > left && metrics.compare(array[j - 1], value) > 0) {
                    array[j] = array[j - 1];
                    j--;
                }
                array[j] = value;
            }
            return;
        }
        // Sort both halves. The right boundary is not included.
        int mid = left + (right - left) / 2;
        sort(array, buffer, left, mid, depth + 1);
        sort(array, buffer, mid, right, depth + 1);
        // Merge the two sorted halves.
        int i = left;
        int j = mid;
        int k = left;
        while (i < mid && j < right) {
            if (metrics.compare(array[i], array[j]) <= 0) {
                buffer[k] = array[i];
                i++;
            } else {
                buffer[k] = array[j];
                j++;
            }
            k++;
        }
        while (i < mid) buffer[k++] = array[i++];
        while (j < right) buffer[k++] = array[j++];
        System.arraycopy(buffer, left, array, left, right - left);
    }
}

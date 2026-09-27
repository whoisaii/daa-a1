/** Finite coordinates bounded to keep subtraction and distance representable. */
public record Point(double x, double y) {
    public Point {
        if (!Double.isFinite(x) || !Double.isFinite(y)
                || Math.abs(x) > 1e150 || Math.abs(y) > 1e150)
            throw new IllegalArgumentException("coordinates must be finite and within +/-1e150");
    }
}

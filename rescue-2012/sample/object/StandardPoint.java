package sample.object;

public class StandardPoint<T extends Number> {
	private T x;
	private T y;

	public StandardPoint(T x, T y) {
		this.x = x;
		this.y = y;
	}

	public T getX() {
		return x;
	}

	public void setX(T x) {
		this.x = x;
	}

	public T getY() {
		return y;
	}

	public void setY(T y) {
		this.y = y;
	}

	public double distanceTo(StandardPoint<T> point) {
		double dx = Math.abs(getX().doubleValue() - point.getX().doubleValue());
		double dy = Math.abs(getY().doubleValue() - point.getY().doubleValue());
		double d = Math.sqrt(dx * dx + dy * dy);
		return d;
	}
}

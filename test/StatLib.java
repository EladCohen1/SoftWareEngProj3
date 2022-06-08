package test;

public class StatLib {

	// simple average
	public static float avg(float[] x) {
		int counter = 0;
		float sum = 0;
		for (float f : x) {
			sum = sum + f;
			counter++;
		}
		return sum / counter;
	}

	// returns the variance of X and Y
	public static float var(float[] x) {
		float[] xSquared = new float[x.length];
		for (int i = 0; i < xSquared.length; i++) {
			xSquared[i] = x[i] * x[i];
		}
		return avg(xSquared) - (avg(x) * avg(x));
	}

	// returns the covariance of X and Y
	public static float cov(float[] x, float[] y) {
		if (x.length != y.length)
			return 0;
		float[] XYArr = new float[x.length];
		for (int i = 0; i < XYArr.length; i++) {
			XYArr[i] = x[i] * y[i];
		}
		return avg(XYArr) - (avg(x) * avg(y));
	}

	// returns the Pearson correlation coefficient of X and Y
	public static float pearson(float[] x, float[] y) {
		return (float) (cov(x, y) / (Math.sqrt(var(x)) * Math.sqrt(var(y))));
	}

	// performs a linear regression and returns the line equation
	public static Line linear_reg(Point[] points) {
		float a, b, avgX = 0, avgY = 0;
		float[] x = new float[points.length];
		float[] y = new float[points.length];
		for (int i = 0; i < points.length; i++) {
			// populating float[] x and float[] y
			x[i] = points[i].x;
			y[i] = points[i].y;
			// summing the x's and y's to calculate averages
			avgX = avgX + points[i].x;
			avgY = avgY + points[i].y;
		}
		avgX = avgX / points.length;
		avgY = avgY / points.length;
		a = cov(x, y) / var(x);
		b = avgY - (a * avgX);
		return new Line(a, b);
	}

	// returns the deviation between point p and the line equation of the points
	public static float dev(Point p, Point[] points) {
		return dev(p, linear_reg(points));
	}

	// returns the deviation between point p and the line
	public static float dev(Point p, Line l) {
		float result;
		result = p.y - (l.a * p.x + l.b);
		if (result < 0)
			return -1 * result;
		return result;
	}

}

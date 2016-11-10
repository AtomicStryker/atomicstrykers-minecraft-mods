package atomicstryker.minions.common.util;

public class Vector2m extends Vector2 {

	public Vector2m() {
	}

	public Vector2m(Double x, Double y) {
		super(x, y);
	}

	public Vector2m(float x, float y) {
		super(x, y);
	}

	public Vector2m(Vector2 original) {
		super(original);
	}


	/**
	 * Sets the X coordinate
	 *
	 * @param x The x coordinate
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * Sets the Y coordinate
	 *
	 * @param y The Y coordinate
	 */
	public void setY(float y) {
		this.z = y;
	}

	@Override
	public Vector2 add(Vector2 that) {
		x += that.x;
		z += that.z;
		return this;
	}

	@Override
	public Vector2 subtract(Vector2 that) {
		x -= that.x;
		z -= that.z;
		return this;
	}

	@Override
	public Vector2 scale(float scale) {
		x *= scale;
		z *= scale;
		return this;
	}

	public Vector2 cross(Vector2 that) {
		float tmp = z;
		z = -x;
		x = tmp;
		return this;
	}
	
	/**
	 * Rounds the X and Y values of this Vector2 up to 
	 * the nearest integer value. 
	 * 
	 * @return 
	 */
	public Vector2 ceil() {
		x = (float) Math.ceil(x);
		z = (float) Math.ceil(z);
		return this;
	}
	
	/**
	 * Rounds the X and Y values of this Vector2 down to 
	 * the nearest integer value. 
	 * 
	 * @return 
	 */
	public Vector2 floor() {
		x = (float) Math.floor(x);
		z = (float) Math.floor(z);
		return this;
	}
	
	/**
	 * Rounds the X and Y values of this Vector2 to 
	 * the nearest integer value. 
	 * 
	 * @return 
	 */
	public Vector2 round() {
		x = Math.round(x);
		z = Math.round(z);
		return this;
	}
	
	/**
	 * Sets the X and Y values of this Vector2 to their
	 * absolute value.
	 * 
	 * @return 
	 */
	public Vector2 abs() {
		x = Math.abs(x);
		z = Math.abs(z);
		return this;
	}

	@Override
	public Vector2 normalize() {
		float length = this.length();
		x *= 1 / length;
		z *= 1 / length;
		return this;
	}
}

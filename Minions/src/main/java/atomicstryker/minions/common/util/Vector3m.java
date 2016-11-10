package atomicstryker.minions.common.util;

public class Vector3m extends Vector3 {

	public Vector3m(float x, float y, float z) {
		super(x, y, z);
	}

	public Vector3m(Double x, Double y, Double z) {
		super(x, y, z);
	}

	public Vector3m(Vector3 vector) {
		super(vector);
	}

	public Vector3m() {
	}

	public void setZ(float z) {
		this.z = z;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setX(float x) {
		this.x = x;
	}

	/**
	 * Adds two vectors
	 *
	 * @param that
	 * @return
	 */

	public Vector3 add(Vector3 that) {
		x += that.x;
		y += that.y;
		z += that.z;
		return this;
	}

	/**
	 * Subtracts two vectors
	 *
	 * @param that
	 * @return
	 */

	public Vector3 subtract(Vector3 that) {
		x -= that.x;
		y -= that.y;
		z -= that.z;
		return this;
	}

	/**
	 * Scales by the scalar value
	 *
	 * @param scale
	 * @return
	 */

	public Vector3 scale(float scale) {
		x *= scale;
		y *= scale;
		z *= scale;
		return this;
	}

	/**
	 * Takes the cross product of two vectors
	 *
	 * @param that
	 * @return
	 */

	public Vector3 cross(Vector3 that) {
		x = getY() * that.getZ() - getZ() * that.getY();
		y = getZ() * that.getX() - getX() * that.getZ();
		z = getX() * that.getY() - getY() * that.getX();

		return this;
	}
	
	/**
	 * Rounds the X, Y, and Z values of this Vector3 up to 
	 * the nearest integer value. 
	 * 
	 * @return 
	 */
	public Vector3 ceil() {
		x = (float) Math.ceil(x);
		y = (float) Math.ceil(y);
		z = (float) Math.ceil(z);
		return this;
	}
	
	/**
	 * Rounds the X, Y, and Z values of this Vector3 down to 
	 * the nearest integer value. 
	 * 
	 * @return 
	 */
	public Vector3 floor() {
		x = (float) Math.floor(x);
		y = (float) Math.floor(y);
		z = (float) Math.floor(z);
		return this;
	}
	
	/**
	 * Rounds the X, Y, and Z values of this Vector3 to 
	 * the nearest integer value. 
	 * 
	 * @return 
	 */
	public Vector3 round() {
		x = Math.round(x);
		y = Math.round(y);
		z = Math.round(z);
		return this;
	}
	
	/**
	 * Sets the X, Y, and Z values of this Vector3 to their
	 * absolute value.
	 * 
	 * @return 
	 */
	public Vector3 abs() {
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);
		return this;
	}

	/**
	 * returns the vector with a length of 1
	 *
	 * @return
	 */

	public Vector3 normalize() {
		float length = this.length();
		x *= 1 / length;
		y *= 1 / length;
		z *= 1 / length;
		return this;
	}
}

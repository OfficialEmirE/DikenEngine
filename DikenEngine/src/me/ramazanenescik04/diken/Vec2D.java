package me.ramazanenescik04.diken;

public record Vec2D(long x, long y) {
	public Vec2D add(Vec2D other) {
		return new Vec2D(this.x + other.x, this.y + other.y);
	}
	
	public Vec2D subtract(Vec2D other) {
		return new Vec2D(this.x - other.x, this.y - other.y);
	}
	
	public Vec2D multiply(long scalar) {
		return new Vec2D(this.x * scalar, this.y * scalar);
	}
	
	public Vec2D multiply(float scalar) {
		long newX = Math.round(this.x * scalar);
		long newY = Math.round(this.y * scalar);
		return new Vec2D(newX, newY);
	}
	
	public Vec2D divide(long scalar) {
		return new Vec2D(this.x / scalar, this.y / scalar);
	}
	
	public double distanceTo(Vec2D other) {
		long deltaX = this.x - other.x;
		long deltaY = this.y - other.y;
		return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	}
	
	public Vec2D negate() {
		return new Vec2D(-this.x, -this.y);
	}
	
	public String toString() {
		return "Vec2D(" + x + ", " + y + ")";
	}
	
	public Vec2D clone() {
		return new Vec2D(this.x, this.y);
	}

}

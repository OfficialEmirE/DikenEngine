package me.ramazanenescik04.diken.game.entity;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.Vec2D;
import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.resource.Bitmap;

public class Player extends Entity {
	public boolean followCamera = true;
	public boolean onEdge = false;
	public boolean canMove = true;
	public float speed = 4.0f;
	
	public Player(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.name = "DefaultPlayer";
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = super.render();
		bitmap.clear(0xffff00ff);
		bitmap.drawText("Override render() pls :(", 2, 2, false);
		return bitmap;
	}

	@Override
	public void tick(World world, DikenEngine engine) {
		int centerX = engine.getWidth() / 2 - this.width / 2;
		int centerY = engine.getHeight() / 2 - this.height / 2;
		
		if (followCamera) {
			if (onEdge) {
				if (this.x < world.camera.x() + centerX) {
					world.camera.add(new Vec2D(this.x - world.camera.x() - centerX, 0).multiply(0.1f));
				} else if (this.x + this.width > world.camera.x() + centerX + engine.getWidth()) {
					world.camera.add(new Vec2D(this.x + this.width - centerX - engine.getWidth() - world.camera.x(), 0).multiply(0.1f));
				}
				
				if (this.y < world.camera.y() + centerY) {
					world.camera.add(new Vec2D(0, this.y - world.camera.y() - centerY).multiply(0.1f));
				} else if (this.y + this.height > world.camera.y() + centerY + engine.getHeight()) {
					world.camera.add(new Vec2D(0, this.y + this.height - centerY - engine.getHeight() - world.camera.y()).multiply(0.1f));
				}
			} else {
				world.camera.add(new Vec2D(this.x - world.camera.x() - centerX, this.y - world.camera.y() - centerY).multiply(0.1f));
			}
		}
	}
	
	public Animation getIdleAnimation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public Animation getWalkAnimation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public void setIdleAnimation(Animation animation) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public void setWalkAnimation(Animation animation) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isFollowCamera() {
		return followCamera;
	}

	public void setFollowCamera(boolean followCamera) {
		this.followCamera = followCamera;
	}

	public boolean isOnEdge() {
		return onEdge;
	}

	public void setOnEdge(boolean onEdge) {
		this.onEdge = onEdge;
	}

	public boolean isCanMove() {
		return canMove;
	}

	public void setCanMove(boolean canMove) {
		this.canMove = canMove;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}
}

package me.ramazanenescik04.diken.game.entity;

import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.SettingCategory.SettingCategoryHelper;
import me.ramazanenescik04.diken.game.world.*;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

@Deprecated(since = "2.2.0", forRemoval = true)
public class SoloPlayer extends Player {
	private static final long serialVersionUID = -8240969167111897631L;
	
	private transient Animation leftWalkAnim, rightWalkAnim, idleAnim;
	public transient boolean isMoving = false;
	
	public SoloPlayer(int x, int y) {
		super(x, y);
		
		this.leftWalkAnim = (Animation)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "leftWalkAnim"));
	    this.rightWalkAnim = (Animation)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "rightWalkAnim"));
	    this.idleAnim = (Animation)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "idleAnim"));
	    this.idleAnim.setFPS(2);
	}
	
	@Override
	protected void playHandAnimation(Bitmap bitmap) {
		Animation currentAnim = (isMoving) ? this.getWalkAnimation() : this.getIdleAnimation();
	    if (currentAnim != null) {
	      Bitmap handFrame = currentAnim.getCurrentFrame();
	      bitmap.draw(handFrame, 0, 0);
	    }
	}

	@Override
	public void update(World world, DikenEngine engine) {
		if (this.getSelectedTool() == null) {
			if (isMoving) {
				playWalkAnimation();
				this.idleAnim.setCurrentFrame(0);
			} else {
				resetWalkAnimation();
				this.idleAnim.update(System.currentTimeMillis());
			}
		} else {
			resetWalkAnimation();
			this.idleAnim.setCurrentFrame(0);
		}
		super.update(world, engine);
	}

	@Override
	public Animation getIdleAnimation() {
		return this.idleAnim;
	}

	@Override
	public Animation getWalkAnimation() {
		return (viewType == 0) ? this.leftWalkAnim : this.rightWalkAnim;
	}
	
	public void playWalkAnimation() {
	    if (this.viewType == 0) {
	      this.leftWalkAnim.update(System.currentTimeMillis());
	    } else {
	      this.rightWalkAnim.update(System.currentTimeMillis());
	    } 
	}
	  
	public void resetWalkAnimation() {
	    this.leftWalkAnim.setCurrentFrame(0);
	    this.rightWalkAnim.setCurrentFrame(0);
	}
	
	@Override
	protected void reloadNode() {
		super.reloadNode();
		
		this.isMoving = false;
		this.leftWalkAnim = (Animation)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "leftWalkAnim"));
	    this.rightWalkAnim = (Animation)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "rightWalkAnim"));
	    this.idleAnim = (Animation)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "idleAnim"));
	    this.idleAnim.setFPS(2);
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("thePlayer", "Player (WTF HOW BRO)", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(2, 1));
		
		var settingCategory = SettingCategoryHelper.getOrCreateCategory(key, () -> SettingCategory
				.createSettingCategory(key));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}

package me.ramazanenescik04.diken.game;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

public class GameObject extends Hitbox {
	public String name; // Object name, used for identification
	public boolean isVisible = true; // Object visibility status
	public Hitbox aabbHitbox; // collision hitbox

	public GameObject(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.aabbHitbox = new Hitbox(x, y, width, height);
	}
	
	public GameObject(int x, int y) {
		super(x, y, 16, 16);
		this.aabbHitbox = new Hitbox(x, y, 16, 16);
	}
	
	public Bitmap render() {
		return new Bitmap(this.width, this.height);
	}
	
	public void tick(World world, DikenEngine engine) {
	}
	
	public void objectCollided(World world, DikenEngine engine, GameObject other) {
		// 1. Merkez noktalarını hesapla
	    float myCenterX = this.getX() + (this.getWidth() / 2f);
	    float myCenterY = this.getY() + (this.getHeight() / 2f);

	    float otherCenterX = other.getX() + (other.getWidth() / 2f);
	    float otherCenterY = other.getY() + (other.getHeight() / 2f);

	    // 2. Merkezler arasındaki mesafeyi bul (Vektör)
	    float dx = myCenterX - otherCenterX;
	    float dy = myCenterY - otherCenterY;

	    // 3. İki nesnenin yarı genişlik ve yarı yüksekliklerinin toplamını bul
	    float combinedHalfWidth = (this.getWidth() / 2f) + (other.getWidth() / 2f);
	    float combinedHalfHeight = (this.getHeight() / 2f) + (other.getHeight() / 2f);

	    // 4. Çakışma miktarını (Overlap) hesapla
	    // Math.abs(dx) merkezler arası uzaklıktır.
	    float overlapX = combinedHalfWidth - Math.abs(dx);
	    float overlapY = combinedHalfHeight - Math.abs(dy);

	    // Eğer bir çakışma varsa (ki bu metod çağrıldığına göre var):
	    if (overlapX > 0 && overlapY > 0) {
	        // Hangi eksendeki çakışma daha küçükse, o yönde dışarı it (En kolay çıkış yolu)nesne.setX(Math.round(x));
	        if (overlapX < overlapY) {
	            // X ekseninde düzeltme yap
	            if (dx > 0) {
	                // Oyuncu sağda, sağa it
	                this.setX((int)(this.getX() + overlapX));
	            } else {
	                // Oyuncu solda, sola it
	                this.setX((int)(this.getX() - overlapX));
	            }
	            
	            // İsteğe bağlı: X hızını sıfırla (Duvara yapışıp kalmaması için)
	            // this.velX = 0; 
	        } else {
	            // Y ekseninde düzeltme yap
	            if (dy > 0) {
	                // Oyuncu aşağıda, aşağı it
	                this.setY((int)(this.getY() + overlapY));
	            } else {
	                // Oyuncu yukarıda, yukarı it
	                this.setY((int)(this.getY() - overlapY));
	            }

	            // İsteğe bağlı: Y hızını sıfırla (Örn: Zıplarken tavana vurursa düşmesi için)
	            // this.velY = 0;
	        }
	    }
	}
	
	// API START
	
	public GameObject setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public GameObject setVisible(boolean visible) {
		this.isVisible = visible;
		return this;
	}
	
	public boolean isVisible() {
		return isVisible;
	}
	
	public Hitbox getAABBHitbox() {
		return aabbHitbox;
	}
	
	public void setAABBHitbox(Hitbox hitbox) {
		this.aabbHitbox = hitbox;
	}
	
	// API END
}

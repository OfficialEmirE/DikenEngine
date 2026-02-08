package me.ramazanenescik04.diken.input;

/**
 * Bu sınıf Key ve Mouse Eventleri almak için var.
 */
public interface IInputListener {
	/**
	 * 
	 * @param inputMode hangi tür basıldığını söyler
	 * @param key basılan tuş değeri
	 * @param character basıllan tuşun karakteri
	 */
	void keyHandled(int inputMode, int key, char character);
	
	/**
	 * 
	 * @param inputMode hangi tür basıldığını söyler
	 * @param x farenin x kordinatı
	 * @param y farenin y kordinatı
	 * @param clicked fareye basılıp basılmadığını söyler. varsayılan: -1
	 */
	void mouseHandled(int inputMode, int x, int y, int clicked);
}

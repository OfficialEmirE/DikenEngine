# DikenEngine

2D oyun motoru + Studio editör.

---

## Şimdi Ne Yapılabilir? (Optimizasyon & Gelecek)

Buraya kadar geldik, motor çalışıyor, Studio'da script düzenleyici var, tab'ler çalışıyor, Explorer'da isim değiştirince tab başlığı da değişiyor. Peki sırada ne var? Aklımda şunlar var, teker teker anlatayım.

---

### 1. Render Pipeline

Şu anda her şey CPU'da çiziliyor. `Bitmap` sınıfı pixel pixel `fill()` ve `drawLine()` ile çalışıyor. Bu belli bir noktaya kadar gider, ama 200+ obje, partikül, ışık gölge derken işin içinden çıkılamaz hale gelir. Çözüm şu:

- **Batch rendering**: Aynı texture'ı kullanan her şeyi tek `draw()` çağrısında birleştirmek. Mesela 10 tane kırmızı kare varsa, 10 kere `fill()` yapmak yerine tek seferde OpenGL'e tek buffer olarak yollamak.

- **Tile-based rendering**: Kamera dışındaki tile'ları çizmemek. Şu anda `getAllNodes()` dünyadaki her şeyi dönüyor, sadece ekranda görünenleri filtrelemek için `shouldRenderSelf()` var ama bu da tek tek bakıyor. Daha akıllı bir spatial hash (ızgara) tutup "kameranın şu an gördüğü 9 hücrede hangi objeler var" diye sormak lazım.

- **FrameBitmapPool**: Zaten var, iyi. Ama pool'un boyutu sabit değil, `max(bitmap count)` diye bir şey yok. Bir anda çok sayıda bitmap oluşursa heap patlar. Pool'a "en fazla 50 tane tut, fazlasını GC'ye bırak" gibi bir limit koysan iyi olur.

- **Layer sorting**: Şu anda `draw()` içinde `sortedChildren.sort(Comparator.comparingInt(Node::getZIndex))` var. Bu her frame her node için liste kopyalayıp sort etmek demek. Bunu bi kere yapıp cache'lemek lazım. ZIndex değişmediği sürece tekrar sort etmeye gerek yok.

- **Dirty flag sistemi**: Sadece değişen şeyi yeniden çizmek. "Node taşındı mı? O zaman sadece onun eski ve yeni bölgesini tekrar çiz." Şu an her frame her şeyi baştan çiziyor.

---

### 2. Fizik (Collision Detection)

`resolveCollision()` metodu var, ama her frame bütün Instance'ların AABB'lerini teker teker birbiriyle karşılaştırıyor. Bu O(n²). 10 obje için sorun değil, 1000 obje için değil.

- **Spatial hash / grid**: Dünyayı 64x64'lük hücrelere böl. Her Instance'ı hangi hücrelerdeyse o hücrenin listesine koy. Çarpışma testi yaparken sadece aynı hücredeki (ve komşu hücrelerdeki) Instance'lara bak. O(n²) değil O(n * average density per cell) olur.

- **Continuous Collision**: Hızlı giden küçük objeler (mermi gibi) tek frame'de bir Instance'ın içinden geçip gidebilir. Buna "tunneling" denir. Bunu çözmek için "geçen frame'de neredeydi, bu frame'de nerede" diye bir çizgi çizip o çizgi boyunca çarpışma testi yapmak lazım.

- **Physics layers**: Her Instance'a bir layer/bitmask ver. "Player layer 1'de, duvarlar layer 2'de, mermiler layer 3'te. Player sadece layer 2 ile çarpışsın." Gereksiz çarpışma testlerini direkt atla.

---

### 3. Script Sistemi (Threading)

Şu anda Script'ler virtual thread kullanıyor (`Thread.ofVirtual()`). Bu iyi, çünkü her script kendi thread'inde çalışıyor ve bir script crash yerse diğerini götürmüyor. Ama birkaç sıkıntı var:

- **Deadlock riski**: İki script birbirini bekleyecek şekilde yazılırsa (mesela A script'i B'nin event'ini tetikliyor, B de A'nınkini), ikisi de kilitlenir. `LuaBridge` üzerinden Java tarafına geçerken bir lock mekanizması düşünmek lazım. Belki her script'in "maksimum 100ms çalış, sonra durdur" gibi bir timeout'u olmalı.

- **Lua state paylaşımı**: Her script'in kendi `Globals`'ı var. Bu güvenli ama hafıza kullanımı açısından kötü. 50 script = 50 tane Lua ortamı. Bunu paylaşmak riskli çünkü bir script diğerinin değişkenine bilerek/bilmeyerek karışabilir. Belki "aynı kaynaktan gelen script'ler aynı globals'ı paylaşsın" gibi bir optimizasyon yapılabilir.

- **Script durdurma**: `stopRequested` var ve DebugLib içinde her 512 instruction'da bir kontrol ediliyor. Bu iyi. Ama sonsuz loop'a giren bir script (while true do end) sadece interrupt ile durdurulabilir, onun dışında durmaz. Belki instruction sayacına hard limit koymak lazım (10 milyon instruction = otomatik durdur).

---

### 4. Studio Performance

- **Explorer rebuild**: Her child ekleme/çıkarma `rebuildExplorer()` çağırıyor. Bu tüm ağacı baştan kurmak demek. Büyük world'lerde (10000+ node) bu delay yaratır. Bunun yerine sadece değişen dalı güncellemek lazım (şu anda `model.nodeChanged()` ile yapılmaya çalışılıyor ama hala çok yerde `rebuildExplorer()` çağrılıyor).

- **PropertiesPanel inspect**: Bir node seçince tüm ayarları baştan oluşturuyor. 30+ setting'i olan bir node için bu gözle görülür bir gecikme. Sadece değişen setting'in UI'ını güncelleyen bir mekanizma lazım.

- **Lua autocomplete**: `LuaCompleter.complete()` her Ctrl+Space'de bütün enum'ları ve class'ları reflection'la tarıyor. Bunu bir kere yapıp cache'lemek lazım. Java reflection yavaştır, her seferinde tüm method'ları dönmek akıllıca değil.

---

### 5. Hafıza Yönetimi

- **Node.getName()**: Eskiden `new String(name)` dönüyordu. Bunu düzelttim, direkt `name` dönüyor artık. Ama bu tarz küçük allocasyonlar birikince GC'yi tetikler. Özellikle `getAllNodes()` gibi çağrılar her seferinde yeni liste oluşturuyor. Mümkünse liste cache'lenmeli.

- **Event sistemi**: `OnAddDescendant`, `OnPropertyChanged` gibi event'ler her değişiklikte tüm parent zincirini tırmanıyor. Bu da her node eklemede O(depth) işlem demek. Sık sık toplu node ekleme yapılıyorsa (import, paste vb.) bu birikir.

- **Bitmap pool**: `FrameBitmapPool`'da `concurrentQueue.clear()` var. Ama clear yapınca içerdeki bitmapler GC'ye gidiyor, ardından yenileri alınıyor. Sürekli allocate/deallocate yapmak yerine pool boyutunu koruyup sadece "şu an kullanılmıyor" olanları döndürmek daha iyi.

---

### 6. Input Sistemi

- Şu anda `InputHandler` her frame klavye/fare durumunu poll ediyor. Daha iyisi event-driven: "fare hareket etti" dediğinde ancak listener'ları uyandırmak. Poll her frame CPU harcar, özellikle input olmadığında boşuna döner.

- Joystick/gamepad desteği: JInput zaten lib'lerde var ama kullanılmıyor. `InputHandler` sadece klavye/fare ile çalışıyor.

---

### 7. Build & Hot Reload

- **Script hot reload**: Şu anda script'in kaynağını değiştirince motoru yeniden başlatmak gerekiyor. `initialize()` metodunu tekrar çağırmak yeterli olabilir aslında (mevcut state'i sıfırlamak kaydıyla). Ama `stop()` + `initialize()` yapınca eski event bağlantıları kopuyor mu, test etmek lazım.

- **Incremental build**: run.bat her seferinde tüm source'ları derliyor. Büyük projede bu 10+ saniye sürer. Sadece değişen dosyayı derleyip JAR'ı güncellemek çok daha hızlı olur.

---

Özet: Render'da batch + spatial hash, fizikte grid + layer, script'te instruction limit + timeout, Studio'da lazy update, hafızada cache + pool. Bunların her biri bir günlük iş. Hangisinden başlamalıyım dersen, render pipeline en çok getiriyi verir çünkü oyuncunun gördüğü her şey oradan geçiyor.

---

## Performans Analizi (Şu Anki Durum)

Aşağıda motorun şu anki halinde nerelerin yavaş olduğunu, neden yavaş olduğunu ve kabaca ne kadar etkilediğini yazdım. Abartılı terimler yok, direkt sıkıntı ne, nerede, nasıl çözülür.

---

### 1. Node.getName() Gereksiz String Kopyası

**Öncesi**: `return new String(name)` — her çağrıda yeni bir String nesnesi oluşuyor.
**Sonrası**: `return name` — direkt mevcut String'i döndürüyor.

- **Neden sorun?** `getName()` çok sık çağrılıyor. Explorer ağacı her yenilendiğinde, PropertiesPanel her açıldığında, her `toString()` yapıldığında vs. Her seferinde yeni bir String yaratmak demek, bu da GC'nin (çöp toplayıcının) daha sık çalışmasına yol açar.
- **Ne kadar etkiler?**: Küçük dünyalarda fark etmezsin. 10.000+ node'lu bir dünyada her frame 10.000 tane gereksiz String oluşur, GC'yi 2-3 saniyede bir tetikler, o da takılma (stutter) olarak gözükür.
- **Yapıldı mı?**: Evet, düzeltildi.

---

### 2. ScriptEditor Her Tuşta Kaydediyor

**Öncesi**: Her harf yazıldığında `textArea.getText()` + `script.setSource()` çağrılıyordu. Yani "a" yaz → tüm kaynağı string'e çevir → Script'e yaz. "b" yaz → yine tüm kaynağı çevir → yine yaz.
**Sonrası**: 400ms bekleyip kullanıcı durunca kaydediyor.

- **Neden sorun?**: 1000 satırlık bir script'te her tuş vuruşunda 1000 satırlık String kopyalanıp Script nesnesine yazılıyor. Kullanıcı saniyede 5 harf yazıyorsa saniyede 5 kere 1000 satır kopyalanıyor. Boşuna işlemci yoruluyor.
- **Ne kadar etkiler?**: Çok büyük script'lerde (5000+ satır) typing lag olarak hissedilir.
- **Yapıldı mı?**: Evet, düzeltildi.

---

### 3. ScriptEditor Kapatılınca Temizlik Yapılmıyor

**Öncesi**: `closing()` metodu boştu. DocumentListener, KeyListener, MouseListener, Timer, ActionMap hepsi hafızada kalıyordu. ScriptEditor'ü aç-kapa yaptıkça bu nesneler birikiyordu.
**Sonrası**: `closing()` içinde tüm listener'lar kaldırılıyor, Timer durduruluyor, tüm referanslar null yapılıyor.

- **Neden sorun?**: Java garbage collector bir nesneyi ancak ona kimse referans vermiyorsa siler. Listener'lar kaldırılmazsa, editor kapanmış olsa bile hala olay dinlemeye devam eder, hafızada yer kaplar. 50 kere script açıp kapatınca 50 tane ölü editor birikir.
- **Ne kadar etkiler?**: Uzun süreli kullanımda (birkaç saat) bellek şişer, GC daha sık çalışır, her şey yavaşlar.
- **Yapıldı mı?**: Evet, düzeltildi.

---

### 4. Explorer Ağacı Her Şeyi Baştan Kuruyor

**Öncesi**: En ufak değişiklikte (bir child ekle, bir isim değiştir) `rebuildExplorer()` çağrılıyor, bu da 50 servis + 500 node için ağacı komple silip baştan kuruyor. Expanded path'leri kaydedip geri yüklüyor ama bu da vakit alıyor.
**Sonrası**: Inline rename'de sadece o node'un görüntüsü güncelleniyor (`model.nodeChanged()`). Ama hala bazı yerlerde rebuildExplorer çağrılıyor (tamamen kaçınılmış değil).

- **Neden sorun?**: Her yeniden kurmada:
  1. Bütün node'lar gezilir
  2. Her node için DefaultMutableTreeNode oluşturulur
  3. Ağaç genişletilmiş/gizlenmiş yollar kaydedilir
  4. Her şey modele eklenir
  5. Kaydedilmiş yollar geri yüklenir
  Bütün bunlar 5000+ node'da 100-200ms sürer. Üst üste bindiğinde gözle görülür gecikme olur.
- **Ne kadar etkiler?**: Orta-büyük dünyalarda (5000+ node) kullanıcı her değişiklikte yarım saniyelik donma hisseder.
- **Yapıldı mı?**: Kısmen. Inline rename düzeltildi ama diğer rebuildExplorer çağrıları hala duruyor.

---

### 5. getAllNodes() Her Seferinde Yeni Liste Oluşturuyor

**Öncesi**: `getAllNodes()` recursive olarak tüm node'ları gezip yeni bir ArrayList doldurup döndürüyor. Bu method birçok yerde çağrılıyor (Explorer rebuild, render, hit test).
**Sonrası**: Hala aynı. Düzeltilmedi.

- **Neden sorun?**: Her frame render yaparken, Studio'da her tıklamada hit test yaparken, her rebuild'de yüzlerce node için liste oluşturuluyor. Kopya liste + yeni ArrayList = gereksiz allocation.
- **Ne kadar etkiler?**: Orta dünyalarda (5000 node) her `getAllNodes()` yaklaşık 0.5-1ms sürer ve 50KB+ heap kullanır. Saniyede 10 kere çağrılsa 10ms + 500KB demek.
- **Yapıldı mı?**: Hayır. Düzeltme: Cache'lenmiş bir liste tutup sadece değişiklik olduğunda güncellemek lazım.

---

### 6. Event Sistemi Parent Zincirini Tırmanıyor

**Öncesi**: `OnAddDescendant`, `OnRemoveDescendant` gibi event'ler ateşlendiğinde `notifyAncestors()` tüm parent zincirini yukarı doğru tırmanıp event'i herkese bildiriyor. 20 seviye derinlikte bir node eklendiğinde 20 kere event gidip geliyor.
**Sonrası**: Hala aynı. Düzeltilmedi.

- **Neden sorun?**: Toplu node eklemede (paste, import, world load) her bir node için bu olaylar zinciri defalarca döner. 1000 node import ederken 20 seviye derinlik varsa, yaklaşık 20.000 event bildirimi gider.
- **Ne kadar etkiler?**: World load'da bariz bir gecikme olarak hissedilir. 50.000 node'luk bir world'ü yüklemek birkaç saniye sürebilir.
- **Yapıldı mı?**: Hayır. Düzeltme: Toplu işlemlerde event'leri susturup en son tek bir "her şey değişti" event'i göndermek lazım (batch mode).

---

### 7. PropertiesPanel Inspect Her Şeyi Baştan Oluşturuyor

**Öncesi**: `inspect(node)` çağrıldığında `contentPanel.removeAll()` yapılıyor, sonra tüm kategoriler ve setting'ler için JPanel'ler, JLabel'lar, JTextField'lar, JComboBox'lar sıfırdan oluşturuluyor.
**Sonrası**: Hala aynı.

- **Neden sorun?**: Aynı node'a tekrar tıklandığında (mesela rename sonrası selection refresh) tüm UI sıfırdan yaratılıyor. 30 setting'li bir node'da bu 30 JPanel + 30 JLabel + 30 input component demek. Her seferinde sıfırdan.
- **Ne kadar etkiler?**: 50ms-100ms arası. Sürekli selection değiştirince takılma hissi.
- **Yapıldı mı?**: Hayır. Düzeltme: Component'leri yeniden kullanmak (sadece değerleri güncellemek).

---

### 8. FrameBitmapPool Limitsiz

**Öncesi**: Pool'a bitmap ekleyip çıkarmada herhangi bir üst sınır yok. Bir anda 200 tane bitmap talep edilirse 200 tane yeni Bitmap oluşur.
**Sonrası**: Hala aynı.

- **Neden sorun?**: Özellikle geçiş efektlerinde (fade, transition) veya yüksek çözünürlüklü render'da çok sayıda bitmap aynı anda havada kalabilir. Her biri 1920x1080 = 8MB civarı. 100 tane = 800MB.
- **Ne kadar etkiler?**: Anlık spike'larda OutOfMemoryError riski var.
- **Yapıldı mı?**: Hayır. Düzeltme: Pool'a maksimum kapasite koymak (`maxPoolSize = 50` gibi).

---

### 9. Draw() Her Frame Sıralama Yapıyor

**Öncesi**: Her `draw()` çağrısında `sortedChildren` diye yeni bir ArrayList oluşturulup sort ediliyor.
**Sonrası**: Hala aynı.

- **Neden sorun?**: Her frame, her parent node için çocuk listesi kopyalanıp ZIndex'e göre sıralanıyor. Sıralama değişmediği halde tekrar tekrar yapılıyor.
- **Ne kadar etkiler?**: 5000 node için her sort yaklaşık 2-5ms sürer. 60 fps'de bu frame time'ın %30'unu yiyebilir.
- **Yapıldı mı?**: Hayır. Düzeltme: Sadece ZIndex değiştiğinde sort'u yeniden yapmak, arada cache'lenmiş sırayı kullanmak.

---

### 10. InputHandler Poll Tabanlı

**Öncesi**: `InputHandler` her frame klavyenin/farenin durumuna tek tek bakıyor (polling).
**Sonrası**: Hala aynı.

- **Neden sorun?**: Hiç tuşa basılmamış olsa bile her frame input kontrol ediliyor. 60 fps'de saniyede 60 kere gereksiz input kontrolü.
- **Ne kadar etkiler?**: Çok az (0.01ms civarı). Ama mobil/batarya ile çalışan cihazlarda gereksiz CPU yükü.
- **Yapıldı mı?**: Hayır. Düzeltme: Event-driven sisteme geçmek (LWJGL'in callback'lerini kullanmak).

---

## Genel Performans Tablosu

| Sorun | Etki Seviyesi | Düzeltildi mi? | Çözüm Ne Kadar Zor? |
|---|---|---|---|
| Node.getName() kopyası | Düşük | **Evet** | 1 dk (satır değişikliği) |
| ScriptEditor her tuşta kaydetme | Orta | **Evet** | 15 dk (debounce ekleme) |
| ScriptEditor hafıza sızıntısı | Orta | **Evet** | 30 dk (listener temizliği) |
| Explorer rebuild her şeyi baştan kurma | Yüksek | Kısmen | 1-2 gün (incremental update) |
| getAllNodes() her seferinde yeni liste | Yüksek | Hayır | 2-3 saat (cache mekanizması) |
| Event sistemi parent tırmanma | Orta | Hayır | 1 gün (batch mode) |
| PropertiesPanel sıfırdan oluşturma | Orta | Hayır | 1 gün (component reuse) |
| FrameBitmapPool limitsiz | Düşük | Hayır | 30 dk (maxPoolSize ekleme) |
| Draw() her frame sort | Yüksek | Hayır | 2-3 saat (cached sort) |
| InputHandler poll | Düşük | Hayır | 1-2 gün (event-driven) |

Şu an en acil: **getAllNodes cache** ve **draw sort cache**. Bunlar gözle görülür frame drop'ları engeller. Studio tarafında **PropertiesPanel reuse** en sinir bozucu gecikmeyi kaldırır.

# ne yapılmalı — kafamdaki roadmap

öncelikle şu anki haliyle motor çalışıyor, bir şeyler yapılabiliyor. ama Roblox 2D gibi düşününce eksikler var. aşağıya yazdım, öncelik sırasına göre.

---

## 1. anasınıfı performans işleri (acil)

bunlar olmadan ileri gitmenin anlamı yok, her şey kasıyor:

- **rotate cache**: Instance.draw her frame rotate çağırıyor. saniyede binlerce bitmap oluşuyor. rotation değişmediyse eski halini kullan.
- **collision O(n²) → spatial grid**: şu an 100 instance olsa 4950 kere intersect kontrol ediliyor. grid'e bölünce sadece aynı hücredekiler karşılaştırılır.
- **event leak fix**: Node silinince Event listener'ları temizlenmiyor. script'ler connect ediyor, disconnect etmiyor, referanslar kalıyor. ya weak reference kullan ya da removeNode içinde temizle.
- **FrameBitmapPool anlamsız**: her frame tüm bitmap pool'u clear'leniyor. aynı boyuttakiler tekrar kullanılabilir, baştan oluşturmaya gerek yok.
- **Lighting.isOccluded bug**: `return false` yerine `continue` olmalı. yoksa gölge sistemi daha ilk görünmez instance'da çöküyor.
- **gereksiz sort'lar**: her frame children listesini sort'luyor, nadiren değişen zIndex için cache + dirty flag yeter.

---

## 2. scripting (lua) tarafı

roblox gibi olacaksa lua tarafı çok önemli:

- **Event.FireEvent önce listener kontrolü**: listener yoksa LuaValue[] oluşturmaya gerek yok.
- **Script'te debug lib'i sadece geliştirme modunda çalıştır**: her instruction'da counter artırıp yield yapmak can sıkıcı.
- **daha fazla Lua binding**: Instance, Part, Camera, Light gibi ana class'lar Lua'ya açık değil. şu an sadece birkaç tane var.
- **sandbox**: Lua script'i şu an Java'daki her şeye erişebiliyor. oyuncu kendi script'ini yazdığında System.exit() çağırabilir.
- **task.wait / task.spawn**: Roblox'taki gibi bir scheduler lazım. şu an her script kendi virtual thread'inde çalışıyor, bu iyi değil.

---

## 3. render pipeline

- **Instance.draw içinde rotate + blendDraw + color blend → tek seferde yap**: şu an rotate edip ayrı bitmap alıyor, sonra onu blendDraw ile renklendirip çiziyor. üç işlem tek geçişte yapılabilir.
- **dirty rect sistemi**: her frame tüm sahneyi yeniden çizmek yerine sadece değişen bölgeleri güncelle.
- **Batch render**: aynı texture'ı kullanan instance'ları tek draw call'da birleştir. LWJGL ile mümkün.
- **zoom != 1 olduğunda resize yapma**: resize çok ağır. direkt GL_TEXTURE ile ölçeklendir.

---

## 4. özellikler (sırası gelince)

- **TweenService**: değerleri zamanla değiştirmek için. Roblox'taki gibi.
- **SoundService**: şu an Audio node'u var ama çalışıyor mu emin değilim.
- **ParticleEmitter**: patlama, duman, yağmur falan.
- **PathfindingService**: A* ya da basit bir grid pathfinding.
- **PhysicsService**: rigidbody, gravity, collision groups. şu an collision sadace overlap kontrol ediyor, kuvvet falan yok.
- **DataStore**: oyun kaydetme, oyuncu verisi. Lua tarafından erişilebilir olmalı.

---

## 5. studio (editor)

- **undo/redo sistemi yok**: herhangi bir şeyi silince geri alınamıyor.
- **properties panel çok basit**: color picker, object select gibi şeyler var ama daha fazla widget gerek.
- **script editor syntax highlighting var mı** bakmak lazım, RSyntaxTextArea kullanılıyor, çalışıyordur.
- **drag-drop ile node ekleme**: BasicObjectsPanel'den sürükleyip explorer'a bırakma.
- **toolbar karmaşık**: gereksiz butonlar var, gruplandırılabilir.

---

## 6. ileri seviye (çok sonra)

- **networking**: 2 oyuncu aynı dünyada. şu an hiçbir şey yok. Lua'dan `game:HttpGet` var o kadar.
- **plugin sistemi**: studio'ya eklenti ekleme.
- **mobile export**: Android'de çalışacak hale getirmek.
- **build & publish**: oyunu tek .jar olarak paketleme.

---

## özet

| sıra | ne | neden |
|------|----|-------|
| 1 | rotate cache, collision grid, event leak | olmazsa olmaz |
| 2 | scripting sandbox + scheduler | güvenlik + oynanabilirlik |
| 3 | dirty rect + batch render | FPS için |
| 4 | TweenService, Pathfinding, Sound | eksik özellikler |
| 5 | undo/redo, studio iyileştirmeleri | editor kullanılabilirliği |

önce maddeleri halledelim derim. sonra özellik eklemeye başlarız. rotate cache ve collision grid arka arkaya yapılınca zaten gözle görülür bir fark olur.

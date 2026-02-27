 ANALISIS KINERJA AKSELERASI PERANGKAT KERAS UNTUK INFERENSI POSE ESTIMATION REAL-TIME PADA APLIKASI KEBUGARAN ANDROID

SKRIPSI
Untuk memenuhi sebagian persyaratan 
memperoleh gelar Sarjana Komputer

Disusun oleh:
Arvan Yudhistia Ardana
NIM: 225150200111014

   PROGRAM STUDI TEKNIK INFORMATIKA
JURUSAN TEKNIK INFORMATIKA
FAKULTAS ILMU KOMPUTER
UNIVERSITAS BRAWIJAYA
MALANG
2026

 
PENDAHULUAN
Latar Belakang
Bagian Aplikasi kebugaran berbasis mobile telah mengalami pertumbuhan signifikan dalam beberapa tahun terakhir, didorong oleh meningkatnya kesadaran masyarakat akan pentingnya gaya hidup sehat dan kemudahan akses teknologi. Menurut Grand View Research (2024), pasar aplikasi kebugaran global mencapai $10,59 miliar pada tahun 2024 dan diproyeksikan tumbuh menjadi $33,58 miliar pada tahun 2033 dengan tingkat pertumbuhan tahunan gabungan (CAGR) sebesar 13,59%. 
Real-time pose estimation menjadi komponen kritis dalam aplikasi tersebut untuk memberikan umpan balik (feedback) koreksi postur secara instan kepada pengguna. Estimasi pose tubuh manusia (human pose estimation) merupakan teknologi computer vision yang dapat mendeteksi dan melacak posisi sendi-sendi utama tubuh manusia dari gambar atau video. Teknologi ini memiliki potensi besar untuk aplikasi kebugaran karena dapat memberikan umpan balik instan mengenaiform atau postur latihan yang benar, sehingga dapat meningkatkan efektivitas latihan dan mengurangi risiko cedera. Namun, implementasi sistem estimasi pose pada perangkat bergerak menghadapi tantangan signifikan terkait keterbatasan sumber daya komputasi, memori, dan baterai.
Meskipun pasar aplikasi kebugaran global tumbuh pesat (data Grand View Research), implementasi fitur AI seperti pose estimation menghadapi tantangan fragmentasi perangkat keras, terutama di negara berkembang seperti Indonesia. 
Berdasarkan data Statcounter(2024), mayoritas pengguna Android di Indonesia masih menggunakan perangkat kelas entry-level dan mid-range dengan sumber daya komputasi (CPU/GPU) yang terbatas. Ketimpangan spesifikasi ini menuntut pengembang untuk tidak hanya mengandalkan CPU saja, tetapi juga memanfaatkan akselerasi perangkat keras (Hardware Acceleration) yang tersedia melalui GPU atau NPU agar aplikasi dapat berjalan secara real-time.
TensorFlow Lite sebagai framework optimasi model machine learning untuk perangkat mobile menyediakan berbagai engine akselerasi yang dikenal sebagai delegate. Setiap delegate memiliki karakteristik performa yang berbeda: GPU Delegate memanfaatkan akselerasi grafis untuk operasi paralel, XNNPACK Delegate berfokus pada efisiensi CPU dengan optimasi instruksi ARM Neon, dan eksekusi CPU standar sebagai fallback universal (Google Developers, 2024). Pemilihan delegate yang tepat sangat krusial untuk memastikan aplikasi dapat berjalan optimal pada perangkat dengan spesifikasi beragam.
Penelitian sebelumnya menunjukkan bahwa model pose estimation seperti MoveNet dan BlazePose telah dioptimalkan untuk deployment pada perangkat mobile. MoveNet Lightning dirancang khusus untuk kecepatan dengan ukuran input 192├ù192 piksel dan mencapai inferensi 25ms pada Pixel 5 dengan GPU delegate (Google Research, 2021). Di sisi lain, BlazePose menggunakan pendekatan detector-tracker yang efisien dengan waktu inferensi 33ms pada Pixel 2 dan mampu mendeteksi 33 keypoints dalam ruang 3D (Bazarevsky et al., 2020). Kedua model ini merepresentasikan pendekatan arsitektur yang berbeda: MoveNet menggunakan bottom-up approach dengan smart cropping, sedangkan BlazePose menggunakan two-stage detector-tracker.
Celah riset yang signifikan teridentifikasi, yaitu kurangnya analisis sistematis perbandingan kinerja berbagai delegate TensorFlow Lite untuk implementasi real-time pose estimation pada perangkat Android low-to-mid range dalam konteks aplikasi kebugaran. Setiap delegate memiliki karakteristik optimasi yang berbeda. GPU delegate dapat memberikan peningkatan kecepatan hingga 5x lipat untuk operasi paralel (Google AI Edge, 2024), namun memiliki overhead inisialisasi yang tinggi dan konsumsi daya lebih besar. XNNPACK Delegate mengoptimalkan operasi CPU dengan peningkatan performa hingga 2.3x dibanding CPU standar (TensorFlow Blog, 2024), dengan efisiensi energi yang lebih baik. Eksekusi CPU standar memberikan konsistensi lintas perangkat namun dengan performa lebih rendah.
Domain kebugaran dipilih karena memerlukan akurasi tinggi untuk keamanan (safety) pengguna, latensi rendah untuk responsivitas real-time, dan efisiensi energi untuk penggunaan berkelanjutan. Pemilihan delegate yang tidak tepat dapat mengakibatkan performa aplikasi yang suboptimal, pengalaman pengguna yang buruk, atau konsumsi baterai yang berlebihan. Untuk menjawab tantangan komputasi pada perangkat mobile tersebut, Google mengembangkan model arsitektur efisien seperti MoveNet dan Mediapipe BlazePose. Meskipun kedua model ini diklaim ringan (lightweight), kinerja inferensinya sangat bergantung pada bagaimana beban komputasi didistribusikan ke unit pemrosesan yang tepat (CPU, GPU, atau XNNPACK) melalui mekanisme delegate.
Hasil penelitian ini diharapkan dapat memberikan panduan empiris bagi para developer untuk memilih konfigurasi delegate dan model yang optimal berdasarkan karakteristik perangkat target dan prioritas aplikasi, sehingga aplikasi kebugaran berbasis pose estimation dapat diakses oleh pengguna dengan beragam spesifikasi perangkat.
Rumusan Masalah
	Bagaimana perbandingan kinerja inferensi (latensi dan throughput) serta konsumsi sumber daya (utilisasi CPU, memori, dan daya) antara GPU delegate, XNNPACK delegate, dan CPU-only execution pada TensorFlow Lite untuk real-time pose estimation dalam aplikasi kebugaran Android?
	Bagaimana perbandingan kinerja antara model MoveNet Lightning dan BlazePose Lite pada setiap konfigurasi delegate dalam konteks aplikasi kebugaran mobile?
	Konfigurasi delegate dan model manakah yang memberikan keseimbangan optimal antara kinerja, efisiensi sumber daya, dan akurasi untuk perangkat Android low-to-mid range?
Tujuan
Berdasarkan rumusan masalah di atas, penelitan memiliki tujuan sebagai berikut:
	Mengukur dan membandingkan kinerja inferensi (latensi dan throughput) serta konsumsi sumber daya (utilisasi CPU, memori, dan daya) antara GPU, delegate, XNNPACK, dan CPU-only execution untuk real-time pose estimation pada aplikasi kebugaran Android.
	Mengevaluasi perbandingan kinerja antara model MoveNet Lightning dan BlazePose Lite pada setiap konfigurasi delegate dalam konteks aplikasi kebugaran mobile.
	Mengidentifikasi konfigurasi delegate dan model yang memberikan keseimbangan optimal antara kinerja, efisiensi sumber daya, dan akurasi untuk aplikasi estimasi pose pada perangkat Android low-to-mid range.
Manfaat
Penelitian ini diharapkan dapat memberikan sejumlah manfaat yang bernilai bagi berbagai pihak.
	Bagi komunitas pengembang perangkat lunak, hasil penelitian ini dapat menyediakan data benchmark yang bersifat kuantitatif dan objektif sebagai dasar pengambilan keputusan teknis dalam pemilihan delegate TensorFlow Lite yang paling sesuai dengan kebutuhan aplikasi, baik dari sisi efisiensi energi maupun akurasi. Selain itu, penelitian ini juga memberikan panduan praktis untuk mengoptimalkan implementasi pose estimation pada aplikasi kebugaran di berbagai konfigurasi perangkat keras Android, khususnya untuk perangkat low-to-mid range yang mendominasi pasar Indonesia, sehingga dapat membantu pengembang dalam meningkatkan performa aplikasi secara efektif dan memperluas jangkauan pengguna.
	Bagi komunitas akademis, penelitian ini berkontribusi dalam mengisi kesenjangan riset yang ada dengan menghadirkan studi komparatif formal mengenai performa berbagai delegate TensorFlow Lite dalam konteks aplikasi mobile dengan fokus pada perangkat dengan keterbatasan sumber daya. Hasilnya dapat dijadikan referensi bagi penelitian lanjutan di bidang analisis performa perangkat lunak maupun rekayasa sistem mobile. Lebih dari itu, metodologi eksperimen yang dikembangkan dalam penelitian ini dapat diadaptasi dan diterapkan untuk mengevaluasi performa delegate pada domain aplikasi lain di masa mendatang.
	Bagi industri teknologi kesehatan, penelitian ini memberikan wawasan teknis yang bermanfaat dalam mengembangkan aplikasi kebugaran berbasis kecerdasan buatan yang lebih efisien, responsif, dan ramah pengguna. Dengan pemahaman mendalam terhadap performa masing-masing delegate, pengembang di sektor ini dapat merancang solusi fitness berbasis AI yang mampu berjalan optimal pada beragam tingkat spesifikasi perangkat Android, sehingga memperluas aksesibilitas dan pengalaman pengguna secara keseluruhan, terutama bagi segmen pasar dengan daya beli menengah ke bawah.
Batasan Masalah
Untuk menjaga agar penelitian tetap fokus dan terarah, ruang lingkup penelitian ini dibatasi pada aspek berikut :
	Platform dan Lingkungan Pengujian: Penelitian ini diimplementasikan menggunakan library TensorFlow Lite versi 2.15.0 (atau terbaru yang stabil) pada sistem operasi Android dengan target SDK API Level 34 (Android 14)
	Model Pose Estimation: Penelitian menggunakan dua varian model pre-trained yang telah dioptimalkan untuk perangkat mobile, yaitu:
	MoveNet Lightning (INT8): Model ultra-cepat dengan input resolusi rendah (192x192), merepresentasikan beban kerja ringan.
	MediaPipe BlazePose Lite (FP16): Model berbasis topologi 33 keypoints dengan kompleksitas deteksi 3D, merepresentasikan beban kerja menengah.
	Perangkat Keras (Device Testbed): Pengujian dilakukan pada dua kategori perangkat pintar (smartphone) untuk memvalidasi performa akselerasi perangkat keras pada segmen pasar yang berbeda:
	Low-End Device: Samsung Galaxy A05/A06 (Chipset MediaTek Helio G85, GPU Mali-G52, RAM 4GB) sebagai representasi perangkat dengan sumber daya terbatas.
	Mid-Range Device: Samsung Galaxy A33 5G (Chipset Exynos 1280, GPU Mali-G68, RAM 6/8GB) sebagai representasi perangkat modern dengan dukungan NPU.
	Fokus Pengukuran Kinerja: Penelitian ini hanya berfokus pada pengukuran kinerja komputasi (computational performance), yang meliputi parameter: Latensi Inferensi (Inference Time), Frame Per Second (FPS), Penggunaan CPU/GPU, dan Konsumsi Memori (RAM). Penelitian tidak membahas akurasi koreksi gerakan olahraga ataupun pengembangan fitur aplikasi kebugaran secara utuh.
	Batasan NNAPI: Mengingat status deprecation NNAPI pada Android 15, evaluasi NNAPI Delegate dalam penelitian ini ditujukan sebagai studi relevansi performa untuk perangkat legacy (Android 13/14 ke bawah) yang masih mendominasi pasar Indonesia.
Sistematika Pembahasan
Untuk memberikan gambaran yang jelas mengenai alur penelitian, laporan skripsi ini disusun dengan sistematika sebagai berikut:
BAB I PENDAHULUAN: Berisi latar belakang masalah yang mengidentifikasi pentingnya optimasi delegate TensorFlow Lite untuk aplikasi kebugaran pada perangkat low-to-mid range, rumusan masalah yang spesifik dan terukur, tujuan penelitian, manfaat yang diharapkan, batasan masalah yang jelas, serta sistematika pembahasan.
BAB II LANDASAN KEPUSTAKAAN: Menguraikan tinjauan literatur mengenai penelitian terkait di bidang pose estimation mobile dan optimasi TensorFlow Lite, serta teori-teori pendukung mengenai arsitektur delegate TensorFlow Lite, model MoveNet, metrik evaluasi kinerja aplikasi mobile, dan metodologi penelitian eksperimental.
BAB III METODOLOGI PENELITIAN: Menjelaskan secara rinci desain eksperimen kuantitatif, variabel penelitian, teknik pengumpulan data menggunakan profiling tools, serta metodologi analisis statistik menggunakan uji ANOVA untuk memvalidasi hipotesis.
BAB IV PERANCANGAN SISTEM: Menguraikan tahapan perancangan aplikasi testbed yang digunakan sebagai alat ukur, meliputi analisis kebutuhan fungsional dan non-fungsional, perancangan arsitektur sistem, diagram alir (flowchart) logika inferensi dan penghitungan repetisi, serta rancangan antarmuka pengguna (wireframe).
BAB V IMPLEMENTASI DAN HASIL PEMBAHASAN: Memuat realisasi sistem berdasarkan rancangan yang telah dibuat, penyajian data hasil benchmarking (latensi, throughput, dan penggunaan sumber daya) yang dikumpulkan secara sistematis, serta analisis pembahasan mendalam (deep dive) mengenai komparasi performa antar-delegate dan implikasinya terhadap perangkat keras.
BAB VI PENUTUP: Berisi kesimpulan yang menjawab setiap rumusan masalah berdasarkan analisis data empiris, kontribusi penelitian terhadap body of knowledge, keterbatasan penelitian yang diidentifikasi selama proses eksperimen, serta saran untuk pengembangan penelitian di masa mendatang.

LANDASAN KEPUSTAKAAN
Kajian Pustaka
Kajian Pustaka dilakukan untuk mengidentifikasi penelitian yang telah ada di bidang analisis kinerja aplikasi mobile, khususnya yang berkaitan dengan framework TFLite dan pose estimation. Dari penelusuran tesebut, beberapa studi menjadi rujukan utama yang menyoroti celah penelitian yang hendak diisi oleh studi ini. Berikut hasil dari kajian pustaka disajikan dalam Tabel 2.1
			Tabel 2.1 Penelitian Terdahulu
Penulis	Judul	Metode	Hasil	Gap Penelitian
Bazarevsky et al. (2020)	BlazePose: On-device Real-time Body Pose tracking	CNN multitahap dengan detector-tracker approach, 33 keypoints 3D	mAP 66,9%, 33ms inferensi pada Pixel 2 GPU	Hanya mengevaluasi GPU delegate tanpa membandingkan alternatif optimasi lainnya seperti CPU, XNNPACK atau NNAPI
Yu et al. (2023)	MovePose: A High-performance Human Pose Estimation Algorithm on Mobile and Edge Devices	Optimasi pose estimation untuk edge/mobile devices	mAP 68,0%, 11+ fps pada Android Snapdragon 8	Tidak menspesifikasi jenis delegate yang digunakan dan tidak menganalisis trade-off resource consumption
Turner et al. (2024)	A Mobile-Phone Pose Estimation for Gym-Exercise Form Correction	Machine learning dengan deteksi anomali untuk koreksi postur gym	Sistem dapat mendeteksi postur salah dan memberikan koreksi valid dalam video real-time	Tidak menyediakan metrik kinerja kuantitatif (latency, CPU, memory) yang esensial untuk evaluasi mobile
Reddi et al. (2022)	MLPerf Mobile INference Benchmark	Evaluasi komprehensif TensorFlow Lite delegates dan PyTorch backends pada 174 skenario	GPU delegate memberikan speedup signifikan untuk model besar, namun overhead untuk model kecil	Bersifat general ML benchmarking, tidak spesifik untuk domain pose estimation dan aplikasi real-time

Ada beberapa penelitian yang telah dilakukan sebelumnya dan dapat dijadikan referensi serta referensi, pertama, penelitian oleh Bazarevsky et al. (2020) mengembangkan BlazePose sebagai solusi terobosan untuk estimasi pose real-time pada perangkat bergerak. Penelitian mereka mengatasi tantangan fundamental dalam implementasi pose estimation pada perangkat dengan keterbatasan sumber daya melalui arsitektur detector-tracker yang efisien. Model BlazePose menggunakan pendekatan dua tahap: tahap pertama mendeteksi keberadaan manusia dalam frame, dan tahap kedua melakukan tracking 33 keypoints dalam ruang 3D. Hasil eksperimen menunjukkan model dapat mencapai akurasi mean Average Precision (mAP) sebesar 66,9% dengan waktu inferensi rata-rata 33ms pada Google Pixel 2 menggunakan GPU delegate. Namun, penelitian ini hanya mengevaluasi satu jenis delegate tanpa membandingkan alternatif optimasi lainnya seperti CPU atau XNNPACK, sehingga belum memberikan analisis menyeluruh terkait trade-off kinerja. 
Yu et al. (2023) mengembangkan MovePose sebagai algoritme pose estimation berkinerja tinggi yang dirancang khusus untuk deployment pada edge dan mobile devices. Pendekatan mereka menggunakan tiga teknik optimasi utama: dekonvolusi untuk meningkatkan resolusi feature maps, konvolusi dengan kernel besar untuk memperluas receptive field, dan metode klasifikasi koordinat untuk presisi lokalisasi keypoints. Evaluasi pada dataset COCO validation menunjukkan MovePose mencapai mAP 68,0% dengan throughput 11+ fps pada perangkat Android dengan prosesor Snapdragon 8. Meskipun menunjukkan kinerja yang superior, penelitian ini tidak menspesifikasi jenis delegate TensorFlow Lite yang digunakan dan tidak memberikan analisis mendalam tentang trade-off antara berbagai opsi akselerasi perangkat keras atau konsumsi sumber daya.
Reddit et al. (2022) melakukan evaluasi komprehensif terhadap kinerja inferensi machine learning pada perangkat bergerak melalui MLPerf Mobile benchmark suite, dengan fokus khusus pada perbandingan berbagai implementasi delegate TensorFlow Lite dan backend PyTorch. Penelitian mereka mencakup pengujian pada 174 skenario berbeda yang meliputi variasi model, ukurna input, dan konfigurasi perangkat keras. Temuan utama menunjukkan bahwa GPU delegate memberikan speedup signifikan untuk model dengan ukuran besar dan operasi yang dapat diparalelkan, namun dapat menjadi bottleneck untuk model kecil karena overhead komunikasi antara CPU dan GPU. Penelitian ini memberikan pemahaman berharga tentang karakterisitik kinerja berbagai delegate, namun bersifat umum dan tidak spesifik untuk domain aplikasi pose estimation yang memiliki karakterisitik workload unik.
Turner dkk. (2024) mengembangkan sistem koreksi postur untuk latihan gym menggunakan kombinasi machine learning dan metode deteksi anomali. Sistem mereka dirancang untuk mendeteksi penyimpangan dari form latihan yang benar dan memberikan feedback korektif dalam waktu nyata. Pendekatan mereka menggunakan pose estimation sebagai input untuk algoritme deteksi anomali yang dapat mengidentifikasi gerakan yang berpotensi berbahaya atau tidak efektif. Hasil eksperimen menunjukkan sistem mampu mendeteksi postur yang salah dan memberikan koreksi yang valid dalam video real-time. Namun, penelitian ini memiliki kelemahan signifikan dalam tidak menyediakan metrik kinerja kuantitatif seperti latensi inferensi, utilisasi CPU, atau konsumsi memori, yang penting untuk evaluasi implementasi pada perangkat bergerak..
Gap Penelitian
Berdasarkan hasil tinjauan pustaka, terdapat beberapa celah penelitian yang cukup signifikan dan relevan untuk dikaji lebih lanjut. Pertama, belum ada penelitian yang secara sistematis menganalisis kinerja berbagai delegate TensorFlow Lite dalam konteks estimasi pose, khususnya untuk aplikasi kebugaran. Studi seperti Reddi et al. (2022) hanya melakukan benchmarking umum, sementara Bazarevsky et al. (2020) berfokus pada penggunaan GPU delegate tanpa membandingkan trade-off kinerja antara delegate lain.
Kedua, sebagian besar penelitian masih terbatas pada skenario terkontrol atau berbasis dataset standar, sehingga kurang merepresentasikan kondisi dunia nyata. Evaluasi dalam konteks aplikasi kebugaran yang sebenarnya dengan variabilitas pencahayaan, kecepatan gerakan pengguna, serta durasi penggunaan yang panjang, serta keterbatasan hardware pada perangkat low-to-mid range masih jarang dilakukan.
Ketiga, penelitian sebelumnya umumnya hanya berfokus pada satu metrik seperti akurasi atau kecepatan inferensi, tanpa mempertimbangkan trade-off komprehensif yang mencakup latensi, konsumsi sumber daya, dan efisiensi baterai secara bersamaan. Terakhir, belum ada metodologi evaluasi yang terstandarisasi dan dapat direplikasi untuk mengukur kinerja delegate TensorFlow Lite secara menyeluruh pada aplikasi estimasi pose bergerak. Hal ini menciptakan kebutuhan akan framework evaluasi yang sistematis, terukur, serta dapat diadaptasi untuk penelitian selanjutnya di bidang ini.
Keempat, perbandingan sistematis antara model-model pose estimation terkini (MoveNet Lightning dan BlazePose Lite) dengan berbagai konfigurasi delegate pada perangkat Android yang representatif terhadap pasar Indonesia belum dilakukan. Hal ini penting mengingat karakteristik hardware yang berbeda dapat menghasilkan performa relatif yang berbeda pula antar delegate.
Terakhir, belum ada metodologi evaluasi yang terstandarisasi dan dapat direplikasi untuk mengukur kinerja delegate Tensorflow Lite secara menyeluruh pada aplikasi pose estimation, khususnya dengan mempertimbangkan konteks perangkat low-to-mid range. Hal ini menciptakan kebutuhan akan framework evaluasi yang sistematis, terukur, serta dapat diadaptasi untuk penelitian selanjutnya di bidang ini.
Landasan Teori
Landasan teori ini membahas lima komponen utama yang mendasari penelitian, yaitu konsep fundamental estimasi pose tubuh manusia, kerangka kerja TensorFlow Lite sebagai platform inferensi model pembelajaran mesin, model MoveNet  dan BlazePose sebagai representasi estimasi pose mutakhir untuk perangkat bergerak, pengembangan aplikasi Android sebagai konteks implementasi, serta metrik kinerja dan framework evaluasi yang digunakan untuk mengevaluasi performa delegate secara empiris.
Pose Estimation
Estimasi pose tubuh manusia (Human Pose Estimation) merupakan proses mendeteksi posisi titik-titik kunci tubuh manusia (keypoints) seperti kepala, bahu, siku, lutut, dan pergelangan kaki dari citra atau video. Dalam konteks 2D pose estimation, setiap keypoint direpresentasikan dengan korrdinat (x,y) dan confidence score yang menunjukkan tingkat kepercayaan deteksi. State-of-the-art pose estimation model umumnya mendeteksi 17 keypoints yang mencakup sendi-sendi utama tubuh manusia berdasarkan standar COCO keypoint annotations: hidung, mata kiri, mata kanan, telinga kiri, telinga kanan, bahu kiri, bahu kanan, siku kiri, siku kanan, pergelangan tangan kiri, pergelangan tangan kanan, pinggul kiri, pinggul kanan, lutut kiri, lutut kanan, pergelangan kaki kiri, dan pergelangan kaki kanan. 
Pose estimation memiliki berbagai aplikasi praktis, termasuk dalam bidang kebugaran dan olahraga. Dalam konteks aplikasi kebugaran, pose estimation digunakan untuk: Mendeteksi penyimpangan dari postur yang benar selama latihan, sehingga dapat mencegah cedera dan meningkatkan efektivitas latihan (Turner et al., 2024). Melacak gerakan berulang dalam latihan seperti squat, push-up, atau sit-up untuk memberikan feedback otomatis kepada pengguna. Memberikan insights tentang kualitas gerakan, rentang gerak (range of motion), dan konsistensi dalam melakukan latihan.
Untuk implementasi pada perangkat mobile, pose estimation menghadapi tantangan khusus terkait keterbatasan sumber daya komputasi, memori, dan daya baterai. Oleh karena itu, model-model seperti MoveNet dan BlazePose dirancang khusus dengan arsitektur yang efisien untuk dapat berjalan real-time pada perangkat mobile tanpa mengorbankan akurasi secara signifikan.
Tensorflow Lite
Tensorflow Lite adalah framework open-source yang dikembangkan oleh Google untuk menjalankan model machine learning pada perangkat mobile, embedded, dan IoT dengan keterbatasan sumber daya(Google, 2024). Framework ini menyediakan tools untuk mengonversi model Tensorflow standar menjadi format yang dioptimalkan (.tflite) dengan ukuran lebih kecil dan latensi inferensi lebih rendah.
Optimasi model dalam TensorFlow Lite dilakukan melalui beberapa teknik. Quantization mengurangi presisi dari float32 ke int8 atau int16, mengurangi ukuran model hingga 4x dan meningkatkan kecepatan inferensi (Google AI Edge, 2024). Pruning menghilangkan weights yang tidak signifikan untuk mengurangi kompleksitas model. Operator fusion menggabungkan multiple operations menjadi single operation untuk mengurangi memory access dan meningkatkan throughput.
Delegate System merupakan fitur kunci TensorFlow Lite yang memungkinkan percepatan hardware-specific. GPU Delegate memanfaatkan Graphics Processing Unit untuk operasi paralel, sangat efektif untuk convolutional layers dengan speedup hingga 5x (Google AI Edge, 2024). XNNPACK Delegate mengoptimalkan operasi CPU menggunakan SIMD instructions (ARM NEON, SSE), memberikan peningkatan 2-3x dibanding CPU baseline (TensorFlow Blog, 2024). CPU Fallback menyediakan eksekusi universal untuk semua operator, menjamin kompatibilitas lintas perangkat.
Pemilihan delegate yang tepat bergantung pada karakteristik model, workload aplikasi, dan hardware target. GPU delegate optimal untuk model dengan banyak convolutional operations, namun memiliki overhead initialization. XNNPACK efisien untuk model yang sudah well-optimized dan cocok untuk perangkat tanpa GPU dedicated. CPU fallback menjamin konsistensi namun dengan performa terendah.
Model MoveNet
MoveNet adalah model estimasi pose mutakhir yang dikembangkan oleh Google khusus untuk perangkat bergerak. Model ini dirancang untuk mendeteksi 17 titik kunci (keypoints) tubuh manusia dengan akurasi tinggi dan latensi rendah (Google Research, 2021). MoveNet menggunakan arsitektur pendekatan bottom-up berdasarkan CenterNet detection API dengan MobileNetV2 sebagai feature extractor.
Arsitektur MoveNet mengimplementasikan algoritme smart cropping yang meningkatkan akurasi pada video dengan melakukan zum ke wilayah dimana pose terdeteksi pada frame sebelumnya (Google Research, 2021). Pendekatan ini memungkinkan model melihat detail yang lebih halus dan membuat prediksi yang lebih akurat, sangat efektif untuk aplikasi kebugaran dimana subjek relatif statis dalam frame.
MoveNet tersedia dalam dua varian utama yang mengakomodasi trade-off antara kecepatan dan akurasi (Google Research, 2021):
MoveNet Lightning dioptimalkan untuk kecepatan dengan ukuran masukan 192├ù192 piksel dan depth multiplier 1,0. Model ini mampu mencapai waktu inferensi 25ms pada Pixel 5 dengan GPU delegate, menjadikannya ideal untuk aplikasi waktu nyata yang memprioritaskan responsivitas (Google Research, 2021). Akurasi model mencapai 63,0% mAP dengan ukuran model 4,8MB (kuantisasi FP16). Varian ini sangat sesuai untuk aplikasi kebugaran yang membutuhkan umpan balik segera dan dapat mentolerir akurasi yang sedikit lebih rendah.
MoveNet Thunder dioptimalkan untuk akurasi dengan ukuran masukan 256├ù256 piksel dan depth multiplier 1,75. Model ini mencapai 72,0% mAP dengan waktu inferensi 45ms pada Pixel 5 GPU, memberikan keseimbangan yang baik antara akurasi dan kecepatan untuk aplikasi yang memerlukan presisi tinggi (Google Research, 2021). Ukuran model adalah 12,6MB (kuantisasi FP16), lebih tepat untuk aplikasi dimana akurasi kritis untuk keselamatan pengguna.
Model BlazePose Lite
BlazePose adalah arsitektur pose estimation yang dikembangkan oleh Google Research untuk real-time body pose tracking pada perangkat mobile (Bazarevsky et al., 2020). Model ini menggunakan pendekatan dua tahap yang terdiri dari pose detector dan pose tracker, memungkinkan deteksi yang cepat dan tracking yang akurat dengan overhead komputasi minimal.
Arsitektur BlazePose terdiri dari dua komponen utama. Pose Detector menggunakan lightweight CNN untuk mendeteksi region of interest (ROI) yang mengandung tubuh manusia dalam frame. Pose Tracker melakukan prediksi 33 keypoints 3D dalam ROI yang telah dideteksi, dengan tracking temporal untuk konsistensi antar frame. Pendekatan two-stage ini lebih efisien dibanding single-stage detection karena pose tracker hanya perlu memproses ROI kecil setelah deteksi awal.
BlazePose Lite adalah varian yang dioptimalkan untuk perangkat mobile dengan kompleksitas model yang dikurangi. Model ini mampu mendeteksi 33 keypoints yang mencakup seluruh tubuh, termasuk keypoints wajah dan tangan yang tidak ada di MoveNet. Waktu inferensi mencapai 25-33ms pada Pixel 2 dengan GPU delegate, dengan akurasi mAP 66,9% (Bazarevsky et al., 2020). Ukuran model sekitar 3,5MB (kuantisasi FP16), menjadikannya sangat efisien untuk deployment mobile.
Keunggulan BlazePose Lite meliputi: detector-tracker approach yang efisien untuk video processing; 33 keypoints memberikan informasi pose yang lebih detail dibanding 17 keypoints standard; model size yang kecil (3,5MB) optimal untuk perangkat dengan keterbatasan storage; temporal consistency melalui tracking mechanism mengurangi jitter dalam deteksi; performa yang konsisten pada berbagai kondisi pencahayaan dan pose complexity.
Perbandingan MoveNet Lightning vs BlazePose Lite menunjukkan trade-off yang menarik: MoveNet Lightning menggunakan bottom-up approach dengan smart cropping, cocok untuk single-person tracking dengan latency sangat rendah. BlazePose Lite menggunakan detector-tracker approach, lebih robust untuk occlusion dan pose variation dengan detail keypoints lebih tinggi (33 vs 17). Kedua model memiliki ukuran dan latensi inferensi yang sebanding, menjadikan keduanya kandidat ideal untuk penelitian komparatif aplikasi kebugaran Android.
Pengembangan Aplikasi Bergerak Android
Android merupakan sistem operasi bergerak berbasis Linux yang dikembangkan oleh Google untuk perangkat bergerak seperti smartphone dan tablet (Android Developers, 2024). Platform Android menyediakan ekosistem pengembangan yang komprehensif dengan dukungan untuk berbagai bahasa pemrograman dan framework pengembangan aplikasi.
Sistem operasi Android memiliki arsitektur berlapis yang terdiri dari Linux kernel, hardware abstraction layer (HAL), Android Runtime (ART), framework API, dan lapisan aplikasi (Android Developers, 2024). Arsitektur ini memungkinkan pengembang untuk mengakses fitur perangkat keras melalui API yang konsisten sambil tetap mempertahankan portabilitas lintas perangkat. Linux kernel bertindak sebagai jembatan antara perangkat keras dan lapisan perangkat lunak lainnya, menyediakan fitur keamanan dasar dan manajemen sumber daya (Android Source, 2024).
Android Studio adalah Integrated Development Environment (IDE) resmi untuk pengembangan aplikasi Android yang dikembangkan oleh Google (Android Developers, 2024). IDE ini menyediakan berbagai fitur untuk mendukung siklus pengembangan aplikasi, termasuk editor kode, debugger, emulator, dan alat profiling kinerja.
Kotlin adalah bahasa pemrograman yang dikembangkan oleh JetBrains dan telah menjadi bahasa resmi untuk pengembangan Android sejak 2017 (Kotlin Foundation, 2024). Kotlin menawarkan sintaks yang lebih ringkas dibanding Java sambil tetap mempertahankan interoperabilitas penuh dengan kode Java yang ada. Bahasa ini mendukung pemrograman fungsional dan berorientasi objek, serta menyediakan fitur null safety untuk mengurangi kesalahan runtime (Kotlin Documentation, 2024). 
Jetpack Compose adalah toolkit modern untuk membangun antarmuka pengguna (User Interface) Android menggunakan pendekatan deklaratif (Android Developers, 2024). Compose memungkinkan pengembang untuk mendeskripsikan UI sebagai fungsi dari state aplikasi, membuat proses pengembangan UI lebih intuitif dan efisien.
Metrik Kinerja Aplikasi Bergerak
Dalam konteks evaluasi kinerja aplikasi pose estimation pada perangkat mobile, beberapa metrik kinerja utama perlu diukur untuk memberikan gambaran komprehensif tentang trade-off yang terjadi.
Latensi inferensi diukur dalam milidetik (ms) dan merepresentasikan waktu yang dibutuhkan untuk memproses satu frame masukan dari pre-processing hingga post-processing (Google AI Edge, 2024). Untuk aplikasi estimasi pose waktu nyata, target latensi maksimal adalah 33ms (30 FPS) untuk memberikan pengalaman yang mulus, dengan target optimal di bawah 20ms untuk gerakan yang cepat. Latensi inferensi diukur menggunakan System.nanoTime() di Java/Kotlin atau TensorFlow Lite Benchmark Tool.
Throughput diukur dalam frames per second (FPS) dan menunjukkan jumlah frame yang dapat diproses per detik (Google AI Edge, 2024). Target minimum untuk aplikasi kebugaran waktu nyata adalah 20 FPS untuk gerakan dasar dan 30 FPS untuk gerakan dinamis seperti latihan kardiovaaskular. Throughput = 1000 / latensi_rata-rata (ms).
Utilisasi CPU diukur dalam persentase penggunaan total CPU selama eksekusi aplikasi dan merupakan indikator penting efisiensi komputasi (Android Developers, 2024). Metrik ini penting untuk menilai dampak terhadap kinerja aplikasi lain yang berjalan bersamaan. Utilisasi CPU yang tinggi (>80%) dapat menyebabkan thermal throttling dan mengurangi masa pakai baterai. P engukuran dilakukan menggunakan Android Profiler API atau /proc/stat system file.
Penggunaan memori (memory usage) mencakup konsumsi RAM untuk menyimpan data aplikasi, struktur data model, dan buffer untuk pemrosesan gambar (Android Developers, 2024). Perangkat bergerak umumnya memiliki keterbatasan memori, sehingga penggunaan memori yang efisien (<100MB untuk aplikasi estimasi pose) penting untuk mencegah kesalahan kehabisan memori dan menjaga stabilitas sistem. Android Runtime (ART) menggunakan garbage collection yang dioptimalkan untuk mengelola memori secara efisien pada perangkat dengan RAM terbatas (Android Source, 2024). Peak memory usage diukur menggunakan Android Memory Profiler atau Debug.MemoryInfo API.
Konsumsi baterai diukur dalam miliampere-hour (mAh) atau milliwatt (mW) dan merepresentasikan jumlah energi yang dikonsumsi aplikasi selama operasi (Android Developers, 2024). Metrik ini kritis untuk aplikasi bergerak karena baterai merupakan sumber daya terbatas yang mempengaruhi pengalaman pengguna secara keseluruhan. Faktor-faktor yang mempengaruhi konsumsi baterai meliputi intensitas penggunaan CPU, aktivitas GPU, penggunaan sensor, dan transmisi data jaringan. Optimasi konsumsi baterai dapat dicapai melalui manajemen thread yang efisien, pengurangan frekuensi pemrosesan, dan implementasi power-aware scheduling. Pengukuran menggunakan Android Battery Historian atau BatteryManager API.
Faktor-faktor yang mempengaruhi konsumsi baterai meliputi intensitas penggunaan CPU, aktivitas GPU, penggunaan sensor, dan transmisi data jaringan (Android Developers, 2024). Optimasi konsumsi baterai dapat dicapai melalui manajemen thread yang efisien, pengurangan frekuensi pemrosesan, dan implementasi power-aware scheduling.

Analisis Varians (ANOVA)
Menurut Montgomery (2017), ANOVA (Analysis of Variance) digunakan untuk menguji apakah terdapat perbedaan yang signifikan antara rata-rata tiga kelompok atau lebih. Keppel dan Wickens (2004) dalam "Design and Analysis: A Researcher's Handbook"  mengklasifikasikan ANOVA berdasarkan jumlah faktor yang terlibat, yaitu One-Way ANOVA untuk satu faktor independen dan Two-Way ANOVA untuk dua atau lebih faktor independen. 
Pada penelitian ini, Three-Way Repeated-Measures ANOVA digunakan untuk menguji apakah terdapat perbedaan yang signifikan antara performa inferensi berbagai delegate TensorFlow Lite (CPU, GPU, XNNPACK) berdasarkan metrik kinerja seperti latensi, throughput, utilisasi CPU, penggunaan memori, dan konsumsi baterai, dengan mempertimbangkan faktor model (MoveNet Lightning vs BlazePose Lite) dan jenis latihan (squat vs push-up). Rumus dasar uji ANOVA satu arah adalah sebagai berikut:
F =  (MS_between)/(MS _within )
MS_(between )=  (SS_between)/(df _between )   
MS_within  =  (SS_within)/(df _within )

 Keterangan:

MS_(between ) = Sum of Squares Between Groups
 SS_within = Sum of Squares Within Groups
df _between = degree of freedom between groups (k - 1)
 df _within = degree of freedom within groups (N - k)
 k = jumlah kelompok perlakuan
 N = jumlah total observasi
Nilai F kemudian dibandingkan dengan nilai F_tabel pada tingkat signifikansi tertentu (misalnya ╬▒ = 0,05). Jika F_hitung > F_tabel, maka terdapat perbedaan yang signifikan antar kelompok.
Uji Shapiro-Wilk merupakan uji statistik untuk menguji normalitas distribusi data dengan menghitung statistik W yang membandingkan data sampel dengan distribusi normal teoretis (Shapiro & Wilk, 1965). Nilai p < 0,05 mengindikasikan bahwa data tidak terdistribusi normal, sehingga diperlukan transformasi data atau penggunaan uji nonparametrik. Uji Shapiro-Wilk dipilih karena memiliki power yang tinggi untuk mendeteksi penyimpangan dari normalitas, terutama untuk ukuran sampel kecil hingga menengah (n < 50) (Field, 2018).
METODOLOGI
Jenis Penelitian
Penelitian ini merupakan penelitian kuantitatif dengan pendekatan eksperimental yang bersifat analitik-komparatif. Tujuannya adalah untuk membandingkan performa berbagai konfigurasi delegate TensorFlow Lite (GPU, XNNPACK, CPU) pada dua model pose estimation (MoveNet Lightning dan BlazePose Lite) untuk aplikasi estimasi pose real-time di perangkat bergerak. Aplikasi yang dikembangkan tidak dimaksudkan sebagai produk akhir, melainkan berfungsi sebagai test harness atau alat ukur untuk mengumpulkan data performa dan akurasi secara objektif. Pendekatan eksperimen terkontrol digunakan agar setiap variabel dapat diuji secara sistematis dengan kondisi yang konsisten, sehingga hasil penelitian dapat direplikasi dan divalidasi.
Strategi penelitian ini menggunakan desain faktorial penuh (full factorial design) untuk menguji pengaruh tiga variabel bebas: jenis delegate TensorFlow Lite, varian model MoveNet, dan jenis latihan yang akan dilakukan. Dengan menggabungkan seluruh kombinasi dari ketiga faktor tersebut, diperoleh rancangan eksperimen 3├ù2├ù2 yang menghasilkan 12 kondisi pengujian, dengan setiap kondisi dieksekusi 30 kali untuk memastikan kekuatan statistik yang memadai (total 360 eksperimen). Pengulangan sebanyak 30 kali dipilih berdasarkan central limit theorem yang menyatakan bahwa distribusi samping means akan mendekati normal untuk n >= 30, memungkinkan penggunaan uji statistik parametrik (Montgomery, 2017). Power analysis a priori menggunakan G*Power 3.1 dengan ╬▒=0.05, power (1-╬▓)=0.80, dan medium effect size (f=0.25) menunjukkan minimum sample size n=24 untuk three-way ANOVA. Dengan demikian, n=30 memberikan adequate statistical power dengan buffer untuk outlier removal.
Desain Eksperimen
Variabel bebas yang digunakan dalam penelitian ini mencakup empat jenis delegate TensorFlow Lite, yaitu GPU Delegate, XNNPACK Delegate, dan CPU-only execution. Selain itu, digunakan varian model MoveNet, yakni Lightning (resolusi input 192├ù192 piksel), dan BlazePose Lite (Detector-tracker approach, 33 keypoints) serta dua jenis latihan, yaitu squat dan push-up, yang dipilih untuk merepresentasikan pola gerakan berbeda. Variabel terikat terdiri dari metrik performa (latensi inferensi, throughput, utilisasi CPU, penggunaan memori, dan konsumsi daya) serta metrik akurasi fungsional (Ketepatan Penghitungan Repetisi). Variabel kontrol meliputi perangkat yang digunakan, kondisi lingkungan, konfigurasi sistem, serta resolusi dan durasi video input agar setiap eksperimen berjalan dalam keadaan yang seragam.
Berdasarkan rancangan ini, hipotesis penelitian diformulasikan sebagai berikut. Pertama, terdapat perbedaan signifikan dalam latensi inferensi antar delegate, dengan dugaan bahwa GPU Delegate memberikan latensi terendah, khususnya untuk model MoveNet Thunder.Ketiga, tidak terdapat perbedaan akurasi yang berarti antar delegate untuk model yang sama, menandakan bahwa optimasi delegate tidak memengaruhi hasil inferensi model. Keempat, performa relatif antar delegate diperkirakan konsisten pada kedua jenis latihan, yang menunjukkan generalisasi hasil penelitian terhadap berbagai pola gerakan.
Tabel 3.1 Perancangan Eksperimen
Faktor	Level	Keterangan
Delegate	3	GPU, XNNPACK, CPU
Model	2	MoveNet Lightning, BlazePose Lite
Latihan	2	Squat, Push-up
Total kombinasi = 3 delegate ├ù 2 model ├ù 2 latihan = 12 kondisi 
Total runs = 12 kondisi ├ù 30 replikasi = 360 eksperimen.
Tahapan Penelitian
Pelaksanaan penelitian ini dilakukan melalui beberapa tahap utama, yaitu tahap persiapan, pengembangan aplikasi uji, pengumpulan data, dan analisis hasil.
 
Pada tahap persiapan, dilakukan pemilihan perangkat keras dan perangkat lunak yang digunakan dalam eksperimen. Pengujian dilakukan menggunakan dua kategori perangkat, perangkat Low-range device, dan perangkat mid-range device. Untuk Low-range device menggunakan perangkat Samsung Galaxy A06 dengan prosesor Mediatek Helio G85, Ram 4 GB, dan sistem operasin android 14. Sedangkat mid-range device menggunakan perangkat Samsung Galaxy A33 dengan prosesor Exynos 1280, RAM 8 GB, dan sistem operasi Android 14. Kedua perangkat dipilih karena mendukung semua jenis delegate TensorFlow Lite yang diuji dan mewakili kategori yang umum digunakan pengguna Indonesia. Lingkungan pengujian dijaga konstan dengan suhu 22ΓÇô24┬░C dan pencahayaan sekitar 500 lux. Aplikasi uji dikembangkan menggunakan Android Studio Iguana (2024.1.1) dengan bahasa Kotlin dan framework TensorFlow Lite versi 2.15.0.
Tahap pengembangan aplikasi dilakukan dengan membangun sistem benchmarking yang berfungsi menjalankan kedua model pose estimation dalam berbagai konfigurasi delegate serta mencatat metrik performa secara otomatis. Aplikasi dirancang dengan arsitektur modular yang terdiri dari tiga lapisan: presentation layer untuk antarmuka dan kontrol pengujian, domain layer untuk logika perhitungan metrik dan validasi data, serta data layer untuk proses inferensi dan pencatatan hasil. Proses inferensi dijalankan menggunakan API TensorFlow Lite, sedangkan data metrik diperoleh melalui Android Profiler API dan TensorFlow Benchmark Tool.
Tahap pengumpulan data dilakukan dengan menjalankan dua skenario latihan, yaitu squat dan push-up, menggunakan video masukan beresolusi 1080p dengan 30 FPS. Sebelum setiap sesi pengujian, perangkat di-restart untuk menghapus proses latar, dan dilakukan fase idle profiling selama 30 detik untuk mencatat baseline performa. Setiap konfigurasi delegate diuji secara bergantian dengan jeda lima menit untuk mencegah peningkatan suhu berlebih. Selama eksperimen, data performa dicatat secara real-time meliputi latensi inferensi, FPS, utilisasi CPU, memori, dan daya.
Tahap analisis data diawali dengan proses pembersihan dan normalisasi data. Nilai outlier diidentifikasi menggunakan metode modified Z-score dengan ambang batas 3,5, sedangkan data yang hilang diinterpolasi secara linier. Selanjutnya, dilakukan normalisasi minΓÇômaks untuk penyamaan skala antar metrik dan transformasi logaritmik pada distribusi yang tidak normal. Setelah data siap, dilakukan analisis deskriptif untuk melihat kecenderungan umum dan sebaran data, dilanjutkan dengan analisis inferensial menggunakan ANOVA tiga arah dengan pengukuran berulang (three-way repeated-measures ANOVA) guna menguji pengaruh faktor delegate, model, dan jenis latihan beserta interaksinya.  Pemilihan desain repeated-measures ANOVA dibandingkan ANOVA independen dilakukan karena seluruh kondisi diuji pada perangkat yang sama, sehingga antar pengukuran bersifat dependen.
 Dengan demikian, pendekatan ini dapat mengurangi error variansi antar subjek dan meningkatkan sensitivitas pengujian statistik. Apabila ditemukan perbedaan signifikan, uji lanjut Tukey HSD dilakukan untuk mengetahui pasangan kelompok yang berbeda nyata, dengan tingkat signifikansi 5%. Selain itu, dilakukan analisis korelasi Pearson atau Spearman untuk menilai hubungan antar metrik performa seperti latensi, FPS, dan konsumsi daya.
Metodologi Pengujian dan Analisis Statistik
Penelitian ini menggunakan metodologi eksperimen kuantitatif dengan desain faktorial dimana variabel bebas adalah jenis delegate (3 level: GPU, XNNPACK, CPU) dan dua varian model yaitu Movenet Lightning dan BlazePose lite(Montgomery, 2017). Desain faktorial memungkinkan analisis efek interaksi antara variabel bebas terhadap variabel terikat.
Eksperimen terkendali dilakukan dengan mengontrol variabel eksternal seperti suhu perangkat, proses latar belakang, level baterai, dan kondisi pencahayaan (Montgomery, 2017). Setiap kombinasi delegate-model diuji dengan beberapa kali pengulangan (minimal 30 iterasi) untuk memastikan validitas statistik.
Pengujian white box dilakukan dengan menganalisis struktur internal aplikasi dan algoritme inferensi untuk memahami perilaku sistem secara detail (Myers dkk., 2011). Pendekatan ini melibatkan profiling kode, analisis call stack, dan pengukuran kinerja pada tingkat fungsi individual.
Pengujian black box fokus pada evaluasi kinerja sistem dari perspektif pengguna akhir tanpa mempertimbangkan implementasi internal (Myers dkk., 2011). Metode ini mengukur metrik kinerja keseluruhan seperti latensi end-to-end, akurasi deteksi, dan responsivitas antarmuka pengguna.
Analysis of Variance (ANOVA) digunakan untuk menguji signifikansi perbedaan rata-rata antar kelompok perlakuan (Field, 2018). Untuk penelitian ini, digunakan ANOVA dua arah untuk menganalisis efek utama dari jenis delegate dan varian model, serta efek interaksi antara keduanya.
Asumsi ANOVA meliputi normalitas distribusi data, homogenitas varians, dan independensi observasi (Field, 2018). Uji Levene digunakan untuk menguji homogenitas varians, sementara uji Shapiro-Wilk digunakan untuk menguji normalitas distribusi data. Jika asumsi tidak terpenuhi, alternatif nonparametrik seperti uji Kruskal-Wallis akan digunakan.
Uji Shapiro-Wilk merupakan uji statistik untuk menguji normalitas distribusi data dengan menghitung statistik W yang membandingkan data sampel dengan distribusi normal teoretis (Shapiro & Wilk, 1965). Nilai p < 0,05 mengindikasikan bahwa data tidak terdistribusi normal, sehingga diperlukan transformasi data atau penggunaan uji nonparametrik.
Uji Shapiro-Wilk dipilih karena memiliki power yang tinggi untuk mendeteksi penyimpangan dari normalitas, terutama untuk ukuran sampel kecil hingga menengah (n < 50) (Field, 2018). Uji ini lebih sensitif dibanding uji normalitas lain seperti Kolmogorov-Smirnov, sehingga memberikan hasil yang lebih akurat untuk pengambilan keputusan statistik.
Variabel Penelitian
Penelitian ini menggunakan desain eksperiman faktorial untuk menganalisis pengaruh konfigurasi akselerasi perangkat keras terhadap kinerja komputasi aplikasi pose estimation. Variabel penelitian diklasifikasikan sebagai berikut: 
	Variabel Bebas
	Jenis Delegate: Terdiri dari tiga level metode akselerasi, yaitu GPU Delegate (akselerasi grafis), dan CPU XNNPACK (eksekusi standar teroptimasi).
	Varian Model: Terdiri dari dua level yang merepresentasikan arsitektur berbeda, yaitu MoveNet Lightning (INT8) yang berbasis pendekatan heatmap ringan, dan MediaPipe BlazePose Lite (FP16) yang berbasis pendekatan detector-tracker topologi 33 keypoints.
	Kategori Perangkat: Dibagi menjadi dua level untuk merepresentasikan segmentasi pasar, yaitu Low-End (Samsung Galaxy A06/A05) dan Mid-Range (Samsung Galaxy A33 5G).
	Variabel Terikat
	Latensi Inferensi: Waktu rata-rata yang dibutuhkan untuk memproses satu frame (satuan: milidetik/ms).
	Throughput: Jumlah frame yang dapat diproses per detik (satuan: FPS).
	Utilisasi Sumber Daya: Persentase penggunaan CPU (CPU Load) dan konsumsi memori dinamis (RAM dalam MB).
	Konsumsi Daya: Estimasi energi yang digunakan selama proses inferensi (satuan: miliwatt/mW)
	Variabel Kontrol
	Input Video: Resolusi video masukan dikunci pada 1080p dengan framerate asli 30 FPS.
	Kondisi Lingkungan: Suhu ruang dijaga stabil pada rentang 24ΓÇô26┬░C untuk mencegah thermal throttling eksternal.
	Kondisi Sistem: Mode pesawat (Airplane Mode) diaktifkan, kecerahan layar diatur pada 50%, dan seluruh aplikasi latar belakang (background processes) dimatikan sebelum pengujian. Baterai perangkat dipastikan berada pada level >50% dan tidak dalam kondisi pengisian daya (not charging) saat pengambilan data.
Metode Eksperimen
Penelitian ini menggunakan pendekatan within-subject repeated-measures design, di mana setiap kombinasi delegate, model, dan jenis latihan diuji pada perangkat yang sama dengan kondisi yang dikontrol secara ketat. Desain faktorial 3├ù2├ù2 menghasilkan 12 kondisi eksperimental, dan masing-masing kondisi dijalankan sebanyak 30 kali untuk memperoleh kekuatan statistik yang memadai. Sebelum eksperimen dimulai, perangkat di-restart untuk menstabilkan suhu dan menghapus proses latar, kemudian dilakukan verifikasi daya baterai dan pencahayaan ruangan agar sesuai dengan standar pengujian.
Pelaksanaan eksperimen dilakukan melalui dua jenis latihan, yaitu squat dan push-up, yang dilakukan dengan tempo terstandar untuk menjaga konsistensi gerakan. Setiap sesi squat terdiri atas fase turun selama tiga detik, tahan satu detik, naik tiga detik, dan istirahat dua detik, sedangkan sesi push-up memiliki pola dua detik turun, satu detik tahan, dua detik naik, dan dua detik istirahat. Data dikumpulkan secara real-time menggunakan Android Profiler API untuk mencatat latensi inferensi, penggunaan CPU, konsumsi memori, serta konsumsi daya. Untuk menghindari bias akibat urutan pengujian, dilakukan proses counterbalancing pada urutan pengujian setiap delegate sehingga efek learning dan akumulasi panas dapat diminimalkan. Seluruh instrumen juga dikalibrasi, dan hasil pengamatan yang menyimpang diidentifikasi menggunakan modified Z-score dengan ambang 3,5 sebagai batas deteksi outlier.
Statistik yang Digunakan
Analisis data dilakukan secara kuantitatif menggunakan gabungan metode statistik deskriptif dan inferensial. Analisis deskriptif mencakup penghitungan nilai rata-rata, median, standar deviasi, varians, serta bentuk distribusi data seperti skewness dan kurtosis. Untuk setiap metrik performa, interval kepercayaan 95% dihitung menggunakan distribusi-t pada data berdistribusi normal, sedangkan data yang tidak normal diestimasi menggunakan metode bootstrap resampling.
Uji normalitas dilakukan dengan uji ShapiroΓÇôWilk pada taraf signifikansi 0,05 untuk memastikan bahwa data memenuhi asumsi parametrik. Apabila data tidak berdistribusi normal, dilakukan transformasi logaritmik atau akar kuadrat, dan jika masih belum memenuhi asumsi, digunakan alternatif non-parametrik. Homogenitas varians diuji dengan uji Levene atau Brown-Forsythe jika data tidak normal. Untuk menguji pengaruh dari faktor-faktor penelitian, digunakan three-way repeated-measures ANOVA guna menganalisis efek utama dari jenis delegate, varian model, dan jenis latihan, serta interaksi di antara ketiganya. Apabila uji ANOVA menunjukkan hasil yang signifikan, maka dilakukan uji lanjut Tukey HSD untuk membandingkan pasangan kelompok secara berpasangan dengan pengendalian tingkat kesalahan keluarga (family-wise error rate) pada ╬▒ = 0,05. Ukuran efek dihitung menggunakan partial eta-squared (╬╖┬▓p) untuk menilai signifikansi praktis, dengan interpretasi efek kecil (╬╖┬▓p ΓëÑ 0,01), sedang (ΓëÑ 0,06), dan besar (ΓëÑ 0,14). Apabila asumsi parametrik tidak terpenuhi, maka analisis digantikan dengan uji Friedman atau Kruskal-Wallis.
  

PERANCANGAN SISTEM
Bab ini membahas perancangan perangkat lunak yang dibangun sebagai alat ukur (test harness) untuk eksperimen. Perancangan meliputi analisis kebutuhan, arsitektur sistem, algoritma inferensi, dan desain antarmuka pengguna. 
Analisis Kebutuhan Sistem
Tahap ini mendefinisikan spesifikasi kebutuhan aplikasi benchmarking untuk memastikan alat ukur dapat mengambil data eksperimen secara valid sesuai variabel penelitian. Sistem harus mampu mengeksekusi eksperimen dengan repeatability tinggi dan bias minimal untuk menghasilkan data valid secara statistik.
Kebutuhan Fungsional
Kebutuhan fungsional mendefinisikan kemampuan-kemampuan yang harus dimiliki aplikasi benchmarking. Aplikasi harus mampu memuat model pose estimation (MoveNet Lightning dan BlazePose Lite) dari folder asset ke memori dan menginisialisasi interpreter dengan konfigurasi delegate yang dipilih.
Tabel 4.1 Kebutuhan Fungsional Aplikasi Benchmarking
Kategori	Fungsi Utama
Manajemen Delegate	Sistem harus mampu mengubah konfigurasi akselerasi perangkat keras (delegate) yang meliputi CPU, XNNPACK, dan GPU secara dinamis (runtime) tanpa memerlukan kompilasi ulang aplikasi.
Manajemen Model	Sistem harus memfasilitasi pemuatan dan eksekusi inferensi menggunakan dua varian model yang diuji: MoveNet Lightning (INT8) dan BlazePose Lite (FP16).
Pengukuran Latensi Real-Time	Sistem harus mampu menghitung waktu eksekusi inferensi (inference time) dalam satuan milidetik (ms) untuk setiap frame yang diproses.
Perhitungan Repetisi	Sistem harus memiliki logika geometri untuk mendeteksi gerakan squat dan push-up berdasarkan koordinat keypoints tubuh manusia.
Pencatatan Data Otomatis	Sistem harus dapat menyimpan data metrik performa (Latensi, FPS, CPU Load) ke dalam format log (CSV) untuk keperluan analisis statistik lebih lanjut.

Kebutuhan Non-Fungsional
	Kompatibilitas: Aplikasi harus dapat berjalan pada sistem operasi Android dengan target SDK API Level 34 (Android 14) namun tetap kompatibel dengan perangkat legacy.
	Framework: Sistem dikembangkan menggunakan library TensorFlow Lite versi 2.15.0.
	Efisiensi UI: Beban rendering antarmuka tidak boleh mengganggu pengukuran kinerja backend TFLite.
	Stabilitas: Aplikasi harus menangani pengecualian (exception handling) saat inisialisasi GPU gagal pada perangkat yang tidak kompatibel.
Perancangan Arsitektur Sistem
Arsitektur aplikasi mengikuti pendekatan berlapis untuk memisahkan concerns antara presentasi, logika bisnis, dan akses data. Pemisahan ini memudahkan testing, maintenance, dan perubahan teknologi tanpa mempengaruhi lapisan lain.
Diagram Arsitektur (3-Layer Architecture) 
Sistem menerapkan arsitektur bertingkat (multi-layer) dengan pola MVVM (Model-View-ViewModel) untuk menjaga reaktivitas antarmuka terhadap perubahan data metrik performa secara real-time.
 
Gambar  4.1 Diagram Arsitektur Sistem


Arsitektur ini menerapkan pola MVVM (Model-View-ViewModel) untuk menjaga reaktivitas antarmuka terhadap perubahan data metrik performa secara real-time.
	Presentation Layer: Bertanggung jawab menampilkan preview kamera dan overlay hasil deteksi pose kepada pengguna.
	Domain Layer: Berisi logika inti, termasuk algoritma penghitungan repetisi dan orkestrasi proses benchmarking.
	Data Layer: Menangani interaksi langsung dengan API TensorFlow Lite, inisialisasi delegate, dan alat profiling.
Diagram Alir Data (Data Flow) Diagram berikut menjelaskan aliran data citra dari kamera hingga menjadi data metrik.
 
Gambar  4.2 Diagram Alir Data Inferensi

Perancangan Algoritma (Flowchart)
Bagian ini menjelaskan logika prosedural untuk proses inferensi dan validasi fungsional (penghitungan repetisi).
Flowchart Alur Inferensi. Proses inferensi dirancang untuk mengukur latensi secara presisi menggunakan penanda waktu (timestamp).
 
Gambar  4.3 Flowchart Logika Inferensi
Flowchart Penghitungan Repetisi Logika penghitungan repetisi menggunakan state machine sederhana berdasarkan sudut sendi untuk gerakan Squat dan Push-up.
 
Gambar  4.4 Flowchart Perhitungan Repetisi

Pseudocode Pengukuran Latensi. Pengukuran latensi dilakukan pada level kode menggunakan fungsi waktu sistem beresolusi tinggi.
FUNCTION RunInference(inputBitmap):
    // Warm-up (opsional, untuk 5 frame awal)
    IF frameCount < 5 THEN
        PerformWarmUp()
    
    // Ambil waktu sebelum inferensi
    startTime = System.nanoTime()
    
    // Jalankan TFLite Interpreter
    results = Interpreter.run(inputBitmap)
    
    // Ambil waktu setelah inferensi selesai
    endTime = System.nanoTime()
    
    // Hitung selisih dan konversi ke milidetik
    latencyNano = endTime - startTime
    latencyMs = latencyNano / 1,000,000
    
    RETURN results, latencyMs
END FUNCTION

Perancangan Antarmuka Pengguna (UI Design)
Antarmuka dirancang minimalis menggunakan Jetpack Compose untuk mengurangi overhead grafis.
Perancangan Antarmuka (UI)
Aplikasi benchmarking ini dikembangkan menggunakan Jetpack Compose dengan desain antarmuka minimalis untuk meminimalkan beban rendering UI terhadap pengukuran performa. Antarmuka utama terdiri dari:
	Rancangan Layar Konfigurasi
 
Gambar  4.5 Wireframe Layar Monitoring

	Rancangan Layar Monitoring Real-Time
 
Gambar  4.6 Wireframe Layar Monitoring

	Layar Hasil Eksperimen
 
Gambar  4.7 Wireframe Layar Hasil

Perancangan Sistem Pengukuran Metrik
Subbab ini menjelaskan strategi teknis yang digunakan untuk mengumpulkan data variabel terikat dengan akurasi dan reliabilitas tinggi. Sistem pengukuran merupakan komponen krusial yang menentukan validitas hasil penelitian.
Strategi Pengukuran Latensi
Pengukuran latensi inferensi menggunakan fungsi System.nanoTime() pada bahasa Kotlin yang menyediakan presisi tingkat nanodetik (10^(-9) detik). Pemilihan instrumen ini didasarkan pada tiga pertimbangan utama:
	Presisi Tinggi: Mampu mendeteksi perbedaan waktu yang sangat kecil (di bawah 1 milidetik)
	Sifat Monotonic Clock: Tidak terpengaruh oleh perubahan waktu sistem atau sinkronisasi jaringan.
	Overhead Rendah: Memiliki overhead eksekusi sekitar 50 nanodetik yang dapat diabaikan (negligible) dibandingkan latensi inferensi model (20-100 milidetik).
Titik pengambilan waktu (timing point) dilakukan tepat sebelum pemanggilan fungsi interpreter.run() dan segera setelah fungsi tersebut selesai. Selisih kedua penanda waktu (timestamp) kemudian dikonversi ke satuan milidetik. Pengukuran dilakukan pada tiga tingkatan: latensi inferensi murni (waktu eksekusi model), latensi pra-pemrosesan (preprocessing), dan latensi total (end-to-end).
Untuk menjaga validitas data, protokol pemanasan (warm-up) diterapkan dengan menjalankan 5 frame awal tanpa pencatatan data. Langkah ini memastikan GPU mencapai kecepatan clock optimal dan menghindari pencilan (outlier) akibat efek inisialisasi cache yang seringkali menyebabkan inferensi awal berjalan 2-3 kali lebih lambat.
Validasi akurasi pengukuran dilakukan melalui uji silang (cross-check) dengan TensorFlow Lite Benchmark Tool (utilitas pengujian resmi dari Google). Kriteria penerimaan (acceptance criteria) adalah selisih rata-rata latensi kurang dari 5% dibandingkan nilai referensi alat standar tersebut.
4.5.2 Strategi Profiling Sumber Daya
Pengukuran sumber daya komputasi dilakukan untuk memahami efisiensi setiap delegate dalam memanfaatkan perangkat keras
	Penggunaan CPU: Diukur menggunakan Android Profiler API dengan membaca berkas sistem /proc/stat yang menyediakan waktu CPU kumulatif dalam satuan jiffy. Interval pengambilan data (polling) ditetapkan setiap 100 milidetik untuk menyeimbangkan antara resolusi temporal dan beban sistem. Persentase penggunaan CPU dihitung menggunakan formula:
CPU Usage (%)=  (Total Time-Idle Time)/(Total Time)  x 100%
	Penggunaan Memori: Diukur menggunakan Debug.MemoryInfo yang menyediakan rincian memori heap (objek Java/Kotlin), memori native (bobot model TFLite dan buffer), serta total Proportional Set Size (PSS). Pelacakan memori puncak (peak memory tracking) dilakukan dengan mencatat nilai maksimum dari setiap komponen selama eksperimen berlangsung untuk mendeteksi potensi kebocoran memori (memory leaks).
	Konsumsi Daya: Estimasi daya menggunakan BatteryManager API. Laju pengurasan baterai (drain rate) dihitung dari selisih penghitung energi (energy counter) dibagi dengan durasi pengujian untuk mendapatkan rata-rata konsumsi daya dalam satuan miliwatt (mW).
4.5.3 Validasi Alat Ukur Fungsional
Validasi fungsional bertujuan memastikan logika deteksi repetisi berjalan benar dan konsisten meskipun terjadi fluktuasi framerate atau variasi akurasi deteksi pose. Prosedur validasi melibatkan tahapan berikut:
	Perekaman Sampel: Merekam video latihan dengan jumlah repetisi yang diketahui dan terkontrol (misalnya 30 squat dengan tempo konsisten).
	Eksekusi Otomatis: Menjalankan sistem deteksi pada video tersebut menggunakan model dan delegate target.
	Verifikasi Manual: Membandingkan hasil hitungan sistem dengan hitungan manual (ground truth) yang dilakukan oleh pengamat.
	Perhitungan Akurasi: Menghitung persentase akurasi dengan rumus:
Akurasi (%)=(Repetisi Terdeteksi)/(Repetisi Sebenarnya)  x 100%

Target akurasi minimum ditetapkan sebesar 90% untuk menjamin keandalan sistem dalam kondisi nyata yang memiliki variabilitas gerakan. Validasi dilakukan pada subset video yang merepresentasikan berbagai kondisi, seperti variasi kecepatan gerakan (slow vs fast) dan kondisi pencahayaan yang berbeda.

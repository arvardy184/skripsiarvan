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
PENGESAHAN
JUDUL SKRIPSI 

SKRIPSI 

Diajukan untuk memenuhi sebagian persyaratan 
memperoleh gelar Sarjana Komputer 

Disusun Oleh : 
Nama Mahasiswa
NIM: 123456789

Skripsi ini telah diuji dan dinyatakan lulus pada
2 Januari 2015
Telah diperiksa dan disetujui oleh:


Dosen Pembimbing I





Nama Dosen Pembimbing 1
NIK: 123456789 
/*jika terdapat NIK saja*/ 
Dosen Pembimbing 2





Nama Dosen Pembimbing 2
NIK: - 
/*jika tidak terdapat NIP, NIK, atau keduanya*/

Mengetahui
Ketua Jurusan Nama Jurusan





Nama Ketua Jurusan
NIP: 123456789
/*jika terdapat NIP*/

PERNYATAAN ORISINALITAS
Saya menyatakan dengan sebenar-benarnya bahwa sepanjang pengetahuan saya, di  dalam naskah skripsi ini tidak terdapat karya ilmiah yang pernah diajukan oleh orang lain untuk memperoleh gelar akademik di suatu perguruan  tinggi, dan tidak terdapat karya atau pendapat yang pernah ditulis atau diterbitkan oleh orang lain, kecuali yang secara tertulis disitasi dalam naskah ini dan disebutkan dalam daftar referensi. 
Apabila ternyata didalam naskah skripsi ini dapat dibuktikan terdapat unsur-unsur plagiasi, saya bersedia skripsi ini digugurkan dan gelar akademik yang telah saya peroleh (sarjana) dibatalkan, serta diproses sesuai dengan peraturan perundang-undangan yang berlaku (UU No. 20 Tahun 2003, Pasal 25 ayat 2 dan Pasal 70). 

Malang, 1 Januari 2015
 

    
¬ 
Nama Mahasiswa
NIM: 123456789
PRAKATA
Bagian ini memuat pernyataan resmi untuk menyampaikan rasa terima kasih penulis kepada berbagai pihak yang telah membantu penyelesaian skripsi ini. Nama-nama penerima ucapan terima kasih sebaiknya dituliskan lengkap, termasuk gelar akademik, dan pihak-pihak yang tidak terkait dihindari untuk dituliskan. Bahasa yang digunakan seharusnya mengikuti kaidah bahasa Indonesia yang baku. Prakata boleh diakhiri dengan paragraf yang menyatakan bahwa penulis menerima kritik dan saran untuk pengembangan penelitian selanjutnya. Terakhir, prakata ditutup dengan mencantumkan kota dan tanggal penulisan prakata, lalu diikuti dengan kata “Penulis”.

Malang, 1 Januari 2026

Penulis
email@domain.com
ABSTRAK
Nama Mahasiswa, Judul Skripsi
Pembimbing: Nama Pembimbing 1 dan Nama Pembimbing 2
Bagian ini diisi dengan abstrak dalam Bahasa Indonesia. Abstrak adalah uraian singkat (umumnya 200-300 kata) yang merupakan intisari dari sebuah skripsi. Abstrak membantu pembaca untuk mendapatkan gambaran secara cepat dan akurat tentang isi dari sebuah skripsi. Melalui abstrak, pembaca juga dapat menentukan apakah akan membaca skripsi lebih lanjut. Oleh karena itu, abstrak sebaiknya memberikan gambaran yang padat tetapi tetap jelas dan akurat tentang (1) apa dan mengapa penelitian dikerjakan: sedikit latar belakang, pertanyaan atau masalah penelitan, dan/atau tujuan penelitian; (2) bagaimana penelitian dikerjakan: rancangan penelitian dan metodologi/metode dasar yang digunakan dalam penelitian; (3) hasil penting yang diperoleh: temuan utama, karakteristik artefak, atau hasil evaluasi artefak yang dibangun; (4) hasil pembahasan dan kesimpulan: hasil dari analisis dan pembahasan temuan atau evaluasi artefak yang dibangun, yang dikaitkan dengan pertanyaan/tujuan penelitian.
Yang harus dihindari dalam sebuah abstrak diantaranya (1) penjelasan latar belakang yang terlalu panjang; (2) sitasi ke pustaka lainnya; (3) kalimat yang tidak lengkap; (3) singkatan, jargon, atau istilah yang membingungkan pembaca, kecuali telah dijelaskan dengan baik; (4) gambar atau tabel; (5) angka-angka yang terlalu banyak.
Di akhir abstrak ditampilkan beberapa kata kunci (normalnya 5-7) untuk membantu pembaca memposisikan isi skripsi dengan area studi dan masalah penelitian. Kata kunci, beserta judul, nama penulis, dan abstrak biasanya dimasukkan dalam basis data perpustakaan. Kata kunci juga dapat diindeks dalam basis data sehingga dapat digunakan untuk proses pencarian tulisan ilmiah yang relevan. Oleh karena itu pemilihan kata kunci yang sesuai dengan area penelitian dan masalah penelitian cukup penting. Pemilihan kata kunci juga bisa didapatkan dari referensi yang dirujuk. 

Kata kunci: abstrak, skripsi, intisari, kata kunci, artefak
ABSTRACT
Student Name, Skripsi Title
Supervisors: First Supervisor’s Name and Second Supervisor’s Name
The absract of your skripsi in English is written here. 
DAFTAR ISI
Table of Contents
ANALISIS KINERJA AKSELERASI PERANGKAT KERAS UNTUK INFERENSI POSE ESTIMATION REAL-TIME PADA APLIKASI KEBUGARAN ANDROID	i
PENGESAHAN	ii
PERNYATAAN ORISINALITAS	iii
PRAKATA	iv
ABSTRAK	v
ABSTRACT	vi
DAFTAR ISI	vii
DAFTAR TABEL	viii
DAFTAR GAMBAR	ix
DAFTAR LAMPIRAN	x
BAB 1 PENDAHULUAN	1
1.1 Latar Belakang	1
1.2 Rumusan Masalah	2
1.3 Tujuan	3
1.4 Manfaat	3
1.5 Batasan Masalah	4
1.6 Sistematika Pembahasan	5
BAB 2 LANDASAN KEPUSTAKAAN	6
2.1 Kajian Pustaka	6
2.1.1 Gap Penelitian	8
2.2 Landasan Teori	9
2.2.1 Pose Estimation	9
2.2.2 Tensorflow Lite	9
2.2.3 Model MoveNet	10
2.2.4 Model BlazePose Lite	11
2.2.5 Pengembangan Aplikasi Bergerak Android	12
2.2.6 Metrik Kinerja Aplikasi Bergerak	12
2.2.7 Analisis Varians (ANOVA)	14
BAB 3 METODOLOGI	16
3.1 Jenis Penelitian	16
3.2 Desain Eksperimen	16
3.3 Tahapan Penelitian	17
3.4 Metodologi Pengujian dan Analisis Statistik	19
3.5 Variabel Penelitian	20
3.6 Metode Eksperimen	21
3.7 Statistik yang Digunakan	22
BAB 4 PERANCANGAN SISTEM	23
4.1 Analisis Kebutuhan Sistem	23
4.1.1 Kebutuhan Fungsional	23
4.1.2 Kebutuhan Non-Fungsional	24
4.2 Perancangan Arsitektur Sistem	24
4.2.1 Diagram Arsitektur (3-Layer Architecture)	24
4.2.2 Diagram Alir Data (Data Flow) Diagram berikut menjelaskan aliran data citra dari kamera hingga menjadi data metrik.	25
4.2.3 Diagram Sekuens	25
4.3 Perancangan Algoritma (Flowchart)	25
4.3.1 Flowchart Alur Inferensi. Proses inferensi dirancang untuk mengukur latensi secara presisi menggunakan penanda waktu (timestamp).	26
4.3.2 Flowchart Penghitungan Repetisi Logika penghitungan repetisi menggunakan state machine sederhana berdasarkan sudut sendi untuk gerakan Squat dan Push-up.	26
4.3.3 Pseudocode Pengukuran Latensi. Pengukuran latensi dilakukan pada level kode menggunakan fungsi waktu sistem beresolusi tinggi.	26
4.3.4 Mekanisme Error Handling & Fallback	27
4.4 Perancangan Antarmuka Pengguna (UI Design)	27
4.4.1 Perancangan Antarmuka (UI)	27
4.5 Perancangan Sistem Pengukuran Metrik	29
4.5.1 Strategi Pengukuran Latensi	29
BAB 5 PENGEMBANGAN APLIKASI	32
7.1 Implementasi Sistem	33
7.2 Hasil Pengujian	33
7.3 Pembahasan	33
7.4 Analisis Performa Delegate (Menjawab Rumusan Masalah 1)	33
7.5 Analisis Perbandingan Model (Menjawab Rumusan Masalah 2)	33
7.6 Analisis Keseimbangan (Trade-off) (Menjawab Rumusan Masalah 3)	33
7.6.1 Subbab Lima Satu Satu	33
7.6.2 Subbab Lima Satu Dua	34
7.7 Subbab Lima Dua	34
7.7.1 Subbab Lima Dua Satu	34
7.7.2 Subbab Lima Dua Dua	34
7.8 Subbab Lima Tiga	35
7.8.1 Contoh Struktur Penelitian Implementatif Pembangunan	35
7.8.2 Contoh Struktur Penelitian Nonimplementatif Eksperimental	36
BAB 8 Penutup	37
8.1 Kesimpulan	37
8.2 Saran	37
DAFTAR REFERENSI	38
LAMPIRAN A PERSYARATAN FISIK DAN TATA LETAK	41
A.1 Kertas	41
A.2 Margin	41
A.3 Jenis dan Ukuran Huruf	41
A.4 Spasi	41
A.5 Kepala Bab dan Subbab	41
A.6 Nomor Halaman	42


DAFTAR TABEL
Tabel ‎2.1 Pembentukan bilangan random untuk Indeks Masa Tubuh (IMT)	Error! Bookmark not defined.
Tabel ‎2.2 Contoh tabel 2	Error! Bookmark not defined.

DAFTAR GAMBAR
Gambar ‎2.1 Pengaruh nilai K terhadap akurasi	Error! Bookmark not defined.

DAFTAR LAMPIRAN
LAMPIRAN A PERSYARATAN FISIK DAN TATA LETAK	41
A.1 Kertas	41
A.2 Margin	41
A.3 Jenis dan Ukuran Huruf	41
A.4 Spasi	41
A.5 Kepala Bab dan Subbab	41
A.6 Nomor Halaman	42
LAMPIRAN B PENGGUNAAN BAHASA	43

 
PENDAHULUAN
Latar Belakang
Aplikasi kebugaran berbasis mobile telah mengalami pertumbuhan signifikan dalam beberapa tahun terakhir, didorong oleh meningkatnya kesadaran masyarakat akan pentingnya gaya hidup sehat dan kemudahan akses teknologi. Menurut Grand View Research (2024), pasar aplikasi kebugaran global mencapai USD 10,59 miliar pada tahun 2024 dan diproyeksikan tumbuh menjadi USD 33,58 miliar pada tahun 2033 dengan tingkat pertumbuhan tahunan gabungan (CAGR) sebesar 13,59% pada periode 2025-2033. Pertumbuhan ini didorong oleh meningkatnya kesadaran masyarakat akan pentingnya gaya hidup sehat, kemudahan akses perangkat mobile, serta inovasi teknologi seperti artificial intelligence (AI) dan machine learning yang memungkinkan personalisasi pengguna secara real-time.
Real-time pose estimation menjadi salah satu komponen kritis dalam aplikasi kebugaran modern karena memungkinkan sistem memberikan umpan balik (feedback) koreksi postur secara langsung kepada pengguna. Estimasi pose tubuh manusia (human pose estimation) merupakan teknologi computer vision yang mendeteksi dan melacak posisi sendi-sendi utama tubuh manusia dari gambar atau video. Teknologi ini memiliki potensi besar untuk aplikasi kebugaran karena dapat memberikan umpan balik instan mengenai form atau postur latihan yang benar, sehingga dapat meningkatkan efektivitas latihan dan mengurangi risiko cedera. Namun, implementasi sistem estimasi pose pada perangkat bergerak menghadapi tantangan signifikan terkait keterbatasan sumber daya komputasi, memori, dan kapasitas baterai.
Di Indonesia, penetrasi smartphone telah mencapai tingkat yang sangat tinggi, namun/tetapi karakteristik pasar didominasi oleh perangkat dengan spesifikasi menengah ke bawah. Berdasarkan laporan IDC yang dikutip oleh berbagai media pada tahun 2023, smartphone pada rentang harga di bawah USD 200 menguasai sekitar 76% dari keseluruhan pasar smartphone Indonesia pada kuartal I 2023, meskipun secara tahunan segmen ini masih mengalami penurunan volume pengiriman sekitar 8% [4][5][6]. Kondisi ini menunjukkan dominasi perangkat entry-level yang umumnya memiliki keterbatasan komputasi dan efisiensi daya, sehingga implementasi fitur AI real-time seperti pose estimation perlu dioptimalkan agar tetap responsif. Oleh karena itu, pemilihan konfigurasi Tensorflow Lite delegate (misalnya GPU delegate atau XNNPACK) menjadi penting untuk mencapai latensi rendah dan performa stabil pada perangkat low-to-mid range.
Salah satu teknologi AI yang memiliki potensi besar dalam aplikasi kebugaran adalah pose estimation, yaitu teknik untuk mendeteksi dan melacak posisi sendi tubuh (keypoints) secara real-time dari citra atau video[7]. Teknologi ini memungkinkan aplikasi untuk memberikan umpan balik kepada pengguna terkait bentuk dan postur tubuh selama melakukan latihan fisik seperti squat, push-up, atau yoga. Berbagai penelitian menunjukkan bahwa pose estimation dapat menyediakan dua jenis umpan balik: descriptive feedback (menginformasikan kesalahan postur) dan prescriptive feedback (mengarahkan cara memperbaiki postur), yang keduanya untuk mencegah cedera dan meningkatkan efektivitas latihan[8][9].
Beberapa model pose estimation yang populer dan dioptimalkan untuk perangkat mobile antara lain MoveNet dan BlazePose. MoveNet, yang dikembangkan oleh Google, hadir dalam dua varian: Thunder (akurasi lebih tinggi) dan Lightning (kecepatan lebih tinggi)[10]. MoveNet Lightning dirancang untuk kecepatan dengan  ukuran input 192x192 piksel dan, dalam pengukuran resmi TensorFlow Lite, mencapai latensi sekitar 25 ms per frame pada perangkat Pixel 5 dengan GPU delegate untuk movel FP16 terkuantisasi[11][12]. 
Sementara itu, BlazePose yang dikembangkan oleh Google Research mendeteksi 33 keypoints (lebih banyak dibandingkan 17 keypoints standar COCO) yang lebih sesuai untuk aplikasi kebugaran, dan mampu berjalan secara real-time pada perangkat Pixel 2 pada latensi inferensi di kisaran puluhan milidetik per frame menggunakan akselerasi GPU, sehingga mendukung skenario on-device pose tracking [13][14]. Kedua model ini merepresentasikan pendekatan arsitektur yang berbeda: MoveNet menggunakan pendekatan single-person bottom-up dengan mekanisme smart cropping, sedangkan BlazePose mengadopsi pendekatan two-stage detector-tracker dengan jumlah keypoints yang lebih banyak.
Meskipun model-model pose estimation modern telah dioptimalkan untuk perangkat mobile, inferensi real-time pada perangkat dengan spesifikasi rendah (harga di bawah USD 200) tetap menjadi tantangan. Faktor-faktor seperti latensi inferensi, konsumsi daya, penggunaan memori, dan utilisasi CPU/GPU sangat mempengaruhi pengalaman pengguna, terutama pada perangkat dengan prosesor dan GPU yang terbatas[15]. Oleh karena itu, diperlukan strategi optimasi yang tepat untuk menyeimbangkan antara performa dan efisiensi energi.
TensorFlow Lite sebagai framework optimasi model machine learning untuk perangkat mobile menyediakan berbagai engine akselerasi yang dikenal sebagai delegate. Setiap delegate memiliki karakteristik performa yang berbeda: GPU delegate memanfaatkan akselerasi grafis untuk operasi paralel dan, pada model konvolusional tertentu, dapat memberikan percepatan beberapa kali lipat dibanding eksekusi CPU murni; XNNPACK delegate mengoptimalkan eksekusi di CPU dengan memanfaatkan instruksi SIMD (seperti ARM NEON) dan dilaporkan memberikan peningkatan performa rata rata sekitar 2,3× untuk inference floating point dibanding backend CPU standar; sementara eksekusi CPU standar berperan sebagai fallback universal yang menjamin kompatibilitas lintas perangkat. Pemilihan delegate yang tepat sangat krusial untuk memastikan aplikasi dapat berjalan optimal pada perangkat dengan spesifikasi beragam, khususnya di segmen low to mid range.
Penelitian terdahulu menunjukkan bahwa pemiliihan delegate yang tepat dapat memberikan dampak signifikan terhadap performa inferensi. Sebagai contoh, penelitian oleh Hayakawa et al. (2020) menunjukkan bahwa GPU mobile memberikan akselerasi rata-rata 1.9x dibandingkan CPU, meskipun utilisasi shader core tidak selalu optimal pada beberapa perangkat[20]. Sementara itu, penelitian mengenai konsumsi energi menunjukkan bahwa GPU cenderung mengonsumsi daya lebih tinggi namun dengan throughput yang lebih besar, sedangkan CPU yang lebih efisien untuk model berukuran kecil[21][22].  Di sisi lain, penelitian benchmarking seperti MLPerf Mobile menunjukkan bahwa kinerja delegate sangat bergantung pada kombinasi model, ukuran input, dan karakteristik hardware, serta bahwa akselerator tertentu dapat memberikan keuntungan besar pada model besar tetapi tidak selalu optimal untuk model kecil.
Namun demikian, sebagian besar penelitian sebelumnya dilakukan pada perangkat kelas menengah ke atas (seperti Google Pixel atau Samsung Galaxy Flagship) dan belum banyak yang secara khusus mengevaluasi performa delegate pada perangkat low-end yang mendominasi pasar Indonesia. Selain itu, perbandingan antara model pose estimation (MoveNet vs BlazePose) dengan berbagai konfigurasi delegate pada perangkat low-end masih sangat terbatas. Padahal, pemahaman mengenai trade-off antara latensi, konsumsi daya, dan akurasi pada perangkat low-end sangat penting untuk mengembangkan aplikasi kebugaran yang dapat diakses oleh mayoritas pengguna di Indonesia.
Oleh karena itu, penelitian ini bertujuan untuk melakukan evaluasi komprehensif terhadap performa tiga delegate TensorFlow Lite (GPU, XNNPACK, dan CPU) pada dua model pose estimation (MoveNet Lightning dan BlazePose Lite) di perangkat Android low-to-mid range yang representatif terhadap kondisi pasar di Indonesia. Penelitian ini diharapkan dapat memberikan rekomendasi praktis bagi pengembang aplikasi kebugaran mengenai konfigurasi optimal untuk mencapai keseimbangan antara performa, efisiensi energi, dan aksesibilitas pada perangkat dengan spesifikasi terbatas.
Penelitian sebelumnya menunjukkan bahwa model pose estimation seperti MoveNet dan BlazePose telah dioptimalkan untuk deployment pada perangkat mobile. MoveNet Lightning dirancang khusus untuk kecepatan dengan ukuran input 192×192 piksel dan mencapai inferensi 25ms pada Pixel 5 dengan GPU delegate (Google Research, 2021). Di sisi lain, BlazePose menggunakan pendekatan detector-tracker yang efisien dengan waktu inferensi 33ms pada Pixel 2 dan mampu mendeteksi 33 keypoints dalam ruang 3D (Bazarevsky et al., 2020). Kedua model ini merepresentasikan pendekatan arsitektur yang berbeda: MoveNet menggunakan bottom-up approach dengan smart cropping, sedangkan BlazePose menggunakan two-stage detector-tracker.
Celah riset yang signifikan teridentifikasi, yaitu kurangnya analisis sistematis perbandingan kinerja berbagai delegate TensorFlow Lite untuk implementasi real-time pose estimation pada perangkat Android low-to-mid range dalam konteks aplikasi kebugaran. Setiap delegate memiliki karakteristik optimasi yang berbeda. GPU delegate dapat memberikan peningkatan kecepatan hingga 5x lipat untuk operasi paralel (Google AI Edge, 2024), namun memiliki overhead inisialisasi yang tinggi dan konsumsi daya lebih besar. XNNPACK Delegate mengoptimalkan operasi CPU dengan peningkatan performa hingga 2.3x dibanding CPU standar (TensorFlow Blog, 2024), dengan efisiensi energi yang lebih baik. Eksekusi CPU standar memberikan konsistensi lintas perangkat namun dengan performa lebih rendah.
Domain kebugaran dipilih karena memerlukan akurasi tinggi untuk keamanan (safety) pengguna, latensi rendah untuk responsivitas real-time, dan efisiensi energi untuk penggunaan berkelanjutan. Pemilihan delegate yang tidak tepat dapat mengakibatkan performa aplikasi yang suboptimal, pengalaman pengguna yang buruk, atau konsumsi baterai yang berlebihan. 
Untuk menjawab tantangan komputasi pada perangkat mobile tersebut, Google mengembangkan model arsitektur efisien seperti MoveNet dan Mediapipe BlazePose yang diklaim ringan (lightweight) dan cocok untuk perangkat mobile. Meskipun demikian, kinerja inferensi aktual sangat bergantung pada bagaimana beban komputasi didistribusikan ke unit pemrosesan yang tepat (CPU, GPU, atau XNNPACK) melalui mekanisme delegate di TensorFlow Lite. Hasil penelitian ini diharapkan dapat memberikan panduan empiris bagi para developer untuk memilih konfigurasi delegate dan model yang optimal berdasarkan karakteristik perangkat target dan prioritas aplikasi, sehingga aplikasi kebugaran berbasis pose estimation dapat diakses oleh pengguna dengan beragam spesifikasi perangkat.
Rumusan Masalah
	Bagaimana perbandingan kinerja inferensi (latensi dan throughput) serta konsumsi sumber daya (utilisasi CPU, memori) antara GPU delegate, CPU with XNNPACK delegate, dan CPU-only execution pada TensorFlow Lite untuk real-time pose estimation dalam aplikasi kebugaran Android?
	Bagaimana perbandingan kinerja antara model MoveNet Lightning dan BlazePose Lite pada setiap konfigurasi delegate dalam konteks aplikasi kebugaran mobile?
	Konfigurasi delegate dan model manakah yang memberikan keseimbangan optimal antara kinerja, efisiensi sumber daya, dan akurasi untuk perangkat Android low-to-mid range?
Tujuan
Berdasarkan rumusan masalah di atas, penelitan memiliki tujuan sebagai berikut:
	Mengukur dan membandingkan kinerja inferensi (latensi dan throughput) serta konsumsi sumber daya (utilisasi CPU, memori) antara GPU, CPU with XNNPACK, dan CPU-only execution untuk real-time pose estimation pada aplikasi kebugaran Android.
	Mengevaluasi perbandingan kinerja antara model MoveNet Lightning dan BlazePose Lite pada setiap konfigurasi delegate dalam konteks aplikasi kebugaran mobile.
	Mengidentifikasi konfigurasi delegate dan model yang memberikan keseimbangan optimal antara kinerja, efisiensi sumber daya, dan akurasi untuk aplikasi estimasi pose pada perangkat Android low-to-mid range.
Manfaat
Penelitian ini diharapkan dapat memberikan sejumlah manfaat yang bernilai bagi berbagai pihak.
	Bagi komunitas pengembang perangkat lunak, hasil penelitian ini dapat menyediakan data benchmark yang bersifat kuantitatif dan objektif sebagai dasar pengambilan keputusan teknis dalam pemilihan delegate TensorFlow Lite yang paling sesuai dengan kebutuhan aplikasi, baik dari sisi efisiensi energi maupun akurasi. Selain itu, penelitian ini juga memberikan panduan praktis untuk mengoptimalkan implementasi pose estimation pada aplikasi kebugaran di berbagai konfigurasi perangkat keras Android, khususnya untuk perangkat low-to-mid range yang mendominasi pasar Indonesia, sehingga dapat membantu pengembang dalam meningkatkan performa aplikasi secara efektif dan memperluas jangkauan pengguna.
	Bagi komunitas akademis, penelitian ini berkontribusi dalam mengisi kesenjangan riset yang ada dengan menghadirkan studi komparatif formal mengenai performa berbagai delegate TensorFlow Lite dalam konteks aplikasi mobile dengan fokus pada perangkat dengan keterbatasan sumber daya. Hasilnya dapat dijadikan referensi bagi penelitian lanjutan di bidang analisis performa perangkat lunak maupun rekayasa sistem mobile. Lebih dari itu, metodologi eksperimen yang dikembangkan dalam penelitian ini dapat diadaptasi dan diterapkan untuk mengevaluasi performa delegate pada domain aplikasi lain di masa mendatang.
	Bagi industri teknologi kesehatan, penelitian ini memberikan wawasan teknis yang bermanfaat dalam mengembangkan aplikasi kebugaran berbasis kecerdasan buatan yang lebih efisien, responsif, dan ramah pengguna. Dengan pemahaman mendalam terhadap performa masing-masing delegate, pengembang di sektor ini dapat merancang solusi fitness berbasis AI yang mampu berjalan optimal pada beragam tingkat spesifikasi perangkat Android, sehingga memperluas aksesibilitas dan pengalaman pengguna secara keseluruhan, terutama bagi segmen pasar dengan daya beli menengah ke bawah.
Batasan Masalah
Untuk menjaga agar penelitian tetap fokus dan terarah, ruang lingkup penelitian ini dibatasi pada aspek berikut :
	Platform dan Lingkungan Pengujian: Penelitian ini diimplementasikan menggunakan library TensorFlow Lite versi 2.14.0 (atau terbaru yang stabil) pada sistem operasi Android dengan target SDK API Level 34 (Android 14).
	Model Pose Estimation: Penelitian menggunakan dua varian model pre-trained yang telah dioptimalkan untuk perangkat mobile, yaitu:
	MoveNet Lightning (INT8): Model ultra-cepat dengan input resolusi rendah (192x192), merepresentasikan beban kerja ringan.
	MediaPipe BlazePose Lite (FP16): Model berbasis topologi 33 keypoints dengan kompleksitas deteksi 3D, merepresentasikan beban kerja menengah.
	Perangkat Keras (Device Testbed): Pengujian dilakukan pada dua kategori perangkat pintar (smartphone) untuk memvalidasi performa akselerasi perangkat keras pada segmen pasar yang berbeda:
	Low-End Device: Samsung Galaxy A06 (Chipset MediaTek Helio G85, GPU Mali-G52, RAM 4GB) sebagai representasi perangkat dengan sumber daya terbatas.
	Mid-Range Device: Samsung Galaxy A33 5G (Chipset Exynos 1280, GPU Mali-G68, RAM 6/8GB) sebagai representasi perangkat modern dengan dukungan NPU.
	Fokus Pengukuran Kinerja: Penelitian ini hanya berfokus pada pengukuran kinerja komputasi (computational performance), yang meliputi parameter: Latensi Inferensi (Inference Time), Frame Per Second (FPS), Penggunaan CPU/GPU, dan Konsumsi Memori (RAM). 

Sistematika Pembahasan
Untuk memberikan gambaran yang jelas mengenai alur penelitian, laporan skripsi ini disusun dengan sistematika sebagai berikut:
BAB I PENDAHULUAN: Berisi latar belakang masalah yang mengidentifikasi pentingnya optimasi delegate TensorFlow Lite untuk aplikasi kebugaran pada perangkat low-to-mid range, rumusan masalah yang spesifik dan terukur, tujuan penelitian, manfaat yang diharapkan, batasan masalah yang jelas, serta sistematika pembahasan.
BAB II LANDASAN KEPUSTAKAAN: Menguraikan tinjauan literatur mengenai penelitian terkait di bidang pose estimation mobile dan optimasi TensorFlow Lite, serta teori-teori pendukung mengenai arsitektur delegate TensorFlow Lite, model MoveNet, metrik evaluasi kinerja aplikasi mobile, dan metodologi penelitian eksperimental.
BAB III METODOLOGI PENELITIAN: Menjelaskan secara rinci desain eksperimen kuantitatif, variabel penelitian, teknik pengumpulan data menggunakan profiling tools, serta metodologi analisis statistik menggunakan uji ANOVA untuk memvalidasi hipotesis.
BAB IV PERANCANGAN SISTEM: Menguraikan tahapan perancangan aplikasi testbed yang digunakan sebagai alat ukur, meliputi analisis kebutuhan fungsional dan non-fungsional, perancangan arsitektur sistem, diagram alir (flowchart) logika inferensi dan penghitungan repetisi, serta rancangan antarmuka pengguna (wireframe).
BAB V PENGEMBANGAN APLIKASI: Menguraikan realisasi sistem berdasarkan rancangan, meliputi implementasi antarmuka, integrasi library TensorFlow Lite, serta pengkodean logika inferensi dan mekanisme logging data. 
BAB VI PENGUJIAN DAN PEMBAHASAN: Menyajikan data hasil benchmarking secara sistematis (latensi, throughput, sumber daya), analisis statistik menggunakan uji ANOVA, serta pembahasan mendalam (deep dive) untuk menjawab rumusan masalah.
BAB VII PENUTUP: Berisi kesimpulan yang menjawab setiap rumusan masalah berdasarkan analisis data empiris, kontribusi penelitian terhadap body of knowledge, keterbatasan penelitian yang diidentifikasi selama proses eksperimen, serta saran untuk pengembangan penelitian di masa mendatang.
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
MoveNet Lightning dioptimalkan untuk kecepatan dengan ukuran masukan 192×192 piksel dan depth multiplier 1,0. Model ini mampu mencapai waktu inferensi 25ms pada Pixel 5 dengan GPU delegate, menjadikannya ideal untuk aplikasi waktu nyata yang memprioritaskan responsivitas (Google Research, 2021). Akurasi model mencapai 63,0% mAP dengan ukuran model 4,8MB (kuantisasi FP16). Varian ini sangat sesuai untuk aplikasi kebugaran yang membutuhkan umpan balik segera dan dapat mentolerir akurasi yang sedikit lebih rendah.
MoveNet Thunder dioptimalkan untuk akurasi dengan ukuran masukan 256×256 piksel dan depth multiplier 1,75. Model ini mencapai 72,0% mAP dengan waktu inferensi 45ms pada Pixel 5 GPU, memberikan keseimbangan yang baik antara akurasi dan kecepatan untuk aplikasi yang memerlukan presisi tinggi (Google Research, 2021). Ukuran model adalah 12,6MB (kuantisasi FP16), lebih tepat untuk aplikasi dimana akurasi kritis untuk keselamatan pengguna.
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
Nilai F kemudian dibandingkan dengan nilai F_tabel pada tingkat signifikansi tertentu (misalnya α = 0,05). Jika F_hitung > F_tabel, maka terdapat perbedaan yang signifikan antar kelompok.
Uji Shapiro-Wilk merupakan uji statistik untuk menguji normalitas distribusi data dengan menghitung statistik W yang membandingkan data sampel dengan distribusi normal teoretis (Shapiro & Wilk, 1965). Nilai p < 0,05 mengindikasikan bahwa data tidak terdistribusi normal, sehingga diperlukan transformasi data atau penggunaan uji nonparametrik. Uji Shapiro-Wilk dipilih karena memiliki power yang tinggi untuk mendeteksi penyimpangan dari normalitas, terutama untuk ukuran sampel kecil hingga menengah (n < 50) (Field, 2018).
METODOLOGI
Jenis Penelitian
Penelitian ini merupakan penelitian kuantitatif dengan pendekatan eksperimental yang bersifat analitik-komparatif. Tujuannya adalah untuk membandingkan performa berbagai konfigurasi delegate TensorFlow Lite (GPU, XNNPACK, CPU) pada dua model pose estimation (MoveNet Lightning dan BlazePose Lite) untuk aplikasi estimasi pose real-time di perangkat bergerak. Aplikasi yang dikembangkan tidak dimaksudkan sebagai produk akhir, melainkan berfungsi sebagai test harness atau alat ukur untuk mengumpulkan data performa dan akurasi secara objektif. Pendekatan eksperimen terkontrol digunakan agar setiap variabel dapat diuji secara sistematis dengan kondisi yang konsisten, sehingga hasil penelitian dapat direplikasi dan divalidasi.
Strategi penelitian ini menggunakan desain faktorial penuh (full factorial design) untuk menguji pengaruh tiga variabel bebas: jenis delegate TensorFlow Lite, varian model MoveNet, dan jenis latihan yang akan dilakukan. Dengan menggabungkan seluruh kombinasi dari ketiga faktor tersebut, diperoleh rancangan eksperimen 3×2×2 yang menghasilkan 12 kondisi pengujian, dengan setiap kondisi dieksekusi 30 kali untuk memastikan kekuatan statistik yang memadai (total 360 eksperimen). Pengulangan sebanyak 30 kali dipilih berdasarkan central limit theorem yang menyatakan bahwa distribusi samping means akan mendekati normal untuk n >= 30, memungkinkan penggunaan uji statistik parametrik (Montgomery, 2017). Power analysis a priori menggunakan G*Power 3.1 dengan α=0.05, power (1-β)=0.80, dan medium effect size (f=0.25) menunjukkan minimum sample size n=24 untuk three-way ANOVA. Dengan demikian, n=30 memberikan adequate statistical power dengan buffer untuk outlier removal.
Desain Eksperimen
Variabel bebas yang digunakan dalam penelitian ini mencakup empat jenis delegate TensorFlow Lite, yaitu GPU Delegate, XNNPACK Delegate, dan CPU-only execution. Selain itu, digunakan varian model MoveNet, yakni Lightning (resolusi input 192×192 piksel), dan BlazePose Lite (Detector-tracker approach, 33 keypoints) serta dua jenis latihan, yaitu squat dan push-up, yang dipilih untuk merepresentasikan pola gerakan berbeda. Variabel terikat terdiri dari metrik performa (latensi inferensi, throughput, utilisasi CPU, penggunaan memori, dan konsumsi daya) serta metrik akurasi fungsional (Ketepatan Penghitungan Repetisi). Variabel kontrol meliputi perangkat yang digunakan, kondisi lingkungan, konfigurasi sistem, serta resolusi dan durasi video input agar setiap eksperimen berjalan dalam keadaan yang seragam.
Berdasarkan rancangan ini, hipotesis penelitian diformulasikan sebagai berikut. Pertama, terdapat perbedaan signifikan dalam latensi inferensi antar delegate, dengan dugaan bahwa GPU Delegate memberikan latensi terendah, khususnya untuk model MoveNet Thunder.Ketiga, tidak terdapat perbedaan akurasi yang berarti antar delegate untuk model yang sama, menandakan bahwa optimasi delegate tidak memengaruhi hasil inferensi model. Keempat, performa relatif antar delegate diperkirakan konsisten pada kedua jenis latihan, yang menunjukkan generalisasi hasil penelitian terhadap berbagai pola gerakan.
Tabel 3.1 Perancangan Eksperimen
Faktor	Level	Keterangan
Delegate	3	GPU, XNNPACK, CPU
Model	2	MoveNet Lightning, BlazePose Lite
Latihan	2	Squat, Push-up
Total kombinasi = 3 delegate × 2 model × 2 latihan = 12 kondisi 
Total runs = 12 kondisi × 30 replikasi = 360 eksperimen.
Tahapan Penelitian
Pelaksanaan penelitian ini dilakukan melalui beberapa tahap utama, yaitu tahap persiapan, pengembangan aplikasi uji, pengumpulan data, dan analisis hasil.
 
Pada tahap persiapan, dilakukan pemilihan perangkat keras dan perangkat lunak yang digunakan dalam eksperimen. Pengujian dilakukan menggunakan dua kategori perangkat, perangkat Low-range device, dan perangkat mid-range device. Untuk Low-range device menggunakan perangkat Samsung Galaxy A06 dengan prosesor Mediatek Helio G85, Ram 4 GB, dan sistem operasin android 14. Sedangkat mid-range device menggunakan perangkat Samsung Galaxy A33 dengan prosesor Exynos 1280, RAM 8 GB, dan sistem operasi Android 14. Kedua perangkat dipilih karena mendukung semua jenis delegate TensorFlow Lite yang diuji dan mewakili kategori yang umum digunakan pengguna Indonesia. Lingkungan pengujian dijaga konstan dengan suhu 24–26°C dan pencahayaan sekitar 500 lux. Aplikasi uji dikembangkan menggunakan Android Studio Iguana (2024.1.1) dengan bahasa Kotlin dan framework TensorFlow Lite versi 2.15.0.
Tahap pengembangan aplikasi dilakukan dengan membangun sistem benchmarking yang berfungsi menjalankan kedua model pose estimation dalam berbagai konfigurasi delegate serta mencatat metrik performa secara otomatis. Aplikasi dirancang dengan arsitektur modular yang terdiri dari tiga lapisan: presentation layer untuk antarmuka dan kontrol pengujian, domain layer untuk logika perhitungan metrik dan validasi data, serta data layer untuk proses inferensi dan pencatatan hasil. Proses inferensi dijalankan menggunakan API TensorFlow Lite, sedangkan data metrik diperoleh melalui Android Profiler API dan TensorFlow Benchmark Tool.
Tahap pengumpulan data dilakukan dengan menjalankan dua skenario latihan, yaitu squat dan push-up, menggunakan video masukan beresolusi 1080p dengan 30 FPS. Sebelum setiap sesi pengujian, perangkat di-restart untuk menghapus proses latar, dan dilakukan fase idle profiling selama 30 detik untuk mencatat baseline performa. Setiap konfigurasi delegate diuji secara bergantian dengan jeda lima menit untuk mencegah peningkatan suhu berlebih. Selama eksperimen, data performa dicatat secara real-time meliputi latensi inferensi, FPS, utilisasi CPU, memori, dan daya.
Tahap analisis data diawali dengan proses pembersihan dan normalisasi data. Nilai outlier diidentifikasi menggunakan metode modified Z-score dengan ambang batas 3,5, sedangkan data yang hilang diinterpolasi secara linier. Selanjutnya, dilakukan normalisasi min–maks untuk penyamaan skala antar metrik dan transformasi logaritmik pada distribusi yang tidak normal. Setelah data siap, dilakukan analisis deskriptif untuk melihat kecenderungan umum dan sebaran data, dilanjutkan dengan analisis inferensial menggunakan ANOVA tiga arah dengan pengukuran berulang (three-way repeated-measures ANOVA) guna menguji pengaruh faktor delegate, model, dan jenis latihan beserta interaksinya.  Pemilihan desain repeated-measures ANOVA dibandingkan ANOVA independen dilakukan karena seluruh kondisi diuji pada perangkat yang sama, sehingga antar pengukuran bersifat dependen.
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
	Kondisi Lingkungan: Suhu ruang dijaga stabil pada rentang 24–26°C untuk mencegah thermal throttling eksternal.
	Kondisi Sistem: Mode pesawat (Airplane Mode) diaktifkan, kecerahan layar diatur pada 50%, dan seluruh aplikasi latar belakang (background processes) dimatikan sebelum pengujian. Baterai perangkat dipastikan berada pada level >50% dan tidak dalam kondisi pengisian daya (not charging) saat pengambilan data.
Metode Eksperimen
Penelitian ini menggunakan pendekatan within-subject repeated-measures design, di mana setiap kombinasi delegate, model, dan jenis latihan diuji pada perangkat yang sama dengan kondisi yang dikontrol secara ketat. Desain faktorial 3×2×2 menghasilkan 12 kondisi eksperimental, dan masing-masing kondisi dijalankan sebanyak 30 kali untuk memperoleh kekuatan statistik yang memadai. Sebelum eksperimen dimulai, perangkat di-restart untuk menstabilkan suhu dan menghapus proses latar, kemudian dilakukan verifikasi daya baterai dan pencahayaan ruangan agar sesuai dengan standar pengujian.
Pelaksanaan eksperimen dilakukan melalui dua jenis latihan, yaitu squat dan push-up, yang dilakukan dengan tempo terstandar untuk menjaga konsistensi gerakan. Setiap sesi squat terdiri atas fase turun selama tiga detik, tahan satu detik, naik tiga detik, dan istirahat dua detik, sedangkan sesi push-up memiliki pola dua detik turun, satu detik tahan, dua detik naik, dan dua detik istirahat. Data dikumpulkan secara real-time menggunakan Android Profiler API untuk mencatat latensi inferensi, penggunaan CPU, konsumsi memori, serta konsumsi daya. Untuk menghindari bias akibat urutan pengujian, dilakukan proses counterbalancing pada urutan pengujian setiap delegate sehingga efek learning dan akumulasi panas dapat diminimalkan. Seluruh instrumen juga dikalibrasi, dan hasil pengamatan yang menyimpang diidentifikasi menggunakan modified Z-score dengan ambang 3,5 sebagai batas deteksi outlier.
Statistik yang Digunakan
Analisis data dilakukan secara kuantitatif menggunakan gabungan metode statistik deskriptif dan inferensial. Analisis deskriptif mencakup penghitungan nilai rata-rata, median, standar deviasi, varians, serta bentuk distribusi data seperti skewness dan kurtosis. Untuk setiap metrik performa, interval kepercayaan 95% dihitung menggunakan distribusi-t pada data berdistribusi normal, sedangkan data yang tidak normal diestimasi menggunakan metode bootstrap resampling.
Uji normalitas dilakukan dengan uji Shapiro–Wilk pada taraf signifikansi 0,05 untuk memastikan bahwa data memenuhi asumsi parametrik. Apabila data tidak berdistribusi normal, dilakukan transformasi logaritmik atau akar kuadrat, dan jika masih belum memenuhi asumsi, digunakan alternatif non-parametrik. Homogenitas varians diuji dengan uji Levene atau Brown-Forsythe jika data tidak normal. Untuk menguji pengaruh dari faktor-faktor penelitian, digunakan three-way repeated-measures ANOVA guna menganalisis efek utama dari jenis delegate, varian model, dan jenis latihan, serta interaksi di antara ketiganya. Apabila uji ANOVA menunjukkan hasil yang signifikan, maka dilakukan uji lanjut Tukey HSD untuk membandingkan pasangan kelompok secara berpasangan dengan pengendalian tingkat kesalahan keluarga (family-wise error rate) pada α = 0,05. Ukuran efek dihitung menggunakan partial eta-squared (η²p) untuk menilai signifikansi praktis, dengan interpretasi efek kecil (η²p ≥ 0,01), sedang (≥ 0,06), dan besar (≥ 0,14). Apabila asumsi parametrik tidak terpenuhi, maka analisis digantikan dengan uji Friedman atau Kruskal-Wallis.
  

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
Diagram Sekuens
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

Mekanisme Error Handling & Fallback
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
Tabel 4.1 Rancangan Struktur File CSV Hasil Benchmarking
No	Nama Kolom (Header)	Tipe Data	Deskripsi
1	Timestamp	Long / String	Waktu pengambilan data (format: yyyy-MM-dd HH:mm:ss) untuk urutan time-series.
2	Model_Name	String	Varian model yang sedang diuji (contoh: "MoveNet Lightning" atau "BlazePose Lite").
3	Delegate_Type	String	Jenis akselerator yang aktif (contoh: "GPU", "CPU", "XNNPACK").
4	Device_Name	String	Identitas perangkat pengujian (contoh: "Samsung A06").
5	Inference_Time_ms	Float	Waktu yang dibutuhkan untuk satu kali proses inferensi dalam milidetik.
6	FPS	Float	Jumlah frame per detik yang berhasil diproses saat itu.
7	CPU_Usage_Percent	Float	Persentase penggunaan CPU saat frame diproses.
8	Memory_Usage_MB	Float	Penggunaan RAM oleh aplikasi dalam satuan Megabyte.
9	Battery_Current_mA	Float	Arus baterai saat pengujian (untuk estimasi konsumsi daya).
10	Repetition_Count	Integer	Jumlah repetisi yang terdeteksi valid (untuk validasi fungsional).









PENGEMBANGAN APLIKASI
Bab ini fokus pada "Bagaimana sistem ini dibangun" berdasarkan rancangan di Bab 4.
	5.1 Lingkungan Pengembangan: Menjelaskan detail tools yang beneran lu pake, seperti Android Studio versi tertentu, versi Kotlin, dan konfigurasi Gradle (minSdk 25, targetSdk 34) .
	5.2 Realisasi Antarmuka Pengguna (UI): Menampilkan hasil jadi dari wireframe Bab 4, mulai dari layar konfigurasi model hingga layar monitoring real-time .
	5.3 Implementasi Logika Inferensi TFLite: Menjelaskan bagian kode untuk memuat model (.tflite), inisialisasi delegate (CPU, GPU, XNNPACK), dan proses running interpreter .
	5.4 Implementasi Algoritma Repetisi: Menjelaskan bagaimana State Machine (UP/DOWN) dikonversi menjadi kode untuk menghitung squat dan push-up .
	5.5 Implementasi Pengukuran Metrik & Logging: Menjelaskan bagian kode yang menggunakan System.nanoTime() untuk latensi dan bagaimana data tersebut ditulis ke file CSV .
	BAB VI: PENGUJIAN DAN PEMBAHASAN
Bab ini adalah "nyawa" dari penelitian lu, di mana data bicara.
	6.1 Prosedur Pengujian: Menjelaskan langkah-langkah saat lu ambil data di Samsung A06 dan A33, termasuk protokol warm-up 5 frame .
	6.2 Hasil Pengujian Performa (Benchmarking):
	6.2.1 Analisis Latensi dan Throughput (FPS): Sajian grafik perbandingan antar delegate .
	6.2.2 Analisis Utilisasi Sumber Daya: Data penggunaan CPU dan RAM.
	6.2.3 Analisis Konsumsi Daya: Estimasi penggunaan baterai selama inferensi .
	6.3 Pengujian Akurasi Fungsional: Tabel perbandingan hitungan repetisi aplikasi vs manual (ground truth) .
	6.4 Analisis Statistik (ANOVA): Hasil uji signifikansi untuk menentukan apakah perbedaan performa antar delegate itu nyata atau cuma kebetulan.
	6.5 Pembahasan Mendalam (Deep Dive): Lu bahas "Kenapa" hasilnya begitu. Misalnya, kenapa GPU di Helio G85 (A06) tidak secepat yang dibayangkan dibanding XNNPACK.
	BAB VII: PENUTUP
Karena ada penambahan bab, maka penutup pindah ke Bab 7.
	7.1 Kesimpulan: Menjawab rumusan masalah .
	7.2 Saran: Masukan untuk pengembangan ke depan .

Pembahasan berfungsi untuk menerjemahkan makna dari hasil yang diperoleh untuk menjawab pertanyaan atau masalah penelitian. Fungsi lainnya adalah untuk menjelaskan pemahaman baru yang didapatkan dari hasil penelitian, yang diharapkan berguna dalam pengembangan keilmuan. Dalam penelitian tingkat lanjut, fungsi pembahasan yang kedua ini sangat penting karena dapat menunjukkan kontribusi penulis terhadap pengembangan keilmuan. Akan tetapi, dalam penelitian tingkat skripsi, fungsi yang kedua ini dapat diterapkan secara terbatas karena pendidikan S1 tidak dituntut untuk pengembangan keilmuan secara substansial, tetapi cukup terhadap pemahaman personal dalam implementasi konsep atau teori. 
Implementasi Sistem
Hasil Pengujian
Pembahasan
Analisis Performa Delegate (Menjawab Rumusan Masalah 1)
Analisis Perbandingan Model (Menjawab Rumusan Masalah 2)
Analisis Keseimbangan (Trade-off) (Menjawab Rumusan Masalah 3)
Dalam menjawab masalah penelitian, penulis diminta untuk melakukan evaluasi kritis terhadap hasil yang diperoleh. Tergantung dari fokus penelitian, beberapa contoh pertanyaan kritis yang dapat dijawab adalah: 
Seberapa jauh tujuan penelitian telah tercapai?
Apakah aplikasi atau sistem yang dibangun sesuai dengan tujuannya?
Apakah metode atau praktik perancangan dan implementasi yang baik telah dijalankan?
Apakah teknologi implementasi yang tepat telah dipilih? Dan sebagainya.
Subbab Lima Satu Satu
Dalam menjelaskan pemahaman baru yang didapatkan, penulis dapat mengubungkan hasil penelitian dengan pengetahuan teoritik atau penelitian sebelumnya yang telah dibahas. Kaitan antara hasil penelitian dan pengetahuan teoritik misalnya berupa:
pendapat tentang metode yang digunakan dari pustaka, apakah dapat digunakan dengan baik secara langsung, dengan penyesuaian, atau dengan batasan tertentu;
konfirmasi tentang batasan dari metodologi yang digunakan sehingga dapat berpengaruh pada hasil;
penjelasan tentang informasi penting pada penelitian lainnya yang membantu penulis untuk menerjemahkan data penelitian penulis; 
penjelasan tentang kemungkinan hasil dari penelitian lainnya yang dapat dikombinasikan dengan penelitian penulis untuk memberikan pengetahuan baru; dan sebagainya.
Subbab Lima Satu Dua
Penulis dapat merefleksikan apa yang telah dipelajari selama melakukan penelitian, tetapi harus tetap terfokus dengan masalah penelitian ini dan tidak melebar ke masalah lainnya. Hal-hal yang berada di luar fokus peneltian tetapi penting dan menarik untuk diteliti dapat disarankan sebagai bahan penelitian berikutnya. Hal ini dapat dipertegas di bab Kesimpulan/ Penutup. 
Subbab Lima Dua
Hasil dan pembahasan dapat diletakkan dengan kemungkinan berikut:
	Dipisahkan secara fisik ke dalam bab-bab yang berbeda
Dipisahkan secara fisik ke dalam dua atau lebih paragraf atau subbab yang berbeda tetapi dalam bab yang sama
Dileburkan menjadi satu dalam paragraf, dijelaskan secara naratif-deskriptif, terdistribusi ke satu atau lebih bab yang ada
Subbab Lima Dua Satu
Cara pertama atau kedua membantu pembaca yang ingin memisahkan observasi dan terjemahan dari observasi tersebut sehingga mereka dapat menilai kualitas dari masing-masing proses dengan lebih mudah. Kadang-kadang cara kedua lebih banyak dipilih daripada cara pertama jika data yang harus dipresentasikan yang cukup banyak dan laporan penelitian cukup panjang agar pembaca tidak perlu menunggu presentasi dari seluruh data selesai baru dapat membaca penerjemahannya. Cara pertama dan kedua ini banyak digunakan untuk penelitian yang bersifat kuantitatif, baik itu deskriptif, eksplanatori, maupun implementatif.    
Subbab Lima Dua Dua
Cara ketiga biasanya digunakan jika data, analisis, dan penafsirannya sulit dipisahkan. Pemisahannya terkadang justru membuat laporan penelitian sulit dibaca. Hal ini dapat berlaku pada tipe penelitian yang bersifat kualitatif, baik itu deskriptif ataupun analitik/eksplanatori. 
Pada dasarnya peletakan dan jumlah bab untuk hasil dan pembahasan sebaiknya disesuaikan karakter penelitian masing-masing. Judul bab pun tidak harus secara eksplisit “Hasil” dan “Pembahasan” tetapi dapat digantikan dengan nama yang lebih deskpritif dan tematik. 
Subbab Lima Tiga
Contoh struktur skripsi untuk implementatif pembangunan dan nonimplementatif eksperimental dapat dilihat pada kedua subbab berikut. 
Contoh Struktur Penelitian Implementatif Pembangunan
Berikut ini adalah contoh bab-bab yang terdapat pada penelitian implementatif pembangunan sistem perangkat lunak. 
Bab 1 Pendahuluan
Bab 2 Landasan Kepustakaan
Bab 3 Metodologi Penelitian
Bab 4 Persyaratan 
Bab 5 Perancangan dan Implementasi
Bab 6 Pengujian 
Bab 7 Penutup
Bab 1 sampai Bab 3 memuat informasi yang sesuai dengan panduan sebelumnya. Isi dari bab-bab berikutnya: 
Bab 4 Persyaratan: 
Pernyataan masalah (problem statement), yang lebih elaboratif daripada yang di Pendahuluan. 
Identifikasi pemangku kepentingan (stakeholders) dan aktor (actors) sistem. 
Daftar terstruktur persyaratan/kebutuhan perangkat lunak, secara fungsional, data, dan non-fungsional
Use cases, use case diagrams, dan use case specifications, dan sebagainya. 
Bab 5 Perancangan dan Implementasi:
Rancangan arsitektur: deskripsi struktur dan setiap komponen utama  
Representasi data dalam model data dan basis data 
Detil implementasi dari fungsi-fungsi utama yang menjadi fokus
Bab 6 Pengujian dan Evaluasi
Strategi, rencana, kasus, dan data pengujian
Ringkasan hasil pengujian perangkat lunak, termasuk data dan analisisnya (detilnya di Lampiran)
Evaluasi hasil proyek secara keseluruhan, misalkan 
Bab 7 Penutup
Ringkasan dari capaian proyek
Saran pengembangan lebih lanjut
Pada contoh struktur ini “hasil” tersebar di beberapa bab mulai Bab 4 Persyaratan sampai Bab 6, sedangkan “pembahasan” secara keseluruhan terhadap masalah penelitian terdapat di Bab 6. Yang dimaksud dengan pengujian dalam Bab 6 terfokus pada pengujian persyaratan perangkat lunak, sedangkan evaluasi berfungsi sebagai “pembahasan” secara keseluruhan, yaitu menentukan apakah “hasil” sudah menjawab masalah penelitian yang dirumuskan pada Bab 1. 
Sebagai catatan, Bab 3 Metodologi umumnya menjelaskan model proses perangkat lunak yang digunakan. Jika strategi untuk setiap aktivitasnya (analisis persyaratan, perancangan, dan seterusnya) sudah dijelaskan di Bab 3 ini juga, maka bab-bab lainnya yang berhubungan dengan aktivitas-aktivitas ini masing-masing langsung dapat menjelaskan hasil pelaksanaan metodenya. 
Contoh Struktur Penelitian Nonimplementatif Eksperimental
Berikut ini adalah contoh bab-bab yang terdapat pada penelitian implementatif pembangunan sistem perangkat lunak. 
Bab 1 Pendahuluan
Bab 2 Landasan Kepustakaan
Bab 3 Metodologi Penelitian
Bab 4 Hasil 
Bab 5 Pembahasan
Bab 6 Penutup
Isi dari setiap bab dapat menyesuaikan dengan panduan yang telah dijelaskan sebelumnya. Jika diperlukan, Bab 4 dapat digabungkan dengan Bab 5, menjadi Hasil dan Pembahasan. 
Struktur dasar ini cukup universal sehingga dapat digunakan juga untuk tipe-tipe penelitian lainnya, khususnya jika belum ada struktur lain yang lebih tematik dan cocok untuk penelitian yang bersangkutan.
Penutup
Bagian ini memuat kesimpulan dan saran terhadap skripsi. Kesimpulan dan saran disajikan secara terpisah, dengan penjelasan sebagai berikut: 
Kesimpulan 
Kesimpulan merupakan pernyataan-pernyataan yang singkat, jelas, dan tepat tentang hasil penelitian yang diperoleh berdasarkan tujuannya. Bagian ini merupakan penegasan dari yang telah dijelaskan pada bagian Pembahasan dan tidak memuat informasi yang baru.  Bagian ini juga mencerminkan jawaban dari rumusan masalah (pertanyaan penelitian).
Saran
Saran berisi pernyataan-pernyataan yang ringkas dan jelas tentang masalah-masalah atau hal-hal yang dapat dilakukan untuk mengembangkan penelitian ini lebih lanjut. Saran itu dapat diarahkan pada aspek metode, instrumen, populasi/sampel, dan sebagainya.
DAFTAR REFERENSI
Adobe Systems Incorporated, 2013. Adobe Air (3.5 beta). [program komputer] Adobe Labs. Tersedia di: <http://labs.adobe.com/technologies/
flashruntimes/air/> [Diakses 1 Mei 2013]
Alif, A., 2013. Komputasi cerdas untuk pemula. Malang: ABC Press.
Angriawan, B., 2014. Sistem pakar untuk penentuan kondisi tubuh ideal atlet sepakbola usia remaja. S1. Universitas Malang Raya. 
Berndtsson, M., Hansson, J., Olsson, B. & Lundell, B., 2008. Thesis projects: a guide for students in Computer Science and Information Systems. 2nd ed. London: Springer-Verlag London Limited.
Boots Group Plc., 2003. Corporate social responsibility. [online] Boots Group Plc. Tersedia di: <http://www.boots-plc.com/information/info.asp?id1=1a> [Diakses 1 April 2004]
Brata, K.C., 2012. Rancang bangun aplikasi jejaring sosial kampus berbasis GPS pada ponsel cerdas Android. S1. Universitas Brawijaya. Tersedia di <http://ptiik.ub.ac.id/skripsi> [Diakses 1 Agustus 2014]
British Standards Institution, 2011. BS EN 594:2011 Timber structures. Test methods. Racking strength and stiffness of timbre frame wall panels. British Standards Online [online] Tersedia melalui: Anglia Ruskin University Library <http://libweb.anglia.ac.uk> [Diakses 31 Augustus 2011]
Brodjonegoro, A., 2009a. Dunia teknologi informasi bagi komunitas open source. Bandung: Bandung Indah Press.
Brodjonegoro, A., 2009b. Peran media sosial dalam pemasaran produk perangkat lunak. Bandung: Bandung Indah Press.
Broughton, J.M., 2002a. The Brettow Woods proposal: a brief look. Political Science Quarterly, 42(6), p.564. 
Broughton, J.M., 2002b. The Brettow Woods proposal: a brief look. Political Science Quarterly, [e-journal] 42(6). Tersedia melalui: Perpustakaan Universitas BX <http://perpustakaan.ubx.ac.id> [Diakses 1 Juli 2013] 
Brown, J. 2005. Evaluating surveys of transparent governance. In: UNDESA (United Nations Department of Economic and Social Affairs), 2005. 6th Global forum for reinventing government: towards participatory dan transparent governance. Seoul, Republic of Korea, 24-27 May 2005. New York: United Nations.
Cakraningrat, R., 2011. Sistem pendukung keputusan untuk UMKM. [e-book]. UBX Press. Tersedia melalui: Perpustakaan Universitas BX <http://perpustakaan.ubx.ac.id> [Diakses 1 Juli 2013] 
Cookson, J. dan Church, S. eds., 2007. Leisure and the tourist. [e-book] Wallingford: ABS Publishers. Tersedia di: Google Books <http://booksgoogle.com> [Diakses 1 Juli 2009] 
Cox, C., Brown, J.T. dan Tumpington, W.T., 2002. What health care assistants know about clean hands. Nursing Today, Spring Issue, pp.64-68.
Diponegoro, A., 2008. The beauty of Indonesian oceans. [electronic print] Tersedia di: <http://adiponegoro.com/store/product_info.php?cPath=3&
productss_id=99> [Diakses 1 Januari 2011] 
Esemka, 2012. Esemka bisa. [image online] Tersedia di: <http://www.esemka.co.id/esemkabisa.aspx> [Diakses 31 Januari 2011]
Goalie, D. 2008. Remote sensing technology for modern soccer. Popular science and Technology, [online] Tersedia di: <http://www.popsci.com/b012378/
soccer.html> [Diakses 1 Juli 2009] 
Haryanto, A. 2002. Dua dunia. [foto] (Koleksi pribadi Alan Haryanto) 
Higher Education Act 2004. (c.8). London: HMSO
International Standards Office, 1998. ISO 690 – 2 Information and documentation: Bibliographical references: Electronic documents. Geneva: ISO.
Kartolo, R., 2010. Wawancara pada Kabar Pagi. Diwawancara oleh Sam Basman [televisi] TVRI Saluran 1, 17 Agustus 2010, 08:30.
Keene, E., ed., 1988. Natural language. Cambridge: University of Cambridge Press.
Kementerian Komunikasi dan Informatika, 2013. Laporan Tahunan Layanan Informasi Publik Tahun 2012. [pdf] Kementerian Komunikasi dan Informatika. Tersedia di: <http://publikasi.kominfo.go.id/bitstream/handle/
54323613/976/laporan-dan-evaluasi-ppid-tahun-2012-ditambahkan-cover-untuk-online-ppid.pdf> [Diakses 1 Agustus 2014]
NHS Evidence, 2003. National Library of Guidelines. [online] Tersedia di: <http://www.library.nhs.uk/guidelinesfinder> [Diakses 1 Juli 2007]
Rahardjo, S. 2001. Presiden Habibie. [foto] (Jakarta, Koleksi Museum Presiden)
Richmod, J., 2005. Customer expectations in the world of elctronic banking: a case study of the Bank of Britain. PhD. Anglia Ruskin University. 
Rumbaugh, J., Jacobson, I. & Booch, G., 2005. The Unified Modeling Language reference manual. 2nd ed. Boston: Addison-Wesley.
Samson, C., 1970. Problems of information studies in history. Dalam: S. Stone, ed. 1980. Humanities information research. Sheffield: CRUS. pp. 44-68. 
Scottish Intercollegiate Guidelines, 2001. Hypertension in the elderly. (SIGN publication 20) [online] Edinburgh: SIGN (Diterbitkan 2001) Tersedia di:<http://www.sign.ac.uk/sign49.pdf> [Diakses 22 November 2004]
Silverman, D.F. dan Propp, K.K. eds., 1990. The active interview. BeverlyHills, CA: Sage.
Smith, J., 1975. A source of information. Dalam: W. Jones, ed. 2000. One hundred and one ways to find information about health. Oxford: Oxford University Press. Ch.2.
Sommerville, I., 2011. Software engineering. 9th ed. London: Addison-Wesley.
Sudirman, Z., 2011. Pembahasan tentang sitasi dan perujukan. [surat] (Komunikasi personal, 11 Juni 2011). 
Tanenbaum, A.S., 1998. Organisasi komputer terstruktur, jilid 1. Diterjemahkan dari Bahasa Inggris oleh T.A.H Al-Hamdany. 2001. Jakarta: Salemba Teknika.
Thompson, A. dan Thomson, B., (in press) Innocent or guilty: a studi to ascertain the status of convicts in highly uncertain situations. Journal of Crime Scene Investigation. (Diterima untuk publikasi Januari 2002). 
Undang-undang Republik Indonesia nomor 12 tahun 2012 tentang Pendidikan Tinggi. Jakarta: Kementerian Sekretariat Negara Republik Indonesia.
UNDESA (United Nations Department of Economic and Social Affairs), 2005. 6th Global forum for reinventing government: towards participatory dan transparent governance. Seoul, Republic of Korea, 24-27 May 2005. New York: United Nations. 
	[1] Grand View Research. (2024). Fitness App Market Size, Share & Trends Analysis Report. Retrieved from https://www.grandviewresearch.com/industry-analysis/fitness-app-market
	[2] Market Research. (2025). Fitness App Market Size, Share & Trends Analysis Report By Type. Retrieved from https://www.marketresearch.com/Grand-View-Research-v4060/Fitness-App-Size-Share-Trends-42906284/
	[3] Straits Research. (2023). Fitness App Market: Country Insights. Retrieved from https://straitsresearch.com/report/fitness-app-market
	[4] Detik Inet. (2023, Juni 1). Pasar Smartphone Indonesia Turun 11,9% di Kuartal I 2023. Retrieved from https://inet.detik.com/business/d-6751251/pasar-smartphone-indonesia-turun-11-9-di-kuartal-i-2023
	[5] Katadata. (2023, Juni 4). Laporan IDC: Pasar Smartphone Indonesia Merosot 11.9% Kuartal I 2023. Retrieved from https://katadata.co.id/digital/gadget/647d9f4c3ed6f/laporan-idc-pasar-smartphone-indonesia-merosot-119-kuartal-i-2023
	[6] Selular.ID. (2023, Mei). IDC: Top 5 Brand Smartphone di Indonesia Q1-2023. Retrieved from https://selular.id/2023/05/idc-top-5-brand-smartphone-di-indonesia-q1-2023-oppo-bukan-1/
	[7] Chen, S., & Yang, R. R. (2020). Pose Trainer: Correcting Exercise Posture using Pose Estimation. arXiv:2006.11718 [cs.CV]. Retrieved from https://arxiv.org/abs/2006.11718
	[8] Tharatipyakul, A., et al. (2024). Deep Learning-Based Human Body Pose Estimation in Healthcare Applications. Heliyon. Retrieved from https://www.sciencedirect.com/science/article/pii/S2405844024126205
	[9] Appiah, K. E., et al. (2024). A Mobile-Phone Pose Estimation for Gym-Exercise Form Correction and Feedback Delivery. VISAPP 2024 Conference Proceedings. Retrieved from https://eprints.whiterose.ac.uk/id/eprint/210366/1/VISAPP_2024_266_CR.pdf
	[10] Hede, S., et al. (2024). Human Pose Estimation & Correction During Exercise and Movement Analysis. International Journal of Scientific Research and Engineering Technology (IJSRET). Retrieved from https://ijsret.com/wp-content/uploads/IJSRET_V11_issue3_1052.pdf
	[11] TensorFlow Blog. (2021, Agustus 15). Pose Estimation and Classification on Edge Devices with MoveNet and TensorFlow Lite. Retrieved from https://blog.tensorflow.org/2021/08/pose-estimation-and-classification-on-edge-devices-with-MoveNet-and-TensorFlow-Lite.html
	[12] TensorFlow Lite. (2024, Januari 15). Pose Estimation Overview. Retrieved from https://www.tensorflow.org/lite/examples/pose_estimation/overview
	[13] Bazarevsky, V., & Grishchenko, I. (2020). On-Device, Real-Time Body Pose Tracking with MediaPipe BlazePose. Google Research Blog. Retrieved from https://research.google/blog/on-device-real-time-body-pose-tracking-with-mediapipe-blazepose/
	[14] Raju, K. (2022). Exercise Detection and Tracking Using MediaPipe BlazePose with Spatial-Temporal Graph Convolutional Networks. Dublin City University Thesis. Retrieved from https://norma.ncirl.ie/6272/1/krishnanunniraju.pdf

PERSYARATAN FISIK DAN TATA LETAK
Kertas
Kertas yang digunakan adalah HVS 70 mg berukuran A4. Apabila terdapat gambar-gambar yang menggunakan kertas berukuran lebih besar dari A4, hendaknya dilipat sesuai dengan aturan yang berlaku. Pengetikan hanya dilakukan pada satu muka kertas, tidak bolak balik. 
Margin
Batas pengetikan naskah adalah sebagai berikut :
Margin kiri: 4 cm
Margin atas: 3 cm
Margin kanan: 3 cm 
Margin bawah: 3 cm 
Jenis dan Ukuran Huruf 
Jenis huruf yang dipakai dalam skripsi adalah Calibri dengan ketentuan sebagai berikut:
Judul bab pada level 1 berukuran 16 pt
Judul subbab pada level 2 berukuran 14 pt
Judul subbab pada level 3 berukuran 14 pt
Judul subbab pada level 4 berukuran 12 pt
Badan teks berukuran 12 pt
Penggunaan jenis dan ukuran ini harus konsisten. Untuk memudahkan memelihara konsistensi sekaligus penyusunan struktur skripsi, fasilitas seperti styles dan multilevel list dalam program pengolah kata dapat digunakan. Sebuah template untuk skripsi ini telah disediakan untuk membantu mahasiswa. Styles dan multilevel list dalam template tersebut sudah dirancang untuk jenis dan ukuran huruf yang disyaratkan.  
Spasi
Jarak  standar antar  baris  dalam  badan teks adalah satu spasi.  Jarak antar paragraf, antara judul bab dan judul subbab, antara judul subbab dan badan teks, dan seterusnya, dapat dilihat pada masing-masing style yang digunakan dan tersedia dalam template untuk skripsi ini. 
Kepala Bab dan Subbab 
Kepala bab terdiri dari kata “BAB” yang diikuti dengan nomor bab dan judul dari bab tersebut, misalnya “BAB 1 PENDAHULUAN” . Kepala subbab diawali dengan nomor sesuai tingkat hirarkinya dan diikuti dengan judul subbab, misalnya “1.2 Rumusan masalah”. Penomoran subbab disarankan tidak lebih dari 4 level (maksimal subbab X.X.X.X). Kepala bab dan subbab tidak boleh mengandung widow atau orphan sehingga nampak menggantung atau terputus di bagian awal atau akhir sebuah halaman. Widow adalah sebuah paragraf dengan hanya satu baris pertama pada akhir halaman sedangkan sisanya berada pada halaman berikutnya. Orphan adalah baris terakhir dari satu paragraf yang tertulis pada awal suatu halaman sedangkan baris lainnya dari paragraf tersebut berada pada halaman sebelumnya. 
Nomor Halaman
Bagian   awal   skripsi   menggunakan   nomor   halaman berupa angka Romawi kecil (i, ii, iii, iv, dan seterusnya) yang dimulai dari sampul dalam. Sedangkan bagian utama dan bagian akhir skripsi menggunakan nomor halaman berupa angka Arab (1, 2, 3, dan seterusnya) yang dimulai dari bab 1. Semua nomor halaman diletakkan di tengah bawah. 


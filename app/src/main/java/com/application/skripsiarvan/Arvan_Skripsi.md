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
ANALISIS KINERJA PERANGKAT KERAS UNTUK INFERENSI POSE ESTIMATION REAL-TIME PADA APLIKASI KEBUGARAN ANDROID

SKRIPSI 

Diajukan untuk memenuhi sebagian persyaratan 
memperoleh gelar Sarjana Komputer 

Disusun Oleh : 
Arvan Yudhistia Ardana
NIM: 225150200111014

Skripsi ini telah diuji dan dinyatakan lulus pada
2 Mei 2026
Telah diperiksa dan disetujui oleh:


Dosen Pembimbing I





Nama Dosen Pembimbing 1
NIK: 123456789 
/*jika terdapat NIK saja*/ 	Dosen Pembimbing 2





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

Malang, 1 Mei 2026
 

    
¬ 
Arvan Yudhistia Ardana
NIM: 225150200111014
PRAKATA
Bagian ini memuat pernyataan resmi untuk menyampaikan rasa terima kasih penulis kepada berbagai pihak yang telah membantu penyelesaian skripsi ini. Nama-nama penerima ucapan terima kasih sebaiknya dituliskan lengkap, termasuk gelar akademik, dan pihak-pihak yang tidak terkait dihindari untuk dituliskan. Bahasa yang digunakan seharusnya mengikuti kaidah bahasa Indonesia yang baku. Prakata boleh diakhiri dengan paragraf yang menyatakan bahwa penulis menerima kritik dan saran untuk pengembangan penelitian selanjutnya. Terakhir, prakata ditutup dengan mencantumkan kota dan tanggal penulisan prakata, lalu diikuti dengan kata “Penulis”.

Malang, 2 Mei 2026

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
PENGESAHAN	ii
PERNYATAAN ORISINALITAS	iii
PRAKATA	iv
ABSTRAK	v
ABSTRACT	vi
DAFTAR ISI	vii
DAFTAR TABEL	xi
DAFTAR GAMBAR	xii
DAFTAR LAMPIRAN	xiii
BAB 1 PENDAHULUAN	1
1.1 Latar Belakang	1
1.2 Rumusan Masalah	3
1.3 Hipotesis	3
1.4 Tujuan	4
1.5 Manfaat	4
1.6 Batasan Masalah	5
1.7 Sistematika Pembahasan	6
BAB 2 LANDASAN KEPUSTAKAAN	7
2.1 Kajian Pustaka	7
2.1.1 Gap Penelitian	10
2.2 Landasan Teori	10
2.2.1 Pose Estimation	10
2.2.2 Biomekanik Gerakan Squat dan Push-up	11
2.2.3 TensorFlow Lite	12
2.2.4 Sistem Delegate TensorFlow Lite	13
2.2.5 Model MoveNet	14
2.2.6 Model BlazePose Lite	14
2.2.7 Thermal Throttling pada Inferensi Mobile	15
2.2.8 Pengembangan Aplikasi Bergerak Android	15
2.2.9 Metrik Kinerja Aplikasi Bergerak	16
2.2.10 Analisis Varians (ANOVA)	17
BAB 3 METODOLOGI PENELITIAN	19
3.1 Jenis Penelitian	19
3.2 Desain Eksperimen	19
3.3 Variabel Penelitian	20
3.3.1 Variabel Bebas	20
3.3.2 Variabel Terikat	20
3.3.3 Variabel Kontrol	21
3.4 Tahap Persiapan	23
3.4.1 Tahap Pengembangan Aplikasi	24
3.4.2 Tahap Pengumpulan Data	24
3.4.3 Tahap Analisis Data	25
3.5 Metodologi Analisis Statistik	26
3.5.1 Uji Asumsi	26
3.5.2 Three-Way Repeated-Measures ANOVA	26
3.5.3 Uji Post-hoc Tukey HSD	27
3.5.4 Analisis Trade-off Deskriptif	27
3.6 Matriks Kondisi Eksperimen	27
3.7 Instrumen dan Alat Penelitian	28
BAB 4 PERANCANGAN SISTEM	29
4.1 Analisis Kebutuhan Sistem	29
4.1.1 Identifikasi Aktor Sistem	29
4.1.2 Kebutuhan Fungsional	29
4.1.3 Kebutuhan Non-Fungsional	30
4.2 Use Case	31
4.2.1 Use Case Diagram	31
4.2.2 Spesifikasi Use Case	31
4.3 Perancangan Arsitektur Sistem	36
4.3.1 Diagram Arsitektur Sistem (3-Layer Architecture)	36
4.3.2 Class Diagram	38
4.4 Perancangan Alur Sistem	40
4.4.1 Activity Diagram Alur utama	41
4.4.2 Activity Diagram Alur Proses Inferensi	43
4.4.3 Sequence Diagram	44
4.5 Perancangan Algoritma	51
4.5.1 Flowchart Alur Inferensi	51
4.5.2 State Machine Repetisi	53
4.5.3 Error Handling & Fallback	54
4.6 Perancangan Anttarmuka	56
4.6.1 Wireframe Layar Utama	56
4.7 Strategi Pengukuran Metrik	57
4.7.1 Pengukuran Latensi Inferensi	57
4.7.2 Profiling Sumber Daya Sistem	58
4.7.3 Struktur File CSV Output	59
BAB 5 IMPLEMENTASI SISTEM	62
5.1 Lingkungan Pengembangan	62
5.2 Implementasi Antarmuka	62
5.2.1 Struktur Aktivitas Utama	62
5.2.2 Layar Deteksi & Panel Kontrol	62
5.3 Implementasi Inferensi TFLite	62
5.3.1 Pemuatan Model & Inisialisasi Delegate	62
5.3.2 Antarmuka Detektor	62
5.3.3 MoveNet Lightning Detector	62
5.3.4 BlazePose Lite Detector	62
5.3.5 Letterbox Padding	62
5.4 Implementasi Algoritma Repetisi	62
5.4.1 Kalkulator Sudut	62
5.4.2 Detektor Squat	62
5.4.3 Detektor Push-Up	62
5.5 Implementasi Logging & Profilling	62
5.5.1 Pengukuran Latensi Inferensi	62
5.5.2 Profilling Sumber Daya	62
5.5.3 Pencatatan & Ekspor CSV	62
5.6 Integrasi Komponen	62
BAB 6 PENGUJIAN DAN PEMBAHASAN	63
6.1 Persiapan Pengujian	63
6.2 Statistik Deskriptif	63
6.3 Validasi Fungsional Penghitungan Repetisi	63
6.4 Uji Asumsi ANOVA	63
6.5 Analisis Varians (Three-Way ANOVA)	63
6.5.1 Latensi Inferensi	63
6.5.2 Utilisasi CPU	63
6.5.3 Penggunaan Memori	63
6.5.4 Ringkasan Signifikansi	63
6.6 Uji Post-hoc Tukey HSD	63
6.7 Analisis Trade-off Deskriptif	63
6.7.1 Kriteria Kelayakan Real-time	63
6.7.2 Profil Trade-off Kualitatif	63
6.7.3 Rekomendasi Per Skenario	63
6.8 Pembahasan	63
6.8.1 Pengaruh Delegate Terhadap Kinerja	63
6.8.2 Perbandingan Model	63
6.8.3 Pengaruh Jenis Latihan	63
6.8.4 Konfigurasi Optimal	63
BAB 7 Penutup	64
7.1 Kesimpulan	64
7.2 Saran	64
DAFTAR REFERENSI	65
DAFTAR TABEL
Tabel ‎2.1 Pembentukan bilangan random untuk Indeks Masa Tubuh (IMT)	18
Tabel ‎2.2 Contoh tabel 2	19

DAFTAR GAMBAR
Gambar ‎2.1 Pengaruh nilai K terhadap akurasi	21

DAFTAR LAMPIRAN
LAMPIRAN A PERSYARATAN FISIK DAN TATA LETAK	38
A.1 Kertas	38
A.2 Margin	38
A.3 Jenis dan Ukuran Huruf	38
A.4 Spasi	38
A.5 Kepala Bab dan Subbab	38
A.6 Nomor Halaman	39
LAMPIRAN B PENGGUNAAN BAHASA	40

 
PENDAHULUAN
Latar Belakang
Kesadaran masyarakat akan gaya hidup sehat dan pesatnya adopsi teknologi telah mendorong pertumbuhan masif aplikasi kebugaran berbasis mobile. Mengutip data Grand View Research (2024), valuasi pasar global aplikasi kebugaran diproyeksikan melonjak dari USD 10,59 miliar pada tahun 2024 menjadi 33,58 miliar pada tahun 2033 dengan tingkat  peertumbuhan tahunan (CAGR) sebesar 13,59%. Lonjakan ini tidak terlepas dari inovasi artificial intelligence (AI) dan machine learning yang kini memungkinkan aplikasi untuk memberikan evaluasi personal secara real-time kepada penggunanya.
Salah satu fitur AI yang paling relevan untuk aplikasi kebugaran adalah real-time pose estimation, teknologi computer vision yang mendeteksi dan melacak posisi sendi-sendi utama tubuh manusia dari kamera secara langsung. Dengan kemampuan ini, aplikasi dapat memberikan umpan balik secara instan kepada pengguna mengenai postur dan teknik gerakan selama latihan, baik berupa descriptive feedback yang menginformsikan kesalahan postur maupun prescriptive feedback yang mengarahkan cara memperbaikinya (Essuming, 2024; Tharatipyakul, Srikaewsiew and Pongnumkul, 2024). Hal ini menjadikan pose estimation komponen yang sangat bernilai untuk mencegah cedera dan meningkatkan efektivitas latihan (Chen dan Yang, 2020).
Namun, menjalankan pose estimation secara real-time di perangkat mobile bukan hal yang mudah. Proses inferensi model deep learning menuntut sumber daya komputasi yang besar, sementara perangkat mobile memiliki keterbatasan di sisi CPU, memori, dan baterai. Terlebih, pada kondisi berkepanjangan, beban komputasi yang tinggi dapat menyebabkan thermal throttling, yaitu penurunan frekuensi prosesor akibat panas berlebih, hal ini berdampak pada penuruan frame rate dan responsivitas aplikasi (Lee et al., 2019; Benoit-Cattin and Fernández-Berni, 2020).
Kondisi ini menjadi semakin kritis apabila dikaitkan dengan realitas karakteristik pasar smartphone di Indonesia. Berdasarkan laporan IDC (2023), sekitar 76% pasar smartphone Indonesia di kuartal I 2023 dikuasai oleh perangkat dengan harga di bawah USD 200 (DetikInet, 2023; Katadata, 2023; Selular.ID, 2023). Segmen low-to-mid range ini umumnya dilengkapi prosesor dan GPU dengan kapablitas terbatas dibandingkan perangkat kelas atas. Dengan demikian, apabila aplikasi kebugaran berbasis pose estimation tidak dioptimasi untuk karakteristik perangkat ini, mayoritas pengguna Indonesia tidak akan dapat menikmati fitur ini secara layak.
Dua model pose estimation yang paling relevan dan banyak digunakan untuk konteks mobile adalah MoveNet Lightning dan BlazePose Lite, keduanya dikembangkan oleh Google. MoveNet Lightning menggunakan pendekatan bottom-up dengan mekanisme smart cropping dan menerima input beresolusi 192×192 piksel. Model ini mencapai latensi sekitar 25 ms per frame pada perangkat Pixel 5 menggunakan GPU delegate dengan kuantisasi FP16, menjadikannya pilihan utama untuk aplikasi yang memprioritaskan kecepatan (TensorFlow Blog, 2021; TensorFlow Lite, 2024). BlazePose Lite, di sisi lain, menggunakan pendekatan two-stage detector-tracker dan mendeteksi 33 keypoints yang lebih rinci dibandingkan 17 keypoints standar COCO pada MoveNet, dengan latensi inferensi di kisaran 25–33 ms pada Pixel 2 menggunakan GPU (Bazarevsky and Grishchenko, 2020). Perbedaan arsitektur dan jumlah keypoints keduanya menjadikan perbandingan antara dua model ini menarik untuk dievaluasi dalam konteks aplikasi kebugaran.
Untuk menjembatani kebutuhan performa tinggi dengan keterbatasan perangkat keras, TensorFlow Lite (yang sejak September 2024 direbrand oleh Google menjadi LiteRT) menyediakan mekanisme delegate, sebuah cara untuk mendistribusikan beban komputasi inferensi ke unit pemrosesan yang paling sesuai. Terdapat tiga opsi utama: GPU Delegate yang memanfaatkan akselerasi grafis dan dapat memberikan peningkatan kecepatan hingga 5x untuk operasi paralel (Google AI Edge, 2024); XNNPACK Delegate yang mengoptimalkan eksekusi CPU melalui instruksi SIMD seperti ARM NEON dengan peningkatan performa rata-rata 2,3x dibanding baseline CPU standar (Google AI Edge, 2024); dan CPU Baseline yang menjadi fallback universal tanpa akselerasi tambahan. Pemilihan delegate yang tepat merupakan keputusan arsitektur yang kritis karena secara langsung memengaruhi latensi, konsumsi daya, dan stabilitas termal aplikasi (Ignatov et al., 2018).
Penelitian terdahulu telah memberikan sejumlah gambaran awal mengenai karakteristik masing-masing delegate. Jiang et al. (2020) menemukan bahwa GPU mobile memberikan akselerasi rata-rata 1,9x dibandingkan CPU, meskipun efisiensi shader core tidak selalu optimal pada semua perangkat. Studi mengenai konsumsi energi menunjukkan bahwa GPU cenderung mengonsumsi daya lebih tinggi namun menghasilkan throughput yang lebih besar, sedangkan CPU lebih efisien untuk model berukuran kecil (Ignatov et al., 2018; Lee et al., 2019). Evaluasi komprehensif dari MLPerf Mobile (Reddi et al., 2022) juga menunjukkan bahwa keunggulan relatif setiap delegate sangat bergantung pada kombinasi model, ukuran input, dan karakteristik hardware perangkat target.
Namun demikian, sebagian besar penelitian tersebut dilakukan pada perangkat kelas menengah ke atas seperti Google Pixel atau Samsung Galaxy flagship, dan bukan pada perangkat yang merepresentasikan pasar Indonesia. Selain itu, penelitian yang secara spesifik membandingkan MoveNet Lightning dan BlazePose Lite pada berbagai konfigurasi delegate di perangkat low-to-mid range masih sangat terbatas (Bazarevsky and Grishchenko, 2020; Yu et al., 2023). Ketiadaan studi semacam ini menyulitkan pengembang dalam mengambil keputusan teknis yang tepat saat membangun aplikasi kebugaran untuk pengguna Indonesia.
Selain kesenjangan pada sisi perangkat dan model, terdapat pula kesenjangan metodologis. Sebagian besar penelitian hanya berfokus pada satu metrik tunggal,  biasanya akurasi atau kecepatan inferensi saja, tanpa mempertimbangkan trade-off yang lebih komprehensif antara latensi, konsumsi sumber daya, dan stabilitas performa dalam durasi penggunaan nyata (Turner et al., 2024). Padahal, dalam konteks aplikasi kebugaran, ketiganya sama pentingnya: akurasi dibutuhkan untuk keamanan pengguna, latensi rendah diperlukan untuk feedback yang responsif, dan efisiensi daya penting agar aplikasi dapat digunakan sepanjang sesi latihan tanpa perangkat menjadi panas berlebih.
Gerakan squat dan push-up dipilih sebagai beban kerja uji dalam penelitian ini karena keduanya merupakan gerakan fundamental yang paling umum diimplementasikan dalam penelitian pose estimation berbasis kebugaran (Heo et al., 2022; Bao et al., 2024). Selain itu, keduanya merepresentasikan dua pola gerakan yang berbeda secara biomekanik, dalam hal ini, squat sebagai gerakan vertikal yang berpusat pada sendi lutut, sementara push-up sebagai gerakan horizontal yang berpusat pada sendi siku, sehingga dapat merepresentasikan variasi workload yang lebih beragam dalam pengujian.
Berdasarkan kesenjangan yang telah diidentifikasi, penelitian ini dirancang untuk melakukan evaluasi sistematis terhadap kinerja tiga konfigurasi delegate TensorFlow Lite (GPU, XNNPACK, dan CPU Baseline) pada dua model pose estimation (MoveNet Lightning dan BlazePose Lite) di perangkat Android low-to-mid range yang representatif terhadap pasar Indonesia, dalam konteks penggunaan nyata aplikasi kebugaran. Hasil penelitian ini diharapkan dapat memberikan panduan empiris bagi pengembang dalam memilih konfigurasi teknis yang optimal sesuai target perangkat, sehingga aplikasi kebugaran berbasis AI dapat diakses secara layak oleh mayoritas pengguna di Indonesia.
Rumusan Masalah
	Bagaimana perbandingan kinerja inferensi (latensi dan throughput) serta konsumsi sumber daya (utilisasi CPU, memori) antara GPU delegate, CPU with XNNPACK delegate, dan CPU-only execution pada TensorFlow Lite untuk real-time pose estimation dalam aplikasi kebugaran Android?
	Bagaimana perbandingan kinerja antara model MoveNet Lightning dan BlazePose Lite pada setiap konfigurasi delegate dalam konteks aplikasi kebugaran mobile?
	Konfigurasi delegate dan model manakah yang memberikan performa terbaik secara keseluruhan berdasarkan analisis trade-off antara kinerja, efisiensi sumber daya, dan akurasi untuk perangkat Android low-to-mid range?
Hipotesis
Berdasarkan kajian literatur mengenai karakteristik teknis masing-masing delegate TensorFlow Lite dan arsitektur kedua model pose estimation, penelitian ini merumuskan empat hipotesis sebagai berikut.
H1: Terdapat perbedaan yang signifikan dalam latensi inferensi dan throughput antar konfigurasi delegate, di mana GPU Delegate diperkirakan menghasilkan latensi terendah dibandingkan XNNPACK dan CPU Baseline, khususnya pada model MoveNet Lightning yang didominasi operasi konvolusional yang dapat diparalelkan secara efisien di GPU (Jiang et al., 2020; Google AI Edge, 2024).
H2: Terdapat perbedaan yang signifikan dalam utilisasi sumber daya antar konfigurasi delegate, di mana GPU Delegate diperkirakan menghasilkan utilisasi CPU yang lebih rendah namun dengan konsumsi memori yang lebih tinggi akibat alokasi buffer GPU, sementara XNNPACK Delegate diperkirakan lebih efisien dalam konsumsi daya dibandingkan GPU Delegate (Ignatov et al., 2018; Lee et al., 2019).
H3: Tidak terdapat perbedaan yang signifikan dalam metrik performa inferensi antara jenis latihan squat dan push-up, karena kedua gerakan diproses melalui alur inferensi yang identik dan model tidak membedakan jenis gerakan pada tahap inferensi (pose-agnostic inference).
H4: Terdapat interaksi yang signifikan antara faktor delegate dan faktor model, di mana keunggulan akselerasi GPU Delegate diperkirakan lebih besar pada BlazePose Lite dibandingkan MoveNet Lightning, mengingat BlazePose Lite memiliki kompleksitas komputasi yang lebih tinggi akibat pendekatan two-stage detector-tracker dan jumlah keypoints yang lebih banyak (Bazarevsky and Grishchenko, 2020).
Tujuan
Berdasarkan rumusan masalah yang telah diuraikan, penelitian ini bertujuan untuk:
	Mengukur dan membandingkan kinerja komputasi—seperti latensi inferensi, throughput (FPS), serta penggunaan CPU dan memori—antara GPU delegate, CPU with XNNPACK, dan eksekusi CPU-only pada TensorFlow Lite.
	Mengevaluasi perbedaan performa antara model MoveNet Lightning dan BlazePose Lite saat dijalankan menggunakan berbagai konfigurasi delegate tersebut di lingkungan aplikasi mobile.
	Menemukan kombinasi delegate dan model yang paling optimal, yakni yang mampu memberikan keseimbangan (trade-off) terbaik antara performa kecepatan, efisiensi sumber daya, dan kelancaran aplikasi pada perangkat Android kelas menengah ke bawah (low-to-mid range).
Manfaat
Penelitian ini diharapkan dapat memberikan kontribusi nyata bagi beberapa pihak berikut:
	Bagi Pengembang Perangkat Lunak (Developer): Menyediakan data pengujian yang objektif sebagai acuan teknis. Data ini akan sangat membantu pengembang dalam memilih konfigurasi TensorFlow Lite yang paling efisien, sehingga mereka dapat merancang aplikasi AI kebugaran yang tetap responsif dan hemat baterai meskipun dijalankan pada smartphone dengan spesifikasi terbatas.
	Bagi Akademisi dan Peneliti: Mengisi celah riset terkait evaluasi performa on-device machine learning khusus di kelas perangkat low-end. Selain itu, metode pengujian dan benchmarking yang digunakan dalam penelitian ini dapat diadaptasi untuk menguji arsitektur model AI lainnya di masa depan.
	Bagi Industri Teknologi Kesehatan: Memberikan wawasan teknis untuk menekan beban komputasi sistem. Dengan pemahaman ini, industri dapat merancang layanan kebugaran pintar yang lebih inklusif dan menjangkau pasar yang lebih luas, terutama pengguna di segmen menengah ke bawah.
	Bagi Pengguna Akhir: Prototipe aplikasi yang dibangun dalam penelitian ini menunjukkan penerapan pose estimation untuk membantu pengguna menghitung repetisi latihan squat dan push-up secara otomatis. Walaupun aplikasi tidak diposisikan sebagai produk kebugaran final, fitur deteksi pose dan penghitungan repetisi memberikan gambaran manfaat langsung teknologi ini pada skenario latihan sederhana.
Batasan Masalah
Agar penelitian lebih fokus dan pembahasannya tidak melebar, ruang lingkup pengujian dibatasi pada parameter berikut:
	Lingkungan Pengembangan: Implementasi sistem dibangun menggunakan library TensorFlow Lite versi 2.14.0 (atau rilis stabil terbaru) pada sistem operasi Android, dengan target pengembangan API Level 34 (Android 14).
	Model Machine Learning: Eksperimen hanya menggunakan dua varian model pre-trained dengan format kuantisasi FP16, yaitu:
	MoveNet Lightning: Model berukuran sangat ringan dengan resolusi input 192x192, digunakan untuk merepresentasikan beban komputasi yang rendah.
	MediaPipe BlazePose Lite: Model dengan topologi deteksi 33 keypoints tubuh, digunakan untuk merepresentasikan beban komputasi menengah.
	Perangkat Uji (Testbed): Pengujian akan dilakukan secara langsung pada dua jenis smartphone untuk melihat perbandingan performa di segmen keras yang berbeda:
	Perangkat Low-End: Samsung Galaxy A06 (Chipset MediaTek Helio G85, GPU Mali-G52, RAM 4GB) sebagai perwakilan perangkat dengan sumber daya terbatas.
	Perangkat Mid-Range: Samsung Galaxy A33 5G (Chipset Exynos 1280, GPU Mali-G68, RAM 6/8GB) sebagai perwakilan perangkat modern yang memiliki spesifikasi menengah.
	Fokus pengukuran kinerja utama penelitian ini adalah kinerja komputasi, meliputi latensi inferensi, FPS, utilisasi CPU, dan penggunaan memori. Evaluasi akurasi model pose estimation seperti mAP berada di luar lingkup penelitian. Namun, penelitian ini tetap melakukan pengujian fungsional terbatas kepada end user untuk memvalidasi ketepatan penghitungan repetisi squat dan push-up pada skenario penggunaan nyata. Hasil pengujian fungsional tersebut dilaporkan secara deskriptif dan tidak dimasukkan ke dalam analisis ANOVA.
Sistematika Pembahasan
Untuk memudahkan pemahaman alur penelitian, laporan skripsi ini disusun menjadi tujuh bab dengan rincian sebagai berikut:
	BAB 1 Pendahuluan: Menjelaskan latar belakang perlunya optimasi TensorFlow Lite pada perangkat low-to-mid range, perumusan masalah, tujuan dan manfaat penelitian, batasan ruang lingkup pengujian, serta sistematika penulisan.
	BAB 2 Landasan Kepustakaan: Mengkaji literatur penelitian terdahulu yang sejalan, serta teori-teori dasar mengenai arsitektur pose estimation, MoveNet, BlazePose, mekanisme kerja delegate di TensorFlow Lite, dan metrik kinerja komputasi mobile.
	BAB 3 Metodologi Penelitian: Memaparkan tahapan desain eksperimen kuantitatif, penentuan variabel pengujian, teknik pengumpulan metrik performa, dan metode analisis statistik (seperti uji ANOVA) yang digunakan untuk memvalidasi data.
	BAB 4 Perancangan Sistem: Menguraikan rancangan aplikasi testbed yang digunakan sebagai alat ukur, meliputi arsitektur perangkat lunak, alur logika program (flowchart) untuk inferensi model, dan rancangan antarmuka pengguna.
	BAB 5 Pengembangan Aplikasi: Menjelaskan tahap realisasi pembuatan aplikasi berdasarkan rancangan sebelumnya, mencakup proses integrasi library AI dan penulisan kode untuk pencatatan (logging) metrik performa sistem.
	BAB 6 Pengujian dan Pembahasan: Menyajikan tabulasi data hasil pengujian performa secara terstruktur, penjabaran hasil analisis statistik, serta diskusi mendalam untuk membandingkan kinerja setiap kombinasi model dan delegate.
	BAB 7 Penutup: Menarik kesimpulan akhir untuk menjawab rumusan masalah berdasarkan bukti empiris yang didapat, menyebutkan keterbatasan selama penelitian, serta memberikan saran bagi riset pengembangan selanjutnya.

LANDASAN KEPUSTAKAAN
Kajian Pustaka
Kajian Pustaka dilakukan untuk mengidentifikasi penelitian yang telah ada di bidang analisis kinerja aplikasi mobile, khususnya yang berkaitan dengan framework TFLite dan pose estimation. Dari penelusuran tersebut, beberapa studi menjadi rujukan utama yang menyoroti celah penelitian yang hendak diisi oleh studi ini. Berikut hasil dari kajian pustaka disajikan dalam Tabel 2.1
Tabel 2.1 Penelitian Terdahulu            
Penulis	Judul	Metode	Hasil	Gap Penelitian
Bazarevsky et al. (2020)	BlazePose: On-device Real-time Body Pose tracking	CNN multitahap dengan detector-tracker approach, 33 keypoints 3D	mAP 66,9%, 33ms inferensi pada Pixel 2 GPU	Hanya mengevaluasi GPU delegate tanpa membandingkan alternatif optimasi lainnya seperti CPU, XNNPACK atau NNAPI
Yu et al. (2023)	MovePose: A High-performance Human Pose Estimation Algorithm on Mobile and Edge Devices	Optimasi pose estimation untuk edge/mobile devices	mAP 68,0%, 11+ fps pada Android Snapdragon 8	Tidak menspesifikasi jenis delegate yang digunakan dan tidak menganalisis trade-off resource consumption
Turner et al. (2024)	A Mobile-Phone Pose Estimation for Gym-Exercise Form Correction	Machine learning dengan deteksi anomali untuk koreksi postur gym	Sistem dapat mendeteksi postur salah dan memberikan koreksi valid dalam video real-time	Tidak menyediakan metrik kinerja kuantitatif (latency, CPU, memory) yang esensial untuk evaluasi mobile
Reddi et al. (2022)	MLPerf Mobile INference Benchmark	Evaluasi komprehensif TensorFlow Lite delegates dan PyTorch backends pada 174 skenario	GPU delegate memberikan speedup signifikan untuk model besar, namun overhead untuk model kecil	Bersifat general ML benchmarking, tidak spesifik untuk domain pose estimation dan aplikasi real-time
Ignatov et al. (2019)	AI Benchmark: All About Deep Learning on Smartphones in 2019	Benchmarking komprehensif 10+ model deep learning pada 48 perangkat Android menggunakan TFLite delegates (CPU, GPU, NNAPI)	GPU delegate 2-5x lebih cepat dari CPU; konsumsi daya GPU lebih tinggi; performa sangat bergantung pada chipset	Tidak spesifik untuk pose estimation; tidak mengevaluasi XNNPACK delegate; tidak mempertimbangkan konteks aplikasi kebugaran real-time
Jiang et al. (2020)	Characterizing the Deployment of Deep Neural Networks on Commercial Edge Devices	Analisis perbandingan performa inferensi pada CPU dan GPU mobile menggunakan TFLite pada 5 perangkat edge komersial	GPU memberikan speedup rata-rata 1.9x; utilisasi shader core tidak selalu optimal; latensi GPU tinggi untuk model kecil	Tidak mengevaluasi NNPACK delegate; model yang diuji bukan pose estimation; tidak mencakup perangkat low-end pasar Indonesia

Bazarevsky and Grishchenko (2020) mengembangkan BlazePose sebagai solusi on-device, real-time pose tracking pada perangkat mobile menggunakan arsitektur dua tahap: pose detector yang menemukan region of interest (ROI) tubuh manusia, dan pose tracker yang memperkirakan 33 keypoints 3D di dalam ROI tersebut. Hasil pengujian pada Google Pixel 2 menunjukkan model dapat berjalan di atas 30 FPS dengan akurasi sekitar 66,9% mAP. Namun, penelitian ini hanya mengevaluasi konfigurasi GPU delegate tanpa perbandingan sistematis terhadap delegate lain, serta tidak mengukur metrik konsumsi daya dan memori secara kuantitatif.
Yu et al. (2023) memperkenalkan MovePose sebagai algoritme pose estimation berkinerja tinggi yang dioptimalkan untuk perangkat edge dan mobile. Model ini memanfaatkan kombinasi dekonvolusi untuk meningkatkan resolusi feature map, kernel konvolusi besar untuk memperluas receptive field, dan skema klasifikasi koordinat untuk meningkatkan presisi lokalisasi keypoints. Evaluasi pada dataset COCO menunjukkan MovePose mencapai mAP sekitar 68% dengan throughput lebih dari 11 FPS pada perangkat Snapdragon 8. Meski demikian, studi ini tidak mendeskripsikan secara eksplisit delegate inferensi yang digunakan, dan tidak mengevaluasi konsumsi sumber daya perangkat.
Reddi et al. (2022) melalui MLPerf Mobile menyajikan benchmark suite untuk mengukur kinerja inferensi mobile pada ratusan kombinasi model, framework, dan backend termasuk TensorFlow Lite delegates dan PyTorch. Hasilnya menunjukkan bahwa GPU delegate menguntungkan untuk model besar, tetapi overhead komunikasi CPU-GPU dapat membuatnya kurang efisien untuk model kecil. Studi ini memberikan gambaran umum yang berguna mengenai karakteristik delegate, tetapi tidak membahas kasus penggunaan spesifik seperti pose estimation atau aplikasi kebugaran.
Ignatov et al. (2019) mengusulkan AI Benchmark sebagai tolok ukur terstandarisasi untuk evaluasi kinerja deep learning pada smartphone Android, mencakup lebih dari sepuluh model dan puluhan perangkat. Hasilnya menunjukkan bahwa performa inferensi sangat bergantung pada kombinasi model, backend (CPU, GPU, NNAPI), dan chipset, dengan GPU umumnya memberikan percepatan 2-5x namun dengan konsumsi daya lebih tinggi. Benchmark ini tidak fokus pada pose estimation dan belum mengevaluasi XNNPACK delegate.
Jiang et al. (2020) melakukan profiling inferensi deep learning pada GPU mobile menggunakan TensorFlow Lite dan menunjukkan bahwa GPU dapat memberikan percepatan rata-rata ~1,9x dibanding CPU, namun terdapat variasi besar antar perangkat. Mereka juga menggarisbawahi bahwa untuk model kecil, overhead GPU dapat mengurangi manfaat percepatan. Studi ini tidak menyentuh konteks pose estimation atau perangkat kelas bawah yang relevan untuk pasar Indonesia.
Turner et al. (2024) merancang sistem koreksi postur latihan gym berbasis pose estimation pada smartphone. Sistem menggunakan keypoints dari model pose estimation sebagai input ke modul deteksi anomali yang mengidentifikasi deviasi dari form latihan yang benar dan memberikan umpan balik korektif secara real-time. Walaupun relevan untuk konteks kebugaran, fokus utama penelitian ini adalah kualitas koreksi postur, bukan analisis kuantitatif terhadap latensi, utilisasi CPU/GPU, dan konsumsi memori di perangkat mobile.

Gap Penelitian
Berdasarkan kajian pustaka di atas, terdapat empat celah penelitian yang relevan dan menjadi landasan bagi penelitian ini.             
Pertama, belum terdapat penelitian yang secara sistematis menganalisis kinerja berbagai delegate TensorFlow Lite (CPU, GPU, XNNPACK) dalam konteks pose estimation untuk aplikasi kebugaran. Studi seperti Reddi et al. (2022) hanya membahas perbandingan delegate secara umum, sedangkan Bazarevsky and Grishchenko (2020) berfokus pada satu konfigurasi GPU delegate tanpa membandingkan trade-off kinerja dengan delegate lain.
Kedua, sebagian besar penelitian dilakukan pada perangkat kelas menengah ke atas dan skenario dataset standar, sehingga kurang merepresentasikan kondisi nyata pengguna perangkat low-to-mid range. Evaluasi dalam konteks aplikasi kebugaran yang berjalan secara kontinu dengan variasi kondisi penggunaan dan keterbatasan sumber daya perangkat masih jarang dilakukan (Ignatov et al., 2019).
Ketiga, sebagian besar studi hanya berfokus pada satu metrik seperti akurasi model atau kecepatan inferensi, tanpa mempertimbangkan trade-off komprehensif antara latensi, throughput, utilisasi CPU, penggunaan memori, dan konsumsi daya secara bersamaan. Dalam konteks aplikasi kebugaran, seluruh aspek ini sama pentingnya untuk menjamin pengalaman pengguna yang baik.
Keempat, belum ada penelitian yang secara eksplisit membandingkan MoveNet Lightning dan BlazePose Lite pada berbagai konfigurasi delegate di perangkat Android low-to-mid range yang merepresentasikan pasar Indonesia. Mengingat bahwa kombinasi arsitektur model dan delegate dapat menghasilkan profil kinerja yang sangat berbeda pada chipset yang berbeda (Jiang et al., 2020), studi komparatif di kelas perangkat ini menjadi penting untuk memberikan rekomendasi praktis bagi pengembang.
Landasan Teori
Landasan teori pada bab ini membahas konsep dan teknologi utama yang mendasari penelitian, meliputi: estimasi pose tubuh manusia, biomekanik gerakan squat dan push-up, framework TensorFlow Lite dan sistem delegate-nya, model MoveNet dan BlazePose Lite, fenomena thermal throttling pada inferensi mobile, pengembangan aplikasi Android sebagai platform implementasi, metrik kinerja aplikasi bergerak, serta analisis varians (ANOVA) sebagai metode analisis statistik.
Pose Estimation
Estimasi pose tubuh manusia (human pose estimation) adalah proses mendeteksi posisi titik-titik kunci (keypoints) tubuh — seperti kepala, bahu, siku, lutut, dan pergelangan kaki — dari citra atau video. Dalam konteks 2D pose estimation, setiap keypoint direpresentasikan sebagai pasangan koordinat (x, y) pada bidang gambar beserta confidence score yang menunjukkan tingkat keyakinan model terhadap deteksi tersebut. Model state-of-the-art umumnya mengikuti skema anotasi COCO yang terdiri dari 17 keypoints utama, meliputi hidung, kedua mata, kedua telinga, kedua bahu, kedua siku, kedua pergelangan tangan, kedua pinggul, kedua lutut, dan kedua pergelangan kaki (TensorFlow Blog, 2021).
Dalam konteks aplikasi kebugaran, pose estimation dimanfaatkan untuk tiga keperluan utama: (1) mendeteksi penyimpangan dari postur latihan yang benar sehingga dapat mengurangi risiko cedera dan meningkatkan efektivitas latihan; (2) melacak jumlah repetisi gerakan seperti squat, push-up, dan sit-up secara otomatis; serta (3) memberikan wawasan mengenai kualitas gerakan, range of motion, dan konsistensi latihan (Chen and Yang, 2020; Turner et al., 2024).
Implementasi pose estimation di perangkat mobile menghadapi tantangan terkait keterbatasan daya komputasi, memori, dan kapasitas baterai dibandingkan dengan komputer desktop atau server. Oleh karena itu, model yang ditujukan untuk perangkat mobile seperti MoveNet dan BlazePose dirancang dengan arsitektur yang lebih ringan dan dukungan akselerasi perangkat keras agar tetap mampu berjalan secara real-time (Bazarevsky and Grishchenko, 2020; TensorFlow Blog, 2021).
Biomekanik Gerakan Squat dan Push-up
Deteksi repetisi dan penilaian kualitas gerakan dalam aplikasi kebugaran berbasis pose estimation memerlukan pemahaman dasar biomekanik gerakan yang dianalisis. Parameter utama yang digunakan adalah sudut sendi (joint angle), yaitu sudut yang dibentuk oleh tiga keypoints yang merepresentasikan segmen tulang di sekitar sendi target.
Secara geometris, sudut sendi pada titik sendi B dihitung dari koordinat tiga keypoints A, B, dan C menggunakan fungsi arc-tangent dua argumen (atan2). 

θ= | atan2(C_y- B_y,C_x- B_x )- atan2(A_y- B_y,A_x- B_x )|* 180π     (2.1)
Pendekatan atan2 dipilih karena mempertahankan informasi kuadran sehingga menghasilkan sudut yang benar untuk semua orientasi tubuh, berbeda dengan pendekatan dot product yang hanya menghasilkan nilai antara 0° hingga 180° tanpa informasi arah fleksi maupun ekstensi (Chen and Yang, 2020).
Gerakan Squat
Squat merupakan gerakan multi-sendi yang melibatkan fleksi dan ekstensi terkoordinasi pada sendi lutut dan pinggul. Dalam konteks pose estimation, sudut lutut dihitung dari keypoints pinggul (hip), lutut (knee), dan pergelangan kaki (ankle), sedangkan sudut pinggul dihitung dari bahu (shoulder), pinggul, dan lutut (Appiah et al., 2024). Kajian biomekanik menunjukkan bahwa kedalaman squat yang dianggap valid secara fungsional umumnya berkaitan dengan fleksi lutut mencapai sekitar 90° atau lebih dalam, kondisi yang dikenal sebagai parallel squat ketika paha sejajar dengan lantai (Straub and Powers, 2024; Chen and Yang, 2020).
Berdasarkan rentang tersebut, penelitian ini menetapkan bahwa satu repetisi squat dinyatakan valid apabila sudut lutut mencapai ≤90° pada fase turun (descent phase), yang menandakan paha telah sejajar atau sedikit melewati bidang horizontal. Fase naik (ascent phase) ditandai dengan kembalinya sudut lutut ke posisi ekstensi penuh (≥160°). Penghitungan repetisi menggunakan state machine sederhana dengan transisi DOWN (θ_lutut ≤ 90°) → UP (θ_lutut ≥ 160°) dihitung sebagai satu repetisi valid.
Gerakan Push-up
Push-up adalah gerakan yang berpusat pada sendi siku dengan pola fleksi dan ekstensi lengan bawah sambil menjaga stabilitas batang tubuh. Sudut siku dihitung dari keypoints bahu (shoulder), siku (elbow), dan pergelangan tangan (wrist) (Appiah et al., 2024). Literatur biomekanik menunjukkan bahwa posisi bawah push-up yang efektif umumnya melibatkan fleksi siku sekitar 60°-90°, yaitu ketika dada berada dekat permukaan lantai (San Juan et al., 2015).
Berdasarkan rentang tersebut, penelitian ini menetapkan bahwa satu repetisi push-up dinyatakan valid apabila sudut siku mencapai ≤70° pada fase turun (down phase). Fase naik ditandai dengan kembalinya sudut siku ke posisi ekstensi penuh (≥160°). State machine push-up bekerja identik dengan squat: transisi DOWN (θ_siku ≤ 70°) → UP (θ_siku ≥ 160°) dihitung sebagai satu repetisi valid.
Penggunaan dua ambang batas (DOWN dan UP) menggantikan satu ambang tunggal bertujuan menghindari double counting akibat fluktuasi sudut di sekitar nilai batas — pendekatan yang dikenal sebagai hysteresis dalam sistem kontrol (Chen and Yang, 2020). Implementasi teknis state machine ini dijabarkan lebih lanjut dalam Flowchart Penghitungan Repetisi pada Bab 4.
Tabel 2.2 Parameter State Machine Penghitungan Repetisi
Gerakan	Sendi	Keypoints (A-B-C)	Threshold DOWN	Threshold UP
Squat	Lutut (kiri/kanan)	Hip – Knee – Ankle	≤ 90°	≥ 160°
Push-up	Siku (kiri/kanan)	Shoulder – Elbow – Wrist	≤ 70°	≥ 160°

TensorFlow Lite
TensorFlow Lite adalah framework open-source yang dikembangkan oleh Google untuk menjalankan model machine learning pada perangkat mobile, embedded, dan IoT dengan keterbatasan sumber daya (TensorFlow Lite, 2024). Framework ini menyediakan alat untuk mengonversi model TensorFlow standar menjadi format .tflite yang lebih ringkas sehingga ukuran model menjadi lebih kecil dan latensi inferensi menurun.
Beberapa teknik optimasi yang didukung TensorFlow Lite meliputi: (1) kuantisasi (quantization), yaitu penurunan presisi bobot dari float32 ke int8 atau int16 yang dapat mengurangi ukuran model hingga sekitar empat kali lipat; (2) pruning, yaitu penghapusan bobot yang bernilai kecil untuk mengurangi kompleksitas model tanpa penurunan akurasi yang berarti; dan (3) operator fusion, yaitu penggabungan beberapa operasi berturut-turut menjadi satu operasi terpadu untuk mengurangi akses memori dan overhead eksekusi (Google AI Edge, 2024).
Selain optimasi di tingkat model, TensorFlow Lite menyediakan mekanisme delegate system sebagai fitur utama untuk memanfaatkan akselerator perangkat keras yang tersedia pada perangkat target. Mekanisme ini dibahas lebih lanjut pada subbab 2.2.4.
Sistem Delegate TensorFlow Lite
Delegate dalam TensorFlow Lite adalah modul yang memungkinkan sebagian atau seluruh graf komputasi model dialihkan dari eksekusi CPU standar ke backend akselerasi tertentu, seperti GPU atau pustaka optimasi CPU (Google AI Edge, 2024). Setiap delegate berinteraksi dengan hardware melalui API spesifik platform sehingga pengembang dapat memanfaatkan kemampuan akselerasi tanpa mengubah kode model. Pemilihan delegate yang tepat merupakan keputusan arsitektur yang kritis karena secara langsung memengaruhi latensi inferensi, konsumsi daya, dan stabilitas termal aplikasi mobile (Ignatov et al., 2019).
GPU delegate memanfaatkan GPU perangkat melalui API OpenGL ES atau OpenCL untuk menjalankan operasi tensor secara paralel. Arsitektur GPU yang terdiri dari banyak inti pemroses sederhana sangat cocok untuk operasi konvolusi yang bersifat paralel, sehingga pada model konvolusional tertentu GPU dapat memberikan percepatan beberapa kali lipat dibanding eksekusi CPU murni (Ignatov et al., 2019; Jiang et al., 2020). Namun, GPU delegate memiliki startup overhead yang signifikan karena memerlukan kompilasi dan inisialisasi shader saat pertama kali dijalankan, serta cenderung mengonsumsi daya lebih tinggi karena aktivasi GPU yang intensif. Tidak semua operator TensorFlow Lite didukung oleh GPU delegate, sehingga operator yang tidak kompatibel akan di-fallback ke CPU secara otomatis (Google AI Edge, 2024).
XNNPACK delegate adalah pustaka optimasi CPU yang memanfaatkan instruksi SIMD seperti ARM NEON pada arsitektur ARM modern untuk mempercepat operasi inti jaringan saraf tiruan. XNNPACK juga melakukan operator fusion untuk mengurangi akses memori dan overhead eksekusi. Integrasi XNNPACK ke TensorFlow Lite dilaporkan memberikan peningkatan performa rata-rata sekitar 2,3x untuk inferensi floating-point dibanding backend CPU standar pada berbagai model (TensorFlow Blog, 2020). Keunggulan utama XNNPACK dibanding GPU delegate adalah tidak memerlukan akselerator khusus, tidak memiliki startup overhead yang besar, dan umumnya lebih hemat daya.
CPU Baseline adalah mode eksekusi default TensorFlow Lite tanpa akselerasi hardware tambahan, menggunakan kemampuan pemrosesan CPU standar secara sekuensial (Google AI Edge, 2024). Mode ini menjamin kompatibilitas penuh dengan semua operator TFLite pada semua perangkat Android dan menjadi fallback universal. Meskipun menghasilkan latensi tertinggi di antara ketiga opsi, CPU Baseline penting sebagai titik acuan untuk mengkuantifikasi keuntungan nyata dari penggunaan GPU delegate maupun XNNPACK.
Beberapa studi menunjukkan bahwa keunggulan relatif antara GPU delegate dan XNNPACK sangat bergantung pada ukuran model, jenis operasi yang dominan, dan karakteristik chipset perangkat target (Jiang et al., 2020; Ignatov et al., 2019). Hal ini menjadi salah satu alasan utama perlunya evaluasi empiris pada perangkat Samsung Galaxy A06 dan A33 5G yang merepresentasikan segmen low-to-mid range di Indonesia.
Model MoveNet
MoveNet adalah model estimasi pose yang dikembangkan oleh Google untuk perangkat bergerak dengan fokus pada kecepatan dan keakuratan dalam mendeteksi 17 keypoints tubuh manusia (Google Research, 2021). MoveNet menggunakan pendekatan single-person dengan arsitektur bottom-up berbasis CenterNet dan MobileNetV2 sebagai feature extractor. Fitur khas MoveNet adalah mekanisme smart cropping yang menyesuaikan region of interest (ROI) secara dinamis berdasarkan posisi pose pada frame sebelumnya, sehingga meningkatkan akurasi tanpa menambah beban komputasi secara signifikan.
MoveNet tersedia dalam dua varian utama yang mengakomodasi trade-off antara kecepatan dan akurasi (TensorFlow Blog, 2021). MoveNet Lightning dioptimalkan untuk kecepatan dengan ukuran masukan 192×192 piksel. Varian ini mampu mencapai latensi sekitar 25 ms per frame pada perangkat Pixel 5 dengan akselerasi GPU delegate dan model FP16, menjadikannya ideal untuk aplikasi real-time yang memprioritaskan responsivitas. Akurasi model mencapai sekitar 63% mAP dengan ukuran model ~4,8 MB. MoveNet Thunder dioptimalkan untuk akurasi dengan ukuran masukan 256×256 piksel, mencapai mAP sekitar 72% dengan latensi ~45 ms pada Pixel 5 GPU, dan lebih tepat untuk aplikasi yang memerlukan presisi tinggi. Dalam penelitian ini, MoveNet Lightning dipilih karena karakteristiknya yang lebih sesuai dengan kebutuhan aplikasi kebugaran real-time.
Model BlazePose Lite
BlazePose adalah arsitektur pose estimation yang dikembangkan oleh Google Research untuk pelacakan pose tubuh secara real-time pada perangkat mobile (Bazarevsky and Grishchenko, 2020). Model ini menggunakan pendekatan dua tahap: (1) pose detector, yaitu lightweight CNN yang mendeteksi ROI tubuh manusia pada citra; dan (2) pose tracker, yang memperkirakan 33 keypoints 3D di dalam ROI dan memanfaatkan informasi temporal antar-frame untuk meningkatkan stabilitas prediksi.
BlazePose Lite adalah varian yang dioptimalkan khusus untuk perangkat mobile dengan kompleksitas model yang dikurangi. Varian ini mempertahankan 33 keypoints yang mencakup seluruh tubuh termasuk keypoints wajah dan tangan yang tidak ada di MoveNet. Implementasi BlazePose dapat berjalan di atas 30 FPS pada Google Pixel 2 dengan akselerasi GPU, dengan akurasi sekitar 66,9% mAP dan ukuran model ~3,5 MB (FP16) (Bazarevsky and Grishchenko, 2020).
Dibanding MoveNet Lightning, BlazePose Lite memberikan representasi pose yang lebih rinci dengan 33 keypoints namun menggunakan arsitektur detector-tracker yang lebih kompleks. Keduanya dirancang untuk perangkat mobile, mendukung delegate TensorFlow Lite, dan menawarkan trade-off berbeda antara jumlah keypoints, latensi, dan kompleksitas komputasi dalam konteks aplikasi kebugaran. Perbandingan inilah yang menjadi salah satu fokus penelitian ini.
Thermal Throttling pada Inferensi Mobile
Thermal throttling adalah mekanisme perlindungan perangkat keras di mana prosesor secara otomatis menurunkan frekuensi kerjanya ketika suhu komponen melebihi ambang batas tertentu (Benoit-Cattin and Fernández-Berni, 2020). Pada perangkat mobile yang menggunakan pendinginan pasif (tanpa kipas), panas yang dihasilkan oleh inferensi model deep learning yang berjalan secara kontinu dapat terakumulasi dengan cepat dan memicu kondisi ini.
Benoit-Cattin and Fernández-Berni (2020) menunjukkan bahwa thermal throttling dapat menurunkan throughput inferensi secara signifikan selama operasi jangka panjang, dengan variasi performa hingga 27,7% tergantung suhu ambien. Lee et al. (2019) juga mencatat bahwa performa inferensi terdegradasi seiring waktu pada skenario penggunaan video real-time yang berjalan terus-menerus akibat akumulasi panas pada prosesor. Fenomena ini sangat relevan untuk penelitian ini karena: (1) aplikasi kebugaran umumnya digunakan dalam sesi yang panjang; (2) perangkat low-end memiliki kapasitas disipasi panas yang lebih terbatas dibanding perangkat kelas atas; dan (3) GPU delegate cenderung menghasilkan panas lebih banyak dibanding XNNPACK atau CPU Baseline karena aktivasi GPU yang intensif.
Untuk mengendalikan efek thermal throttling dalam pengujian, penelitian ini menerapkan protokol kontrol yang ketat, termasuk pembatasan suhu perangkat di bawah 40°C sebelum setiap sesi pengujian, jeda antar-sesi, dan pengukuran di lingkungan dengan suhu ruang yang stabil (24–26°C).
Pengembangan Aplikasi Bergerak Android
Penelitian ini diimplementasikan pada platform Android menggunakan bahasa pemrograman Kotlin dan Android Studio sebagai integrated development environment (IDE). Kotlin merupakan bahasa resmi pengembangan Android sejak 2017 dan menawarkan fitur null safety serta interoperabilitas penuh dengan Java, sehingga memudahkan integrasi dengan pustaka TensorFlow Lite (Kotlin Foundation, 2024).
Arsitektur sistem operasi Android terdiri atas beberapa lapisan, termasuk Linux kernel, Hardware Abstraction Layer (HAL), dan Android Runtime (ART). HAL berperan penting dalam bagaimana GPU delegate berinteraksi dengan GPU melalui API OpenGL ES, sementara XNNPACK beroperasi pada lapisan CPU melalui instruksi ARM NEON (Android Developers, 2024). Android Neural Networks API (NNAPI) menyediakan antarmuka untuk akselerator AI khusus seperti NPU, namun tidak digunakan dalam penelitian ini karena dukungannya tidak konsisten antar chipset — khususnya tidak tersedia pada Samsung Galaxy A06 — sehingga tidak memungkinkan perbandingan yang adil antar kedua perangkat uji.
Pengukuran metrik kinerja dalam penelitian ini dilakukan menggunakan: System.nanoTime() untuk mengukur latensi inferensi secara presisi; pembacaan berkas sistem /proc/stat untuk mengukur utilisasi CPU per inti; BatteryManager API untuk mengestimasi konsumsi daya; serta Debug.MemoryInfo API untuk memantau penggunaan memori berdasarkan Proportional Set Size (PSS) — metrik yang lebih akurat dibanding RSS karena memperhitungkan memori yang dibagi antar proses (Android Developers, 2024). Seluruh data dicatat ke dalam berkas CSV pada setiap frame untuk analisis statistik selanjutnya.
Metrik Kinerja Aplikasi Bergerak
Untuk mengevaluasi kinerja aplikasi pose estimation pada perangkat mobile secara komprehensif, penelitian ini menggunakan lima metrik utama berikut.
Latensi inferensi diukur dalam milidetik (ms) dan merepresentasikan waktu yang dibutuhkan untuk memproses satu frame masukan dari tahap pra-proses hingga pasca-proses. Untuk aplikasi pose estimation real-time, target latensi maksimal adalah sekitar 33 ms (setara 30 FPS) agar gerakan tampak mulus, dengan target optimal di bawah 20 ms untuk gerakan yang cepat (Google AI Edge, 2024).
Throughput diukur dalam frames per second (FPS) dan menyatakan jumlah frame yang dapat diproses per detik, dihitung sebagai 1000 dibagi latensi rata-rata dalam milidetik. Target minimum yang ditetapkan dalam penelitian ini adalah 20 FPS untuk gerakan dasar dan 30 FPS untuk gerakan yang lebih dinamis.
Utilisasi CPU diukur dalam persentase penggunaan CPU selama eksekusi dan merupakan indikator penting efisiensi komputasi. Utilisasi CPU yang tinggi (>80%) dapat memicu thermal throttling dan mengurangi masa pakai baterai (Android Developers, 2024). Nilai ini diukur melalui pembacaan /proc/stat.
Penggunaan memori (RAM) mencakup konsumsi memori oleh model, struktur data, dan buffer pemrosesan gambar. Penggunaan memori yang efisien (ditargetkan <100 MB) penting untuk menghindari out-of-memory dan menjaga stabilitas sistem pada perangkat dengan RAM terbatas. Pengukuran dilakukan menggunakan Debug.MemoryInfo API berdasarkan nilai PSS (Android Developers, 2024).
Konsumsi daya diestimasi menggunakan BatteryManager API dengan menghitung perkalian tegangan dan arus baterai. Dalam penelitian ini, konsumsi daya dilaporkan secara deskriptif sebagai metrik tambahan — tidak dimasukkan ke dalam analisis ANOVA — mengingat granularitas sensor baterai pada perangkat low-to-mid range tidak memadai untuk sesi pengukuran berdurasi pendek.
Analisis Varians (ANOVA)
Analisis varians (ANOVA) adalah metode statistik yang digunakan untuk menguji apakah terdapat perbedaan signifikan antara rata-rata lebih dari dua kelompok (Montgomery, 2017). Montgomery (2017) mengklasifikasikan ANOVA berdasarkan jumlah faktor yang terlibat, dari one-way ANOVA untuk satu faktor independen hingga multi-way ANOVA untuk dua faktor atau lebih. Keppel and Wickens (2004) membahas ANOVA dalam konteks desain eksperimen faktorial dan pengukuran berulang (repeated-measures), termasuk cara menginterpretasikan efek utama dan interaksi antar-faktor.
Dalam penelitian ini, three-way repeated-measures ANOVA digunakan untuk menguji pengaruh tiga faktor — jenis delegate (CPU, XNNPACK, GPU), model pose estimation (MoveNet Lightning vs BlazePose Lite), dan jenis latihan (squat vs push-up) — terhadap metrik kinerja seperti latensi, throughput, utilisasi CPU, dan penggunaan memori. Desain repeated-measures dipilih karena seluruh kondisi pengujian dilakukan pada perangkat yang sama, sehingga pengukuran antar kondisi bersifat dependen. Pendekatan ini mengurangi variasi antar-subjek dan meningkatkan sensitivitas statistik (Field, 2018).
Rumus dasar uji F pada ANOVA adalah sebagai berikut: 
F =  (MS_between)/(MS _within )          						(2.2)
MS_(between )=  (SS_between)/(df _between )    &  MS_within  =  (SS_within)/(df _within )			(2.3)
 Nilai F kemudian dibandingkan dengan F_tabel pada tingkat signifikansi α = 0,05. Jika F_hitung > F_tabel, terdapat perbedaan signifikan antar kelompok.
Sebelum ANOVA dijalankan, beberapa asumsi harus dipenuhi. Normalitas residu diuji menggunakan uji Shapiro-Wilk (Shapiro and Wilk, 1965), yang memiliki statistical power yang baik untuk ukuran sampel kecil hingga menengah (Field, 2018).
Data frame-level yang diperoleh dari CSV terlebih dahulu dibersihkan dengan menghapus frame warm-up, frame dengan latensi 0 ms, dan outlier ekstrem. Setelah itu, data diagregasi menjadi data session-level berdasarkan perangkat, replikasi, model, delegate, dan jenis latihan. 
Analisis statistik dilakukan secara terpisah untuk setiap perangkat karena penelitian hanya menggunakan satu unit perangkat pada masing-masing kategori low-end dan mid-range. Dengan demikian, perangkat tidak diperlakukan sebagai faktor inferensial dalam ANOVA. Untuk data yang lengkap dan seimbang, analisis dilakukan menggunakan three-way repeated-measures ANOVA dengan delegate, model, dan jenis latihan sebagai faktor dalam-subjek. Analisis dilakukan menggunakan pustaka statsmodels pada Python. 
Apabila hasil ANOVA menunjukkan efek yang signifikan, analisis lanjutan dilakukan menggunakan pairwise comparison berpasangan dengan koreksi Holm. Koreksi Holm digunakan untuk mengontrol family-wise error rate pada pengujian berulang antar pasangan kondisi.
 Homogenitas varians diuji menggunakan uji Levene, dan asumsi sphericity untuk data repeated-measures diuji menggunakan uji Mauchly. Apabila asumsi sphericity dilanggar, koreksi Greenhouse-Geisser diterapkan. Jika asumsi parametrik secara keseluruhan tidak terpenuhi, analisis digantikan dengan uji Friedman atau Kruskal-Wallis sebagai alternatif nonparametrik. Apabila ANOVA menunjukkan hasil yang signifikan, uji lanjut Tukey HSD dilakukan untuk mengidentifikasi pasangan kelompok yang berbeda nyata, dengan pengendalian family-wise error rate pada α = 0,05. Ukuran efek dihitung menggunakan partial eta-squared (η²p).
METODOLOGI PENELITIAN
Jenis Penelitian
Penelitian ini merupakan penelitian kuantitatif dengan pendekatan eksperimental yang bersifat analitik-komparatif. Tujuannya adalah membandingkan performa berbagai konfigurasi delegate TensorFLow Lite (GPU, XNNPACK, CPU Baseline) pada dua model pose estimation (MoveNet Lightning dan BlazePose Lite) untuk aplikasi estimasi pose real-time di perangkat Android. Aplikasi yang dikembangkan tidak dimaksudkan sebagai produk akhir, melainkan berfungsi sebagai test harness atau alat ukur untuk mengumpulkan data performa secara objektif dan terkontrol.
Pendekatan eksperimen terkontrol dipilih agar setiap variabel dapat diuji secara sistematis dengan kondisi yang konsisten, sehingga hasil penelitian dapat direplikasi dan divalidasi. Strategi penelitian ini menggunakan desain faktorial penuh (full factorial design) 3x2x2, yaitu tiga level delegate x dua varian model x dua jenis latihan, yang menghasilkan 12 kondisi pengujian per perangkat. Setiap kondisi dieksekusi sebanyak 30 kali (replikasi) berdasarkan Central Limit Theorem yang menyatakan bahwa distribusi sampel rata-rata mendekato normal untuk n > 30, sehingga memungkinkan penggunaan uji statistik parametrik (Montgomery, 2017). Power analysis a priori menggunakan G*Power 3.1 dengan α = 0.05, power (1-β) = 0.80, dan medium effect size (f = 0.25) menunjukkan minimum sample size n = 24 untuk three-way ANOVA. Dengan demikian, n = 30 memberikan kekuatan statistik yang memadai dengan buffer untuk data pencilan.
Pengujian dilakukan secara independen pada dua perangkat uji, yaitu Samsung Galaxy A06 (low-end) dan Samsung Galaxy A33 (mid-range). Analisis ANOVA dijalankan secara terpisah untuk setiap perangkat, menghasilkan dua set hasil yang kemudian dibandingkan secara deskriptif. Total sesi pengukuran yaitu 12 kondisi x 30 replikasi x 2 perangkat = 720 sesi.
Desain Eksperimen
Desain eksperimen penelitian ini menggunakan rancangan faktorial penuh (full factorial design) dengan tiga faktor independen. Faktor pertama adalah jenis delegate (A) dengan tiga level: CPU Baseline, XNNPACK, dan GPU Delegate. Faktor kedua adalah varian model pose estimation (B) dengan dua level: Movenet Lightning dan BlazePose Lite. Faktor ketiga adalah jenis latihan (C) dengan dua level: squat dan push-up.
Kombinasi seluruh faktor menghasilkan 12 kondisi eksperimental per perangkat. Desain faktorial penuh dipilih karena memungkinkan analisis efek interaksi antar-faktor, bukan hanya efek utama masing-masing faktor secara terpisah (Montgomery, 2017).

Tabel 3.1 Rancangan Eksperimen Faktorial
Faktor	Jumlah Level	Level	Keterangan
Delegate (A)	3	CPU Baseline, XNNPACK, GPU	Metode akselerasi inferensi TFLite
Model (B)	2	MoveNet Lightning, BlazePose Lite	Arsitektur model pose estimation
Latihan (C)	2	Squat, Push-up	Jenis gerakan fisik yang dideteksi

Total kombinasi = 3 x 2 x 2 = 12 kondisi per perangkat. Total sesi pengukuran = 12 kondisi x 30 replikasi x 2 perangkat = 720 sesi. Analisis ANOVA dijalankan sebagai dua set three-way ANOVA independen (Satu per perangkat) karena perbedaan arsitektur chipset antar perangkat menyebabkan ketidaksetaraan varians yang melanggar asumsi homogenitas apabila digabungkan.
Variabel Penelitian
Variabel Bebas
a. Jenis Delegate (Faktor A). Terdiri dari tiga level metode akselerasi: (1) CPU Baseline — eksekusi menggunakan kernel bawaan TensorFlow Lite tanpa optimasi tambahan, dengan jumlah thread ditetapkan empat; (2) XNNPACK — eksekusi pada CPU dengan optimasi SIMD melalui instruksi ARM NEON untuk operasi aritmetika vektor; dan (3) GPU Delegate — eksekusi pada GPU perangkat menggunakan OpenGL ES atau OpenCL.
b. Varian Model (Faktor B). Terdiri dari dua level: (1) MoveNet Lightning (FP16) — arsitektur bottom-up dengan resolusi masukan 192×192 piksel dan 17 keypoints standar COCO; dan (2) BlazePose Lite (FP16) — arsitektur detector-tracker dengan resolusi masukan 256×256 piksel dan 33 keypoints yang dipetakan ke 17 format COCO untuk konsistensi perbandingan.
c. Jenis Latihan (Faktor C). Terdiri dari dua level: (1) Squat — gerakan vertikal yang berpusat pada sendi lutut; dan (2) Push-up — gerakan horizontal yang berpusat pada sendi siku. Keduanya dipilih karena merepresentasikan pola gerakan yang berbeda secara biomekanik.
Variabel Terikat
Empat variabel terikat utama diukur dan dianalisis menggunakan three-way ANOVA:

Tabel 3.2 Variabel Terikat Utama
No	Variabel	Satuan	Metode Pengukuran
1	Latensi Inferensi	Milidetik (ms)	System.nanoTime() sebelum dan sesudah Interpreter.run()
2	Throughput	Frame per second (FPS)	Penghitungan frame yang berhasil diproses per interval 1 detik
3	Utilisasi CPU	Persen (%)	Pembacaan /proc/stat dan /proc/[pid]/stat per 100 ms
4	Penggunaan Memori	Megabyte (MB)	Debug.MemoryInfo API — Proportional Set Size (PSS)

Selain keempat variabel terikat utama, dua metrik tambahan dicatat namun tidak dimasukkan ke dalam analisis ANOVA. Pertama, konsumsi daya (mW) diestimasi menggunakan BatteryManager API dengan menghitung perkalian tegangan dan arus baterai. Metrik ini dilaporkan secara deskriptif karena granularitas sensor baterai pada perangkat low-to-mid range tidak memadai untuk sesi pengukuran berdurasi pendek, menghasilkan fluktuasi tinggi yang dapat menurunkan validitas uji statistik. Kedua, ketepatan penghitungan repetisi divalidasi secara terpisah sebagai uji fungsionalitas sistem dengan membandingkan jumlah repetisi yang terdeteksi oleh algoritma state machine terhadap hitungan manual (ground truth), dengan target akurasi minimum 90%.
Variabel Kontrol
Variabel kontrol ditetapkan untuk memastikan konsistensi kondisi antar sesi pengujian dan meminimalkan sumber variasi yang tidak relevan.
Tabel 3.3 Variabel Kontrol
Variabel Kontrol	Nilai/Kondisi	Justifikasi
Mode Perangkat	Mode pesawat aktif	Menghilangkan gangguan jaringan dan proses sinkronisasi latar belakang
Kecerahan Layar	50%	Menyeragamkan konsumsi daya layar antar sesi
Aplikasi Latar Belakang	Seluruhnya ditutup	Mencegah kontaminasi penggunaan CPU dan memori
Level Baterai	≥80%, tidak dalam pengisian	Menghindari mode hemat daya dan variabilitas arus pengisian
Suhu Perangkat	<40°C sebelum setiap sesi	Mencegah thermal throttling yang menurunkan clock speed prosesor
Suhu Ruang	24–26°C	Menjaga kondisi disipasi panas yang seragam antar sesi (Benoit-Cattin and Fernández-Berni, 2020)
Pencahayaan	±500 lux	Memastikan kondisi deteksi kamera yang konsisten untuk pose estimation
Sistem Operasi	Android 15 (API 35) — kedua perangkat	Menyeragamkan perilaku runtime dan API yang tersedia
Warm-up Protocol	5 frame awal tidak dicatat	Menghindari pencilan akibat cold start GPU dan JIT compilation
Versi TFLite	2.14.0	Memastikan perilaku delegate yang identik pada kedua perangkat
Pengujian Fungsional pada End User
Selain pengujian performa, penelitian ini juga melakukan pengujian fungsional terbatas pada end user. Pengujian ini bertujuan untuk memvalidasi bahwa aplikasi tidak hanya dapat menghasilkan data benchmark, tetapi juga mampu menjalankan fungsi dasar aplikasi kebugaran, yaitu mendeteksi pose dan menghitung repetisi squat serta push-up secara otomatis.

             Pengujian dilakukan pada 5 subjek. Setiap subjek diminta melakukan 1 set squat sebanyak 10 repetisi dan 1 set push-up sebanyak 10 repetisi. Konfigurasi model dan delegate yang digunakan pada pengujian ini adalah konfigurasi terbaik berdasarkan hasil pengujian performa. Hasil pengujian dibandingkan dengan hitungan manual sebagai ground truth. Metrik yang digunakan adalah akurasi penghitungan repetisi, yang dihitung dengan rumus:

Akurasi (%) = (jumlah repetisi terdeteksi benar / jumlah repetisi aktual) × 100%

             Pengujian ini tidak dimasukkan ke dalam analisis ANOVA, melainkan dilaporkan menggunakan statistik deskriptif berupa akurasi per subjek dan rata-rata akurasi per jenis latihan.
Tahap Persiapan
 
Gambar 3.1 Tahapan Alur Penelitian

Pada tahap persiapan, dilakukan pemilihan dan konfigurasi perangkat keras serta perangkat lunak yang digunakan dala eksperimen. Dua perangkat uji dipilih untuk merepresentasikan segmentasi pasar Android Indonesia, dengan spesifikasi yang disajikan pada Tabel 3.4.
Tabel 3.4 Spesifikasi Perangkat Uji
Spesifikasi	Samsung Galaxy A06 (Low-end)	Samsung Galaxy A33 5G (Mid-range)
Chipset	MediaTek Helio G85	Samsung Exynos 1280
CPU	2×2,0 GHz Cortex-A75 + 6×1,8 GHz Cortex-A55	2×2,4 GHz Cortex-A78 + 6×2,0 GHz Cortex-A55
GPU	Mali-G52 MC2	Mali-G68
RAM	4 GB	6/8 GB
Sistem Operasi	Android 15 (API 35)	Android 15 (API 35)
Segmen Pasar	Entry-level (<Rp. 1 Juta)	Mid-range (Rp. 3-5 Juta)

Kedua perangkat dipilih karena mendukung seluruh konfigurasi delegate TensorFlow Lite yang diuji dan merepresentasikan segmen yang paling dominan di pasar Indonesia (IDC, 2023). Lingkungan pengujian dijaga pada suhu 24–26°C dengan pencahayaan sekitar 500 lux. Aplikasi test harness dikembangkan menggunakan Android Studio Iguana (2024.1.1) dengan bahasa Kotlin dan pustaka TensorFlow Lite versi 2.14.0.
Tahap Pengembangan Aplikasi
Tahap pengembangan dilakukan dengan membangun sistem benchmarking yang mampu menjalankan kedua model pose estimation dalam berbagai konfigurasi delegate serta mencatat metrik performa secara otomatis. Aplikasi dirancang dengan arsitektur berlapis mengikuti pola MVVM (Model-View-ViewModel): Presentation Layer untuk antarmuka dan kontrol pengujian, Domain Layer untuk logika penghitungan repetisi dan orkestrasi benchmarking, serta Data Layer untuk proses inferensi TFLite, profiling sumber daya, dan pencatatan data.
Pergantian model dan delegate difasilitasi secara dinamis menggunakan Strategy Pattern melalui antarmuka PoseDetector. Pra-pemrosesan citra menggunakan teknik letterbox padding untuk menjaga rasio aspek masukan kamera. Protokol warm-up 5 frame diterapkan sebelum pencatatan data dimulai untuk menghindari pencilan akibat cold start GPU dan JIT compilation. Detail perancangan dan implementasi diuraikan pada Bab 4 dan Bab 5.
Tahap Pengumpulan Data
Pengumpulan data dilakukan dengan menjalankan dua skenario latihan — squat dan push-up — secara langsung di depan kamera perangkat uji (live camera feed). Subjek melakukan gerakan dengan tempo terstandar untuk menjaga konsistensi antar sesi: setiap sesi squat menggunakan pola 3 detik turun, 1 detik tahan, 3 detik naik, dan 2 detik istirahat; sedangkan sesi push-up menggunakan pola 2 detik turun, 1 detik tahan, 2 detik naik, dan 2 detik istirahat.
Setiap replikasi mengikuti protokol berikut secara ketat:
No	Langkah Protokol
1	Perangkat di-restart sebelum rangkaian pengujian dimulai untuk menghapus proses latar belakang dan menstabilkan suhu
2	Mode pesawat diaktifkan, kecerahan layar 50%, seluruh aplikasi latar belakang ditutup
3	Suhu perangkat dipastikan di bawah 40°C sebelum setiap sesi
4	Baterai dipastikan terisi minimal 80% dan tidak dalam kondisi pengisian daya
5	Konfigurasi delegate dan model dipilih melalui panel kontrol aplikasi
6	Interpreter TFLite diinisialisasi ulang dengan konfigurasi terpilih
7	5 frame pertama (fase warm-up) dijalankan tanpa pencatatan data
8	Subjek melakukan gerakan latihan di depan kamera; sistem mencatat metrik untuk minimal 300 frame inferensi stabil
9	Data diekspor ke berkas CSV; perangkat diberi jeda pendinginan sebelum sesi berikutnya

Untuk menghindari bias urutan pengujian, dilakukan counterbalancing pada urutan konfigurasi delegate dalam satu rangkaian sesi. Seluruh 12 kondisi pada satu perangkat diselesaikan sebelum berpindah ke perangkat lainnya. Seluruh metrik performa dicatat secara real-time dan diekspor ke format CSV untuk analisis lanjutan.
Tahap Analisis Data
Tahap analisis data diawali dengan pemeriksaan kualitas data. Pencilan diidentifikasi menggunakan modified Z-score dengan ambang batas 3,5. Pencilan yang teridentifikasi sebagai artefak prosedural (misalnya akibat thermal throttling yang tidak terkontrol) dihapus dari dataset, sedangkan pencilan yang merupakan variasi alami dipertahankan. Data yang hilang diinterpolasi secara linier apabila jumlahnya tidak melebihi 5% dari total observasi per kondisi.
Selanjutnya dilakukan analisis deskriptif berupa penghitungan rata-rata (μ), simpangan baku (σ), nilai minimum, dan nilai maksimum untuk setiap variabel terikat pada setiap kondisi. Analisis inferensial menggunakan three-way repeated-measures ANOVA untuk menguji pengaruh faktor Delegate (A), Model (B), dan Jenis Latihan (C) beserta seluruh efek interaksinya. Detail prosedur statistik diuraikan pada Subbab 3.5.
Metodologi Analisis Statistik
Uji Asumsi
Sebelum ANOVA dijalankan, dua asumsi fundamental diuji untuk setiap variabel terikat pada setiap kondisi.
Normalitas diuji menggunakan uji Shapiro-Wilk karena ukuran sampel per kelompok relatif kecil (n = 30) dan uji ini memiliki statistical power yang baik untuk mendeteksi penyimpangan dari normalitas pada ukuran sampel kecil hingga menengah (Field, 2018). Hipotesis nol menyatakan data berdistribusi normal. Apabila asumsi tidak terpenuhi (p < 0,05), dilakukan transformasi logaritmik. Jika setelah transformasi asumsi masih tidak terpenuhi, analisis digantikan dengan uji Friedman atau Kruskal-Wallis sebagai alternatif nonparametrik.
Homogenitas varians diuji menggunakan uji Levene karena lebih robust terhadap pelanggaran normalitas dibandingkan uji Bartlett. Hipotesis nol menyatakan varians antar kelompok homogen pada α = 0,05.
Three-Way Repeated-Measures ANOVA
Three-way repeated-measures ANOVA digunakan untuk menguji pengaruh tiga faktor — Delegate (A, 3 level), Model (B, 2 level), dan Jenis Latihan (C, 2 level) terhadap masing-masing variabel terikat. Desain repeated-measures dipilih karena seluruh kondisi diuji pada perangkat yang sama, sehingga pengukuran antar kondisi bersifat dependen. Pendekatan ini mengurangi variasi antar-subjek dan meningkatkan sensitivitas statistik (Field, 2018). Analisis menguji tujuh sumber variasi: tiga efek utama (A, B, C), tiga efek interaksi dua arah (A×B, A×C, B×C), dan satu efek interaksi tiga arah (A×B×C). Tingkat signifikansi ditetapkan pada α = 0,05.
Model statistik three-way ANOVA diformulasikan sebagai: 
Y_ijk= μ + α_i+ β_j+ γ_k+ (αβ)_ij+ (αγ)_ik+ (βγ)_jk+ (αβγ)_ijk+ ε_ijk (3.1)
di mana Y_ijk adalah nilai variabel terikat, μ adalah rata-rata keseluruhan, α_i adalah efek faktor Delegate, β_j adalah efek faktor Model, γ_k adalah efek faktor Jenis Latihan, dan ε_ijk adalah galat acak (Montgomery, 2017).
Ukuran efek dihitung menggunakan partial eta-squared (η²p) untuk menilai signifikansi praktis, dengan interpretasi: efek kecil (η²p ≥ 0,01), efek sedang (η²p ≥ 0,06), dan efek besar (η²p ≥ 0,14) (Cohen, 1988; Field, 2018). ANOVA dijalankan secara terpisah untuk setiap variabel terikat pada masing-masing perangkat.
Uji Post-hoc Tukey HSD
Apabila ANOVA menunjukkan efek utama yang signifikan pada faktor Delegate (yang memiliki tiga level), uji lanjut Tukey Honestly Significant Difference (HSD) dilakukan untuk mengidentifikasi pasangan level mana yang berbeda secara signifikan. Tukey HSD dipilih karena mampu mengendalikan family-wise error rate pada α = 0,05 saat melakukan perbandingan berpasangan ganda. Faktor Model dan Jenis Latihan tidak memerlukan uji post-hoc karena masing-masing hanya memiliki dua level, sehingga perbedaannya sudah cukup jelas dari nilai F dan arah rata-rata pada ANOVA.
Analisis Trade-off Deskriptif
Untuk menjawab rumusan masalah ketiga mengenai konfigurasi yang memberikan keseimbangan optimal, dilakukan analisis trade-off secara deskriptif berdasarkan interpretasi gabungan dari hasil ANOVA dan statistik deskriptif. Setiap konfigurasi dievaluasi berdasarkan tiga kriteria: (1) kelayakan real-time — FPS ≥30 ideal, ≥25 acceptable, ≥15 minimum untuk aplikasi kebugaran; (2) efisiensi sumber daya — utilisasi CPU dan konsumsi memori; dan (3) trade-off antar-metrik secara keseluruhan. Rekomendasi disusun berdasarkan skenario penggunaan yang berbeda, mengingat bahwa tidak ada satu konfigurasi yang optimal untuk semua kondisi.
Matriks Kondisi Eksperimen
Tabel 3.5 menyajikan seluruh 12 kondisi eksperimental beserta jumlah replikasi yang akan dilaksanakan pada setiap perangkat uji.

Tabel 3.5 Matriks Kondisi Eksperimen
No	Model	Delegate	Latihan	Replikasi
1	MoveNet Lightning	CPU Baseline	Squat	30
2	MoveNet Lightning	CPU Baseline	Push-up	30
3	MoveNet Lightning	XNNPACK	Squat	30
4	MoveNet Lightning	XNNPACK	Push-up	30
5	MoveNet Lightning	GPU Delegate	Squat	30
6	MoveNet Lightning	GPU Delegate	Push-up	30
7	BlazePose Lite	CPU Baseline	Squat	30
8	BlazePose Lite	CPU Baseline	Push-up	30
9	BlazePose Lite	XNNPACK	Squat	30
10	BlazePose Lite	XNNPACK	Push-up	30
11	BlazePose Lite	GPU Delegate	Squat	30
12	BlazePose Lite	GPU Delegate	Push-up	30
Total				360 per perangkat / 720 total

Instrumen dan Alat Penelitian
Tabel 3.6 merangkum seluruh perangkat lunak dan pustaka yang digunakan dalam penelitian ini.
Komponen	Versi/Spesifikasi	Fungsi
Android Studio	Iguana 2024.1.1	IDE pengembangan aplikasi Android
Kotlin	1.9.x	Bahasa pemrograman utama
TensorFlow Lite	2.14.0	Runtime inferensi model ML on-device
tensorflow-lite-gpu	2.14.0	Pustaka GPU Delegate
tensorflow-lite-support	0.4.4	Utilitas pra-pemrosesan citra
CameraX	1.3.x	API kamera Android untuk live feed
Model MoveNet Lightning	FP16, 192×192 px	Model pose estimation ringan
Model BlazePose Lite	FP16, 256×256 px	Model pose estimation 33 keypoints
Python + SciPy/Pingouin	3.10+	Analisis statistik ANOVA pasca pengumpulan data
PERANCANGAN SISTEM
Bab ini membahas perancangan perangkat lunak yang dibangun sebagai alat ukur (test harness) untuk eksperimen benchmarking. Perancangan meliputi analisis kebutuhan, arsitektur dan diagram perancangan, algoritma inferensi, desain antarmuka, dan strategi pengukurna metrik. Seluruh keputusan perancangan diarahkan untuk menghasilkan alat ukur yang valid, reproducible, dan memiliki bias pengukuran minimal.
Analisis Kebutuhan Sistem
Tahap analisis kebutuhan mendefinisikan spesifikasi aplikasi benchmarking agar alat ukur dapat mengumpulkan data eksperimen secara vaid sesuai variabel penelitian yang telah ditetapkan pada Bab 3. Sistem harus mampu mengeksekusi eksperimen dengan repeatability tinggi dan bias minimal untuk menghasilkan data yang valid secara statistik.
Identifikasi Aktor Sistem
Karena aplikasi ini merupakan test harness yang dioperasikan langsung oleh peneliti, terdapat satu aktor utama dan satu aktor sistem pendukung.
Tabel 4.1 Identifikasi Aktor Sistem
No	Aktor	Tipe	Deskripsi
1	Peneliti	Primary	Pengguna utama yang mengonfigurasi parameter eksperimen (model, delegate, jenis latihan), memulai/menghentikan sesi benchmarking, memantau metrik real-time, dan mengekspor data hasil pengujian ke format CSV.

Hanya ada satu aktor yaitu aktor Peneliti, merepresentasikan pengguna yang menjalankan eksperimen secara langsung pada perangkat uji.
Kebutuhan Fungsional 
Kebutuhan fungsional mendefinisikan kemampuan utama yang harus dimiliki apliaksi benchmarking. Tabel 4.2 merangkum kebutuhan fungsional beserta justifikasi teknis masing-masing.
Tabel 4.2 Kebutuhan Fungsional Aplikasi Benchmarking
Kode	Kategori	Deskripsi Kebutuhan
KF-01	Manajemen Delegate	Sistem harus mampu mengubah konfigurasi delegate (CPU Baseline, XNNPACK, GPU) secara dinamis pada runtime tanpa memerlukan kompilasi ulang aplikasi.
KF-02	Manajemen Model	Sistem harus memfasilitasi pemuatan dan eksekusi inferensi menggunakan dua model: MoveNet Lightning (FP16) dan BlazePose Lite (FP16).
KF-03	Pengukuran Latensi	Sistem harus mengukur waktu eksekusi inferensi (inference time) dalam milidetik untuk setiap frame menggunakan System.nanoTime().
KF-04	Perhitungan Repetisi	Sistem harus memiliki logika geometri untuk mendeteksi gerakan squat dan push-up berdasarkan sudut sendi keypoints tubuh dengan state machine hysteresis.
KF-05	Pencatatan Data Otomatis	Sistem harus menyimpan data metrik performa (latensi, FPS, utilisasi CPU, penggunaan memori, estimasi daya) ke format CSV per-frame untuk analisis statistik.
KF-06	Ekspor CSV	Sistem harus mengekspor data benchmarking yang tersimpan ke file CSV pada penyimpanan perangkat menggunakan MediaStore API.

Kebutuhan Non-Fungsional
Kebutuhan non-fungsional mendfinisikan batasan teknis dan kualitas sistem yang harus dipenuhi agar eksperimen dapat berjalan dengan valid.
Tabel 4.3 Kebutuhan Non-Fungsional Aplikasi Benchmarking
Aspek	Kebutuhan
Kompatibilitas	Aplikasi harus berjalan pada Android API Level 35 (Android 15) dan kompatibel dengan kedua perangkat uji (Samsung Galaxy A06 dan A33 5G).
Framework	Sistem dikembangkan menggunakan TensorFlow Lite versi 2.14.0.
Efisiensi UI	Beban rendering antarmuka tidak boleh mengganggu pengukuran kinerja backend TFLite. UI diimplementasikan dengan Jetpack Compose yang ringan.
Stabilitas	Aplikasi harus menangani kegagalan inisialisasi GPU (fallback ke XNNPACK) tanpa crash dan tanpa membatalkan sesi eksperimen yang sedang berjalan.
Reproducibility	Protokol warm-up 5 frame dan counterbalancing urutan delegate harus dapat dieksekusi secara konsisten pada setiap sesi pengujian.

Use Case 
Use Case Diagram
Diagram use case menggambarkan interaksi antara aktor Peneliti dengan fungsi-fungsi utama sistem. Terdapat enam use case yang merepresentasikan kebutuhan fungsional yang telah diidentifikasi.
 
Gambar 4.1 Use Case Diagram Aplikasi Benchmarking
Use case utama terdiri dari: (UC-01) Mengonfigurasi Parameter Eksperimen, (UC-02) Menjalankan Inferensi Real-time, (UC-03) Menghitung Repetisi Latihan, (UC-04) Memantau Metrik Performa, (UC-05) Merekam Data Benchmarking, dan (UC-06) Mengekspor Data ke CSV. UC-03 memiliki relasi include terhadap UC-02 karena penghitungan repetisi memerlukan hasil inferensi pose. UC-05 memiliki relasi include terhadap UC-04 karena proses perekaman mencatat metrik yang sedang dipantau.
Spesifikasi Use Case
UC-01: Mengonfigurasi Parameter Eksperimen
Tabel 4.4 Use Case Scenario Mengonfigurasi Parameter Eksperimen
Use Case ID	UC-01
Nama	Mengonfigurasi Parameter Eksperimen
Aktor	Peneliti
Include	UC-02, UC-03, UC-04
Deskripsi	Peneliti memilih kombinasi model pose estimation, delegate akselerasi, dan jenis latihan yang akan diuji sebelum atau selama sesi benchmarking berlangsung.
Pre-condition	1. Aplikasi telah terbuka dan izin kamera telah diberikan.
2. Layar utama (CameraScreen) ditampilkan.
Post-condition	1. Interpreter TFLite diinisialisasi ulang dengan konfigurasi baru.
2. Preview kamera menampilkan overlay pose sesuai model terpilih.
3. Fase warm-up (5 frame) dijalankan sebelum metrik dicatat.
Main Flow	1. Peneliti membuka panel kontrol di bagian bawah layar.
2. Peneliti memilih model dari dropdown (MoveNet Lightning / BlazePose Lite).
3. Peneliti memilih delegate dari dropdown (CPU Baseline / XNNPACK / GPU).
4. Peneliti memilih jenis latihan dari dropdown (Squat / Push-up / None).
5. Sistem melepaskan interpreter lama dan menginisialisasi interpreter baru.
6. Sistem menjalankan warm-up 5 frame tanpa pencatatan metrik.
7. Sistem menampilkan overlay skeleton pose pada preview kamera.
Alternative Flow	5a. Jika inisialisasi GPU gagal:
5a.1. Sistem menampilkan peringatan "GPU tidak didukung, fallback ke XNNPACK".
5a.2. Sistem otomatis menginisialisasi interpreter dengan delegate XNNPACK.
5a.3. Dropdown delegate diperbarui ke XNNPACK.
Exception Flow	5b. Jika pemuatan model gagal (file .tflite corrupt/tidak ditemukan):
5b.1. Sistem menampilkan pesan error "Model gagal dimuat".
5b.2. Inferensi tidak berjalan hingga konfigurasi diperbaiki.



Tabel 4.5 Use Case Scenario Memilih Model Pose Estimation
Use Case ID	UC-02
Nama	Memilih Model Pose Estimation
Aktor	Peneliti
Deskripsi	Peneliti memilih varian model pose estimation yang akan digunakan untuk sesi pengujian.
Pre-condition	1. Panel kontrol ditampilkan (dipanggil dari UC-01).
Post-condition	1. Koordinat keypoints (17 format COCO) tersedia untuk setiap frame.
2. Latensi inferensi per-frame terukur dalam milidetik.
3. Overlay skeleton ditampilkan pada preview kamera.
Main Flow		Sistem menampilkan dropdown dengan dua opsi: MoveNet Lightning (FP16) dan BlazePose Lite (FP16)
	Peneliti memilih salah satu model
	Sistem menyimpan variabel ke activeModel
Alternative Flow		

Tabel 4.6 Use Case Scenario Memilih Delegate
Use Case ID	UC-02
Nama	Memilih Delegate
Aktor	Peneliti
Deskripsi	Peneliti memilih konfigurasi akselerasi perangkat keras yang akan digunakan untuk inferensi.
Pre-condition	1. Panel kontrol ditampilkan (dipanggil dari UC-10).
Post-condition	1. Variabel activeDelegate diperbarui sesuai pilihan.
Main Flow		Sistem menampilkan dropdown dengan tiga CPU Baseline, XNNPACK, GPU Delegate.
	Peneliti memilih salah satu delegate
	Sistem menyimpan pilihan ke variabel activeDelegate.
Alternative Flow	2a. Jika perangkat tidak mendukung GPU: opsi GPU Delegate ditampilkan dengan tanda peringatan.

Tabel 4.7 Use Case Scenario Memilih Jenis Latihan
Use Case ID	UC-04
Nama	Memilih Jenis Latihan
Aktor	Peneliti
Deskripsi	Peneliti memilih jenis gerakan latihan yang akan dideteksi dan dihitung repetisinya.
Pre-condition	Panel kontrol ditampilkan (dipanggil dari UC-01)

Post-condition	State machine repetisi direset dan dikonfigurasi sesuai jenis latihan yang dipilih.
Main Flow		Sistem menampilkan dropdown dengan tiga opsi: Squat, Push-up, None.
	Peneliti memilih salah satu jenis latihan
	Sistem mereset counter repetisi dan menginisialisasi state machine sesuai pilihan
Alternative Flow	2a. Jika None dipilih: state machine dinonaktifkan, counter tidak ditampilkan.




Tabel 4.8 Use Case Scenario Menjalankan Benchmark
Use Case ID	UC-05
Nama	Menjalankan Benchmark
Aktor	Peneliti
Include	UC-06, UC-07
Extend	UC-08
Deskripsi	Sistem menjalankan loop inferensi real-time secara kontinu, memproses setiap frame dari kamera, dan menampilkan hasil deteksi pose beserta metrik performa.
Pre-condition	UC-01 telah selesai dieksekusi dan interpreter TFLite aktif.
Post-condition	1. Loop inferensi berjalann kontinu hingga peneliti menghentikan atau mengubah konfigurasi.
Main Flow		CameraX imageAnalyzer menerima frame baru dari sensor kamera
	Sistem memproses frame (UC-06) 
	Sistem menampilkan overlay skeleton dan metrik pada UI
	Loop kembali ke langkah 1 untuk frame berikutnya
Alternative Flow	1a. Jika frame kosong atau format tidak didukung: frame diskip tanpa pencatatan, loop berlanjut.

Tabel 4.9 Use Case Scenario Memproses Frame Kamera
Use Case ID	UC-06
Nama	Memproses Frame Kamera
Aktor		(dipanggil dari UC-05)
Deskripsi	Sistem melakukan pra-pemrosesan frame, menjalankan inferensi TFLite, dan memparsing output keypoints.
Pre-condition	1. Interpreter TFLite aktif dan frame tersedia.
Post-condition	1. Koordinat keypoints tersedia dan latensi inferensi terukur dalam milidetik.
Main Flow		Sistem mengonversi ImageProxy ke Bitmap 
	Sistem menerapkan letterbox padding untuk menjaga rasio aspek
	Sistem melakukan normalisasi piksel sesuai spesifikasi model
	Sistem mencatat StartTime menggunakan System.nanoTime()
	Sistem menjalankan Interpreter.run()
	Sistem mencatat endTime dan menghitung latensi
	Sistem mem-parsing output tensor menjadi keypoints
	Sistem mengoreksi korrdinasi dari ruang letterbox ke ruang kamera.
Alternative Flow	7a. Untuk model BlazePose: parsing menghasilkan 33 keypoints, kemudian dipetakan ke 17 keypoints COCO + EMA smoothing (α=0.4)
7b. Jika rata-rata confidence keypoints < 0.3: sistem mengembalikan null, frame diskip untuk deteksi repetisi.

Perancangan Arsitektur Sistem
Arsitektur dirancang menggunakan pendekatan berlapis untuk memisahkan tanggung jawab antara antarmuka pengguna, logika eksperimen, dan akses terhadap komponen inferensi serta pencatatan data. Pemisahan ini bertujuan agar perubahan pada model pose estimation, delegate TensorFlow Lite, maupun mekanisme pencatatan metrik tidak memengaruhi keseluruhan sistem secara langsung.
Diagram Arsitektur Sistem (3-Layer Architecture)
Sistem menerapkan arsitektur bertingkat (multi-layer) dengan pola MVVM (Model-View-ViewModel) untuk menjaga reaktivitas antarmuka terhadap perubahan data metrik performa secara real-time.
 
Gambar 4.2 Diagram Arsitektur Sistem
Arsitektur sistem dibagi menjadi tiga lapisan utama, yaitu Presentation Layer, Domain Layer, dan Data Layer. Presentation Layer bertanggung jawab terhadap interaksi pengguna, tampilan kamera, overlay pose, serta panel metrik real-time. Domain Layer berisi logika utama eksperimen, seperti pengaturan alur benchmarking, perhitungan repetisi, dan perhitungan metrik performa. Data Layer menangani interaksi dengan komponen teknis, seperti CameraX, TensorFlow Lite Interpreter, delegate, profiler sumber daya, logger, dan ekspor data CSV.

Tabel 4.10 Tabel Penjelasan Arsitektur Sistem
Layer	Komponen	Tanggung Jawab
Presentation Layer	
CameraScreen, PoseOverlay, MetricsPanel
	Menampilkan kamera, skeleton pose, konfigurasi, dan metrik real-time
Domain Layer	BenchmarkController, RepetitionCounter, MetricsCalculator	Mengatur alur benchmark, menghitung repetisi, dan mengolah metrik
Data Layer	TFLitePoseDetector, DelegateManager, ResourceProfiler, BenchmarkLogger, CsvExporter	Menjalankan inferensi, memilih delegate, membaca resource, mencatat data, dan ekspor CSV
External System	CameraX, TensorFlow Lite Runtime, Android Storage	Menyediakan frame kamera, runtime inferensi, dan penyimpanan file

Class Diagram
 
Gambar 4.3 Class Diagram
	Berdasarkan Gambar 4.4 rancangan kelas aplikasi AccelPose dibagi menjadi empat kelompok utama, yaitu Presentation Layer, Domain Layer, Data Layer, dan Model. Pembagian ini dilakukan agar setiap bagian sistem memiliki tanggung jawab yang jelas. Presentation Layer berfokus pada tampilan dan interaksi pengguna, Domain Layer berisi logika utama aplikasi, Data Layer menangani proses teknis seperti inferensi dan pencatatan data, sedangkan Model berisi struktur data yang digunakan oleh seluruh lapisan sistem. Pembagian ini sejalan dengan prinsip arsitektur aplikasi Android, yaitu UI layer berperan menampilkan data aplikasi dan menerima input pengguna, sedangkan domain layer digunakan untuk menampung logika bisnis yang kompleks atau dapat digunakan ulang.
	Presentation Layer terdiri atas kelas MainActivity, CameraScreen, PoseVisualization, dan PoseImageAnalyzer. MainActivity berperan sebagai entry point aplikasi serta menangani pemeriksaan izin kamera sebelum halaman utama ditampilkan. CameraScreen merupakan komponen antarmuka utama yang menampilkan preview kamera, panel konfigurasi model, pilihan delegate, pilihan jenis latihan, serta metrik real-time. PoseVisualization bertanggung jawab menggambar skeleton pose berdasarkan keypoints yang dihasilkan dari proses inferensi. Sementara itu, PoseImageAnalyzer berperan sebagai penghubung antara frame kamera dan proses analisis pose, yaitu menerima frame dari CameraX, melakukan pemrosesan awal, lalu meneruskan frame tersebut ke komponen deteksi pose.
Domain Layer berisi kelas dan interface yang menangani logika utama aplikasi. PoseViewModel menjadi komponen pusat yang mengelola state aplikasi, konfigurasi model, konfigurasi delegate, jenis latihan, hasil pose, metrik performa, dan status logging. Meskipun PoseViewModel menjadi penghubung utama antara antarmuka dan komponen lain, logika khusus tetap dipisahkan ke kelas yang lebih spesifik agar tanggung jawab setiap kelas tidak bercampur. Interface PoseDetector digunakan untuk menyeragamkan mekanisme deteksi pose, sehingga aplikasi dapat menggunakan MoveNetDetector atau BlazePoseDetector tanpa mengubah alur utama sistem. Interface ExerciseDetector digunakan untuk menyeragamkan mekanisme analisis latihan, sedangkan SquatDetector dan PushUpDetector menjadi implementasi khusus untuk masing-masing gerakan. Selain itu, AngleCalculator digunakan sebagai komponen bantu untuk menghitung sudut sendi berdasarkan keypoints tubuh.
Data Layer berisi komponen yang berhubungan dengan proses teknis sistem. MoveNetDetector dan BlazePoseDetector merupakan implementasi dari PoseDetector yang menjalankan model pose estimation sesuai pilihan pengguna. Kedua detector tersebut menggunakan TFLiteHelper untuk memuat model dan membentuk interpreter TensorFlow Lite berdasarkan konfigurasi delegate yang digunakan. BenchmarkLogger bertanggung jawab mencatat metrik performa selama sesi benchmark berlangsung, seperti latensi inferensi, FPS, penggunaan CPU, penggunaan memori, estimasi konsumsi daya, jenis latihan, dan jumlah repetisi. ResourceProfiler digunakan untuk membaca penggunaan sumber daya perangkat secara berkala agar data performa tidak hanya terbatas pada waktu inferensi, tetapi juga mencakup kondisi perangkat selama pengujian.
Kelompok Model berisi data class dan enumerasi yang digunakan oleh sistem. Person merepresentasikan hasil deteksi pose yang terdiri atas kumpulan Keypoint. Setiap Keypoint menyimpan informasi posisi koordinat, skor keypoint, serta label bagian tubuh. BenchmarkMetrics digunakan untuk merepresentasikan satu baris data hasil benchmark per frame, yang mencakup informasi waktu, model, delegate, latensi, FPS, penggunaan sumber daya, jenis latihan, jumlah repetisi, dan label sesi. Selain itu, ModelType, DelegateType, dan ExerciseType digunakan untuk membatasi pilihan konfigurasi eksperimen agar sesuai dengan rancangan pengujian, yaitu model MoveNet Lightning dan BlazePose Lite, delegate CPU Baseline, XNNPACK, dan GPU, serta jenis latihan squat dan push-up.
Relasi antar kelas pada diagram menunjukkan alur ketergantungan utama dalam sistem. CameraScreen menggunakan PoseViewModel untuk membaca dan memperbarui state aplikasi. PoseViewModel memiliki dependensi terhadap PoseDetector, ExerciseDetector, BenchmarkLogger, dan ResourceProfiler. Relasi ini menunjukkan bahwa PoseViewModel tidak menjalankan seluruh proses secara langsung, tetapi mendelegasikan tugas khusus ke komponen yang sesuai. MoveNetDetector dan BlazePoseDetector merealisasikan interface PoseDetector, sedangkan SquatDetector dan PushUpDetector merealisasikan interface ExerciseDetector. Dengan rancangan seperti ini, sistem dapat mengganti model pose estimation atau jenis latihan tanpa perlu mengubah struktur utama aplikasi.
Secara keseluruhan, class diagram ini menunjukkan bahwa aplikasi AccelPose dirancang dengan pemisahan tanggung jawab yang jelas antar komponen. Antarmuka pengguna dipisahkan dari logika inferensi, proses deteksi pose dipisahkan dari penghitungan repetisi, dan pencatatan metrik dipisahkan dari proses visualisasi. Pemisahan tersebut penting karena penelitian ini membutuhkan alat uji yang konsisten untuk membandingkan kombinasi model dan delegate. Dengan struktur kelas seperti pada Gambar 4.x, perubahan pada model, delegate, atau jenis latihan dapat dilakukan secara lebih terkontrol tanpa memengaruhi keseluruhan sistem.
Perancangan Alur Sistem
Bagian ini menjelaskan data dari kamera berubah menjadi metrik CSV. Alur data sistem dimulai dari frame kamera yang diperoleh melalui CameraX. Frame tersebut diproses pada tahap preprocessing yang meliputi resize, letterbox padding, dan normalisasi piksel sesuai kebutuhan model. Hasil preprocessing kemudian menjadi input bagi TensorFlow Lite Interpreter yang menjalankan model MoveNet Lightning atau BlazePose Lite dengan konfigurasi delegate tertentu.
Output inferensi berupa keypoints tubuh diproses kembali pada tahap postprocessing untuk menghasilkan koordinat pose yang sesuai dengan ruang tampilan kamera. Data pose kemudian digunakan oleh RepetitionCounter untuk menghitung sudut sendi dan menentukan jumlah repetisi gerakan. Secara paralel, MetricsCalculator menghitung metrik performa seperti latensi inferensi, FPS, penggunaan CPU, dan penggunaan memori. Seluruh data tersebut dicatat oleh BenchmarkLogger dan dapat diekspor menjadi file CSV untuk dianalisis pada tahap pengujian.
 
Gambar 4.4 Diagram Alir Data Inferensi

Activity Diagram Alur utama
Activity diagram berikut menggambarkan alur aktivitas utama dalam menjalankan satu sesi benchmarking lengkap, mulai dari membuka aplikasi hingga mengekspor data hasil.
Alur dimulai dengan inisialisasi aplikasi dan pengecekan izin kamera. Setelah izin diberikan, sistem menampilkan layar utama (CameraScreen) dengan konfigurasi default. Peneliti memilih kombinasi model, delegate, dan jenis latihan melalui panel kontrol. Setiap perubahan konfigurasi memicu reinisialisasi interpreter yang mencakup pelepasan sumber daya lama, pembuatan TFLiteHelper baru, dan eksekusi fase warm-up 5 frame.
Setelah konfigurasi selesai, sistem memasuki loop inferensi real-time yang berjalan secara kontinu. Pada titik ini, peneliti dapat memulai perekaman data dengan menekan tombol Record. Selama perekaman aktif, setiap frame yang melewati fase warm-up dicatat ke dalam logger. Peneliti menghentikan perekaman dengan tombol Stop, kemudian memilih untuk mengekspor data ke CSV atau mengubah konfigurasi untuk sesi berikutnya.
Activity diagram berikut mendetailkan proses internal yang terjadi pada setiap frame kamera, mencakup pra-pemrosesan, inferensi, pasca-pemrosesan, dan pencatatan data. Proses dimulai ketika CameraX ImageAnalyzer menerima frame baru. Frame dikonversi dari ImageProxy ke Bitmap, kemudian memasuki tahap letterbox padding untuk menjaga rasio aspek. Selanjutnya, normalisasi piksel dilakukan sesuai spesifikasi model: rentang [0, 1] untuk MoveNet dan [-1, 1] untuk BlazePose.
Setelah pra-pemrosesan, penanda waktu awal dicatat dan inferensi dijalankan melalui TFLite Interpreter. Penanda waktu akhir dicatat segera setelah inferensi selesai. Pada tahap pasca-pemrosesan, terdapat decision node yang memisahkan alur berdasarkan model: MoveNet langsung mem-parsing 17 keypoints COCO, sedangkan BlazePose memetakan 33 keypoints ke 17 COCO disertai transformasi sigmoid dan EMA smoothing. Koordinat kemudian dikoreksi dari ruang letterbox ke ruang kamera asli.
Terakhir, terdapat fork node yang menjalankan tiga aktivitas paralel: (1) menampilkan overlay skeleton, (2) memperbarui metrik performa pada UI, dan (3) apabila logging aktif, mencatat data ke BenchmarkLogger.
 
Gambar 4.5 Activity Diagram Alur Utama Sesi Benchmarking Pose Estimation


Activity Diagram Alur Proses Inferensi
Activity diagram pada Gambar 4.6 menggambarkan alur pemrosesan satu frame kamera mulai dari penerimaan frame oleh CameraX ImageAnalyzer hingga hasil inferensi ditampilkan dan dicatat sebagai data benchmark. Diagram ini berfokus pada proses internal aplikasi ketika sistem menerima satu frame baru, melakukan preprocessing, menjalankan inferensi TensorFlow Lite, memproses output model, memperbarui tampilan, dan menutup ImageProxy setelah pemrosesan selesai.
 
Gambar 4.6 Activity Diagram Alur Proses Inferensi 
Berdasarkan Gambar 4.6, proses dimulai ketika CameraX ImageAnalyzer menerima frame baru dalam bentuk ImageProxy. Frame tersebut dikonversi menjadi Bitmap, kemudian diproses menggunakan letterbox padding agar rasio citra tetap terjaga sebelum masuk ke model pose estimation. Tahap preprocessing disesuaikan dengan model yang aktif. MoveNet Lightning menggunakan ukuran input 192 × 192 dengan normalisasi [0, 1], sedangkan BlazePose Lite menggunakan ukuran input 256 × 256 dengan normalisasi [-1, 1].
Setelah preprocessing selesai, sistem mencatat waktu awal menggunakan System.nanoTime(), menjalankan inferensi melalui TFLite Interpreter, lalu mencatat waktu akhir untuk menghitung latensi inferensi. Output inferensi kemudian diproses sesuai model yang digunakan. Pada MoveNet, output diproses menjadi 17 keypoints dalam format COCO. Pada BlazePose, output 33 keypoints dipetakan ke format COCO agar dapat digunakan oleh alur visualisasi dan perhitungan metrik yang sama.
Hasil keypoints kemudian dikoreksi dari ruang letterbox ke ruang kamera dan dihitung rata-rata confidence-nya. Apabila confidence memenuhi ambang minimum, sistem membentuk hasil pose, memperbarui state aplikasi, menggambar overlay skeleton, dan memperbarui panel metrik. Apabila logging aktif, data per-frame disimpan ke buffer BenchmarkLogger sebagai BenchmarkMetrics. Apabila confidence tidak memenuhi ambang minimum, sistem menandai bahwa pose tidak terdeteksi pada frame tersebut. Pada akhir proses, ImageProxy ditutup agar pipeline CameraX dapat menerima frame berikutnya. Penutupan ImageProxy penting karena dokumentasi CameraX menyatakan bahwa frame harus dirilis dengan memanggil ImageProxy.close() setelah analisis selesai.
Sequence Diagram
Perancangan sequence diagram merupakan bagian dari pendekatan Object Oriented Design (OOD) yang digunakan untuk memodelkan interaksi antar objek dalam sistem. Sequence diagram menggambarkan alur komunikasi dan pertukaran pesan antar objek dalam urutan waktu tertentu untuk setiap use case atau skenario yang telah didefinisikan. Diagram ini memvisualisasikan bagaimana objek-objek dalam sistem berkolaborasi untuk menyelesaikan suatu fungsi atau proses bisnis tertentu.
Diagram sekuen berikut menggambarkan interaksi antar komponen sistem pada tiga skenario utama: inisialisasi sistem, proses inferensi real-time, serta penghitungan repetisi dan pencatatan data. Ketiga diagram ini merepresentasikan alur kontrol yang mengikuti pola arsitektur MVVM berlapis (Presentation-Domain-Data Layer).
Perancangan Sequence Diagram Inisialisasi Sistem
Sequence diagram inisialisasi sistem menggambarkan interaksi antar komponen ketika aplikasi pertama kali dijalankan hingga sistem siap menerima frame kamera untuk proses inferensi. Diagram ini melibatkan aktor Peneliti serta beberapa komponen utama, yaitu MainActivity, CameraScreen, PoseViewModel, TFLiteHelper, ResourceProfiler, dan TFLite Interpreter. Alur ini dirancang untuk memastikan bahwa izin kamera, konfigurasi model, konfigurasi delegate, dan proses profiling sumber daya telah disiapkan sebelum sesi benchmarking dilakukan.
 
Gambar 4.7 Sequence Diagram Inisialisasi Sistem
Berdasarkan Gambar 4.5, proses inisialisasi dimulai ketika Peneliti membuka aplikasi. MainActivity terlebih dahulu memeriksa izin kamera. Apabila izin belum diberikan, sistem menampilkan PermissionRequestScreen agar Peneliti dapat memberikan akses kamera. Setelah izin tersedia, MainActivity menampilkan CameraScreen sebagai antarmuka utama untuk menjalankan proses benchmarking.
Pada tahap berikutnya, CameraScreen menginisialisasi PoseViewModel dengan konfigurasi awal, yaitu model MoveNet Lightning dan CPU Baseline. PoseViewModel kemudian membentuk objek TFLiteHelper untuk memuat berkas model dan membuat TFLite Interpreter sesuai konfigurasi delegate yang dipilih. Apabila sistem menggunakan CPU Baseline, interpreter dibuat tanpa XNNPACK. Apabila sistem menggunakan XNNPACK, interpreter dibuat dengan mengaktifkan XNNPACK. Sementara itu, apabila sistem menggunakan GPU, interpreter dibuat dengan GPU delegate. Delegate dalam LiteRT/TensorFlow Lite digunakan untuk memanfaatkan akselerator perangkat seperti GPU atau DSP, sehingga bagian ini penting untuk mendukung perbandingan performa antar konfigurasi akselerasi. 
Diagram ini juga menunjukkan alur alternatif ketika inisialisasi GPU gagal. Pada kondisi tersebut, TFLiteHelper melakukan fallback ke XNNPACK agar aplikasi tetap dapat berjalan meskipun GPU delegate tidak tersedia atau tidak kompatibel pada perangkat uji. Setelah interpreter berhasil dibuat, ResourceProfiler mulai melakukan polling penggunaan CPU, memori, dan daya, sedangkan CameraScreen menjalankan CameraX Preview serta mendaftarkan PoseImageAnalyzer. Dengan demikian, keluaran dari proses ini adalah sistem yang telah siap menerima frame dan menjalankan inferensi pose estimation secara real-time.

Perancangan Sequence Diagram Proses Inferensi Real-time
 
Gambar 4.8 Sequence Diagram Siklus Inferensi Real-time
Berdasarkan Gambar 4.6, proses inferensi dimulai ketika CameraX mengirimkan frame kamera dalam bentuk ImageProxy kepada PoseImageAnalyzer. Setiap frame yang diterima dihitung sebagai bagian dari total frame, kemudian dikonversi menjadi bitmap dan diproses melalui tahap letterbox agar ukuran input sesuai dengan kebutuhan model. Pada tahap ini, frame juga dipersiapkan agar dapat digunakan oleh model pose estimation tanpa mengubah rasio citra secara berlebihan.
Setelah preprocessing selesai, PoseImageAnalyzer memanggil PoseDetector untuk menjalankan proses deteksi pose. PoseDetector mencatat waktu awal sebelum inferensi dan waktu akhir setelah inferensi selesai, sehingga latensi inferensi dapat dihitung dalam satuan milidetik. Proses inferensi dilakukan oleh TFLite Interpreter melalui input buffer dan output buffer. Pada implementasi CameraX, frame yang diterima melalui ImageAnalysis harus dianalisis secepat mungkin dan ImageProxy perlu ditutup setelah selesai agar pipeline kamera tidak terhambat. 
Diagram ini juga menunjukkan adanya percabangan proses berdasarkan model yang digunakan. Jika model yang aktif adalah MoveNet Lightning, output tensor diproses menjadi 17 keypoints. Jika model yang aktif adalah BlazePose Lite, output tensor diproses menjadi 33 keypoints kemudian dipetakan ke format keypoints yang digunakan sistem. Setelah keypoints diperoleh, sistem menerapkan smoothing untuk mengurangi fluktuasi posisi titik tubuh antar-frame. Hasil akhir dari tahap ini berupa data pose yang telah dikoreksi koordinatnya agar sesuai dengan tampilan kamera.
Pada akhir siklus, hasil inferensi dikirimkan ke PoseViewModel. Jika frame masih berada pada fase warm-up, hasil tidak digunakan sebagai data benchmark. Jika warm-up telah selesai, PoseViewModel memperbarui state aplikasi dengan data pose, latensi, FPS, dan jumlah repetisi. Apabila jenis latihan yang dipilih bukan NONE, ExerciseDetector menganalisis pose untuk menghitung repetisi gerakan. Selanjutnya, CameraScreen menampilkan skeleton overlay dan memperbarui panel metrik. Dengan demikian, sequence diagram ini menjelaskan alur inti sistem dari frame kamera hingga visualisasi pose dan metrik real-time.


Perancangan Sequence Diagram Pencatatan dan Ekspor Data
 
Gambar 4.9 Sequence Diagram Pencatatan dan Ekspor Data

Berdasarkan Gambar 4.7, proses dimulai ketika Peneliti menekan tombol Record pada CameraScreen. CameraScreen meneruskan perintah tersebut ke PoseViewModel untuk memulai proses logging. PoseViewModel kemudian meminta BenchmarkLogger membuka sesi pencatatan baru berdasarkan label konfigurasi eksperimen. Pada tahap ini, BenchmarkLogger mengaktifkan status logging, membersihkan antrean data sebelumnya, dan mengatur ulang nomor frame. Setelah sesi berhasil dimulai, CameraScreen memperbarui tampilan tombol Record menjadi Stop dan menampilkan indikator bahwa proses perekaman sedang berjalan.
Selama proses logging aktif, setiap frame yang telah melewati proses inferensi menghasilkan data benchmark yang dicatat ke dalam BenchmarkLogger. Data yang dicatat mencakup timestamp, model yang digunakan, delegate yang digunakan, latensi inferensi, FPS, penggunaan CPU, penggunaan memori, estimasi konsumsi daya, jenis latihan, jumlah repetisi, nomor frame, status deteksi pose, dan label sesi. Namun, frame pada fase warm-up tidak dimasukkan ke dalam data benchmark karena fase tersebut digunakan untuk menstabilkan proses awal inferensi. Setelah fase warm-up selesai, metrik per-frame disimpan ke dalam antrean logger dan nomor frame diperbarui.
Ketika Peneliti menekan tombol Stop, CameraScreen meminta PoseViewModel menghentikan proses logging. BenchmarkLogger kemudian mengubah status logging menjadi tidak aktif dan menghitung statistik ringkasan seperti nilai rata-rata, minimum, maksimum, dan standar deviasi untuk setiap metrik. Statistik tersebut dikembalikan ke PoseViewModel dan ditampilkan oleh CameraScreen sebagai ringkasan hasil sesi benchmark.
Pada fase ekspor, Peneliti menekan tombol Export CSV. PoseViewModel meneruskan perintah ekspor kepada BenchmarkLogger untuk mengubah antrean data menjadi format CSV. Sistem kemudian membangkitkan nama file berdasarkan timestamp agar setiap hasil pengujian memiliki nama unik. Untuk penyimpanan pada perangkat Android modern, diagram menggunakan MediaStore API agar file hasil ekspor dapat disimpan melalui mekanisme penyimpanan yang sesuai dengan kebijakan shared storage Android. Android menyediakan MediaStore API untuk mengakses dan mengelola file pada shared storage, termasuk file yang dibuat oleh aplikasi sendiri. 
Diagram ini juga menunjukkan alur alternatif ketika ekspor berhasil atau gagal. Apabila proses penyimpanan berhasil, sistem mengembalikan lokasi file dan menampilkan pesan bahwa data berhasil diekspor. Apabila terjadi kesalahan seperti IOException, sistem menampilkan pesan bahwa data gagal disimpan. Dengan demikian, diagram ini memastikan bahwa proses pencatatan data benchmark tidak hanya mencakup pengumpulan metrik, tetapi juga validasi akhir berupa penyimpanan file CSV yang akan digunakan pada tahap analisis hasil penelitian.

Perancangan Algoritma 
Flowchart Alur Inferensi
 
Gambar 4.10 Flowchart Alur Inferensi Per Frame
Gambar 4.10 menggambarkan alur inferensi pose yang dirancang untuk berjalan pada setiap frame yang diterima dari kamera. Proses dimulai dari penerimaan frame mentah, dilanjutkan dengan pra-pemrosesan yang mencakup letterbox padding untuk menjaga rasio aspek, resize ke dimensi masukan model, dan normalisasi nilai piksel. Pra-pemrosesan bercabang berdasarkan model yang aktif: MoveNet Lightning menggunakan resolusi 192×192 piksel dengan normalisasi ke rentang [0, 1], sedangkan BlazePose Lite menggunakan 256×256 piksel dengan rentang [-1, 1].
Setelah inferensi dijalankan, latensi diukur dan keluaran model melalui pasca-pemrosesan untuk menghasilkan 17 keypoints dalam format COCO. Koordinat keypoints kemudian dikoreksi kembali ke ruang koordinat kamera asli dengan menginversi transformasi letterbox. Pose dinyatakan valid apabila rata-rata skor kepercayaan seluruh keypoints memenuhi ambang batas yang ditetapkan. Terakhir, terdapat protokol warm-up untuk lima frame pertama — selama fase ini hasil inferensi tidak dicatat sebagai data benchmark guna menghindari pencilan akibat inisialisasi sistem.
State Machine Repetisi
 
Gambar 4.11 State Machine Perhitungan Repetisi Squat dan Push-up
State machine pada Gambar 4.11 digunakan untuk memodelkan perubahan fase gerakan pada proses penghitungan repetisi squat dan push-up. Pendekatan ini dipilih karena satu repetisi tidak dapat ditentukan hanya dari nilai sudut pada satu frame, melainkan dari urutan perubahan fase gerakan. Oleh karena itu, sistem menyimpan state gerakan saat ini dan hanya menambah jumlah repetisi apabila pengguna telah melewati urutan state yang valid.
Pada squat detector, state awal adalah STANDING, yaitu kondisi ketika sudut lutut berada pada posisi relatif lurus. Ketika sudut lutut menurun melewati threshold tertentu, sistem berpindah ke state GOING_DOWN. Jika sudut terus mengecil hingga mencapai fase bawah, sistem masuk ke state SQUATTING. Setelah itu, ketika sudut lutut kembali meningkat, sistem berpindah ke state GOING_UP. Repetisi baru dihitung ketika sistem kembali mencapai posisi berdiri dengan sudut lutut memenuhi threshold akhir. Dengan alur ini, sistem dapat menghindari penghitungan repetisi ganda akibat fluktuasi sudut pada satu fase gerakan.
Pada push-up detector, state awal adalah UP, yaitu kondisi ketika sudut siku menunjukkan posisi lengan relatif lurus. Ketika sudut siku menurun, sistem berpindah ke state GOING_DOWN_P dan kemudian masuk ke state DOWN ketika sudut mencapai fase bawah. Setelah itu, sistem berpindah ke state GOING_UP_P ketika sudut siku meningkat kembali. Repetisi dihitung ketika sudut siku mencapai threshold posisi atas. Sama seperti pada squat, mekanisme ini memastikan bahwa repetisi dihitung berdasarkan urutan gerakan penuh dari posisi awal, turun, lalu kembali ke posisi awal.
State machine ini juga menerapkan hysteresis, yaitu penggunaan threshold masuk dan keluar yang berbeda untuk mengurangi perubahan state yang terlalu cepat akibat noise pada hasil pose estimation. Selain itu, sudut sendi dihaluskan terlebih dahulu sebelum dievaluasi oleh state machine agar perubahan kecil antar-frame tidak langsung menyebabkan transisi state. Hold time pada posisi bawah digunakan sebagai indikator kestabilan gerakan. Pada implementasi saat ini, hold time berfungsi sebagai validasi tambahan dan dapat menghasilkan peringatan apabila durasi terlalu pendek, tetapi tidak menjadi syarat utama yang memblokir transisi ke state berikutnya.
Error Handling & Fallback
Pada umumnya, perancangan sistem harus mampu menangani berbagai kondisi kegagalan yang mungkin terjadi selama eksperimen tanpa menyebabkan aplikasi crash. Mekanisme penanganan kesalahan dibagi menjadi tiga kategori berdasarkan tingkat keparahan.
Tabel 4.11 Tabel Skenario Kegagalan dan Mekanisme Penanganan
Kategori	Kondisi Kegagalan	Mekanisme Penanganan	Dampak terhadap Eksperimen
Kegagalan Inisialisasi GPU	GpuDelegate gagal dibuat karena driver GPU tidak kompatibel atau operator model tidak didukung oleh GPU.	1. Sistem menangkap exception pada blok try-catch.
2. GpuDelegate dilepaskan (dispose).
3. Interpreter diinisialisasi ulang dengan XNNPACK.
4. Pesan peringatan ditampilkan pada UI.	Eksperimen tetap berjalan dengan delegate XNNPACK. Data konfigurasi GPU pada perangkat ini ditandai sebagai "tidak tersedia".
Kegagalan Pemuatan Model	File .tflite tidak ditemukan di folder assets atau file corrupt.	1. Exception ditangkap saat loadModelFile().
2. Interpreter diset ke null.
3. Pesan error ditampilkan pada UI.	Inferensi tidak berjalan. Peneliti harus memverifikasi file model dan menginstal ulang aplikasi.
Frame Kamera Tidak Tersedia	ImageProxy kosong atau format tidak didukung.	1. Fungsi analyze() mengembalikan hasil kosong.
2. Frame diskip tanpa pencatatan.
3. Counter totalFrameCount tidak bertambah.	Frame yang gagal tidak tercatat. Tidak memengaruhi data yang sudah dikumpulkan.
Keypoints Confidence Rendah	Rata-rata skor confidence seluruh keypoints di bawah 0,3 (pose tidak terdeteksi).	1. Fungsi detectPose() mengembalikan null.
2. Overlay skeleton tidak digambar.
3. Deteksi repetisi diskip untuk frame ini.	Frame dengan confidence rendah tetap mencatat latensi inferensi (karena inferensi tetap berjalan), namun repetisi tidak dihitung.
Kegagalan Ekspor CSV	Penyimpanan perangkat penuh atau izin tulis tidak diberikan.	1. Exception ditangkap saat operasi I/O.
2. Pesan error ditampilkan.
3. Data tetap tersimpan di memori (antrian logger).	Peneliti dapat mencoba ekspor ulang setelah mengosongkan penyimpanan.
Kegagalan Pembacaan /proc/stat	Android membatasi akses ke file sistem pada API Level 26+.	1. Fallback ke Process.getElapsedCpuTime().
2. Metrik CPU tetap tersedia meskipun granularitas lebih rendah.	Nilai utilisasi CPU mungkin kurang granular tetapi tetap representatif.

Perancangan Anttarmuka
Antarmuka aplikasi dirancang dengan prioritas fungsionalitas pengujian, bukan estetika. Seluruh elemen kontrol eksperimen dan tampilan metrik harus dapat diakses dari satu layar tanpa perpindahan halaman (single-screen design). Pendekatan ini dipilih untuk meminimalkan latensi interaksi saat peneliti berganti konfigurasi antar-sesi pengujian.
Wireframe Layar Utama
Layar utama terdiri dari tiga lapisan yang ditumpuk secara vertikal menggunakan Box layout: lapisan kamera, lapisan overlay skeleton, dan lapisan panel kontrol. Gambar 4.12 menunjukkan wireframe rancangan antarmuka dalam dua kondisi panel: collapsed (ringkas) dan expanded (lengkap).
 
Gambar 4.12 Wireframe Layar Utama
Pada mode collapsed, panel kontrol menampilkan hanya dua metrik utama (latensi dan FPS) serta penghitung repetisi apabila jenis latihan dipilih. Mode ini digunakan saat pengujian berlangsung untuk memaksimalkan area tampilan kamera. Pada mode expanded, panel kontrol menampilkan seluruh komponen berikut:
	Tiga dropdown selector untuk memilih model, delegate, dan jenis latihan.
	Panel metrik lengkap: latensi (ms), FPS, utilisasi CPU (%), penggunaan memori (MB), dan estimasi konsumsi daya (mW).
	Panel latihan: jumlah repetisi, sudut sendi aktual, dan tombol reset.
	Panel logging: tombol Record/Stop, counter frame, durasi sesi, dan tombol Export CSV.
	Informasi perangkat: model, versi Android, dan chipset.

Indikator warm-up ditampilkan di bagian atas layar selama lima frame pertama setelah inisialisasi, memberikan umpan balik visual kepada peneliti bahwa sistem belum dalam kondisi stabil untuk pencatatan data. Indikator ini menghilang secara otomatis setelah fase warm-up selesai.
Strategi Pengukuran Metrik
Keandalan data eksperimen bergantung pada ketepatan strategi pengukuran. Subbab ini merinci rancangan mekanisme pengukuran untuk setiap variabel terikat: latensi inferensi, profiling sumber daya sistem, dan struktur pencatatan data.
Pengukuran Latensi Inferensi
Latensi inferensi didefinisikan sebagai waktu yang dibutuhkan oleh TFLite Interpreter untuk memproses satu frame masukan dan menghasilkan prediksi keypoints. Pengukuran dibatasi hanya pada pemanggilan interpreter.run() tanpa menyertakan waktu pra-pemrosesan dan pasca-pemrosesan, sehingga nilai yang diperoleh mencerminkan performa akselerasi delegate secara murni.
Pengukuran menggunakan System.nanoTime() yang memberikan resolusi waktu dalam nanosecond. Metode ini dipilih karena tidak terpengaruh oleh perubahan jam sistem (wall clock) dan memberikan presisi yang cukup untuk mengukur latensi dalam rentang puluhan milidetik. Pseudocode pengukuran dirancang sebagai berikut:
Tabel 4.12 Pseudocode Pengukuran Latensi Inferensi
// Inisialisasi warm-up counter
frameCount ← 0
WARM_UP_FRAMES ← 5
PROCEDURE analyzeFrame(imageProxy):
    frameCount ← frameCount + 1
    isWarmUp ← (frameCount ≤ WARM_UP_FRAMES)
    bitmap ← convertToBitmap(imageProxy)
    letterboxed ← applyLetterbox(bitmap, model.inputSize)
    startTime ← System.nanoTime()             // catat waktu mulai
    result ← interpreter.run(letterboxed)      // HANYA blok ini yang diukur
    endTime ← System.nanoTime()               // catat waktu selesai
    latencyMs ← (endTime - startTime) / 1_000_000
    IF isWarmUp THEN
        sendToViewModel(result, latency = 0)   // tidak dicatat
    ELSE
        sendToViewModel(result, latencyMs)     // dicatat ke logger
    END IF
    imageProxy.close()
END PROCEDURE

Protokol warm-up sebanyak 5 frame pertama diterapkan untuk menghindari pencilan akibat dua fenomena, yaitu inisialisasi cache GPU yang menyebabkan inferensi pertama berjalan 2–3 kali lebih lambat dari kondisi stabil, dan JIT compilation yang memengaruhi kinerja beberapa iterasi pertama eksekusi kode. Data latensi yang dicatat dimulai dari frame keenam dan seterusnya, yang merepresentasikan kondisi operasional normal.
Profiling Sumber Daya Sistem
Profiling sumber daya dilakukan secara paralel dengan inferensi menggunakan interval polling 100 milidetik. Interval ini dipilih untuk menyeimbangkan resolusi temporal yang memadai dengan beban overhead pengukuran yang minimal. Tiga metrik sumber daya diukur menggunakan API Android yang berbeda-beda, sebagaimana dijelaskan pada Tabel 4.13.
Tabel 4.13 Metode Pengukuran Profiling Sumber Daya
Metrik	API Utama	Mekanisme	Fallback
Utilisasi CPU (%)	/proc/stat /proc/[pid]/stat	Delta waktu CPU aplikasi dibagi delta total waktu CPU, dikalikan jumlah inti prosesor.	Process.getElapsedCpuTime() — jika /proc/stat tidak dapat diakses (Android ≥ 8.0).
Penggunaan Memori (MB)	Debug.MemoryInfo API	Proportional Set Size (PSS) dalam kilobyte dibagi 1024. PSS memperhitungkan pembagian memori antar-proses secara proporsional.	Runtime.totalMemory() − freeMemory() sebagai estimasi kasar Java heap.
Konsumsi Daya (mW) [deskriptif]	BatteryManager API	P = V (mV) × I (µA) / 1.000.000, di mana V adalah tegangan baterai dan I adalah arus baterai saat ini.	Tidak tersedia jika perangkat tidak mendukung BATTERY_PROPERTY_CURRENT_NOW.

Konsumsi daya dilaporkan sebagai metrik deskriptif tambahan dan tidak dimasukkan ke dalam analisis ANOVA. Alasannya adalah keterbatasan granularitas sensor baterai pada perangkat Android low-to-mid range, sehingga nilai arus baterai diperbarui dengan interval yang tidak konsisten dan sangat dipengaruhi oleh faktor eksternal seperti aktivitas layar dan sensor akselerometer. Akibatnya, fluktuasi nilai yang tinggi dalam sesi pengukuran berdurasi pendek menurunkan validitas uji statistik.
Struktur File CSV Output
Seluruh data metrik per-frame diekspor ke format CSV untuk analisis statistik lanjutan. Format penamaan berkas mengikuti pola: accelpose_benchmark_[yyyyMMdd_HHmmss].csv. Data dari masing-masing perangkat uji disimpan dalam berkas terpisah untuk memudahkan analisis per-perangkat. Struktur kolom CSV dirancang sebagaimana disajikan pada Tabel 4.14.
No	Nama Kolom	Tipe Data	Deskripsi
1	timestamp	Long	Waktu pengambilan data (epoch milliseconds)
2	model_type	String	Nama model: MoveNet Lightning atau BlazePose Lite
3	delegate_type	String	Delegate aktif: CPU Baseline, XNNPACK, atau GPU
4	inference_time_ms	Long	Latensi inferensi murni TFLite (milidetik) — variabel terikat utama
5	processing_time_ms	Long	Waktu end-to-end termasuk pra/pasca-pemrosesan (milidetik)
6	fps	Float	Throughput (frame per second) — variabel terikat utama
7	cpu_usage_percent	Float	Utilisasi CPU proses aplikasi (%) — variabel terikat utama
8	memory_usage_mb	Float	Penggunaan memori PSS (megabyte) — variabel terikat utama
9	power_consumption_mw	Float	Estimasi konsumsi daya (miliwatt) — metrik deskriptif
10	exercise_type	String	Jenis latihan: SQUAT, PUSH_UP, atau NONE
11	repetition_count	Integer	Jumlah repetisi kumulatif yang terdeteksi dalam sesi
12	frame_number	Integer	Nomor urut frame dalam sesi logging (mulai dari 1)
13	pose_detected	Integer	Status deteksi pose: 1 = terdeteksi, 0 = tidak terdeteksi
14	session_label	String	Label sesi dalam format [Model]_[Delegate]_[Latihan]
15	effective_delegate_type	String	Delegate yang benar-benar digunakan (berbeda dari delegate_type jika terjadi fallback GPU → XNNPACK)

Kolom effective_delegate_type (kolom ke-15) merupakan tambahan penting untuk keandalan data eksperimen. Kolom ini mencatat delegate yang benar-benar digunakan oleh interpreter, yang dapat berbeda dari kolom delegate_type apabila terjadi fallback otomatis dari GPU ke XNNPACK. Dengan demikian, analisis statistik dapat mengidentifikasi dan mengeksklusi sesi yang tidak sesuai dengan konfigurasi yang dimaksud, tanpa bergantung pada observasi manual peneliti.
Pencatatan data hanya aktif ketika peneliti menekan tombol Record pada panel kontrol dan berjalan pada thread terpisah dari thread UI untuk memastikan operasi I/O tidak memengaruhi pengukuran latensi. Data diekspor ke folder Downloads perangkat menggunakan MediaStore API (Android 10+) atau akses penyimpanan eksternal langsung untuk versi Android sebelumnya.


IMPLEMENTASI SISTEM
Lingkungan Pengembangan
Pengembangan aplikasi menggunakan Android Studio Iguana (2024.1.1) dengan bahasa pemrograman Kotlin dan sistem build Gradle Kotlin DSL. Pustaka inferensi menggunakan TensorFlow Lite versi 2.14.0 yang terdiri dari tiga komponen terpisah untuk kontrol granular terhadap fitur akselerasi. Spesifikasi lengkap lingkungan pengembangan disajikan pada Tabel 5.1
Tabel 5.1 Spesifikasi Lingkungan Pengembangan
Komponen	Spesifikasi
IDE	Android Studio Iguana (2024.1.1)
Bahasa Pemrograman	Kotlin (JVM Target 11)
Build System	Gradle Kotlin DSL
Minimum SDK	API Level 26 (Android 8.0 Oreo)
Target SDK	API Level 34 (Android 14)
UI Framework	Jetpack Compose (Material 3)
Kamera	CameraX 1.3.0
tensorflow-lite	2.14.0 — runtime inferensi utama
tensorflow-lite-gpu	2.14.0 — dukungan GPU Delegate
tensorflow-lite-support	0.4.4 — utilitas pra-pemrosesan citra
Arsitektur	MVVM (Model-View-ViewModel)

Minimum SDK ditetapkan pada API Level 26 (Android 8.0) untuk memastikan kompatibilitas dengan kedua perangkat uji. Pemisahan library TensorFlow Lite menjadi tiga komponen memungkinkan fleksibilitas dalam memuat GPU Delegate secara kondisional sesuai dukungan perangkat, tanpa mewajibkan seluruh perangkat memiliki driver OpenCL/OpenGL yang kompatibel.

Implementasi Antarmuka
Antarmuka pengguna diimplementasikan menggunakan Jetpack Compose dengan pendekatan deklaratif dan reaktif. Seluruh elemen UI merespons perubahan state secara otomatis melalui mekanisme StateFlow dari ViewModel tanpa manipulasi tampilan manual.
Struktur Aktivitas Utama
Antarmuka pengguna diimplementasikan menggunakan Jetpack Compose dengan pendekatan deklaratif dan reaktif. Seluruh elemen UI merespons perubahan state secara otomatis melalui mekanisme StateFlow dari ViewModel tanpa manipulasi tampilan manual.
class MainActivity : ComponentActivity() {
    private var hasCameraPermission by mutableStateOf(false)
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasCameraPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        setContent {
            if (hasCameraPermission) CameraScreen()
            else PermissionRequestScreen { cameraPermissionLauncher.launch(...) }
        }
    }
}

Pendekatan ini memastikan akses ke sensor kamera hanya dilakukan setelah izin eksplisit diberikan pengguna, sesuai kebijakan keamanan Android sejak API Level 23.

Layar Deteksi & Panel Kontrol
Komponen utama antarmuka diimplementasikan dalam fungsi composable bernama CameraScreen. Layar ini menggunakan Box layout untuk menumpuk empat lapisan secara vertikal: (1) preview kamera sebagai dasar, (2) visualisasi overlay skeleton pose, (3) indikator warm-up, dan (4) panel kontrol di bagian bawah. CameraX dikonfigurasi dengan resolusi target 640×480 piksel dan strategi STRATEGY_KEEP_ONLY_LATEST untuk membuang frame yang tidak sempat diproses, sehingga menjaga responsivitas antarmuka.
	Panel kontrol (ControlPanel) dirancang dalam dua mode: collapsed dan expanded. Pada mode collapsed, hanya metrik FPS dan latensi yang ditampilkan bersama penghitung repetisi. Pada mode expanded, seluruh komponen pengujian tersedia: tiga dropdown selector untuk konfigurasi model, delegate, dan jenis latihan; panel metrik lengkap (latensi, FPS, CPU, memori, daya); panel latihan dengan sudut sendi aktual; panel logging dengan tombol Record/Stop dan Export CSV; serta informasi perangkat. Setiap perubahan konfigurasi memicu reinisialisasi interpreter melalui PoseViewModel.
Indikator warm-up (kelas WarmUpIndicator) ditampilkan di bagian atas layar dengan latar jingga selama 5 frame pertama setelah inisialisasi, memberikan umpan balik visual kepada peneliti bahwa sistem belum dalam kondisi stabil.
Konfigurasi preview kamera menggunakan ScaleType.FIT_CENTER (bukan FILL_CENTER yang merupakan nilai default). Pemilihan ini penting: FILL_CENTER melakukan cropping pada tepi frame sehingga koordinat keypoints yang dihasilkan model tidak sesuai dengan posisi aktual pada tampilan, sedangkan FIT_CENTER mempertahankan seluruh konten frame dan menjamin akurasi overlay skeleton 100%.
Implementasi Inferensi TFLite
Pemuatan Model & Inisialisasi Delegate
Panel kontrol (ControlPanel) dirancang dalam dua mode: collapsed dan expanded. Pada mode collapsed, hanya metrik FPS dan latensi yang ditampilkan bersama penghitung repetisi. Pada mode expanded, seluruh komponen pengujian tersedia: tiga dropdown selector untuk konfigurasi model, delegate, dan jenis latihan; panel metrik lengkap (latensi, FPS, CPU, memori, daya); panel latihan dengan sudut sendi aktual; panel logging dengan tombol Record/Stop dan Export CSV; serta informasi perangkat. Setiap perubahan konfigurasi memicu reinisialisasi interpreter melalui PoseViewModel.
Indikator warm-up (kelas WarmUpIndicator) ditampilkan di bagian atas layar dengan latar jingga selama 5 frame pertama setelah inisialisasi, memberikan umpan balik visual kepada peneliti bahwa sistem belum dalam kondisi stabil.
Konfigurasi preview kamera menggunakan ScaleType.FIT_CENTER (bukan FILL_CENTER yang merupakan nilai default). Pemilihan ini penting: FILL_CENTER melakukan cropping pada tepi frame sehingga koordinat keypoints yang dihasilkan model tidak sesuai dengan posisi aktual pada tampilan, sedangkan FIT_CENTER mempertahankan seluruh konten frame dan menjamin akurasi overlay skeleton 100%.
fun initializeInterpreter(): TFLiteInitializationResult {
    val modelBuffer = loadModelFile(modelFileName)
    var options = Interpreter.Options()
    var effectiveDelegateType = delegateType
    var usedFallback = false

    when (delegateType) {
        CPU_BASELINE -> {
            options.setNumThreads(4)
            options.setUseXNNPACK(false)   // Tanpa XNNPACK — baseline murni
        }
        CPU_XNNPACK -> {
            options.setNumThreads(4)
            options.setUseXNNPACK(true)    // Aktifkan SIMD ARM NEON
        }
        GPU -> {
            try {
                gpuDelegate = GpuDelegate()
                options.addDelegate(gpuDelegate)
            } catch (e: Throwable) {
                // Fallback ke XNNPACK jika GPU tidak kompatibel
                options.setUseXNNPACK(true)
                effectiveDelegateType = CPU_XNNPACK
                usedFallback = true
            }
        }
    }
    interpreter = Interpreter(modelBuffer, options)
    return TFLiteInitializationResult(interpreter, effectiveDelegateType, usedFallback)
}
Pada tingkat pertama (CPU Baseline), interpreter dijalankan menggunakan kernel bawaan TFLite tanpa optimasi XNNPACK, berfungsi sebagai acuan perbandingan. Tingkat kedua (XNNPACK) mengaktifkan instruksi SIMD ARM NEON untuk operasi aritmetika vektor yang lebih efisien. Tingkat ketiga (GPU) mendaftarkan objek GpuDelegate untuk mengalihkan komputasi ke GPU. Mekanisme fallback otomatis diterapkan: jika GPU gagal, sistem beralih ke XNNPACK dan mencatat nilai effectiveDelegateType yang sebenarnya ke dalam data CSV melalui kolom effective_delegate_type, sehingga analisis statistik dapat mengidentifikasi sesi yang tidak sesuai konfigurasi yang dimaksud.
Antarmuka Detektor 
Pergantian model secara dinamis difasilitasi menggunakan pola desain Strategy Pattern melalui antarmuka PoseDetector. Antarmuka ini mendefinisikan kontrak empat fungsi yang harus diimplementasikan oleh setiap detektor: detectPose(bitmap) untuk menjalankan inferensi, getInputSize() untuk mengembalikan dimensi masukan model, isClosed() untuk memeriksa status, dan close() untuk melepas sumber daya. Dengan abstraksi ini, PoseViewModel dan PoseImageAnalyzer tidak perlu mengetahui detail implementasi model yang sedang aktif.

MoveNet Lightning Detector
Kelas MoveNetDetector mengimplementasikan antarmuka PoseDetector untuk model MoveNet Lightning. Pra-pemrosesan citra menggunakan ImageProcessor dengan dua tahap: resize bilinear ke 192×192 piksel, diikuti normalisasi nilai piksel ke rentang [0, 1] menggunakan NormalizeOp(0f, 255f) khusus untuk input bertipe FLOAT32. Untuk input UINT8, normalisasi tidak diperlukan.
Tensor keluaran MoveNet berformat [1, 1, 17, 3] yang merepresentasikan satu orang, 17 keypoints format COCO, dengan tiga nilai per keypoint: koordinat y ternormalisasi, koordinat x ternormalisasi, dan skor kepercayaan. Pose dinyatakan valid apabila rata-rata skor kepercayaan seluruh keypoints melebihi ambang batas 0,3.
EMA (Exponential Moving Average) diterapkan pada koordinat keypoints untuk meredam jitter antar-frame. Koefisien adaptif digunakan: α = 0,4 untuk keypoints dengan skor ≥ 0,15, dan α = 0,1 untuk keypoints dengan skor rendah. Penggunaan α yang lebih kecil (bukan pembekuan penuh) pada keypoints berkepercayaan rendah merupakan perbaikan kritis dari implementasi sebelumnya — pembekuan penuh menyebabkan keypoints bagian bawah tubuh (pinggul, lutut, pergelangan kaki) terkunci di posisi berdiri selama gerakan squat, mengakibatkan algoritma penghitungan repetisi tidak dapat mendeteksi transisi state. Selain α adaptif, mekanisme deadband (ambang batas 0,008 dalam ruang ternormalisasi) diterapkan untuk mengabaikan pergerakan sub-piksel saat subjek diam.
Untuk menangani situasi di mana pose tidak terdeteksi sesaat (skor rata-rata turun di bawah 0,3 secara temporer, yang sering terjadi pada fase terbawah gerakan squat karena foreshortening kamera depan), riwayat EMA tidak langsung direset. Riwayat baru dihapus setelah 10 kegagalan berturut-turut, sehingga sudut sendi tidak melompat drastis saat deteksi kembali.
BlazePose Lite Detector
Kelas BlazePoseDetector mengimplementasikan antarmuka PoseDetector untuk model BlazePose Lite dengan beberapa perbedaan fundamental dibandingkan MoveNet yang memerlukan penanganan khusus.
Pra-pemrosesan citra. BlazePose memerlukan resolusi 256×256 piksel dengan normalisasi ke rentang [-1, 1] menggunakan NormalizeOp(127.5f, 127.5f). Inisialisasi buffer. Buffer output dialokasi sekali secara lazy pada frame pertama dan digunakan ulang pada setiap frame berikutnya (cachedOutputBuffer). Pendekatan ini menghindari pemanggilan ByteBuffer.allocateDirect() berulang yang menekan garbage collector dan menyebabkan stuttering pada tampilan.
Jika model memiliki tensor keluaran kedua (global presence), inferensi dijalankan menggunakan runForMultipleInputsOutputs(). Nilai logit pada tensor kedua ditransformasi menggunakan fungsi sigmoid dan jika hasilnya di bawah 0,5, fungsi mengembalikan null tanpa mem-parsing seluruh keypoints — menghemat komputasi pasca-pemrosesan saat tidak ada orang di frame.
Auto-deteksi rentang koordinat. Model BlazePose dapat mengeluarkan koordinat dalam rentang piksel [0, 256] atau ternormalisasi [0, 1] tergantung varian model. Sistem mendeteksi rentang ini secara otomatis pada 10 frame pertama dengan mengambil sampel tujuh keypoints kunci (hidung, bahu, pinggul, lutut) dan menentukan konsensus berdasarkan mayoritas nilai. Hasil deteksi disimpan sebagai coordsDivisor yang digunakan untuk normalisasi koordinat.
Transformasi sigmoid pada visibility dan presence. BlazePose mengeluarkan nilai visibility dan presence sebagai logit (nilai kontinu tanpa batas), bukan probabilitas. Transformasi sigmoid σ(x) = 1 / (1 + e⁻ˣ) diterapkan untuk mengonversi keduanya ke rentang [0, 1]. Skor efektif per-keypoint dihitung sebagai nilai minimum antara visibility dan presence untuk menekan false positive.
Pemetaan 33 ke 17 keypoints. BlazePose menghasilkan 33 keypoints (mencakup wajah detail, jari, dan kaki) sedangkan penelitian ini menggunakan 17 format COCO untuk konsistensi perbandingan dengan MoveNet. Pemetaan dilakukan menggunakan tabel konversi statis (COCO_TO_BLAZEPOSE) yang disajikan pada Tabel 5.2.
Tabel 5.2 Pemetaan Indeks Keypoints BlazePose (33) ke Format COCO (17)
Indeks COCO	Label	Indeks BlazePose	Label BlazePose
0	nose	0	nose
1	left_eye	2	left_eye
2	right_eye	5	right_eye
3	left_ear	7	left_ear
4	right_ear	8	right_ear
5	left_shoulder	11	left_shoulder
6	right_shoulder	12	right_shoulder
7	left_elbow	13	left_elbow
8	right_elbow	14	right_elbow
9	left_wrist	15	left_wrist
10	right_wrist	16	right_wrist
11	left_hip	23	left_hip
12	right_hip	24	right_hip
13	left_knee	25	left_knee
14	right_knee	26	right_knee
15	left_ankle	27	left_ankle
16	right_ankle	28	right_ankle

EMA smoothing pada BlazePoseDetector menggunakan parameter yang identik dengan MoveNetDetector (α = 0,4 normal, α = 0,1 low-confidence, deadband 0,008). Keseragaman ini penting untuk menjamin fairness perbandingan antar-model dalam analisis statistik.

Pra-pemrosesan Citra dengan Letterbox Padding
Konversi citra kamera ke format masukan model menggunakan teknik letterbox padding yang diimplementasikan dalam kelas PoseImageAnalyzer. Teknik ini dipilih karena mengatasi masalah distorsi koordinat yang terjadi ketika citra kamera (rasio 4:3 atau 16:9) diregangkan langsung ke dimensi persegi model (192×192 atau 256×256 piksel).
// Hitung skala seragam agar seluruh citra muat dalam dimensi target
val scale = min(targetW / srcW, targetH / srcH)
val padX  = (targetW - srcW * scale) / 2f
val padY  = (targetH - srcH * scale) / 2f

// Gambar bitmap ke kanvas hitam (letterbox)
val letterboxed = Bitmap.createBitmap(targetW, targetH, ARGB_8888)
Canvas(letterboxed).apply {
    drawColor(Color.BLACK)
    drawBitmap(Bitmap.createScaledBitmap(source, scaledW, scaledH, true), padX, padY, null)
}
// Koreksi balik koordinat keypoint dari ruang letterbox ke ruang kamera asli
fun correctLetterboxCoordinates(person: Person, lb: LetterboxResult): Person {
    return Person(person.keypoints.map { kp ->
        val pixelX = kp.x * lb.targetW
        val pixelY = kp.y * lb.targetH
        val relX   = (pixelX - lb.padX) / (lb.srcW * lb.scale)
        val relY   = (pixelY - lb.padY) / (lb.srcH * lb.scale)
        kp.copy(x = relX.coerceIn(0f, 1f), y = relY.coerceIn(0f, 1f))
    }, person.score)
}

Metadata transformasi (faktor skala, offset padX dan padY, dimensi sumber dan target) disimpan dalam data kelas LetterboxResult dan digunakan oleh fungsi correctLetterboxCoordinates() untuk menginversi transformasi pada koordinat keypoints hasil inferensi. Konversi YUV dari CameraX menggunakan metode bawaan toBitmap() yang memanfaatkan libyuv secara internal — lossless dan lebih efisien 5–15 ms per frame dibandingkan jalur YUV→NV21→JPEG→Bitmap.
Implementasi Algoritma Perhitungan Repetisi
Algoritma penghitungan repetisi diimplementasikan menggunakan pendekatan state machine berbasis sudut sendi. Terdapat tiga komponen: kalkulator sudut, detektor squat, dan detektor push-up. Ketiganya mengimplementasikan antarmuka ExerciseDetector yang mendefinisikan kontrak analyzeFrame(), getRepetitionCount(), getCurrentAngle(), dan reset().
Kalkulator Sudut
Objek AngleCalculator menyediakan fungsi penghitungan sudut antara tiga titik keypoint menggunakan formula dot product. Sudut dihitung di titik tengah (vertex) melalui lima langkah: (1) membentuk dua vektor dari titik tengah ke titik pertama dan terakhir, (2) menghitung dot product, (3) menghitung magnitudo masing-masing vektor, (4) menerapkan invers kosinus (acos), dan (5) mengonversi dari radian ke derajat.
fun calculateAngle(first: Keypoint, middle: Keypoint, last: Keypoint): Double {
    val vA = Pair(first.x - middle.x,  first.y - middle.y)
    val vB = Pair(last.x  - middle.x,  last.y  - middle.y)
    val dot       = vA.first * vB.first + vA.second * vB.second
    val magA      = sqrt((vA.first.pow(2) + vA.second.pow(2)).toDouble())
    val magB      = sqrt((vB.first.pow(2) + vB.second.pow(2)).toDouble())
    val cosAngle  = (dot / (magA * magB)).coerceIn(-1.0, 1.0)
    return Math.toDegrees(acos(cosAngle))
}

Fungsi turunan tersedia untuk menghitung sudut lutut (menggunakan keypoints hip-knee-ankle) dan sudut siku (shoulder-elbow-wrist) pada kedua sisi tubuh. Fungsi getAverageKneeAngle() dan getAverageElbowAngle() mengembalikan rata-rata kedua sisi apabila keduanya terdeteksi, atau nilai sisi tunggal apabila hanya satu sisi yang memiliki skor kepercayaan di atas 0,2. Keypoints yang menghasilkan sudut di bawah 30° (lutut) atau 20° (siku) dibuang karena mengindikasikan posisi keypoints yang terlalu berdekatan atau bermasalah secara anatomis.
Detektor Squat
Kelas SquatDetector mengimplementasikan state machine empat-state dengan parameter sudut lutut sebagai input. Sudut mentah melalui moving average smoothing dengan ukuran jendela tiga frame sebelum dievaluasi untuk transisi state. Tabel 5.3 menyajikan ambang batas dan kondisi transisi antar-state.
Tabel 5.3 Ambang Batas dan Transisi State Machine Squat
State Asal	Kondisi Transisi	State Tujuan	Keterangan
STANDING	θ < 145°	GOING_DOWN	Mulai turun (hysteresis exit)
GOING_DOWN	θ ≤ 110°	SQUATTING	Mencapai kedalaman squat
GOING_DOWN	θ > 160° (3 frame)	STANDING	Abort — batal turun
SQUATTING	θ > 120° & hold ≥ 200ms	GOING_UP	Mulai naik
GOING_UP	θ ≥ 160°	STANDING	+1 Rep tercatat
GOING_UP	θ < 110°	SQUATTING	Turun kembali

Tiga mekanisme pengamanan diimplementasikan: (1) Hysteresis threshold — setiap transisi menggunakan dua ambang batas berbeda (masuk dan keluar) untuk mencegah osilasi state akibat fluktuasi kecil sudut; (2) Abort confirmation — transisi pembatalan ke STANDING memerlukan 3 frame berturut-turut di atas 160° untuk mencegah satu frame noise mereset state machine; (3) Temporal constraint — durasi minimum 200 ms pada state SQUATTING untuk menghindari deteksi repetisi palsu akibat gerakan terlalu cepat.
Detektor Push-Up
Kelas PushUpDetector memiliki arsitektur serupa dengan SquatDetector tetapi menggunakan sudut siku sebagai parameter utama. State machine terdiri dari empat state: UP (lengan lurus, θ ≥ 140°), GOING_DOWN, DOWN (fase bawah, θ ≤ 115°), dan GOING_UP. Ambang batas ANGLE_UP_ENTER ditetapkan pada 140° (bukan 155° seperti rancangan awal) berdasarkan hasil observasi eksperimen: akibat efek foreshortening kamera depan dan lag EMA, gerakan push-up alami dari kamera depan sering membaca sudut siku maksimum hanya di 140–150°, sehingga ambang 155° menyebabkan banyak repetisi yang sebenarnya valid tidak terhitung.
Ambang batas DOWN ditetapkan pada 115° (lebih toleran dari standar biomekanika 90°) karena akurasi deteksi sudut siku dari kamera umumnya lebih rendah dibandingkan sudut lutut, terutama saat posisi horizontal. Mekanisme abort confirmation (3 frame berturut-turut) dan durasi minimum 150 ms pada state DOWN juga diterapkan untuk menghindari deteksi ganda (double-counting).
Implementasi Logging & Profilling
Pengukuran Latensi Inferensi
Pengukuran latensi dilakukan di dalam kelas PoseImageAnalyzer yang mengimplementasikan ImageAnalysis.Analyzer dari CameraX. Penanda waktu diambil menggunakan System.nanoTime() tepat sebelum dan sesudah pemanggilan poseDetector.detectPose(), kemudian selisihnya dibagi 1.000.000 untuk menghasilkan latensi dalam milidetik. Metode ini mengukur latensi inferensi murni tanpa menyertakan waktu pra-pemrosesan (konversi YUV, rotasi, letterbox) dan pasca-pemrosesan (koreksi koordinat).
Selain latensi inferensi murni, end-to-end processing time (mencakup seluruh tahap dari penerimaan ImageProxy hingga pengiriman hasil ke ViewModel) juga diukur dan disimpan dalam kolom processing_time_ms di CSV. Kedua nilai ini dikirim ke ViewModel sebagai dua parameter terpisah untuk memungkinkan analisis overhead pra/pasca-pemrosesan.
Protokol warm-up sebanyak 5 frame pertama diterapkan melalui penghitung totalFrameCount. Selama fase ini, nilai latensi dikirim sebagai 0 dan tidak dicatat oleh BenchmarkLogger. FPS dihitung menggunakan dua metode: rolling average per satu detik sebagai nilai utama, dan instantaneous FPS (1000 ms / delta antar-frame) sebagai nilai awal pada detik pertama sebelum data rolling tersedia.

Profilling Sumber Daya
Kelas ResourceProfiler mengukur tiga metrik sumber daya dengan interval polling 100 ms yang dilaksanakan pada thread yang sama dengan inferensi untuk meminimalkan latensi pembacaan.
Utilisasi CPU diukur melalui pembacaan file sistem /proc/stat (total waktu CPU) dan /proc/[pid]/stat (waktu CPU proses aplikasi). Persentase dihitung dari delta waktu CPU aplikasi terhadap delta total waktu CPU, dikalikan jumlah inti prosesor. Mekanisme fallback menggunakan Process.getElapsedCpuTime() diterapkan untuk perangkat Android 8.0+ yang membatasi akses ke /proc/stat.
Penggunaan memori diukur menggunakan Debug.MemoryInfo API yang menyediakan nilai Proportional Set Size (PSS) dalam kilobyte, kemudian dikonversi ke megabyte. PSS dipilih sebagai metrik karena merepresentasikan penggunaan memori aktual aplikasi dengan memperhitungkan pembagian memori antar-proses secara proporsional, berbeda dengan RSS (Resident Set Size) yang menghitung seluruh memori yang dipetakan tanpa mempertimbangkan pembagian.
Konsumsi daya diestimasi menggunakan BatteryManager API dengan formula P(mW) = V(mV) × I / faktor, di mana faktor bergantung pada unit arus yang dikembalikan perangkat. Heuristik diterapkan: jika nilai arus absolut < 50.000, dianggap dalam satuan miliampere (mA) sehingga faktor 1.000; jika ≥ 50.000, dianggap dalam mikroampere (µA) sehingga faktor 1.000.000. Nilai ini dicatat sebagai metrik deskriptif dan tidak dimasukkan ke dalam analisis ANOVA karena keterbatasan granularitas sensor baterai.
Pencatatan & Ekspor CSV
Kelas BenchmarkLogger mengelola pencatatan metrik secara thread-safe menggunakan tiga primitif atomik: AtomicBoolean untuk flag isLogging, AtomicInteger untuk penghitung frame, dan AtomicLong untuk penanda waktu mulai/selesai. Data disimpan dalam  ConcurrentLinkedQueue<BenchmarkMetrics> yang aman diakses secara bersamaan dari camera thread (logMetrics) dan UI thread (start/stop/export) tanpa risiko race condition.
Label sesi dibuat secara otomatis dalam format [Model]_[Delegate]_[Latihan] (contoh: MoveNet_GPU_SQUAT). Apabila terjadi fallback delegate GPU ke XNNPACK, label menyertakan informasi ini: MoveNet_GPU_AS_XNNPACK_SQUAT, sehingga setiap baris data dalam CSV dapat diidentifikasi kondisi eksperimen aktualnya tanpa ambiguitas.
Ekspor data menggunakan MediaStore API (Android 10+) atau akses penyimpanan eksternal langsung untuk versi sebelumnya. Statistik ringkasan sesi (BenchmarkSummary) dihitung saat logging dihentikan, mencakup rata-rata, simpangan baku, minimum, dan maksimum latensi inferensi, rata-rata FPS/CPU/memori/daya, serta detection rate (persentase frame dengan pose terdeteksi).
Integrasi Komponen Melalui ViewModel
Seluruh komponen yang telah diuraikan di atas diintegrasikan melalui kelas PoseViewModel yang mengikuti pola arsitektur MVVM. ViewModel bertindak sebagai orkestrator yang mengoordinasikan aliran data antara lapisan presentasi, domain, dan data, serta menjaga state aplikasi melalui PoseUiState yang didistribusikan menggunakan StateFlow.
PoseUiState merangkum seluruh informasi yang diperlukan oleh antarmuka: model dan delegate terpilih (termasuk effectiveDelegate yang mencatat nilai aktual setelah kemungkinan fallback), hasil deteksi pose, metrik performa, data latihan, status logging, status warm-up, pesan kesalahan, dan jalur file ekspor terakhir.
Setiap perubahan konfigurasi (model, delegate, atau jenis latihan) memicu siklus reinisialisasi pada background thread (Dispatchers.IO): melepas interpreter lama, membuat TFLiteHelper baru, menginisialisasi interpreter, mereset fase warm-up, dan memperbarui PoseUiState dengan hasil inisialisasi termasuk nilai effectiveDelegateType.
Fungsi updateResults() dipanggil oleh PoseImageAnalyzer setiap frame untuk memperbarui state UI. Fungsi ini menjalankan tiga operasi berurutan: (1) memperbarui metrik performa, (2) menjalankan ExerciseDetector jika latihan dipilih, dan (3) mencatat data ke BenchmarkLogger apabila sesi logging aktif dan fase warm-up telah selesai. Data yang dicatat menyertakan nilai effectiveDelegateType dari state saat itu untuk memastikan setiap baris CSV mencerminkan kondisi aktual eksperimen.


PENGUJIAN DAN PEMBAHASAN
Persiapan Pengujian
Statistik Deskriptif
Validasi Fungsional Penghitungan Repetisi
Uji Asumsi ANOVA
Analisis Varians (Three-Way ANOVA)
Latensi Inferensi
Utilisasi CPU
Penggunaan Memori
Ringkasan Signifikansi
Uji Post-hoc Tukey HSD
Analisis Trade-off Deskriptif
Kriteria Kelayakan Real-time
Profil Trade-off Kualitatif
Rekomendasi Per Skenario
Pembalhasan
Pengaruh Delegate Terhadap Kinerja 
Perbandingan Model
Pengaruh Jenis Latihan
Konfigurasi Optimal
Penutup
Kesimpulan 
Saran
DAFTAR REFERENSI
Blog, T., 2021. Pose estimation and classification on edge devices with MoveNet and TensorFlow Lite — The TensorFlow Blog. [online] Available at: <https://blog.tensorflow.org/2021/08/pose-estimation-and-classification-on-edge-devices-with-MoveNet-and-TensorFlow-Lite.html> [Accessed 5 March 2026].
Chen, S. and Yang, R.R., 2020. Pose Trainer: Correcting Exercise Posture using Pose Estimation. [online] Available at: <http://arxiv.org/abs/2006.11718>.
DetikInet, 2023. Pasar Smartphone Indonesia Turun 11,9% di Kuartal I 2023. [online] Available at: <https://inet.detik.com/business/d-6751251/pasar-smartphone-indonesia-turun-11-9-di-kuartal-i-2023> [Accessed 5 March 2026].
Essuming, K., 2024. A Mobile-Phone Pose Estimation for Gym-Exercise Form Correction. Proceedings, Joint Conference on Computer Vision, Imaging and Computer Graphics Theory and Applications., [online] pp.559–566. Available at: <https://eprints.whiterose.ac.uk/210366/>.
Ignatov, A., Timofte, R., Chou, W., Ke Wang, M.W., Hartley, T. and Gool, L. Van, 2018. AI Benchmark: Running Deep Neural Networks on Android Smartphones. Proceedings of the European Conference on Computer Vision (ECCV), 11133, pp.288–314.
Jiang, S., Ran, L., Cao, T., Xu, Y. and Liu, Y., 2020. Profiling and optimizing deep learning inference on mobile GPUs. APSys 2020 - Proceedings of the 2020 ACM SIGOPS Asia-Pacific Workshop on Systems, pp.75–81. https://doi.org/10.1145/3409963.3410493.
Katadata, 2023. Laporan IDC: Pasar Smartphone Indonesia Merosot 11.9% Kuartal I 2023 - Gadget Katadata.co.id. [online] Available at: <https://katadata.co.id/digital/gadget/647d9f4c3ed6f/laporan-idc-pasar-smartphone-indonesia-merosot-119-kuartal-i-2023> [Accessed 5 March 2026].
Lee, J., Chirkov, N., Ignasheva, E., Pisarchyk, Y., Shieh, M., Riccardi, F., Sarokin, R., Kulik, A. and Grundmann, M., 2019. On-Device Neural Net Inference with Mobile GPUs. [online] Available at: <http://arxiv.org/abs/1907.01989>.
Lite, T., 2024. examples/lite/examples/pose_estimation at master · tensorflow/examples. [online] Available at: <https://github.com/tensorflow/examples/tree/master/lite/examples/pose_estimation> [Accessed 5 March 2026].
Selular.ID, 2023. IDC: Top 5 Brand Smartphone di Indonesia Q1-2023, Oppo Bukan #1 | Selular.ID. [online] Available at: <https://selular.id/2023/05/idc-top-5-brand-smartphone-di-indonesia-q1-2023-oppo-bukan-1/> [Accessed 5 March 2026].
Tharatipyakul, A., Srikaewsiew, T. and Pongnumkul, S., 2024. Deep learning-based human body pose estimation in providing feedback for physical movement: A review. Heliyon, [online] 10(17), p.e36589. https://doi.org/10.1016/j.heliyon.2024.e36589.
Valentin Bazarevsky and Ivan Grishchenko, 2020. On-device, Real-time Body Pose Tracking with MediaPipe BlazePose. [online] Available at: https://research.google/blog/on-device-real-time-body-pose-tracking-with-mediapipe-blazepose/> [Accessed 6 March 2026].


 

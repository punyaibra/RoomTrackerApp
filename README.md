Aplikasi Manajemen Pemesanan Ruangan Kampus

1. Deskripsi Aplikasi

Room Tracker adalah aplikasi mobile berbasis Android yang dikembangkan untuk mempermudah proses pemesanan dan pengelolaan ruangan kampus.
Aplikasi ini menerapkan Role-Based Access Control (RBAC) sehingga setiap pengguna mendapatkan fitur dan tampilan sesuai perannya, yaitu Admin, Mahasiswa, dan Petugas.

Aplikasi dirancang dengan konsep offline-first, menggunakan SQLite sebagai database lokal, sehingga dapat digunakan tanpa koneksi internet. Antarmuka dibangun dengan Material Design 3 untuk pengalaman pengguna yang modern dan konsisten.

---

2. Fitur Unggulan

Fitur untuk Mahasiswa

a. Dashboard Beranda Interaktif

* Slider horizontal rekomendasi ruangan terbaru
* Akses cepat ke ruangan populer
* Tampilan visual menarik dengan header gambar gedung

b. Sistem Pemesanan Ruangan

* Form booking lengkap (tanggal & waktu menggunakan date/time picker)
* Input alasan pemesanan
* Tracking status booking:

  * Pending
  * Approved
  * Rejected

c. Kalender Jadwal

* Kalender bulanan interaktif
* Highlight tanggal hari ini
* Detail booking per tanggal
* Navigasi cepat dengan month & year picker

d. Explore & Search Ruangan

* Katalog ruangan dengan fitur filter
* Pencarian berdasarkan:

  * Nama ruangan
  * Lokasi
  * Kapasitas
* Detail ruangan lengkap dengan foto

e. Profil & Riwayat

* Edit data pribadi (Fakultas, Prodi, Angkatan)
* Upload foto KTM
* Riwayat booking beserta statusnya

---

Fitur untuk Admin

a. Manajemen Ruangan (CRUD)

* Tambah, edit, dan hapus data ruangan
* Upload foto ruangan
* Pengaturan status ruangan (**ACTIVE / INACTIVE**)
* Tabel data dengan pagination

b. Manajemen Pengguna

* Verifikasi mahasiswa baru
* Edit role pengguna (Mahasiswa → Petugas/Admin)
* Validasi KTM mahasiswa
* Kelola seluruh akun pengguna

c. Approval Pemesanan

* Review seluruh permintaan booking
* Approve atau reject dengan alasan
* Filter berdasarkan status booking

d. Log Aktivitas

* Monitoring seluruh aktivitas sistem
* Pencatatan perubahan data
* Audit trail untuk keamanan dan evaluasi

---

Fitur untuk Petugas

a. Kelola Pemesanan

* Approve atau reject pemesanan ruangan
* Melihat detail jadwal ruangan

b. Monitoring

* Kalender jadwal booking
* Daftar ruangan yang tersedia dan terpakai

---

3. Keunggulan Teknis

a. Arsitektur & Desain

* **MVC Pattern**: Pemisahan Model, View, dan Controller
* **Role-Based UI**: Tampilan menyesuaikan hak akses pengguna
* **Offline-First System**: Data tersimpan secara lokal

b. User Experience

* Material Design 3 untuk tampilan modern
* Layout responsif untuk berbagai ukuran layar
* Navigasi halus dengan Bottom Navigation
* Elemen interaktif (date/time picker, kalender custom, search real-time)

c. Data Management

* Database SQLite dengan 3 tabel utama
* Penyimpanan gambar di internal storage
* Session management menggunakan SharedPreferences
* Validasi input dan error handling yang komprehensif

d. Security & Permission

* Penyimpanan password dengan hashing (basic encryption)
* Auto logout saat aplikasi ditutup
* Runtime permission untuk akses storage
* Validasi role untuk setiap aksi pengguna

---

4. Teknologi yang Digunakan

| Komponen           | Detail                           |
| ------------------ | -------------------------------- |
| Bahasa Pemrograman | Java (Native Android)            |
| Database           | SQLite 3                         |
| UI Framework       | XML Layout + Material Components |
| Minimum SDK        | API 24 (Android 7.0 Nougat)      |
| Target SDK         | API 34 (Android 14)              |
| Build System       | Gradle                           |

Dependencies Utama:

* AndroidX AppCompat & ConstraintLayout
* Material Design Components
* RecyclerView & CardView
* CoordinatorLayout & NestedScrollView

---

5. Skenario Penggunaan

Flow Mahasiswa

1. Login menggunakan email kampus
2. Melihat dashboard dan rekomendasi ruangan
3. Memilih ruangan dan melihat detail
4. Melakukan booking (tanggal, jam, alasan)
5. Menunggu approval
6. Mengecek status booking dan kalender jadwal

Flow Admin

1. Login sebagai admin
2. Mengelola data ruangan
3. Verifikasi mahasiswa baru
4. Review dan approval booking
5. Monitoring aktivitas melalui log sistem

---

6. Target Pengguna

Aplikasi ini ditujukan untuk:

* Mahasiswa – booking ruangan kuliah, organisasi, atau study group
* Admin Kampus – mengelola ruangan dan pengguna
* Petugas/Staff – memproses pemesanan harian

---

7. Pengembangan Masa Depan (Roadmap)

Fitur yang dapat dikembangkan selanjutnya:

* Push notification status booking
* Export laporan ke PDF / Excel
* Integrasi Google Calendar
* QR Code untuk check-in ruangan
* Dashboard analytics untuk admin
* Multi-language (ID / EN)
* Dark mode

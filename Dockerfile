# Gunakan image base yang sesuai
FROM openjdk:17

# Buat direktori aplikasi
RUN mkdir /app

# Pindahkan semua file aplikasi ke dalam direktori /app di dalam container
COPY . /app

# Set direktori kerja saat container dijalankan
WORKDIR /app

# Compile aplikasi Ktor (pastikan Anda sudah memiliki kode yang dikompilasi sebelumnya)
#RUN ./gradlew build
+RUNx /app/gradlew && /app/gradlew build

# Port yang akan digunakan oleh aplikasi Ktor
EXPOSE 8080

# Jalankan aplikasi Ktor saat container dijalankan
CMD ["java", "-jar", "build/libs/id.bts.leave-app-0.0.1.jar"]
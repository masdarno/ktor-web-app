#!/bin/bash

# Exit immediately jika ada perintah yang gagal
set -e

# Tentukan direktori lokasi script ini berada (folder database/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# File kredensial berada di root proyek (naik 1 tingkat dari folder database/)
ENV_FILE="$SCRIPT_DIR/../.env"

# Cek apakah file .env ada
if [ ! -f "$ENV_FILE" ]; then
    echo "[ERROR] File .env tidak ditemukan di root proyek: $ENV_FILE"
    exit 1
fi

# Load variabel lingkungan dari file .env
# shellcheck disable=SC1090
source "$ENV_FILE"

# Validasi variabel yang dibutuhkan
if [ -z "$DB_USER" ] || [ -z "$DB_PASSWORD" ]; then
    echo "[ERROR] Variabel DB_USER atau DB_PASSWORD tidak ditemukan di file .env."
    exit 1
fi

# Set default nilai jika tidak ada di .env
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"

# Pastikan password dibersihkan dari memori saat script selesai/error
trap 'unset MYSQL_PWD' EXIT

# Daftar file SQL yang akan dieksekusi secara berurutan
SQL_FILES=(
    "create_database.sql"
    "create_table.sql"
    "insert_data_dasar.sql"
)

# Validasi keberadaan semua file SQL sebelum eksekusi
echo "[INFO] Memeriksa keberadaan file SQL di '$SCRIPT_DIR'..."
for file in "${SQL_FILES[@]}"; do
    if [ ! -f "$SCRIPT_DIR/$file" ]; then
        echo "[ERROR] File SQL '$file' tidak ditemukan di $SCRIPT_DIR"
        exit 1
    fi
done

# Eksekusi file SQL satu per satu secara berurutan
export MYSQL_PWD="$DB_PASSWORD"

echo "[INFO] Mulai mengeksekusi file SQL pada host: $DB_HOST:$DB_PORT..."

for file in "${SQL_FILES[@]}"; do
    echo "----------------------------------------"
    echo "[INFO] Mengeksekusi: $file"
    
    if mariadb -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" < "$SCRIPT_DIR/$file"; then
        echo "[SUCCESS] Berhasil mengeksekusi: $file"
    else
        echo "[ERROR] Gagal mengeksekusi: $file"
        exit 1
    fi
done

echo "========================================"
echo "[SUCCESS] Semua file SQL berhasil dieksekusi secara berurutan!"

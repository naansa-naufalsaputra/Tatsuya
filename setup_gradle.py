import os
import urllib.request
import zipfile
import subprocess
import shutil
import sys

# Konfigurasi Versi
GRADLE_VERSION = "8.13"
GRADLE_URL = f"https://services.gradle.org/distributions/gradle-{GRADLE_VERSION}-bin.zip"
ZIP_NAME = f"gradle-{GRADLE_VERSION}-bin.zip"
EXTRACT_DIR = "gradle_temp"

def download_gradle():
    print(f"Downloading {GRADLE_URL}...")
    try:
        urllib.request.urlretrieve(GRADLE_URL, ZIP_NAME)
        print("Download complete.")
    except Exception as e:
        print(f"Gagal download: {e}")
        sys.exit(1)

def extract_gradle():
    print("Extracting zip...")
    try:
        with zipfile.ZipFile(ZIP_NAME, 'r') as zip_ref:
            zip_ref.extractall(EXTRACT_DIR)
        print("Extraction complete.")
    except Exception as e:
        print(f"Gagal extract: {e}")
        sys.exit(1)

def run_wrapper():
    # Cari executable gradle
    gradle_bin_dir = os.path.join(EXTRACT_DIR, f"gradle-{GRADLE_VERSION}", "bin")
    gradle_bat = os.path.join(gradle_bin_dir, "gradle.bat")
    gradle_sh = os.path.join(gradle_bin_dir, "gradle")
    
    cmd = []
    if sys.platform == "win32":
        if not os.path.exists(gradle_bat):
            print(f"Error: {gradle_bat} tidak ditemukan.")
            return
        cmd = [gradle_bat, "wrapper"]
    else:
        if not os.path.exists(gradle_sh):
             print(f"Error: {gradle_sh} tidak ditemukan.")
             return
        # Pastikan executable
        os.chmod(gradle_sh, 0o755)
        cmd = [gradle_sh, "wrapper"]

    print(f"Running command: {' '.join(cmd)}")
    try:
        # Set JAVA_HOME jika perlu, tapi asumsi user sudah punya java di PATH
        subprocess.check_call(cmd)
        print("\nSUKSES: File 'gradlew' dan 'gradlew.bat' berhasil digenerate!")
    except subprocess.CalledProcessError as e:
        print(f"\nERROR: Gagal menjalankan gradle wrapper. Pastikan Java (JDK) terinstall dan ada di PATH.\nDetail: {e}")

def cleanup():
    print("Cleaning up temporary files...")
    if os.path.exists(ZIP_NAME):
        try:
            os.remove(ZIP_NAME)
        except:
            pass
    if os.path.exists(EXTRACT_DIR):
        try:
            shutil.rmtree(EXTRACT_DIR)
        except:
            pass
    print("Cleanup complete.")

if __name__ == "__main__":
    current_dir = os.getcwd()
    print(f"Working directory: {current_dir}")
    
    try:
        # Cek apakah JDK ada
        try:
            subprocess.check_call(["java", "-version"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        except:
            print("WARNING: Java (JDK) mungkin tidak terdeteksi di PATH. Gradle butuh Java untuk berjalan.")
            
        download_gradle()
        extract_gradle()
        run_wrapper()
    except KeyboardInterrupt:
        print("\nProcess interrupted.")
    finally:
        cleanup()

import zipfile
import os

# Define the zip file path
zip_file = 'bdtask_android.zip'
extract_to = '.'  # Extract to current directory

# Unzip the file
if os.path.exists(zip_file):
    with zipfile.ZipFile(zip_file, 'r') as zip_ref:
        zip_ref.extractall(extract_to)
    print(f"✓ Successfully extracted {zip_file}")
else:
    print(f"✗ {zip_file} not found")

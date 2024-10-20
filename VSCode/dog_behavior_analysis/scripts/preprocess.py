import gdown
import zipfile
import os
import numpy as np
from sklearn.model_selection import train_test_split
from tensorflow.keras.preprocessing.image import load_img, img_to_array

def download_file(file_id, output):
    url = f'https://drive.google.com/uc?id={file_id}'
    print(f"Downloading {output}...")
    gdown.download(url, output, quiet=False)
    print(f"{output} downloaded.")

def extract_zip(file_name, extract_to):
    if os.path.exists(file_name):
        print(f"Extracting {file_name}...")
        with zipfile.ZipFile(file_name, 'r') as zip_ref:
            zip_ref.extractall(extract_to)
        print(f"{file_name} extracted to {extract_to}.")
    else:
        print(f"{file_name} not found.")

# Google Drive 에서 가져온 파일 ID
sit_file_id = '1QFqTjMe4Ti_WcY3Ez8q6laK1aQjJlNIE'  # sit.zip 파일 ID
other_file_id = '1-yr0u0e9xHFV6_tjWsi0yYlceyb8l3pa'  # other.zip 파일 ID

# 데이터 폴더 생성 
os.makedirs('dog_behavior_analysis/data', exist_ok=True)

# 파일 다운로드 
download_file(sit_file_id, 'dog_behavior_analysis/data/sit.zip')
download_file(other_file_id, 'dog_behavior_analysis/data/other.zip')

# 압축 해제 경로 설정
os.makedirs('dog_behavior_analysis/data/sit', exist_ok=True)
os.makedirs('dog_behavior_analysis/data/other', exist_ok=True)

# 압축 해제
extract_zip('dog_behavior_analysis/data/sit.zip', 'dog_behavior_analysis/data/sit')
extract_zip('dog_behavior_analysis/data/other.zip', 'dog_behavior_analysis/data/other')

# 데이터 경로 설정
sit_path = 'dog_behavior_analysis/data/sit'
other_path = 'dog_behavior_analysis/data/other'

# 이미지 데이터와 레이블 저장할 리스트 초기화
images = []
labels = []

# 앉아있는 행동 이미지 불러오기
for img_name in os.listdir(sit_path):
    img_path = os.path.join(sit_path, img_name)
    if img_name.endswith(('.png', '.jpg', '.jpeg')):
        image = load_img(img_path, target_size=(224, 224))
        image = img_to_array(image)
        images.append(image)
        labels.append(1)  # "앉기" 행동을 1로 레이블링

# 기타 행동 이미지 불러오기
for img_name in os.listdir(other_path):
    img_path = os.path.join(other_path, img_name)
    if img_name.endswith(('.png', '.jpg', '.jpeg')):
        image = load_img(img_path, target_size=(224, 224))
        image = img_to_array(image)
        images.append(image)
        labels.append(0)  # "기타 행동"을 0으로 레이블링

# 리스트를 넘파이 배열로 변환
images = np.array(images, dtype="float") / 255.0  # 픽셀 값을 0-1 사이로 정규화
labels = np.array(labels)

# 훈련용과 검증용 데이터셋으로 나누기
train_images, val_images, train_labels, val_labels = train_test_split(images, labels, test_size=0.2, random_state=42)

# 훈련 및 검증 데이터를 외부에서 접근 가능하도록 하는 함수 정의
def get_data():
    global train_images, val_images, train_labels, val_labels
    return train_images, val_images, train_labels, val_labels

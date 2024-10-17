import os
import numpy as np
from sklearn.model_selection import train_test_split
from tensorflow.keras.preprocessing.image import load_img, img_to_array

# 데이터 경로 설정
sit_path = r'C:/dev/python/dog_behavior_analysis/data/sit'
other_path = r'C:/dev/python/dog_behavior_analysis/data/other'


# 이미지 데이터와 레이블 저장할 리스트 초기화
images = []
labels = []

# 앉아있는 행동 이미지 불러오기
for img_name in os.listdir(sit_path):
    img_path = os.path.join(sit_path, img_name)
    image = load_img(img_path, target_size=(224, 224))
    image = img_to_array(image)
    images.append(image)
    labels.append(1)  # "앉기" 행동을 1로 레이블링

# 기타 행동 이미지 불러오기
for img_name in os.listdir(other_path):
    img_path = os.path.join(other_path, img_name)
    image = load_img(img_path, target_size=(224, 224))
    image = img_to_array(image)
    images.append(image)
    labels.append(0)  # "기타 행동"을 0으로 레이블링

# 리스트를 넘파이 배열로 변환
images = np.array(images, dtype="float") / 255.0  # 픽셀 값을 0-1 사이로 정규화
labels = np.array(labels)

# 훈련용과 검증용 데이터셋으로 나누기
(train_images, val_images, train_labels, val_labels) = train_test_split(images, labels, test_size=0.2, random_state=42)

# 데이터를 저장하거나 다음 단계에서 바로 사용할 수 있도록 함
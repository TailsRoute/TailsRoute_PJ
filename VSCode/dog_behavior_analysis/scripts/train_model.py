import tensorflow as tf
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras import layers, models
from tensorflow.keras.optimizers import Adam
import os

# 모델 로드
base_model = MobileNetV2(weights='imagenet', include_top=False, input_shape=(224, 224, 3))
base_model.trainable = False  # 사전 학습된 가중치 고정

# 모델 정의
model = models.Sequential([
    base_model,
    layers.GlobalAveragePooling2D(),
    layers.Dense(128, activation='relu'),
    layers.Dropout(0.5),
    layers.Dense(1, activation='sigmoid')  # 이진 분류 (앉기 vs 기타 행동)
])

# 모델 컴파일
model.compile(optimizer=Adam(learning_rate=1e-4), loss='binary_crossentropy', metrics=['accuracy'])

# 데이터 로드 (preprocess.py에서 생성된 데이터 사용)
from preprocess import train_images, val_images, train_labels, val_labels

# 모델 훈련
history = model.fit(
    train_images, train_labels,
    validation_data=(val_images, val_labels),
    epochs=10,
    batch_size=32
)

# 모델 저장 폴더 경로 설정 및 폴더 생성
save_dir = os.path.join(os.getcwd(), 'models')
if not os.path.exists(save_dir):
    os.makedirs(save_dir)

# 모델 저장
model_path = os.path.join(save_dir, 'dog_behavior_model.h5')
model.save(model_path)

print(f"모델 훈련 완료 및 저장 완료: {model_path}")
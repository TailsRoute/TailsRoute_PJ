import tensorflow as tf
from tensorflow.keras.applications import ResNet50
from tensorflow.keras import layers, models
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint
import os
import numpy as np


# 모델 로드
base_model = ResNet50(weights='imagenet', include_top=False, input_shape=(224, 224, 3))
base_model.trainable = True

# 첫 100개의 레이어는 고정하고 나머지는 학습 가능하게 설정
for layer in base_model.layers[:100]: 
    layer.trainable = False
    
train_datagen = ImageDataGenerator(
    rescale=1./255,
    rotation_range=30,
    width_shift_range=0.2,
    height_shift_range=0.2,
    shear_range=0.2,
    zoom_range=0.2,
    horizontal_flip=True,
    fill_mode='nearest'
)

val_datagen = ImageDataGenerator(rescale=1./255)

# 데이터 로드 (preprocess.py에서 생성된 데이터 사용)
from preprocess import get_data

train_images, val_images, train_labels, val_labels = get_data()

# 데이터 생성기 정의 
train_generator = train_datagen.flow(train_images, train_labels, batch_size=32)
val_generator = val_datagen.flow(val_images, val_labels, batch_size=32)

model = models.Sequential([
    base_model,
    layers.GlobalAveragePooling2D(),
    layers.Dense(216, activation='relu'),  # 기존 128에서 216로 확장
    layers.Dropout(0.5),
    layers.Dense(128, activation='relu'),
    layers.Dropout(0.3),
    layers.Dense(1, activation='sigmoid')  # 이진 분류
])

# 모델 컴파일
model.compile(optimizer=Adam(learning_rate=1e-5), loss='binary_crossentropy', metrics=['accuracy'])

# 모델 저장 폴더 경로 설정 및 폴더 생성
save_dir = os.path.join(os.getcwd(), 'models')
if not os.path.exists(save_dir):
    os.makedirs(save_dir)

model_path = os.path.join(save_dir, 'dog_behavior_model.keras')

early_stopping = EarlyStopping(monitor='val_loss', patience=10, restore_best_weights=True)
model_checkpoint = ModelCheckpoint(model_path, save_best_only=True, monitor='val_loss')


# 모델 훈련
history = model.fit(
    train_generator, 
    validation_data=val_generator,
    epochs=10,
    callbacks=[early_stopping, model_checkpoint]
)

# 모델 저장
model.save(model_path)

print(f"모델 훈련 완료 및 저장 완료: {model_path}")
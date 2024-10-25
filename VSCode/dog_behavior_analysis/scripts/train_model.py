import tensorflow as tf
from tensorflow.keras.applications import ResNet50
from tensorflow.keras import layers, models
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint
import cv2
import numpy as np
import os
from sklearn.utils.class_weight import compute_class_weight

# 데이터 로드 (preprocess.py에서 생성된 데이터 사용)
from preprocess import get_data
train_images, val_images, train_labels, val_labels = get_data()

print(f"훈련 이미지 개수: {len(train_images)}")
print(f"검증 이미지 개수: {len(val_images)}")

# 이미지 로드 및 전처리 함수
def load_and_preprocess_image(images):
    images = os.path.abspath(images)

    image = cv2.imread(images)
    if image is None:
        raise ValueError(f"이미지를 불러올 수 없습니다: {images}")
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY) # 흑백 변환
    image = cv2.resize(image, (224, 224))
    image = image / 255.0
    return image 


# 이미지 증강 함수 정의 (OpenCV 활용)
def augment_image(image):
    # 밝기 조절
    brightness = np.random.uniform(0.6, 1.4)
    image = cv2.convertScaleAbs(image, alpha=brightness)

    # 회전
    angle = np.random.uniform(-45, 45)
    (h, w) = image.shape[:2]
    M = cv2.getRotationMatrix2D((w / 2, h / 2), angle, 1.0)
    image = cv2.warpAffine(image, M, (w, h), flags=cv2.INTER_NEAREST)

    # 대비 조정
    contrast = np.random.uniform(0.8, 1.2)
    image = cv2.addWeighted(image, contrast, np.zeros_like(image), 0, 0)

    # 노이즈 추가
    noise = np.random.normal(0, 0.05, image.shape)
    image = np.clip(image + noise, 0, 1)

    # 이동
    tx = np.random.uniform(-0.2 * w, 0.2 * w)
    ty = np.random.uniform(-0.2 * h, 0.2 * h)
    M = np.float32([[1, 0, tx], [0, 1, ty]])
    image = cv2.warpAffine(image, M, (w, h), flags=cv2.INTER_NEAREST)

    # 확대/축소
    zoom = np.random.uniform(0.8, 1.2)
    image = cv2.resize(image, None, fx=zoom, fy=zoom)

    # 수평 뒤집기
    if np.random.rand() < 0.5:
        image = cv2.flip(image, 1)

    # 이미지 크기를 ResNet50 입력 크기에 맞게 조정
    image = cv2.resize(image, (224, 224))
    image = image / 255.0

    return image

# 데이터셋 생성기 정의
def data_generator(images, labels, batch_size):
    while True:
        for i in range(0, len(images), batch_size):
            batch_images = []
            batch_labels = labels[i:i + batch_size]
            for image in images[i:i + batch_size]:
                augmented_image = augment_image(image)  # OpenCV를 사용한 증강
                batch_images.append(augmented_image)
            yield np.array(batch_images), np.array(batch_labels)

# 배치 생성기 초기화
batch_size = 16
train_generator = data_generator(train_images, train_labels, batch_size)
val_generator = data_generator(val_images, val_labels, batch_size)


# ResNet50 모델 설정
base_model = ResNet50(weights='imagenet', include_top=False, input_shape=(224, 224, 3))
base_model.trainable = True

# 첫 120개의 레이어 고정
for layer in base_model.layers[:120]:
    layer.trainable = False


# 모델 구성
model = models.Sequential([
    base_model,
    layers.GlobalAveragePooling2D(),
    layers.Dense(216, activation='relu'),
    layers.Dropout(0.4),
    layers.Dense(128, activation='relu'),
    layers.Dropout(0.2),
    layers.Dense(1, activation='sigmoid')  # 이진 분류
])

# 모델 컴파일
model.compile(optimizer=Adam(learning_rate=1e-5), loss='binary_crossentropy', metrics=['accuracy'])

# 모델 체크포인트 설정
save_dir = os.path.join(os.getcwd(), 'models')
os.makedirs(save_dir, exist_ok=True)
model_path = os.path.join(save_dir, 'dog_behavior_model.keras')

early_stopping = EarlyStopping(monitor='val_loss', patience=5, restore_best_weights=True)
model_checkpoint = ModelCheckpoint(model_path, save_best_only=True, monitor='val_loss')

class_weights = compute_class_weight(
    class_weight = 'balanced',
    classes=np.unique(train_labels),
    y=train_labels
)

class_weights_dict = {i: weight for i, weight in enumerate(class_weights)}
print(f"클래스 가중치: {class_weights_dict}")

# 모델 훈련
history = model.fit(
    train_generator,
    validation_data=val_generator,
    steps_per_epoch=len(train_images) // batch_size,
    validation_steps=len(val_images) // batch_size,
    epochs=10,
    callbacks=[early_stopping, model_checkpoint]
)

# 모델 저장
model.save(model_path)
print(f"모델 훈련 완료 및 저장 완료: {model_path}")

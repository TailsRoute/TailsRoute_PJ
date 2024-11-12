import os
import numpy as np
import tensorflow as tf
import cv2
from tensorflow.keras.applications import ResNet50
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D, Dropout
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint, ReduceLROnPlateau
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt
from preprocess import load_and_match_data  # preprocess.py의 함수를 불러옴

# 이미지 로드 및 전처리 함수 (OpenCV 활용)
def load_and_preprocess_image(filepath):
    img = cv2.imread(filepath)
    img = cv2.resize(img, (224, 224))  # ResNet50 입력 크기 맞추기
    img = img / 255.0  # 정규화
    return img

# OpenCV 기반 증강 함수 정의
def augment_image(image):
    # 밝기 조절 (alpha 범위는 0.8~1.2로 조정)
    brightness = np.random.uniform(0.8, 1.2)
    image = cv2.convertScaleAbs(image, alpha=brightness)

    # 회전 (각도 범위 -30도~30도)
    angle = np.random.uniform(-30, 30)
    (h, w) = image.shape[:2]
    M = cv2.getRotationMatrix2D((w / 2, h / 2), angle, 1.0)
    image = cv2.warpAffine(image, M, (w, h), flags=cv2.INTER_LINEAR, borderMode=cv2.BORDER_REFLECT_101)

    # 대비 조정 (1.0~1.3 범위)
    contrast = np.random.uniform(1.0, 1.3)
    image = cv2.addWeighted(image, contrast, np.zeros_like(image), 0, 0)

    # 노이즈 추가
    noise = np.random.normal(0, 15, image.shape).astype(np.uint8)
    image = cv2.add(image, noise)

    # 이동 (폭과 높이의 ±10% 범위)
    tx = np.random.uniform(-0.1 * w, 0.1 * w)
    ty = np.random.uniform(-0.1 * h, 0.1 * h)
    M = np.float32([[1, 0, tx], [0, 1, ty]])
    image = cv2.warpAffine(image, M, (w, h), flags=cv2.INTER_LINEAR, borderMode=cv2.BORDER_REFLECT_101)

    # 확대/축소 (0.9배~1.1배 범위)
    zoom = np.random.uniform(0.9, 1.1)
    new_w, new_h = int(w * zoom), int(h * zoom)
    image = cv2.resize(image, (new_w, new_h), interpolation=cv2.INTER_LINEAR)

    # 확대 후 크기 조정 (224x224로 맞춤)
    if zoom < 1.0:
        pad_w = (w - new_w) // 2
        pad_h = (h - new_h) // 2
        image = cv2.copyMakeBorder(image, pad_h, pad_h, pad_w, pad_w, cv2.BORDER_REFLECT_101)
    else:
         # 최종 이미지 크기를 224x224로 고정
        image = cv2.resize(image, (224, 224), interpolation=cv2.INTER_LINEAR)

    # 수평 뒤집기 (50% 확률)
    if np.random.rand() < 0.5:
        image = cv2.flip(image, 1)

    # 색조 변화 추가 (Hue shift)
    image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    hue_shift = np.random.uniform(-10, 10)
    image[:, :, 0] = np.clip(image[:, :, 0] + hue_shift, 0, 255)
    image = cv2.cvtColor(image, cv2.COLOR_HSV2BGR)

    # 정규화
    image = image / 255.0
    mean = np.array([0.485, 0.456, 0.406])
    std = np.array([0.229, 0.224, 0.225])
    image = (image - mean) / std

    # 최종 이미지 크기를 224x224로 고정
    image = cv2.resize(image, (224, 224), interpolation=cv2.INTER_LINEAR)

    return image

# Custom generator 정의 (OpenCV 증강 포함)
def custom_generator(X, y, batch_size=16):
    while True:
        idx = np.random.randint(0, X.shape[0], batch_size)
        batch_X = X[idx]
        batch_y = y[idx]
        augmented_X = np.array([augment_image(img) for img in batch_X])
        yield augmented_X, batch_y

# 데이터 전처리 함수
def prepare_data(data_pairs, img_size=(224, 224)):
    X = []
    y = []
    for frame_path, label_data in data_pairs:
        img = load_and_preprocess_image(frame_path)
        keypoints = []
        for _, point in label_data["annotations"][0]["keypoints"].items():
            keypoints.append([point["x"], point["y"]])
        keypoints = np.array(keypoints).flatten()
        X.append(img)
        y.append(keypoints)
    return np.array(X), np.array(y)

# ResNet50 기반 모델 구축
def create_resnet_model(output_shape):
    base_model = ResNet50(weights="imagenet", include_top=False, input_shape=(224, 224, 3))
    base_model.trainable = False
    model = Sequential([
        base_model,
        GlobalAveragePooling2D(),
        Dense(512, activation='relu'),
        Dropout(0.5),
        Dense(output_shape, activation='linear')
    ])
    return model

if __name__ == "__main__":
    # 디렉토리 설정
    labeling_dir = 'dog_behavior_analysis/train/sit/labeling_sit'
    frame_dir = 'dog_behavior_analysis/train/sit/frame_sit'

    # 데이터 로드 및 전처리
    data_pairs = load_and_match_data(labeling_dir, frame_dir)
    X, y = prepare_data(data_pairs)

    # 모델 생성
    model = create_resnet_model(output_shape=y.shape[1])
    model.compile(optimizer=Adam(), loss='mse', metrics=['mae', 'mse'])

    # 학습 데이터와 검증 데이터 분리
    X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2, random_state=42)

    # 사용자 정의 데이터 생성기 사용
    train_generator = custom_generator(X_train, y_train, batch_size=16)

    # 콜백 설정
    early_stopping = EarlyStopping(monitor='val_loss', patience=5, restore_best_weights=True)
    model_checkpoint = ModelCheckpoint('best_model.keras', save_best_only=True, monitor='val_loss')
    reduce_lr = ReduceLROnPlateau(monitor='val_loss', factor=0.5, patience=3, min_lr=1e-6)

    # 모델 훈련
    history = model.fit(
        train_generator,
        steps_per_epoch=len(X_train) // 16,
        epochs=30,
        validation_data=(X_val, y_val),
        callbacks=[early_stopping, model_checkpoint, reduce_lr]
    )

    # 훈련 결과 시각화
    plt.figure(figsize=(12, 6))
    plt.subplot(1, 2, 1)
    plt.plot(history.history['mse'], label='Train MSE')
    plt.plot(history.history['val_mse'], label='Validation MSE')
    plt.xlabel('Epochs')
    plt.ylabel('Mean Squared Error')
    plt.title('Training and Validation MSE')
    plt.legend()

    plt.subplot(1, 2, 2)
    plt.plot(history.history['mae'], label='Train MAE')
    plt.plot(history.history['val_mae'], label='Validation MAE')
    plt.xlabel('Epochs')
    plt.ylabel('Mean Absolute Error')
    plt.title('Training and Validation MAE')
    plt.legend()
    plt.show()

    # 모델 저장
    save_dir = os.path.join(os.getcwd(), 'models')
    os.makedirs(save_dir, exist_ok=True)
    model_path = os.path.join(save_dir, 'dog_behavior_analysis_model.keras')
    model.save(model_path)

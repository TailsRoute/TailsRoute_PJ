import os
import numpy as np
import tensorflow as tf
from tensorflow.keras.applications import ResNet50
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.preprocessing.image import img_to_array, load_img
import json
import matplotlib.pyplot as plt
from preprocess import load_and_match_data  # preprocess.py의 함수를 불러옴

# 데이터 전처리 함수
def prepare_data(data_pairs, img_size=(224, 224)):
    X = []
    y = []

    for frame_path, label_data in data_pairs:
        # 이미지를 ResNet50 입력 크기에 맞게 로드
        img = load_img(frame_path, target_size=img_size)
        img_array = img_to_array(img)
        img_array = tf.keras.applications.resnet50.preprocess_input(img_array)
        
        # JSON의 키포인트 데이터 전처리
        keypoints = []
        for _, point in label_data["annotations"][0]["keypoints"].items():
            keypoints.append([point["x"], point["y"]])
        keypoints = np.array(keypoints).flatten()

        X.append(img_array)
        y.append(keypoints)

    X = np.array(X)
    y = np.array(y)
    return X, y

# ResNet50 기반 모델 구축
def create_resnet_model(output_shape):
    base_model = ResNet50(weights="imagenet", include_top=False, input_shape=(224, 224, 3))
    base_model.trainable = False  # 사전 학습된 계층 고정
    
    model = Sequential([
        base_model,
        GlobalAveragePooling2D(),
        Dense(512, activation='relu'),
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

    # 모델 훈련 및 로그 저장
    history = model.fit(X, y, epochs=10, batch_size=16, validation_split=0.2)

    # 모델 체크포인트 설정
    save_dir = os.path.join(os.getcwd(), 'models')
    os.makedirs(save_dir, exist_ok=True)
    model_path = os.path.join(save_dir, 'dog_behavior_anlaysis.keras')

    # 훈련 결과 시각화
    plt.figure(figsize=(12, 6))

    # 훈련 및 검증 MSE
    plt.subplot(1, 2, 1)
    plt.plot(history.history['mse'], label='Train MSE')
    plt.plot(history.history['val_mse'], label='Validation MSE')
    plt.xlabel('Epochs')
    plt.ylabel('Mean Squared Error')
    plt.title('Training and Validation MSE')
    plt.legend()

    # 훈련 및 검증 MAE
    plt.subplot(1, 2, 2)
    plt.plot(history.history['mae'], label='Train MAE')
    plt.plot(history.history['val_mae'], label='Validation MAE')
    plt.xlabel('Epochs')
    plt.ylabel('Mean Absolute Error')
    plt.title('Training and Validation MAE')
    plt.legend()

    plt.show()

    # 모델 저장
    model.save(model_path)
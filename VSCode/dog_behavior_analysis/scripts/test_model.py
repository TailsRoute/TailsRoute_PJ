import numpy as np
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import load_img, img_to_array


# 모델 경로 설정
model_path = r'C:\Users\admin\IdeaProjects\TailsRoute_PJ\VScode\models\dog_behavior_model.keras'

# 모델 불러오기
model = load_model(model_path)
print("모델 정상 작동")

#테스트 이미지 로드 및 전처리
image_path = r'C:\Users\admin\IdeaProjects\TailsRoute_PJ\VSCode\dog_behavior_analysis\data\sit\n02085620_4290.jpg' # 이미지 경로
image = load_img(image_path, target_size=(224, 224)) # 이미지 크기 
image = img_to_array(image) / 255.0 # 정류화 (0-1 사이 값으로 변환)
image = np.expand_dims(image, axis=0) # 배치 차원 추가

# 예측 수행
prediction = model.predict(image)

# 결과 출력
if prediction[0][0] >= 0.5:
    print(f"예측 결과: 앉기 행동 (확률: {prediction[0][0]:.2f})")
else:
    print(f"예측 결과: 기타 행동 (확률: {prediction[0][0]:.2f})")
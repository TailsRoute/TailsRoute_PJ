import numpy as np
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import load_img, img_to_array


# 모델 경로 설정
model_path = r'C:\Users\admin\IdeaProjects\TailsRoute_PJ\VScode\models\dog_behavior_model.keras'

# 모델 불러오기
model = load_model(model_path)
print("모델 정상 작동")

# OpenCV로 전처리
def preprocess_image(model_path):
    image = cv2.imread(model_path)
    gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    blurred_image = cv2.GaussianBlur(gray_image, (5, 5), 0)
    edges = cv2.Canny(blurred_image, threshold1=50, threshold2=150)
    processed_image = cv2.resize(edges, (224, 224))
    processed_image = np.expand_dims(processed_image, axis=-1)
    processed_image = np.expand_dims(processed_image, axis=0) / 255.0
    return processed_image

#테스트 이미지 로드
image_path = r'C:\Users\admin\IdeaProjects\TailsRoute_PJ\VSCode\dog_behavior_analysis\data\other\n02085620_3033.jpg' # 이미지 경로
processed_image = processed_image(image_path)


# 예측 수행
prediction = model.predict(processed_image)

# 결과 출력 
prediction_percentage = prediction[0][0] * 100

# 결과 출력
if prediction_percentage >= 50:
    print(f"예측 결과: 앉기 행동 (확률: {prediction_percentage:.2f}%)")
else:
    print(f"예측 결과: 기타 행동 (확률: {prediction_percentage:.2f}%)")
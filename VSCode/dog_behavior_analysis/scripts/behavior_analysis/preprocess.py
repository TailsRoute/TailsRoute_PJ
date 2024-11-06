import gdown
import zipfile
import os
import numpy as np
from sklearn.model_selection import train_test_split
import json
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
sit_file_id = '11BjnynF280Qfs2eDOQiKIYkHe7o6O04o'  # sit.zip 파일 ID


# 데이터 폴더 생성 
os.makedirs('dog_behavior_analysis/train', exist_ok=True)
os.makedirs('dog_behavior_analysis/validation', exist_ok=True)

# 파일 다운로드 
download_file(sit_file_id, 'dog_behavior_analysis/train/sit.zip')
download_file(sit_file_id, 'dog_behavior_analysis/validation/sit.zip')


# 압축 해제 경로 설정
os.makedirs('dog_behavior_analysis/train', exist_ok=True)
os.makedirs('dog_behavior_analysis/validation', exist_ok=True)

# 압축 해제
extract_zip('dog_behavior_analysis/train/sit.zip', 'dog_behavior_analysis/train')
extract_zip('dog_behavior_analysis/validation/sit.zip', 'dog_behavior_analysis/validation')


def load_and_match_data(labeling_dir, frame_dir):
    # 데이터 경로 설정
    labeling_sit_dir = 'dog_behavior_analysis/train/sit/labeling_sit'
    frame_sit_dir= 'dog_behavior_analysis/train/sit/frame_sit'


    data_pairs = []

    # 강아지 데이터 불러오기 
    for json_file in os.listdir(labeling_sit_dir):
        if json_file.endswith(".json"):
            json_path = os.path.join(labeling_sit_dir, json_file)

            # JSON 파일 열기 
            try:
                with open(json_path, 'r', encoding='utf-8') as f:
                    label_sit_data = json.load(f)
            except UnicodeDecodeError:
                with open(json_path, 'r', encoding='utf-8-sig') as f:
                    label_sit_data = json.load(f)

            #file_video URL을 변수로 저장 
            original_video_url = label_sit_data.get("file_video", "")

            if "annotations" in label_sit_data:
                for annotation in label_sit_data["annotations"]:
                    if "frame_url" in annotation:
                        frame_url = annotation["frame_url"]
                        annotation["frame_url"] = original_video_url
                        label_sit_data["file_video"] = frame_url

            # 해당 JSON 파일에 맞는 이미지 폴더 불러오기 
            dog_name = json_file.split(".")[0] 
            frames_path = os.path.join(frame_sit_dir, dog_name)

            # 이미지 파일 불러오기 
            if os.path.exists(frames_path):
                frame_files = sorted(os.listdir(frames_path))
                for frame_file in frame_files:
                    frame_path = os.path.join(frames_path, frame_file)
                
                    # 이미지와 라벨 데이터 매칭
                    data_pairs.append((frame_path, label_sit_data))
    return data_pairs

if __name__ == "__main__":
    labeling_dir = 'dog_behavior_analysis/train/sit/labeling_sit'
    frame_dir = 'dog_behavior_analysis/train/sit/frame_sit'
    load_and_match_data(labeling_dir, frame_dir)




// 페이지 로드 시 diary.imagePath 값이 있다면 미리보기로 설정
document.addEventListener('DOMContentLoaded', function() {
    const imagePreview = document.getElementById('imagePreview');
    const diaryImagePath = document.getElementById('diaryImagePath').value;

    if (diaryImagePath) {
        imagePreview.src = diaryImagePath;  // 기존 이미지 경로로 설정
        imagePreview.classList.remove('hidden');  // 미리보기 이미지 보이기

        // 업로드 버튼과 텍스트 숨기기
        document.getElementById('uploadContent').style.display = 'none';

        // 이미지 지우기 버튼 표시
        document.getElementById('removeImageBtn').classList.remove('hidden');
    }
});


// 등록한 파일 미리보기
document.getElementById('fileInput').addEventListener('change', function(event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();

        reader.onload = function(e) {
            const imagePreview = document.getElementById('imagePreview');
            imagePreview.src = e.target.result;
            imagePreview.classList.remove('hidden');

            // 업로드 버튼과 텍스트 숨기기
            document.getElementById('uploadContent').style.display = 'none';

            // 이미지 지우기 버튼 표시
            document.getElementById('removeImageBtn').classList.remove('hidden');
        };

        reader.readAsDataURL(file);
    }
});

// 업로드 버튼을 클릭하면 파일 입력 창이 열리도록 설정
document.getElementById('fileBtn').addEventListener('click', function() {
    document.getElementById('fileInput').click();
});

// 이미지 지우기 버튼 클릭 이벤트 추가
document.getElementById('removeImageBtn').addEventListener('click', function() {
    const imagePreview = document.getElementById('imagePreview');

    // 미리보기 이미지 숨기기 및 초기화
    imagePreview.src = '';
    imagePreview.classList.add('hidden');

    // 업로드 버튼과 텍스트 다시 표시
    document.getElementById('uploadContent').style.display = 'flex';

    // 이미지 지우기 버튼 숨기기
    this.classList.add('hidden');

    // 파일 입력 초기화
    document.getElementById('fileInput').value = null;

    // hidden input의 diaryImagePath 값도 초기화
    document.getElementById('diaryImagePath').value = '';
});
document.getElementById('fileInput').addEventListener('change', function(event) {
    const file = event.target.files[0];  // 선택한 파일
    if (file) {
        const reader = new FileReader();

        reader.onload = function(e) {
            const imagePreview = document.getElementById('imagePreview');
            imagePreview.src = e.target.result;  // 미리보기 이미지 설정
            imagePreview.classList.remove('hidden');  // 미리보기 이미지 보이기

            // 업로드 버튼과 텍스트 숨기기
            document.getElementById('uploadContent').style.display = 'none';
        };

        reader.readAsDataURL(file);  // 파일을 읽어 미리보기로 표시
    }
});


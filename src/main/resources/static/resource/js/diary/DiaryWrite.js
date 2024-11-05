/* 등록한 파일 미리보기 */
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

            // 이미지 지우기 버튼 표시
            document.getElementById('removeImageBtn').classList.remove('hidden');
        };

        reader.readAsDataURL(file);  // 파일을 읽어 미리보기로 표시
    }
});

// 업로드 버튼을 클릭하면 파일 입력 창이 열리도록 설정
document.getElementById('fileBtn').addEventListener('click', function() {
    document.getElementById('fileInput').click();  // 파일 선택 창 열기
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
    document.getElementById('fileInput').value = '';
});
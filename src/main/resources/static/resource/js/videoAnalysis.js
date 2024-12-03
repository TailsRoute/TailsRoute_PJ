// 버튼 컨테이너와 파일 입력 필드 참조
const fileInput = document.getElementById("video");
const buttonContainer = document.getElementById("button-container");

// 파일 입력 상태 감지
fileInput.addEventListener("change", () => {
    // 파일이 선택된 경우 버튼 추가
    if (fileInput.files.length > 0) {
        if (!document.getElementById("submit-button")) {
            const submitButton = document.createElement("button");
            submitButton.type = "submit";
            submitButton.id = "submit-button";
            submitButton.innerText = "Start Analysis";
            submitButton.style.marginLeft = "10px"; // 파일 입력 필드와의 간격
            buttonContainer.appendChild(submitButton);
        }
    } else {
        // 파일이 선택되지 않은 경우 버튼 제거
        const submitButton = document.getElementById("submit-button");
        if (submitButton) {
            buttonContainer.removeChild(submitButton);
        }
    }
});
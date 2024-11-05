function shareDiary() {
    const diaryId = document.querySelector('button[data-diary-id]').getAttribute('data-diary-id');
    if (confirm("이 일기를 공유하시겠습니까?")) {
        window.location.href = "/usr/article/shareDiary/" +  diaryId ;
    }
}
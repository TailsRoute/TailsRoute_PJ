// AJAX 요청을 보내는 함수
function deleteLocation(dogId) {

    if (!confirm('정말로 삭제하시겠습니까?')) {
        return;
    }

    $.ajax({
        url: '/usr/gpsAlert/deleteLocation',
        type: 'POST',
        data: {
            dogId: dogId
        },
        success: function () {
            alert("삭제되었습니다.");
            window.location.reload();
        },
        error: function (xhr) {
            alert(xhr.responseText);
            console.log(xhr.responseText);
        }
    });
}
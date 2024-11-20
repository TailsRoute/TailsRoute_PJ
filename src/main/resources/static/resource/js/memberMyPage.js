// AJAX 요청을 보내는 함수
function deleteLocation(dogId) {
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

$(document).ready(function () {
    $('.myPage_modify_button').click(function (event) {
        if ($(event.target).attr('type') === 'button') {
            event.stopPropagation();
            $('.passwordCheck').fadeIn();
        }
    });

    $(document).click(function (event) {
        if (!$(event.target).closest('.passwordCheck').length && !$(event.target).closest('.myPage_modify_button').length) {
            $('.passwordCheck').fadeOut();
        }
    });

    $('.passwordCheck').click(function (event) {
        event.stopPropagation();
    });
});


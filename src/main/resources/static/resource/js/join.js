const autoHyphen = (target) => {
    target.value = target.value
        .replace(/[^0-9]/g, '') // 숫자 외 문자 제거
        .replace(/^(\d{2,3})(\d{3,4})(\d{4})$/, '$1-$2-$3'); // 하이픈 추가
};

let isEmailVerified = false; // 이메일 인증 여부를 추적하는 변수

document.addEventListener("DOMContentLoaded", function () {
    const validationRules = {
        loginId: {
            regex: /^[a-z0-9]{5,20}$/,
            message: '아이디는 5~20자의 영문 소문자, 숫자만 가능합니다.',
        },
        loginPw: {
            regex: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,16}$/,
            message: '비밀번호는 8~16자의 영문, 숫자, 특수문자 조합이어야 합니다.',
        },
        confirmPw: {
            custom: (value, form) => value === form.loginPw.value.trim(),
            message: '비밀번호가 일치하지 않습니다.',
        },
        cellphoneNum: {
            regex: /^\d{2,3}-?\d{3,4}-?\d{4}$/,
            message: '전화번호는 10~11자 형식이어야 합니다.',
        },
        nickname: {
            regex: /^[a-zA-Z가-힣0-9]{1,7}$/,
            message: '닉네임은 7자 이하, 특수문자 사용 불가입니다.',
        },
        email: {
            regex: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
            message: '유효한 이메일 주소를 입력하세요.',
        },
        name: {
            regex: /^[a-zA-Z가-힣\s'-]{2,20}$/,
            message: '이름은 2~20자의 한글, 영문, 띄어쓰기, 일부 특수문자(-)만 가능합니다.',
        },
    };

    // 이메일 인증 버튼 클릭 이벤트
    $(".sendMail").click(function () {
        const emailField = $("#email");
        const email = emailField.val().trim();

        if (!validationRules.email.regex.test(email)) {
            alert(validationRules.email.message);
            return;
        }

        $.ajax({
            type: 'POST',
            url: '/usr/member/CheckMail',
            data: { mail: email },
            dataType: 'json',
            success: function (response) {
                if (response.success) {
                    alert("인증번호가 전송되었습니다.");
                    emailField.prop("readonly", true);
                    $("#auth-code-container").show();

                    $(".verifyAuthCode").off("click").on("click", function () {
                        handleAuthCodeVerification(email);
                    });
                } else {
                    alert("이메일 전송에 실패했습니다: " + response.message);
                }
            },
            error: function () {
                alert("서버 통신 중 오류가 발생했습니다.");
            },
        });
    });

    // 인증번호 확인 처리
    const handleAuthCodeVerification = (email) => {
        const code = $("#authCode").val().trim();

        if (!email || !code) {
            alert("이메일 또는 인증번호가 입력되지 않았습니다.");
            return;
        }

        $.ajax({
            type: "POST",
            url: "/usr/member/verifyCode",
            data: { mail: email, code: code },
            dataType: "json",
            success: function (response) {
                if (response.success) {
                    alert("이메일 인증이 완료되었습니다.");
                    isEmailVerified = true; // 이메일 인증 상태 변경
                    $("#authCode-check").show();  // ✅ 표시
                    $("#authCode-error").hide();  // ❌ 숨기기
                } else {
                    alert("인증번호가 올바르지 않습니다.");
                    $("#authCode-check").hide();  // ✅ 숨기기
                    $("#authCode-error").show();  // ❌ 표시
                }
            },
            error: function (xhr) {
                handleAjaxError(xhr);
            },
        });
    };

    // 유효성 검사
    const validateField = (field, form) => {
        const { id, value } = field;
        const messageElement = document.getElementById(`${id}-message`);
        const rule = validationRules[id];

        if (!rule) return true; // 규칙이 없으면 유효성 검사 건너뜀

        const isValid = rule.regex
            ? rule.regex.test(value.trim())
            : rule.custom(value.trim(), form);

        if (messageElement) {
            if (isValid) {
                messageElement.textContent = '사용 가능합니다.';
                messageElement.className = 'success-message';
            } else {
                messageElement.textContent = rule.message;
                messageElement.className = 'error-message';
            }
        }

        return isValid;
    };

    // 폼 제출 처리
    document.querySelector(".join-form").addEventListener("submit", function (event) {
        event.preventDefault();

        let isValid = true;

        // 유효성 검사 실행
        Array.from(this.elements).forEach(field => {
            if (field.tagName === 'INPUT') {
                const valid = validateField(field, this);
                isValid = isValid && valid;
            }
        });

        // 유효성 검사 실패 시 처리
        if (!isValid) {
            alert("폼 입력값을 확인해주세요.");
            return;
        }

        // 이메일 인증 확인
        if (!isEmailVerified) {
            alert("이메일 인증을 먼저 완료해주세요.");
            return;
        }

        submitForm(this);
    });

    // 폼 제출 처리 함수
    const submitForm = (form) => {
        const formData = new FormData(form);

        fetch(form.action, {
            method: 'POST',
            body: formData,
        })
            .then(response => {
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    return response.json();
                } else {
                    return response.text().then(html => {
                        throw new Error('HTML 응답이 반환되었습니다: ' + html);
                    });
                }
            })
            .then(data => {
                if (data.success) {
                    alert(data.message);
                    if (data.redirectUri) {
                        window.location.href = data.redirectUri;
                    }
                } else {
                    alert('회원가입에 실패했습니다: ' + data.message);
                    resetFormInputs(form); // 회원가입 실패 시 폼 초기화
                }
            })
            .catch(error => {
                console.error('에러 발생:', error);
                resetFormInputs(form); // 회원가입 실패 시 폼 초기화
                alert('서버와의 통신 중 문제가 발생했습니다: ' + error.message);
            });
    };

    // 폼 초기화 함수
    const resetFormInputs = (form) => {
        Array.from(form.elements).forEach(field => {
            if (field.tagName === 'INPUT') {
                field.value = ''; // input 값을 비움

                //유효성 검사 메시지 초기화
                const messageElement = document.getElementById(`${field.id}-message`);
                if (messageElement) {
                    messageElement.textContent = ''; // 메시지 제거
                    messageElement.className = ''; // 클래스 제거
                }
            }
        });

        isEmailVerified = false; // 이메일 인증 상태 초기화
        $("#auth-code-container").hide(); // 인증번호 입력창 숨김
        $("#authCode-check").hide(); // ✅ 숨김
        $("#authCode-error").hide(); // ❌ 숨김
        $("#email").prop("readonly", false); // 이메일 수정 가능
    };

    // AJAX 요청 에러 처리
    const handleAjaxError = (xhr) => {
        if (xhr.status === 400) {
            alert("인증번호가 올바르지 않거나 만료되었습니다.");
        } else if (xhr.status === 500) {
            alert("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        } else {
            alert("알 수 없는 오류가 발생했습니다.");
        }
        console.error("서버 응답:", xhr.responseText);
    };

    // 모든 input의 실시간 유효성 검사
    document.querySelectorAll(".join-form input").forEach(input => {
        input.addEventListener("input", function () {
            validateField(input, document.querySelector(".join-form"));
        });
    });
});

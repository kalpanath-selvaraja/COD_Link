$(document).ready(function () {
    $('#btn-toggle-password').on('click', function () {
        const password = $('#password');
        password.attr('type', password.attr('type') === 'password' ? 'text' : 'password');
    });
    $('#login-form').on('submit', function (e) {
        e.preventDefault();

        let username = $('#username').val().trim();
        let password = $('#password').val();

        if (!username || !password) {
            alert("Please enter both username and password.");
            return;
        }

        let loginData = JSON.stringify({
            "username": username,
            "password": password
        });

        $.ajax({
            url: '/api/auth/login',
            type: 'POST',
            contentType: 'application/json',
            data: loginData,
            success: function (response) {
                if (response.body && response.body.token) {
                    localStorage.clear();
                    sessionStorage.clear();
                    localStorage.setItem("JWT", response.body.token);
                    localStorage.setItem("UserId", response.body.userId);
                    localStorage.setItem("username",response.body.username)
                    if(response.body.isReceptionist == true){
                        localStorage.setItem("Role", 'RECEPTIONIST')
                    }else{
                        localStorage.setItem("Role", response.body.role)
                    }


                    alert("Logged in successfully!");
                    if (response.body.role === 'USER') {
                        window.location.href = '../customer/customer-setup.html'
                    } else if (response.body.role === 'COURIER') {
                        window.location.href = 'courier-dashboard.html';
                    } else if (response.body.isReceptionist === true) {
                        window.location.href = '/admin/shipments.html';
                    } else {
                        window.location.href = '/admin/dashboard.html';
                    }
                } else {
                    alert(response.message || "Login failed!");
                }
            },
            error: function (xhr, status, error) {
                alert("Login failed: " + (xhr.responseText || xhr.status));
            }
        });
    });
});
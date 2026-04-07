// Ankard- main.js
// Placeholder cho frontend dev tuỳ chỉnh sau

// Tự động ẩn thông báo success/error sau 4 giây
document.addEventListener('DOMContentLoaded', function () {
    const messages = document.querySelectorAll('.msg-success, .msg-error');
    messages.forEach(function (msg) {
        setTimeout(function () {
            msg.style.transition = 'opacity 0.5s';
            msg.style.opacity = '0';
            setTimeout(function () { msg.remove(); }, 500);
        }, 4000);
    });
});

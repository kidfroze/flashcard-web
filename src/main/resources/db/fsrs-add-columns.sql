-- SQL Server: thêm cột FSRS cho bảng flashcard_progress (chạy thủ công khi dùng spring.jpa.hibernate.ddl-auto=none)

ALTER TABLE flashcard_progress ADD fsrs_stability FLOAT NULL;
ALTER TABLE flashcard_progress ADD fsrs_difficulty FLOAT NULL;

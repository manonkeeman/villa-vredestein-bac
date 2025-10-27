INSERT INTO users (email, username, password, role)
VALUES
    ('admin@villavredestein.nl', 'AdminUser', 'admin123', 'ADMIN'),
    ('student@villavredestein.nl', 'StudentUser', 'student123', 'STUDENT'),
    ('cleaner@villavredestein.nl', 'CleanerUser', 'cleaner123', 'CLEANER');

INSERT INTO documents (file_name, content_type, size, storage_path, uploaded_at, uploader_id)
VALUES
    ('huurcontract_Simon_Talsma.pdf', 'application/pdf', 248000, 'uploads/huurcontract_Simon_Talsma.pdf', CURRENT_TIMESTAMP, 1);

INSERT INTO tasks (name, description, completed, created_at, assigned_user_id, evidence_document_id)
VALUES
    ('Keuken schoonmaken', 'Grondige schoonmaak van de keuken en aanrecht', false, CURRENT_TIMESTAMP, 2, NULL),
    ('Badkamer poetsen', 'Reinig wastafel, douche en vloer', false, CURRENT_TIMESTAMP, 3, NULL),
    ('Afval buiten zetten', 'Restafval en plastic containers buiten zetten', true, CURRENT_TIMESTAMP, 2, 1);

INSERT INTO assignments (task_id, assignee_id, due_date, status, created_at)
VALUES
    (1, 2, '2025-11-01', 'OPEN', CURRENT_TIMESTAMP),
    (2, 3, '2025-11-05', 'PLANNED', CURRENT_TIMESTAMP);
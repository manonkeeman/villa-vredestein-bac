-- =======================================
-- 👥 GEBRUIKERS
-- =======================================
INSERT INTO users (email, username, password, role) VALUES
                                                        ('villavredestein@gmail.com', 'Admin',
                                                         '$2a$10$EixZaYVK1fsbw1Zfbx3OXePaWxn96p36DdxEnCYZ5oOJj0ksN8DAG', 'ADMIN'),
                                                        ('ikheetalvar@gmail.com', 'Alvar',
                                                         '$2a$10$dJeoIo6yCZyD/6jzA1X7IuzwzQhKyCMLoE/CS6BHFJSfUVc3glXOG', 'STUDENT'),
                                                        ('desmondstaal@gmail.com', 'Desmond',
                                                         '$2a$10$ZvnfY.xgj4euJWJZfQpsqe6sHhFvBk4yAT7PvJYvBe4v4U7j6vYy6', 'STUDENT'),
                                                        ('medocstaal@gmail.com', 'Medoc',
                                                         '$2a$10$WCuN6WfYpRLN4fQfHYxKVuF88tW9TR0mStJjEYoDDr8g77nm6k6Ei', 'STUDENT'),
                                                        ('simontalsma2@gmail.com', 'Simon',
                                                         '$2a$10$hBRdhNvI.vcjcAoPO9x4GuWpEKavAtB5Jmx.1Y8FPh3yDFZW29UMO', 'STUDENT'),
                                                        ('cleaner', 'Cleaner',
                                                         '$2a$10$4YW93z7bCkTkDLoOuzgqeeCekNQOEBo4B4KXdsTFoQ9sxV3Sl7OeC', 'CLEANER');

-- =======================================
-- 🏠 KAMERS
-- =======================================
INSERT INTO rooms (name, occupant_id) VALUES
                                          ('Japan', NULL),
                                          ('Argentinië', (SELECT id FROM users WHERE email='simontalsma2@gmail.com')),
                                          ('Thailand', (SELECT id FROM users WHERE email='desmondstaal@gmail.com')),
                                          ('Italië', NULL),
                                          ('Frankrijk', (SELECT id FROM users WHERE email='medocstaal@gmail.com')),
                                          ('Oekraïne', (SELECT id FROM users WHERE email='ikheetalvar@gmail.com'));

-- =======================================
-- 📄 DOCUMENTEN
-- =======================================
INSERT INTO documents
(title, description, storage_path, content_type, size, uploaded_at, role_access, uploaded_by_id)
VALUES
    ('Huisregels Villa Vredestein',
     'Overzicht van huisregels en gedragscode.',
     'uploads/Huisregels.pdf', 'application/pdf', 24576, CURRENT_TIMESTAMP, 'ALL',
     (SELECT id FROM users WHERE email='villavredestein@gmail.com')),
    ('Veiligheidsinstructies',
     'Brandveiligheid en noodprocedures.',
     'uploads/Veiligheid.pdf', 'application/pdf', 38912, CURRENT_TIMESTAMP, 'ALL',
     (SELECT id FROM users WHERE email='villavredestein@gmail.com')),
    ('Pensionovereenkomst',
     'Modelovereenkomst voor huur.',
     'uploads/Pensionovereenkomst.pdf', 'application/pdf', 32768, CURRENT_TIMESTAMP, 'STUDENT',
     (SELECT id FROM users WHERE email='villavredestein@gmail.com'));

-- =======================================
-- 💶 BETALINGEN
-- =======================================
INSERT INTO payments (amount, date, status, description, student_id) VALUES
                                                                         (350.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), 'PAID', 'Huur - Alvar (vorige maand)',
                                                                          (SELECT id FROM users WHERE email='ikheetalvar@gmail.com')),
                                                                         (350.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), 'PAID', 'Huur - Medoc (vorige maand)',
                                                                          (SELECT id FROM users WHERE email='medocstaal@gmail.com')),
                                                                         (350.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), 'PAID', 'Huur - Simon (vorige maand)',
                                                                          (SELECT id FROM users WHERE email='simontalsma2@gmail.com')),
                                                                         (350.00, CURRENT_TIMESTAMP, 'OPEN', 'Huur - Desmond (huidige maand)',
                                                                          (SELECT id FROM users WHERE email='desmondstaal@gmail.com'));

-- =======================================
-- 🧾 FACTUREN (H2-compatible)
-- =======================================

INSERT INTO invoices
(student_id, invoice_month, invoice_year, amount, status, due_date, description, reminder_sent)
VALUES
    ((SELECT id FROM users WHERE email='ikheetalvar@gmail.com'),
        MONTH(CURRENT_DATE()), YEAR(CURRENT_DATE()), 350.00, 'OPEN',
     DATEADD('DAY', 14, CURRENT_DATE()), 'Huur huidige maand - Alvar', FALSE);

INSERT INTO invoices
(student_id, invoice_month, invoice_year, amount, status, due_date, description, reminder_sent)
VALUES
    ((SELECT id FROM users WHERE email='desmondstaal@gmail.com'),
        MONTH(CURRENT_DATE()), YEAR(CURRENT_DATE()), 350.00, 'OPEN',
     DATEADD('DAY', 14, CURRENT_DATE()), 'Huur huidige maand - Desmond', FALSE);

INSERT INTO invoices
(student_id, invoice_month, invoice_year, amount, status, due_date, description, reminder_sent)
VALUES
    ((SELECT id FROM users WHERE email='medocstaal@gmail.com'),
        MONTH(CURRENT_DATE()), YEAR(CURRENT_DATE()), 350.00, 'OPEN',
     DATEADD('DAY', 14, CURRENT_DATE()), 'Huur huidige maand - Medoc', FALSE);

INSERT INTO invoices
(student_id, invoice_month, invoice_year, amount, status, due_date, description, reminder_sent)
VALUES
    ((SELECT id FROM users WHERE email='simontalsma2@gmail.com'),
        MONTH(CURRENT_DATE()), YEAR(CURRENT_DATE()), 350.00, 'OPEN',
     DATEADD('DAY', 14, CURRENT_DATE()), 'Huur huidige maand - Simon', FALSE);

-- =======================================
-- 🧹 SCHOONMAAKTAKEN
-- =======================================

-- Week 1
INSERT INTO cleaning_tasks
(week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access)
VALUES
    (1, 'Keuken & vaatwasser verzorgen',
     'Leeg de vaatwasser, maak het aanrecht schoon en reinig de oven.',
     FALSE, 'Controleer filter vaatwasser', NULL,
     (SELECT id FROM users WHERE email='simontalsma2@gmail.com'),
     'ALL'),
    (1, 'Badkamer & toilet poetsen',
     'Maak wastafel, douche, spiegel en toilet grondig schoon en droog.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='ikheetalvar@gmail.com'),
     'ALL'),
    (1, 'Afval buiten zetten',
     'Zorg voor goede afvalscheiding: GFT, PMD, papier/karton, restafval en statiegeldflessen apart.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='desmondstaal@gmail.com'),
     'ALL'),
    (1, 'Woonkamer stofzuigen & dweilen',
     'Stofzuig en dweil de woonkamer, hal en trap.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='medocstaal@gmail.com'),
     'ALL');

-- Week 2
INSERT INTO cleaning_tasks
(week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access)
VALUES
    (2, 'Keuken & vaatwasser verzorgen',
     'Leeg de vaatwasser, maak het aanrecht schoon en reinig de oven.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='ikheetalvar@gmail.com'),
     'ALL'),
    (2, 'Badkamer & toilet poetsen',
     'Maak wastafel, douche, spiegel en toilet grondig schoon en droog.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='desmondstaal@gmail.com'),
     'ALL'),
    (2, 'Afval buiten zetten',
     'Zorg voor goede afvalscheiding: GFT, PMD, papier/karton, restafval en statiegeldflessen apart.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='medocstaal@gmail.com'),
     'ALL'),
    (2, 'Woonkamer stofzuigen & dweilen',
     'Stofzuig en dweil de woonkamer, hal en trap.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='simontalsma2@gmail.com'),
     'ALL');

-- =======================================
-- 👁️ VIEWS
-- =======================================

CREATE OR REPLACE VIEW view_cleaning_tasks AS
SELECT
    t.id,
    t.week_number,
    t.name AS task_name,
    t.description AS task_description,
    t.completed,
    t.comment,
    t.incident_report,
    t.role_access,
    u.username AS assigned_to
FROM cleaning_tasks t
         LEFT JOIN users u ON t.assigned_to_id = u.id
ORDER BY t.week_number, t.id;

CREATE OR REPLACE VIEW view_invoices AS
SELECT
    i.id, i.title, i.amount, i.status,
    i.invoice_month, i.invoice_year, i.due_date,
    u.username AS student_name, u.email AS student_email
FROM invoices i
         LEFT JOIN users u ON i.student_id = u.id
ORDER BY i.invoice_year DESC, i.invoice_month DESC;

CREATE OR REPLACE VIEW view_payments AS
SELECT
    p.id, p.amount, p.date, p.status, p.description,
    u.username AS student_name, u.email AS student_email
FROM payments p
         LEFT JOIN users u ON p.student_id = u.id
ORDER BY p.date DESC;

CREATE OR REPLACE VIEW view_rooms AS
SELECT
    r.id, r.name AS room_name,
    u.username AS occupant_name, u.email AS occupant_email
FROM rooms r
         LEFT JOIN users u ON r.occupant_id = u.id
ORDER BY r.name;

CREATE OR REPLACE VIEW view_documents AS
SELECT
    d.id, d.title, d.description, d.storage_path, d.size, d.uploaded_at, d.role_access,
    u.username AS uploaded_by_name, u.email AS uploaded_by_email
FROM documents d
         LEFT JOIN users u ON d.uploaded_by_id = u.id
ORDER BY d.uploaded_at DESC;

CREATE OR REPLACE VIEW view_users AS
SELECT
    u.id, u.username AS name, u.email, u.role,
    r.name AS room_name
FROM users u
         LEFT JOIN rooms r ON r.occupant_id = u.id
ORDER BY u.role, u.username;
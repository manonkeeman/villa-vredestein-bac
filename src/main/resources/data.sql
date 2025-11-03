-- =======================================
-- 👥 Gebruikers
-- =======================================
INSERT INTO users (email, username, password, role)
VALUES
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
-- 🏠 Kamers
-- =======================================
INSERT INTO rooms (name, occupant_id)
VALUES
    ('Japan', NULL),
    ('Argentinië', 4),
    ('Thailand', 2),
    ('Italië', NULL),
    ('Frankrijk', 3),
    ('Oekraïne', 5);

-- =======================================
-- 📄 Documenten
-- =======================================
INSERT INTO documents (title, description, storage_path, size, uploaded_at, uploaded_by_id)
VALUES
    ('Huisregels Villa Vredestein', 'Overzicht van huisregels en gedragscode.',
     'uploads/Huisregels.pdf', 12345, CURRENT_TIMESTAMP, 1),
    ('Veiligheidsinstructies', 'Brandveiligheid en noodprocedures.',
     'uploads/Veiligheid.pdf', 67890, CURRENT_TIMESTAMP, 1),
    ('Pensionovereenkomst', 'Modelovereenkomst voor huur.',
     'uploads/Pensionovereenkomst.pdf', 34567, CURRENT_TIMESTAMP, 1);

-- =======================================
-- 💶 Betalingen
-- =======================================
INSERT INTO payments (amount, date, status, description, student_id)
VALUES
    (650.00, DATEADD('MONTH', -2, CURRENT_TIMESTAMP), 'PAID', 'Huur twee maanden terug', 2),
    (650.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), 'PAID', 'Huur vorige maand', 2),
    (650.00, CURRENT_TIMESTAMP, 'OPEN', 'Huur huidige maand', 3);

-- =======================================
-- 🧾 Facturen (huidige maand)
-- =======================================
INSERT INTO invoices (student_id, invoice_month, invoice_year, amount, status, due_date, description, reminder_sent)
VALUES
    (2, MONTH(CURRENT_DATE()), YEAR(CURRENT_DATE()), 350.00, 'OPEN',
     DATEADD('DAY', 14, CURRENT_DATE()), 'Huur huidige maand - Alvar', FALSE),
    (3, MONTH(CURRENT_DATE()), YEAR(CURRENT_DATE()), 350.00, 'OPEN',
     DATEADD('DAY', 14, CURRENT_DATE()), 'Huur huidige maand - Desmond', FALSE),
    (4, MONTH(CURRENT_DATE()), YEAR(CURRENT_DATE()), 350.00, 'OPEN',
     DATEADD('DAY', 14, CURRENT_DATE()), 'Huur huidige maand - Medoc', FALSE),
    (5, MONTH(CURRENT_DATE()), YEAR(CURRENT_DATE()), 350.00, 'OPEN',
     DATEADD('DAY', 14, CURRENT_DATE()), 'Huur huidige maand - Simon', FALSE);

-- =======================================
-- 🧹 Schoonmaak
-- =======================================
INSERT INTO cleaning_schedules (week_number)
VALUES
    (WEEK(CURRENT_DATE())),
    (WEEK(DATEADD('WEEK', 1, CURRENT_DATE())));

INSERT INTO cleaning_tasks (name, description, due_date, completed, assigned_to_id, cleaning_schedule_id)
VALUES
    ('Keuken schoonmaken', 'Grondige schoonmaak van aanrecht, oven en vloer.',
     CURRENT_DATE(), false, 2, 1),
    ('Badkamer poetsen', 'Reinig wastafel, toilet en douche.',
     DATEADD('DAY', 2, CURRENT_DATE()), false, 3, 1),
    ('Afval buiten zetten', 'Containers buiten op de juiste dag.',
     DATEADD('DAY', 3, CURRENT_DATE()), true, 4, 2),
    ('Woonkamer stofzuigen', 'Stofzuig woonkamer en gang.',
     DATEADD('DAY', 4, CURRENT_DATE()), false, 5, 2);
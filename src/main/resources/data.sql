-- =======================================
-- GEBRUIKERS
-- =======================================
INSERT INTO users
    (email, username, password, role)
VALUES
    ('villavredestein@gmail.com', 'Admin',
     '$2a$10$SKodiL2.Mb6IZG7/Xzgh3O5JXFQZcNs/91o/MGSXmmt2E/97o9Nii', 'ADMIN'),
    ('ikheetalvar@gmail.com', 'Alvar',
     '$2y$10$f.uYDURDl4qwpisty/XrR.ofByjHgIYRoxo9MD6NmI4ydzDKPMjlG', 'STUDENT'),
    ('desmondstaal@gmail.com', 'Desmond',
     '$2y$10$f.uYDURDl4qwpisty/XrR.ofByjHgIYRoxo9MD6NmI4ydzDKPMjlG', 'STUDENT'),
    ('medocstaal@gmail.com', 'Medoc',
     '$2y$10$f.uYDURDl4qwpisty/XrR.ofByjHgIYRoxo9MD6NmI4ydzDKPMjlG', 'STUDENT'),
    ('simontalsma2@gmail.com', 'Simon',
     '$2y$10$f.uYDURDl4qwpisty/XrR.ofByjHgIYRoxo9MD6NmI4ydzDKPMjlG', 'STUDENT'),
    ('cleaner', 'Cleaner',
     '$2y$10$PLlkWJLXVcgREF1r91h7v.US8xmX/lriMrIrAT.ePPby7N7tMSNOC', 'CLEANER');



-- =======================================
-- KAMERS
-- =======================================
INSERT INTO rooms
    (name, occupant_id)
VALUES
    ('Japan', NULL),
    ('Argentinië', (SELECT id FROM users WHERE email='simontalsma2@gmail.com')),
    ('Thailand', (SELECT id FROM users WHERE email='desmondstaal@gmail.com')),
    ('Italië', NULL),
    ('Frankrijk', (SELECT id FROM users WHERE email='medocstaal@gmail.com')),
    ('Oekraïne', (SELECT id FROM users WHERE email='ikheetalvar@gmail.com'));

-- =======================================
-- DOCUMENTEN
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
-- BETALINGEN
-- =======================================
INSERT INTO payments
    (amount, date, status, description, student_id)
VALUES
    (350.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), 'PAID',
    'Huur - Alvar (vorige maand)', (SELECT id FROM users WHERE email='ikheetalvar@gmail.com')),
    (350.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), 'PAID',
    'Huur - Medoc (vorige maand)', (SELECT id FROM users WHERE email='medocstaal@gmail.com')),
    (350.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), 'PAID',
    'Huur - Simon (vorige maand)', (SELECT id FROM users WHERE email='simontalsma2@gmail.com')),
    (350.00, CURRENT_TIMESTAMP, 'OPEN',
    'Huur - Desmond (huidige maand)', (SELECT id FROM users WHERE email='desmondstaal@gmail.com'));

-- =======================================
-- FACTUREN
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
-- SCHOONMAAKTAKEN – WEEK 1
-- =======================================
INSERT INTO cleaning_tasks
(week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access)
VALUES
    (1, 'Keuken & vaatwasser verzorgen',
     'Leeg de vaatwasser, maak het aanrecht schoon en reinig de oven.',
     FALSE, 'Controleer filter vaatwasser', NULL,
     (SELECT id FROM users WHERE email='simontalsma2@gmail.com'), 'ALL'),
    (1, 'Badkamer & toilet poetsen',
     'Maak wastafel, douche, spiegel en toilet grondig schoon en droog.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='ikheetalvar@gmail.com'), 'ALL'),
    (1, 'Afval buiten zetten',
     'Scheiding: GFT, PMD, papier/karton, restafval, statiegeld.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='desmondstaal@gmail.com'), 'ALL'),
    (1, 'Woonkamer stofzuigen & dweilen',
     'Stofzuig en dweil woonkamer, hal en trap.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='medocstaal@gmail.com'), 'ALL');

-- =======================================
-- SCHOONMAAKTAKEN – WEEK 2
-- =======================================
INSERT INTO cleaning_tasks
(week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access)
VALUES
    (2, 'Keuken & vaatwasser verzorgen',
     'Leeg de vaatwasser, maak het aanrecht schoon en reinig de oven.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='ikheetalvar@gmail.com'), 'ALL'),
    (2, 'Badkamer & toilet poetsen',
     'Maak wastafel, douche, spiegel en toilet grondig schoon en droog.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='desmondstaal@gmail.com'), 'ALL'),
    (2, 'Afval buiten zetten',
     'Scheiding: GFT, PMD, papier/karton, restafval, statiegeld.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='medocstaal@gmail.com'), 'ALL'),
    (2, 'Woonkamer stofzuigen & dweilen',
     'Stofzuig en dweil woonkamer, hal en trap.',
     FALSE, NULL, NULL,
     (SELECT id FROM users WHERE email='simontalsma2@gmail.com'), 'ALL');

-- =======================================
-- VIEW
-- =======================================
CREATE OR REPLACE VIEW view_users AS
SELECT
    u.id,
    u.username AS name,
    u.email,
    u.role,
    r.name AS room_name
FROM users u
         LEFT JOIN rooms r ON r.occupant_id = u.id
ORDER BY u.role, u.username;
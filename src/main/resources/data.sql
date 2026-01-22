-- ==================================================
-- Villa Vredestein
-- ==================================================

BEGIN;

-- ==================================================
-- USERS
-- ==================================================

-- Admin
INSERT INTO users (email, username, password, role)
VALUES
  ('admin@villavredestein.test', 'Admin', '$2a$10$SKodiL2.Mb6IZG7/Xzgh3O5JXFQZcNs/91o/MGSXmmt2E/97o9Nii', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

-- Students
INSERT INTO users (email, username, password, role)
VALUES
  ('alvar@villavredestein.test',   'Alvar',   '$2y$10$f.uYDURDl4qwpisty/XrR.ofByjHgIYRoxo9MD6NmI4ydzDKPMjlG', 'STUDENT'),
  ('desmond@villavredestein.test', 'Desmond', '$2y$10$f.uYDURDl4qwpisty/XrR.ofByjHgIYRoxo9MD6NmI4ydzDKPMjlG', 'STUDENT'),
  ('medoc@villavredestein.test',   'Medoc',   '$2y$10$f.uYDURDl4qwpisty/XrR.ofByjHgIYRoxo9MD6NmI4ydzDKPMjlG', 'STUDENT'),
  ('simon@villavredestein.test',   'Simon',   '$2y$10$f.uYDURDl4qwpisty/XrR.ofByjHgIYRoxo9MD6NmI4ydzDKPMjlG', 'STUDENT'),
  ('arwen@villavredestein.test',   'Arwen',   '$2y$10$f.uYDURDl4qwpisty/XrR.ofByjHgIYRoxo9MD6NmI4ydzDKPMjlG', 'STUDENT')
ON CONFLICT (email) DO NOTHING;

-- Cleaner
INSERT INTO users (email, username, password, role)
VALUES
  ('cleaner@villavredestein.test', 'Cleaner', '$2y$10$PLlkWJLXVcgREF1r91h7v.US8xmX/lriMrIrAT.ePPby7N7tMSNOC', 'CLEANER')
ON CONFLICT (email) DO NOTHING;

-- ==================================================
-- ROOMS
-- ==================================================

INSERT INTO rooms (name, occupant_id)
VALUES
  ('Japan', NULL),
  ('Argentinië', NULL),
  ('Thailand', NULL),
  ('Italië', NULL),
  ('Frankrijk', NULL),
  ('Oekraïne', NULL)
ON CONFLICT (name) DO NOTHING;

UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'simon@villavredestein.test')
WHERE name = 'Argentinië'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'simon@villavredestein.test'));

UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'desmond@villavredestein.test')
WHERE name = 'Thailand'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'desmond@villavredestein.test'));

UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'arwen@villavredestein.test')
WHERE name = 'Italië'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'arwen@villavredestein.test'));

UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'medoc@villavredestein.test')
WHERE name = 'Frankrijk'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'medoc@villavredestein.test'));

UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'alvar@villavredestein.test')
WHERE name = 'Oekraïne'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'alvar@villavredestein.test'));

-- ==================================================
-- DOCUMENTS
-- ==================================================

INSERT INTO documents (title, description, storage_path, role_access, uploaded_by_id)
VALUES
  ('Huisregels Villa Vredestein', 'Overview of house rules and code of conduct.', 'uploads/Huisregels.pdf', 'ALL',
   (SELECT id FROM users WHERE email = 'admin@villavredestein.test')),
  ('Veiligheidsinstructies', 'Fire safety and emergency procedures.', 'uploads/Veiligheid.pdf', 'ALL',
   (SELECT id FROM users WHERE email = 'admin@villavredestein.test')),
  ('Pensionovereenkomst', 'Rental agreement template.', 'uploads/Pensionovereenkomst.pdf', 'STUDENT',
   (SELECT id FROM users WHERE email = 'admin@villavredestein.test'))
ON CONFLICT (storage_path) DO NOTHING;

-- ==================================================
-- PAYMENTS
-- ==================================================

INSERT INTO payments (amount, created_at, paid_at, status, description, student_id)
SELECT
  350.00,
  NOW() - INTERVAL '1 month',
  NOW() - INTERVAL '1 month',
  'PAID',
  'Rent - Alvar (previous month)',
  (SELECT id FROM users WHERE email = 'alvar@villavredestein.test')
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE description = 'Rent - Alvar (previous month)');

INSERT INTO payments (amount, created_at, paid_at, status, description, student_id)
SELECT
  350.00,
  NOW() - INTERVAL '1 month',
  NOW() - INTERVAL '1 month',
  'PAID',
  'Rent - Medoc (previous month)',
  (SELECT id FROM users WHERE email = 'medoc@villavredestein.test')
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE description = 'Rent - Medoc (previous month)');

INSERT INTO payments (amount, created_at, paid_at, status, description, student_id)
SELECT
  350.00,
  NOW() - INTERVAL '1 month',
  NOW() - INTERVAL '1 month',
  'PAID',
  'Rent - Simon (previous month)',
  (SELECT id FROM users WHERE email = 'simon@villavredestein.test')
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE description = 'Rent - Simon (previous month)');

INSERT INTO payments (amount, created_at, paid_at, status, description, student_id)
SELECT
  350.00,
  NOW(),
  NULL,
  'OPEN',
  'Rent - Desmond (current month)',
  (SELECT id FROM users WHERE email = 'desmond@villavredestein.test')
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE description = 'Rent - Desmond (current month)');

-- ==================================================
-- INVOICES
-- ==================================================

INSERT INTO invoices (title, description, amount, issue_date, due_date, invoice_month, invoice_year, reminder_count, status, student_id)
SELECT
  'Rent current month - Alvar',
  'Monthly rent invoice - Alvar',
  350.00,
  CURRENT_DATE,
  CURRENT_DATE + INTERVAL '14 day',
  CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int),
  CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS int),
  0,
  'OPEN',
  (SELECT id FROM users WHERE email = 'alvar@villavredestein.test')
WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE title = 'Rent current month - Alvar');

INSERT INTO invoices (title, description, amount, issue_date, due_date, invoice_month, invoice_year, reminder_count, status, student_id)
SELECT
  'Rent current month - Desmond',
  'Monthly rent invoice - Desmond',
  350.00,
  CURRENT_DATE,
  CURRENT_DATE + INTERVAL '14 day',
  CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int),
  CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS int),
  0,
  'OPEN',
  (SELECT id FROM users WHERE email = 'desmond@villavredestein.test')
WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE title = 'Rent current month - Desmond');

INSERT INTO invoices (title, description, amount, issue_date, due_date, invoice_month, invoice_year, reminder_count, status, student_id)
SELECT
  'Rent current month - Medoc',
  'Monthly rent invoice - Medoc',
  350.00,
  CURRENT_DATE,
  CURRENT_DATE + INTERVAL '14 day',
  CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int),
  CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS int),
  0,
  'OPEN',
  (SELECT id FROM users WHERE email = 'medoc@villavredestein.test')
WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE title = 'Rent current month - Medoc');

INSERT INTO invoices (title, description, amount, issue_date, due_date, invoice_month, invoice_year, reminder_count, status, student_id)
SELECT
  'Rent current month - Simon',
  'Monthly rent invoice - Simon',
  350.00,
  CURRENT_DATE,
  CURRENT_DATE + INTERVAL '14 day',
  CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int),
  CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS int),
  0,
  'OPEN',
  (SELECT id FROM users WHERE email = 'simon@villavredestein.test')
WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE title = 'Rent current month - Simon');

-- ==================================================
-- CLEANING TASKS – WEEK 1 + 2
-- ==================================================

-- Week 1
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access)
SELECT
  1,
  'Kitchen & dishwasher',
  'Empty the dishwasher, clean the countertop and clean the oven & induction plate.',
  FALSE,
  'Check dishwasher filter',
  NULL,
  (SELECT id FROM users WHERE email = 'simon@villavredestein.test'),
  'ALL'
WHERE NOT EXISTS (
  SELECT 1 FROM cleaning_tasks WHERE week_number = 1 AND name = 'Kitchen & dishwasher'
);

INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access)
SELECT
  1,
  'Bathroom & toilet',
  'Clean the sink, shower, mirror and toilet thoroughly and dry everything.',
  FALSE,
  NULL,
  NULL,
  (SELECT id FROM users WHERE email = 'alvar@villavredestein.test'),
  'ALL'
WHERE NOT EXISTS (
  SELECT 1 FROM cleaning_tasks WHERE week_number = 1 AND name = 'Bathroom & toilet'
);

INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access)
SELECT
  1,
  'Take out the trash',
  'Separate: organic, plastics/metal, paper/cardboard, residual waste, deposit bottles = shared pot.',
  FALSE,
  NULL,
  NULL,
  (SELECT id FROM users WHERE email = 'desmond@villavredestein.test'),
  'ALL'
WHERE NOT EXISTS (
  SELECT 1 FROM cleaning_tasks WHERE week_number = 1 AND name = 'Take out the trash'
);

INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access)
SELECT
  1,
  'Hallway & stairs vacuum/mop',
  'Vacuum and mop the hallway and stairs. Keep the dining table clean and tidy.',
  FALSE,
  NULL,
  NULL,
  (SELECT id FROM users WHERE email = 'medoc@villavredestein.test'),
  'ALL'
WHERE NOT EXISTS (
  SELECT 1 FROM cleaning_tasks WHERE week_number = 1 AND name = 'Hallway & stairs vacuum/mop'
);

-- Week 2
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access)
SELECT
  2,
  'Kitchen & dishwasher',
  'Empty the dishwasher, clean the countertop and clean the oven.',
  FALSE,
  NULL,
  NULL,
  (SELECT id FROM users WHERE email = 'alvar@villavredestein.test'),
  'ALL'
WHERE NOT EXISTS (
  SELECT 1 FROM cleaning_tasks WHERE week_number = 2 AND name = 'Kitchen & dishwasher'
);

INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access)
SELECT
  2,
  'Bathroom & toilet',
  'Clean the sink, shower, mirror and toilet thoroughly and dry everything.',
  FALSE,
  NULL,
  NULL,
  (SELECT id FROM users WHERE email = 'desmond@villavredestein.test'),
  'ALL'
WHERE NOT EXISTS (
  SELECT 1 FROM cleaning_tasks WHERE week_number = 2 AND name = 'Bathroom & toilet'
);

INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access)
SELECT
  2,
  'Take out the trash',
  'Separate: organic, plastics/metal, paper/cardboard, residual waste, deposit bottles.',
  FALSE,
  NULL,
  NULL,
  (SELECT id FROM users WHERE email = 'medoc@villavredestein.test'),
  'ALL'
WHERE NOT EXISTS (
  SELECT 1 FROM cleaning_tasks WHERE week_number = 2 AND name = 'Take out the trash'
);

INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access)
SELECT
  2,
  'Living room vacuum/mop',
  'Vacuum and mop the living room, hallway and stairs and keep the table clean and tidy.',
  FALSE,
  NULL,
  NULL,
  (SELECT id FROM users WHERE email = 'simon@villavredestein.test'),
  'ALL'
WHERE NOT EXISTS (
  SELECT 1 FROM cleaning_tasks WHERE week_number = 2 AND name = 'Living room vacuum/mop'
);

-- ==================================================
-- VIEW
-- ==================================================

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

COMMIT;

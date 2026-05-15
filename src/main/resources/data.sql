-- src/main/resources/data.sql

-- O 'ON CONFLICT DO NOTHING' impede que o Spring tente recriar o usuario se ele ja existir, evitando erros.
INSERT INTO "user" (name, enrollment_number, email, password, type)
VALUES
    ('Administrador do Sistema', '2355551352', 'admin@fluxo.com', '$2a$10$ZUna2AxA38Z3S6DxNuNKe.D2PVsq3476N0tuczAG.DUhBHuFGQbHi', 'ADMIN'),
    ('Aluno', '2355551353', 'aluno@fluxo.com', '$2a$10$DKDlXaWzCbEbIP9hPxvLyeipgAQE3nRz5vsNrVDrNt0Dbf1OLLSL6', 'STUDENT'),
    ('Professor', '2355551354', 'professor@fluxo.com', '$2a$10$xqtam99YRjJmfYAEHiwXoueQ/BLK5MKbu0v2ezeq02iJhN722xxaS', 'PROFESSOR')
ON CONFLICT (enrollment_number) DO NOTHING;

INSERT INTO project (id_user_teacher, name, description, status, period, observation, git_lab_link)
SELECT
    professor_user.id_user,
    'Projeto Exemplo',
    'Projeto seed para perfil do aluno',
    'ATIVO',
    '2026.1',
    'Projeto vinculado ao perfil seeded',
    'https://gitlab.com/fluxo/projeto-exemplo'
FROM "user" professor_user
WHERE professor_user.email = 'professor@fluxo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM project p
      WHERE p.name = 'Projeto Exemplo'
  );

INSERT INTO project (id_user_teacher, name, description, status, period, observation, git_lab_link)
SELECT
    professor_user.id_user,
    'Projeto Exemplo Secundario',
    'Projeto seed adicional para testar filtro de horas por projeto',
    'ATIVO',
    '2026.1',
    'Projeto adicional vinculado ao aluno seeded',
    'https://gitlab.com/fluxo/projeto-exemplo-secundario'
FROM "user" professor_user
WHERE professor_user.email = 'professor@fluxo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM project p
      WHERE p.name = 'Projeto Exemplo Secundario'
  );

INSERT INTO team (id_project, id_user_teacher)
SELECT
    project_seed.id_project,
    professor_user.id_user
FROM project project_seed
JOIN "user" professor_user ON professor_user.email = 'professor@fluxo.com'
WHERE project_seed.name = 'Projeto Exemplo'
  AND NOT EXISTS (
      SELECT 1
      FROM team t
      WHERE t.id_project = project_seed.id_project
  );

INSERT INTO team (id_project, id_user_teacher)
SELECT
    project_seed.id_project,
    professor_user.id_user
FROM project project_seed
JOIN "user" professor_user ON professor_user.email = 'professor@fluxo.com'
WHERE project_seed.name = 'Projeto Exemplo Secundario'
  AND NOT EXISTS (
      SELECT 1
      FROM team t
      WHERE t.id_project = project_seed.id_project
  );

INSERT INTO student_profile (ages_position, course, image_url, id_user_student, id_team)
SELECT
    3,
    'Computacao',
    '/uploads/avatars/aluno.png',
    student_user.id_user,
    team_seed.id_team
FROM "user" student_user
JOIN project project_seed ON project_seed.name = 'Projeto Exemplo'
JOIN team team_seed ON team_seed.id_project = project_seed.id_project
WHERE student_user.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM student_profile sp
      WHERE sp.id_user_student = student_user.id_user
  );
-- report RA com projeto ANDAMENTO
INSERT INTO project (id_user_teacher, name, description, status, period, observation, git_lab_link)
SELECT
    u.id_user,
    'ANDAMENTO',
    'Projeto seed para relatório de andamento',
    'ATIVO',
    '2026.1',
    NULL,
    'https://gitlab.com/fluxo/andamento'
FROM "user" u
WHERE u.email = 'professor@fluxo.com'
  AND NOT EXISTS (SELECT 1 FROM project p WHERE p.name = 'ANDAMENTO');

INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT 'RA', '2026-03-10', '2026-03-10', 8.5, u.id_user, p.id_project
FROM "user" u
JOIN project p ON p.name = 'ANDAMENTO'
WHERE u.email = 'aluno@fluxo.com'
  AND NOT EXISTS (SELECT 1 FROM report r WHERE r.id_project = p.id_project AND r.type = 'RA');

INSERT INTO report_review (id_report, comment, correction_url, revision_date)
SELECT r.id_report, 'Bom progresso', 'https://drive.com/andamento', '2026-03-12 10:00:00+00'
FROM report r
JOIN project p ON p.id_project = r.id_project
WHERE p.name = 'ANDAMENTO' AND r.type = 'RA'
  AND NOT EXISTS (SELECT 1 FROM report_review rr WHERE rr.id_report = r.id_report);

-- report RF com projeto FINAL
INSERT INTO project (id_user_teacher, name, description, status, period, observation, git_lab_link)
SELECT
    u.id_user,
    'FINAL',
    'Projeto seed para relatório final',
    'ATIVO',
    '2026.1',
    NULL,
    'https://gitlab.com/fluxo/final'
FROM "user" u
WHERE u.email = 'professor@fluxo.com'
  AND NOT EXISTS (SELECT 1 FROM project p WHERE p.name = 'FINAL');

INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT 'RF', '2026-06-10', '2026-06-10', 9.0, u.id_user, p.id_project
FROM "user" u
JOIN project p ON p.name = 'FINAL'
WHERE u.email = 'aluno@fluxo.com'
  AND NOT EXISTS (SELECT 1 FROM report r WHERE r.id_project = p.id_project AND r.type = 'RF');

INSERT INTO report_review (id_report, comment, correction_url, revision_date)
SELECT r.id_report, 'Excelente trabalho', 'https://drive.com/final', '2026-06-12 10:00:00+00'
FROM report r
JOIN project p ON p.id_project = r.id_project
WHERE p.name = 'FINAL' AND r.type = 'RF'
  AND NOT EXISTS (SELECT 1 FROM report_review rr WHERE rr.id_report = r.id_report);

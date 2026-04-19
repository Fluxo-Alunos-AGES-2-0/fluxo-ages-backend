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

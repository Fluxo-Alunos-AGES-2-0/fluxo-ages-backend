-- src/main/resources/data.sql

-- O 'ON CONFLICT DO NOTHING' impede que o Spring tente recriar o usuário se ele já existir, evitando erros.
INSERT INTO tb_user (name, enrollment_number, email, password, role) 
VALUES 
    -- 1. Administrador
    ('Administrador do Sistema', '2355551352', 'admin@fluxo.com', '$2a$10$ZUna2AxA38Z3S6DxNuNKe.D2PVsq3476N0tuczAG.DUhBHuFGQbHi', 'ADMIN'),
    
    -- 2. Aluno Principal
    ('Aluno', '2355551353', 'aluno@fluxo.com', '$2a$10$DKDlXaWzCbEbIP9hPxvLyeipgAQE3nRz5vsNrVDrNt0Dbf1OLLSL6', 'STUDENT'),
    
    -- 3. Professor
    ('Professor', '2355551354', 'professor@fluxo.com', '$2a$10$xqtam99YRjJmfYAEHiwXoueQ/BLK5MKbu0v2ezeq02iJhN722xxaS', 'PROFESSOR')
ON CONFLICT (enrollment_number) DO NOTHING;

INSERT INTO team (team_id)
SELECT 1
WHERE NOT EXISTS (
    SELECT 1
    FROM team
    WHERE team_id = 1
);

INSERT INTO student_profile (agpa_position, course, avatar_url, student_user_id, team_id)
SELECT
    '3',
    'Computacao',
    '/uploads/avatars/aluno.png',
    student_user.user_id,
    1
FROM tb_user student_user
WHERE student_user.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM student_profile sp
      WHERE sp.student_user_id = student_user.user_id
  );

INSERT INTO project (name, description, status, period, notes, technologies, practices, team_id, professor_user_id)
SELECT
    'Projeto Exemplo',
    'Projeto seed para perfil do aluno',
    'ATIVO',
    '2026.1',
    'Projeto vinculado ao perfil seeded',
    'Java, Spring Boot',
    'Scrum',
    1,
    professor_user.user_id
FROM tb_user professor_user
WHERE professor_user.email = 'professor@fluxo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM project p
      WHERE p.team_id = 1
  );

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
    4,
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

-- hours reports para testar historico e filtro por projeto
INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT
    'HOURS',
    DATE '2026-03-19',
    DATE '2026-03-19',
    NULL,
    student_user.id_user,
    project_seed.id_project
FROM "user" student_user
JOIN project project_seed ON project_seed.name = 'Projeto Exemplo'
WHERE student_user.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM report r
      WHERE r.type = 'HOURS'
        AND r.id_user_student = student_user.id_user
        AND r.id_project = project_seed.id_project
        AND r.create_date = DATE '2026-03-19'
  );

INSERT INTO hours_report (id_report, activities, status, entry_time, exit_time, total_time_seconds, rejection_justification)
SELECT
    seeded_report.id_report,
    'Desenvolvimento da tela de login.',
    'APPROVED',
    TIMESTAMPTZ '2026-03-19 18:00:00+00',
    TIMESTAMPTZ '2026-03-19 19:00:00+00',
    3600,
    NULL
FROM report seeded_report
JOIN "user" student_user ON student_user.id_user = seeded_report.id_user_student
JOIN project project_seed ON project_seed.id_project = seeded_report.id_project
WHERE student_user.email = 'aluno@fluxo.com'
  AND project_seed.name = 'Projeto Exemplo'
  AND seeded_report.type = 'HOURS'
  AND seeded_report.create_date = DATE '2026-03-19'
  AND NOT EXISTS (
      SELECT 1
      FROM hours_report hr
      WHERE hr.id_report = seeded_report.id_report
  );

INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT
    'HOURS',
    DATE '2026-03-21',
    DATE '2026-03-21',
    NULL,
    student_user.id_user,
    project_seed.id_project
FROM "user" student_user
JOIN project project_seed ON project_seed.name = 'Projeto Exemplo'
WHERE student_user.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM report r
      WHERE r.type = 'HOURS'
        AND r.id_user_student = student_user.id_user
        AND r.id_project = project_seed.id_project
        AND r.create_date = DATE '2026-03-21'
  );

INSERT INTO hours_report (id_report, activities, status, entry_time, exit_time, total_time_seconds, rejection_justification)
SELECT
    seeded_report.id_report,
    'Desenvolvimento de testes.',
    'PENDING',
    TIMESTAMPTZ '2026-03-21 18:00:00+00',
    TIMESTAMPTZ '2026-03-21 19:15:00+00',
    4500,
    NULL
FROM report seeded_report
JOIN "user" student_user ON student_user.id_user = seeded_report.id_user_student
JOIN project project_seed ON project_seed.id_project = seeded_report.id_project
WHERE student_user.email = 'aluno@fluxo.com'
  AND project_seed.name = 'Projeto Exemplo'
  AND seeded_report.type = 'HOURS'
  AND seeded_report.create_date = DATE '2026-03-21'
  AND NOT EXISTS (
      SELECT 1
      FROM hours_report hr
      WHERE hr.id_report = seeded_report.id_report
  );

INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT
    'HOURS',
    DATE '2026-03-25',
    DATE '2026-03-25',
    NULL,
    student_user.id_user,
    project_seed.id_project
FROM "user" student_user
JOIN project project_seed ON project_seed.name = 'Projeto Exemplo Secundario'
WHERE student_user.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM report r
      WHERE r.type = 'HOURS'
        AND r.id_user_student = student_user.id_user
        AND r.id_project = project_seed.id_project
        AND r.create_date = DATE '2026-03-25'
  );

INSERT INTO hours_report (id_report, activities, status, entry_time, exit_time, total_time_seconds, rejection_justification)
SELECT
    seeded_report.id_report,
    'Planejamento da segunda entrega.',
    'APPROVED',
    TIMESTAMPTZ '2026-03-25 18:00:00+00',
    TIMESTAMPTZ '2026-03-25 19:30:00+00',
    5400,
    NULL
FROM report seeded_report
JOIN "user" student_user ON student_user.id_user = seeded_report.id_user_student
JOIN project project_seed ON project_seed.id_project = seeded_report.id_project
WHERE student_user.email = 'aluno@fluxo.com'
  AND project_seed.name = 'Projeto Exemplo Secundario'
  AND seeded_report.type = 'HOURS'
  AND seeded_report.create_date = DATE '2026-03-25'
  AND NOT EXISTS (
      SELECT 1
      FROM hours_report hr
      WHERE hr.id_report = seeded_report.id_report
  );

INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT
    'HOURS',
    DATE '2026-03-27',
    DATE '2026-03-27',
    NULL,
    student_user.id_user,
    project_seed.id_project
FROM "user" student_user
JOIN project project_seed ON project_seed.name = 'Projeto Exemplo Secundario'
WHERE student_user.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM report r
      WHERE r.type = 'HOURS'
        AND r.id_user_student = student_user.id_user
        AND r.id_project = project_seed.id_project
        AND r.create_date = DATE '2026-03-27'
  );

INSERT INTO hours_report (id_report, activities, status, entry_time, exit_time, total_time_seconds, rejection_justification)
SELECT
    seeded_report.id_report,
    'Ajustes finais no modulo secundario.',
    'PENDING',
    TIMESTAMPTZ '2026-03-27 18:15:00+00',
    TIMESTAMPTZ '2026-03-27 19:00:00+00',
    2700,
    NULL
FROM report seeded_report
JOIN "user" student_user ON student_user.id_user = seeded_report.id_user_student
JOIN project project_seed ON project_seed.id_project = seeded_report.id_project
WHERE student_user.email = 'aluno@fluxo.com'
  AND project_seed.name = 'Projeto Exemplo Secundario'
  AND seeded_report.type = 'HOURS'
  AND seeded_report.create_date = DATE '2026-03-27'
  AND NOT EXISTS (
      SELECT 1
      FROM hours_report hr
      WHERE hr.id_report = seeded_report.id_report
  );

-- Projeto antigo

INSERT INTO project (
    id_user_teacher,
    name,
    description,
    status,
    period,
    observation,
    git_lab_link
)
SELECT
    u.id_user,
    'EduTrack',
    'Ferramenta de acompanhamento de aprendizagem para professores e alunos.',
    'CONCLUIDO',
    '2025.2',
    'Projeto concluído para histórico do aluno',
    'https://gitlab.com/fluxo/edutrack'
FROM "user" u
WHERE u.email = 'professor@fluxo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM project p
      WHERE p.name = 'EduTrack'
  );

INSERT INTO class (date)
SELECT '2025-08-01'
WHERE NOT EXISTS (SELECT 1 FROM class WHERE date = '2025-08-01');

INSERT INTO student_historic (id_user_student, id_project, semester_year, ages_level, student_status, grade, id_class)
SELECT u.id_user, p.id_project, '2025.2', 2, 'CONCLUIDO', 9.0,
       (SELECT id_class FROM class ORDER BY id_class DESC LIMIT 1)
FROM "user" u
JOIN project p ON p.name = 'EduTrack'
WHERE u.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1 FROM student_historic sh
      JOIN "user" u2 ON u2.id_user = sh.id_user_student
      JOIN project p2 ON p2.id_project = sh.id_project
      WHERE u2.email = 'aluno@fluxo.com' AND p2.name = 'EduTrack'
  );

  INSERT INTO student_historic (id_user_student, id_project, semester_year, ages_level, student_status, grade, id_class)
SELECT u.id_user, p.id_project, '2026.1', 2, 'CONCLUIDO', 8.0,
       (SELECT id_class FROM class ORDER BY id_class DESC LIMIT 1)
FROM "user" u
JOIN project p ON p.name = 'Projeto Exemplo Secundario'
WHERE u.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1 FROM student_historic sh
      JOIN "user" u2 ON u2.id_user = sh.id_user_student
      JOIN project p2 ON p2.id_project = sh.id_project
      WHERE u2.email = 'aluno@fluxo.com' AND p2.name = 'Projeto Exemplo Secundario'
  );


-- Relatório de Sprint (1)

INSERT INTO report (
    type,
    create_date,
    edit_date,
    grade,
    id_user_student,
    id_project
)
SELECT
    'SPRINT',
    DATE '2026-03-21',
    DATE '2026-03-21',
    8.5,
    u.id_user,
    p.id_project
FROM "user" u
JOIN project p ON p.name = 'Projeto Exemplo'
WHERE u.email = 'aluno@fluxo.com';

INSERT INTO sprint_report (
    id_report,
    sprint,
    predicted_activity,
    activity_completed,
    problems_encountered,
    learned_lessons,
    next_steps
)
SELECT
    r.id_report,
    '1',
    'Implementação da autenticação e dashboard inicial.',
    'Login, recuperação de senha e integração JWT concluídos.',
    'Ajustes de integração entre frontend e backend.',
    'Melhor compreensão do fluxo de autenticação com Spring Security.',
    'Iniciar desenvolvimento dos relatórios.'
FROM report r
WHERE r.type = 'SPRINT'
  AND r.create_date = DATE '2026-03-21'
  AND NOT EXISTS (
      SELECT 1
      FROM sprint_report sr
      WHERE sr.id_report = r.id_report
  );
  
-- Relatório de dSprint (2)
  INSERT INTO report (
    type,
    create_date,
    edit_date,
    grade,
    id_user_student,
    id_project
)
SELECT
    'SPRINT',
    DATE '2026-03-28',
    DATE '2026-03-28',
    9.0,
    u.id_user,
    p.id_project
FROM "user" u
JOIN project p ON p.name = 'Projeto Exemplo'
WHERE u.email = 'aluno@fluxo.com';

INSERT INTO sprint_report (
    id_report,
    sprint,
    predicted_activity,
    activity_completed,
    problems_encountered,
    learned_lessons,
    next_steps
)
SELECT
    r.id_report,
    '2',
    'Implementação das telas e endpoints de relatórios.',
    'Relatórios de horas, sprint, andamento e final implementados.',
    'Ajustes de modelagem envolvendo associação entre relatório e projeto.',
    'Padronização dos relatórios e integração de uploads.',
    'Refinar edição, exclusão e histórico de projetos.'
FROM report r
WHERE r.type = 'SPRINT'
  AND r.create_date = DATE '2026-03-28'
  AND NOT EXISTS (
      SELECT 1
      FROM sprint_report sr
      WHERE sr.id_report = r.id_report
  )
  ON CONFLICT DO NOTHING;
  

-- Relatório de Andamento

INSERT INTO report (
    type,
    create_date,
    edit_date,
    grade,
    id_user_student,
    id_project
)
SELECT
    'RA',
    DATE '2026-04-05',
    DATE '2026-04-05',
    8.5,
    u.id_user,
    p.id_project
FROM "user" u
JOIN project p ON p.name = 'Projeto Exemplo'
WHERE u.email = 'aluno@fluxo.com';

INSERT INTO report_review (
    id_report,
    comment,
    correction_url,
    revision_date
)
SELECT
    r.id_report,
    'Bom progresso. A estrutura dos relatórios está consistente, porém ainda faltam refinamentos na edição e exclusão.',
    'https://drive.com/ra-projeto-exemplo',
    TIMESTAMPTZ '2026-04-08 10:00:00+00'
FROM report r
WHERE r.type = 'RA'
  AND r.create_date = DATE '2026-04-05'
  ON CONFLICT DO NOTHING;

-- Relatório Final

INSERT INTO report (
    type,
    create_date,
    edit_date,
    grade,
    id_user_student,
    id_project
)
SELECT
    'RF',
    DATE '2026-06-20',
    DATE '2026-06-20',
    9.3,
    u.id_user,
    p.id_project
FROM "user" u
JOIN project p ON p.name = 'Projeto Exemplo'
WHERE u.email = 'aluno@fluxo.com';

INSERT INTO report_review (
    id_report,
    comment,
    correction_url,
    revision_date
)
SELECT
    r.id_report,
    'Excelente evolução durante o semestre. Entregas consistentes e boa participação no projeto.',
    'https://drive.com/rf-projeto-exemplo',
    TIMESTAMPTZ '2026-06-25 10:00:00+00'
FROM report r
WHERE r.type = 'RF'
  AND r.create_date = DATE '2026-06-20'
  ON CONFLICT DO NOTHING;

-- Horas do EduTrack (projeto antigo)

INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT 'HOURS', DATE '2025-09-10', DATE '2025-09-10', NULL, u.id_user, p.id_project
FROM "user" u JOIN project p ON p.name = 'EduTrack'
WHERE u.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1 FROM report r WHERE r.type = 'HOURS'
        AND r.id_user_student = u.id_user AND r.id_project = p.id_project
        AND r.create_date = DATE '2025-09-10'
  );

INSERT INTO hours_report (id_report, activities, status, entry_time, exit_time, total_time_seconds, rejection_justification)
SELECT r.id_report, 'Modelagem do banco de dados e setup inicial do projeto.', 'APPROVED',
    TIMESTAMPTZ '2025-09-10 18:00:00+00', TIMESTAMPTZ '2025-09-10 20:00:00+00', 7200, NULL
FROM report r JOIN "user" u ON u.id_user = r.id_user_student JOIN project p ON p.id_project = r.id_project
WHERE u.email = 'aluno@fluxo.com' AND p.name = 'EduTrack'
  AND r.type = 'HOURS' AND r.create_date = DATE '2025-09-10'
  AND NOT EXISTS (SELECT 1 FROM hours_report hr WHERE hr.id_report = r.id_report);

INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT 'HOURS', DATE '2025-09-18', DATE '2025-09-18', NULL, u.id_user, p.id_project
FROM "user" u JOIN project p ON p.name = 'EduTrack'
WHERE u.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1 FROM report r WHERE r.type = 'HOURS'
        AND r.id_user_student = u.id_user AND r.id_project = p.id_project
        AND r.create_date = DATE '2025-09-18'
  );

INSERT INTO hours_report (id_report, activities, status, entry_time, exit_time, total_time_seconds, rejection_justification)
SELECT r.id_report, 'Implementação das telas de login e cadastro.', 'APPROVED',
    TIMESTAMPTZ '2025-09-18 19:00:00+00', TIMESTAMPTZ '2025-09-18 20:30:00+00', 5400, NULL
FROM report r JOIN "user" u ON u.id_user = r.id_user_student JOIN project p ON p.id_project = r.id_project
WHERE u.email = 'aluno@fluxo.com' AND p.name = 'EduTrack'
  AND r.type = 'HOURS' AND r.create_date = DATE '2025-09-18'
  AND NOT EXISTS (SELECT 1 FROM hours_report hr WHERE hr.id_report = r.id_report);

INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT 'HOURS', DATE '2025-10-02', DATE '2025-10-02', NULL, u.id_user, p.id_project
FROM "user" u JOIN project p ON p.name = 'EduTrack'
WHERE u.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1 FROM report r WHERE r.type = 'HOURS'
        AND r.id_user_student = u.id_user AND r.id_project = p.id_project
        AND r.create_date = DATE '2025-10-02'
  );

INSERT INTO hours_report (id_report, activities, status, entry_time, exit_time, total_time_seconds, rejection_justification)
SELECT r.id_report, 'Integração com a API de notas e frequência.', 'REJECTED',
    TIMESTAMPTZ '2025-10-02 18:30:00+00', TIMESTAMPTZ '2025-10-02 19:30:00+00', 3600,
    'Atividade não condiz com o escopo do projeto.'
FROM report r JOIN "user" u ON u.id_user = r.id_user_student JOIN project p ON p.id_project = r.id_project
WHERE u.email = 'aluno@fluxo.com' AND p.name = 'EduTrack'
  AND r.type = 'HOURS' AND r.create_date = DATE '2025-10-02'
  AND NOT EXISTS (SELECT 1 FROM hours_report hr WHERE hr.id_report = r.id_report);

-- Sprints do EduTrack

INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT 'SPRINT', DATE '2025-09-12', DATE '2025-09-12', 7.5, u.id_user, p.id_project
FROM "user" u JOIN project p ON p.name = 'EduTrack'
WHERE u.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1 FROM report r WHERE r.type = 'SPRINT'
        AND r.id_user_student = u.id_user AND r.id_project = p.id_project
        AND r.create_date = DATE '2025-09-12'
  );

INSERT INTO sprint_report (id_report, sprint, predicted_activity, activity_completed, problems_encountered, learned_lessons, next_steps)
SELECT r.id_report, '1',
    'Setup do projeto e modelagem inicial.',
    'Banco modelado, repositório configurado e telas base criadas.',
    'Dificuldades com configuração do ambiente Docker.',
    'Importância de padronizar o ambiente de desenvolvimento desde o início.',
    'Implementar integração com API externa de notas.'
FROM report r JOIN "user" u ON u.id_user = r.id_user_student JOIN project p ON p.id_project = r.id_project
WHERE u.email = 'aluno@fluxo.com' AND p.name = 'EduTrack'
  AND r.type = 'SPRINT' AND r.create_date = DATE '2025-09-12'
  AND NOT EXISTS (SELECT 1 FROM sprint_report sr WHERE sr.id_report = r.id_report);

INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT 'SPRINT', DATE '2025-10-03', DATE '2025-10-03', 8.0, u.id_user, p.id_project
FROM "user" u JOIN project p ON p.name = 'EduTrack'
WHERE u.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1 FROM report r WHERE r.type = 'SPRINT'
        AND r.id_user_student = u.id_user AND r.id_project = p.id_project
        AND r.create_date = DATE '2025-10-03'
  );

INSERT INTO sprint_report (id_report, sprint, predicted_activity, activity_completed, problems_encountered, learned_lessons, next_steps)
SELECT r.id_report, '2',
    'Integração com API externa e tela de dashboard do professor.',
    'Dashboard implementado com dados mockados, integração parcial.',
    'API externa com documentação incompleta dificultou a integração.',
    'Sempre validar a qualidade da documentação de APIs externas antes de planejar.',
    'Finalizar integração e implementar tela de relatórios.'
FROM report r JOIN "user" u ON u.id_user = r.id_user_student JOIN project p ON p.id_project = r.id_project
WHERE u.email = 'aluno@fluxo.com' AND p.name = 'EduTrack'
  AND r.type = 'SPRINT' AND r.create_date = DATE '2025-10-03'
  AND NOT EXISTS (SELECT 1 FROM sprint_report sr WHERE sr.id_report = r.id_report);

-- Relatório Final do EduTrack

INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT 'RF', DATE '2025-11-28', DATE '2025-11-28', 9.0, u.id_user, p.id_project
FROM "user" u JOIN project p ON p.name = 'EduTrack'
WHERE u.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1 FROM report r WHERE r.type = 'RF'
        AND r.id_user_student = u.id_user AND r.id_project = p.id_project
  );

INSERT INTO report_review (id_report, comment, correction_url, revision_date)
SELECT r.id_report, 'Excelente trabalho', 'https://drive.com/final', '2026-06-12 10:00:00+00'
FROM report r
JOIN project p ON p.id_project = r.id_project
WHERE p.name = 'FINAL' AND r.type = 'RF'
  AND NOT EXISTS (SELECT 1 FROM report_review rr WHERE rr.id_report = r.id_report);

-- Sprint Report para Projeto Exemplo

INSERT INTO report (type, create_date, edit_date, grade, id_user_student, id_project)
SELECT
    'SPRINT',
    DATE '2026-04-05',
    DATE '2026-04-05',
    NULL,
    student_user.id_user,
    project_seed.id_project
FROM "user" student_user
JOIN project project_seed ON project_seed.name = 'Projeto Exemplo'
WHERE student_user.email = 'aluno@fluxo.com'
  AND NOT EXISTS (
      SELECT 1
      FROM report r
      WHERE r.type = 'SPRINT'
        AND r.id_user_student = student_user.id_user
        AND r.id_project = project_seed.id_project
        AND r.create_date = DATE '2026-04-05'
  );

INSERT INTO sprint_report (
    id_report,
    sprint,
    activity_completed,
    learned_lessons,
    next_steps,
    predicted_activity,
    problems_encountered
)
SELECT
    seeded_report.id_report,
    '01',
    'Implementação da autenticação e tela de login',
    'Aprendizado sobre Spring Security e JWT',
    'Finalizar cadastro de usuários',
    'Desenvolvimento da funcionalidade de recuperação de senha',
    'Dificuldades na configuração inicial do Spring Security'
FROM report seeded_report
JOIN "user" student_user
    ON student_user.id_user = seeded_report.id_user_student
JOIN project project_seed
    ON project_seed.id_project = seeded_report.id_project
WHERE student_user.email = 'aluno@fluxo.com'
  AND project_seed.name = 'Projeto Exemplo'
  AND seeded_report.type = 'SPRINT'
  AND seeded_report.create_date = DATE '2026-04-05'
  AND NOT EXISTS (
      SELECT 1
      FROM sprint_report sr
      WHERE sr.id_report = seeded_report.id_report
  );

INSERT INTO report_review (id_report, comment, correction_url, revision_date)
SELECT r.id_report,
    'Boa entrega final. O projeto atingiu os objetivos propostos com qualidade satisfatoria.',
    'https://drive.com/rf-edutrack',
    TIMESTAMPTZ '2025-12-02 10:00:00+00'
FROM report r JOIN "user" u ON u.id_user = r.id_user_student JOIN project p ON p.id_project = r.id_project
WHERE u.email = 'aluno@fluxo.com' AND p.name = 'EduTrack'
  AND r.type = 'RF'
  AND NOT EXISTS (SELECT 1 FROM report_review rr WHERE rr.id_report = r.id_report);

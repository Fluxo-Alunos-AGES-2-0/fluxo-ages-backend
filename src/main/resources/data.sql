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
    'EM_ANDAMENTO',
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
    'EM_ANDAMENTO',
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
    'https://i.pravatar.cc/300?img=12',
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

-- frequencia seedada para alinhar card e modal no dashboard
INSERT INTO class (date, date_time)
SELECT DATE '2026-03-13', '19:15 - 20:45'
WHERE NOT EXISTS (
    SELECT 1
    FROM class
    WHERE date = DATE '2026-03-13'
      AND date_time = '19:15 - 20:45'
);

INSERT INTO class (date, date_time)
SELECT DATE '2026-03-13', '21:00 - 22:30'
WHERE NOT EXISTS (
    SELECT 1
    FROM class
    WHERE date = DATE '2026-03-13'
      AND date_time = '21:00 - 22:30'
);

INSERT INTO class (date, date_time)
SELECT DATE '2026-03-20', '19:15 - 20:45'
WHERE NOT EXISTS (
    SELECT 1
    FROM class
    WHERE date = DATE '2026-03-20'
      AND date_time = '19:15 - 20:45'
);

INSERT INTO class (date, date_time)
SELECT DATE '2026-03-20', '21:00 - 22:30'
WHERE NOT EXISTS (
    SELECT 1
    FROM class
    WHERE date = DATE '2026-03-20'
      AND date_time = '21:00 - 22:30'
);

INSERT INTO class (date, date_time)
SELECT DATE '2026-03-27', '19:15 - 20:45'
WHERE NOT EXISTS (
    SELECT 1
    FROM class
    WHERE date = DATE '2026-03-27'
      AND date_time = '19:15 - 20:45'
);

INSERT INTO class (date, date_time)
SELECT DATE '2026-03-27', '21:00 - 22:30'
WHERE NOT EXISTS (
    SELECT 1
    FROM class
    WHERE date = DATE '2026-03-27'
      AND date_time = '21:00 - 22:30'
);

INSERT INTO lesson_session (date, id_class, id_user_teacher)
SELECT DATE '2026-03-13', class_seed.id_class, teacher_user.id_user
FROM class class_seed
JOIN "user" teacher_user ON teacher_user.email = 'professor@fluxo.com'
WHERE class_seed.date = DATE '2026-03-13'
  AND class_seed.date_time = '19:15 - 20:45'
  AND NOT EXISTS (
      SELECT 1
      FROM lesson_session lesson_session_seed
      WHERE lesson_session_seed.date = DATE '2026-03-13'
        AND lesson_session_seed.id_class = class_seed.id_class
        AND lesson_session_seed.id_user_teacher = teacher_user.id_user
  );

INSERT INTO lesson_session (date, id_class, id_user_teacher)
SELECT DATE '2026-03-13', class_seed.id_class, teacher_user.id_user
FROM class class_seed
JOIN "user" teacher_user ON teacher_user.email = 'professor@fluxo.com'
WHERE class_seed.date = DATE '2026-03-13'
  AND class_seed.date_time = '21:00 - 22:30'
  AND NOT EXISTS (
      SELECT 1
      FROM lesson_session lesson_session_seed
      WHERE lesson_session_seed.date = DATE '2026-03-13'
        AND lesson_session_seed.id_class = class_seed.id_class
        AND lesson_session_seed.id_user_teacher = teacher_user.id_user
  );

INSERT INTO lesson_session (date, id_class, id_user_teacher)
SELECT DATE '2026-03-20', class_seed.id_class, teacher_user.id_user
FROM class class_seed
JOIN "user" teacher_user ON teacher_user.email = 'professor@fluxo.com'
WHERE class_seed.date = DATE '2026-03-20'
  AND class_seed.date_time = '19:15 - 20:45'
  AND NOT EXISTS (
      SELECT 1
      FROM lesson_session lesson_session_seed
      WHERE lesson_session_seed.date = DATE '2026-03-20'
        AND lesson_session_seed.id_class = class_seed.id_class
        AND lesson_session_seed.id_user_teacher = teacher_user.id_user
  );

INSERT INTO lesson_session (date, id_class, id_user_teacher)
SELECT DATE '2026-03-20', class_seed.id_class, teacher_user.id_user
FROM class class_seed
JOIN "user" teacher_user ON teacher_user.email = 'professor@fluxo.com'
WHERE class_seed.date = DATE '2026-03-20'
  AND class_seed.date_time = '21:00 - 22:30'
  AND NOT EXISTS (
      SELECT 1
      FROM lesson_session lesson_session_seed
      WHERE lesson_session_seed.date = DATE '2026-03-20'
        AND lesson_session_seed.id_class = class_seed.id_class
        AND lesson_session_seed.id_user_teacher = teacher_user.id_user
  );

INSERT INTO lesson_session (date, id_class, id_user_teacher)
SELECT DATE '2026-03-27', class_seed.id_class, teacher_user.id_user
FROM class class_seed
JOIN "user" teacher_user ON teacher_user.email = 'professor@fluxo.com'
WHERE class_seed.date = DATE '2026-03-27'
  AND class_seed.date_time = '19:15 - 20:45'
  AND NOT EXISTS (
      SELECT 1
      FROM lesson_session lesson_session_seed
      WHERE lesson_session_seed.date = DATE '2026-03-27'
        AND lesson_session_seed.id_class = class_seed.id_class
        AND lesson_session_seed.id_user_teacher = teacher_user.id_user
  );

INSERT INTO lesson_session (date, id_class, id_user_teacher)
SELECT DATE '2026-03-27', class_seed.id_class, teacher_user.id_user
FROM class class_seed
JOIN "user" teacher_user ON teacher_user.email = 'professor@fluxo.com'
WHERE class_seed.date = DATE '2026-03-27'
  AND class_seed.date_time = '21:00 - 22:30'
  AND NOT EXISTS (
      SELECT 1
      FROM lesson_session lesson_session_seed
      WHERE lesson_session_seed.date = DATE '2026-03-27'
        AND lesson_session_seed.id_class = class_seed.id_class
        AND lesson_session_seed.id_user_teacher = teacher_user.id_user
  );

INSERT INTO attendance_record (id_attendance, status, id_lesson_session, id_user_student)
SELECT 'AT260313A1', 'PRESENTE', lesson_session_seed.id_lesson_session, student_user.id_user
FROM lesson_session lesson_session_seed
JOIN class class_seed ON class_seed.id_class = lesson_session_seed.id_class
JOIN "user" student_user ON student_user.email = 'aluno@fluxo.com'
WHERE lesson_session_seed.date = DATE '2026-03-13'
  AND class_seed.date_time = '19:15 - 20:45'
  AND NOT EXISTS (
      SELECT 1
      FROM attendance_record attendance_record_seed
      WHERE attendance_record_seed.id_user_student = student_user.id_user
        AND attendance_record_seed.id_lesson_session = lesson_session_seed.id_lesson_session
  );

INSERT INTO attendance_record (id_attendance, status, id_lesson_session, id_user_student)
SELECT 'AT260313A2', 'PRESENTE', lesson_session_seed.id_lesson_session, student_user.id_user
FROM lesson_session lesson_session_seed
JOIN class class_seed ON class_seed.id_class = lesson_session_seed.id_class
JOIN "user" student_user ON student_user.email = 'aluno@fluxo.com'
WHERE lesson_session_seed.date = DATE '2026-03-13'
  AND class_seed.date_time = '21:00 - 22:30'
  AND NOT EXISTS (
      SELECT 1
      FROM attendance_record attendance_record_seed
      WHERE attendance_record_seed.id_user_student = student_user.id_user
        AND attendance_record_seed.id_lesson_session = lesson_session_seed.id_lesson_session
  );

INSERT INTO attendance_record (id_attendance, status, id_lesson_session, id_user_student)
SELECT 'AT260320B1', 'AUSENTE', lesson_session_seed.id_lesson_session, student_user.id_user
FROM lesson_session lesson_session_seed
JOIN class class_seed ON class_seed.id_class = lesson_session_seed.id_class
JOIN "user" student_user ON student_user.email = 'aluno@fluxo.com'
WHERE lesson_session_seed.date = DATE '2026-03-20'
  AND class_seed.date_time = '19:15 - 20:45'
  AND NOT EXISTS (
      SELECT 1
      FROM attendance_record attendance_record_seed
      WHERE attendance_record_seed.id_user_student = student_user.id_user
        AND attendance_record_seed.id_lesson_session = lesson_session_seed.id_lesson_session
  );

INSERT INTO attendance_record (id_attendance, status, id_lesson_session, id_user_student)
SELECT 'AT260320B2', 'AUSENTE', lesson_session_seed.id_lesson_session, student_user.id_user
FROM lesson_session lesson_session_seed
JOIN class class_seed ON class_seed.id_class = lesson_session_seed.id_class
JOIN "user" student_user ON student_user.email = 'aluno@fluxo.com'
WHERE lesson_session_seed.date = DATE '2026-03-20'
  AND class_seed.date_time = '21:00 - 22:30'
  AND NOT EXISTS (
      SELECT 1
      FROM attendance_record attendance_record_seed
      WHERE attendance_record_seed.id_user_student = student_user.id_user
        AND attendance_record_seed.id_lesson_session = lesson_session_seed.id_lesson_session
  );

INSERT INTO attendance_record (id_attendance, status, id_lesson_session, id_user_student)
SELECT 'AT260327C1', 'PRESENTE', lesson_session_seed.id_lesson_session, student_user.id_user
FROM lesson_session lesson_session_seed
JOIN class class_seed ON class_seed.id_class = lesson_session_seed.id_class
JOIN "user" student_user ON student_user.email = 'aluno@fluxo.com'
WHERE lesson_session_seed.date = DATE '2026-03-27'
  AND class_seed.date_time = '19:15 - 20:45'
  AND NOT EXISTS (
      SELECT 1
      FROM attendance_record attendance_record_seed
      WHERE attendance_record_seed.id_user_student = student_user.id_user
        AND attendance_record_seed.id_lesson_session = lesson_session_seed.id_lesson_session
  );

INSERT INTO attendance_record (id_attendance, status, id_lesson_session, id_user_student)
SELECT 'AT260327C2', 'PRESENTE', lesson_session_seed.id_lesson_session, student_user.id_user
FROM lesson_session lesson_session_seed
JOIN class class_seed ON class_seed.id_class = lesson_session_seed.id_class
JOIN "user" student_user ON student_user.email = 'aluno@fluxo.com'
WHERE lesson_session_seed.date = DATE '2026-03-27'
  AND class_seed.date_time = '21:00 - 22:30'
  AND NOT EXISTS (
      SELECT 1
      FROM attendance_record attendance_record_seed
      WHERE attendance_record_seed.id_user_student = student_user.id_user
        AND attendance_record_seed.id_lesson_session = lesson_session_seed.id_lesson_session
  );

INSERT INTO class (date, date_time)
SELECT attendance_seed.slot_date, attendance_seed.slot_time
FROM (
    VALUES
        (DATE '2026-04-03', '19:15 - 20:45'),
        (DATE '2026-04-03', '21:00 - 22:30'),
        (DATE '2026-04-10', '19:15 - 20:45'),
        (DATE '2026-04-10', '21:00 - 22:30'),
        (DATE '2026-04-17', '19:15 - 20:45'),
        (DATE '2026-04-17', '21:00 - 22:30'),
        (DATE '2026-04-24', '19:15 - 20:45'),
        (DATE '2026-04-24', '21:00 - 22:30'),
        (DATE '2026-05-08', '19:15 - 20:45'),
        (DATE '2026-05-08', '21:00 - 22:30'),
        (DATE '2026-05-15', '19:15 - 20:45'),
        (DATE '2026-05-15', '21:00 - 22:30')
) AS attendance_seed(slot_date, slot_time)
WHERE NOT EXISTS (
    SELECT 1
    FROM class class_seed
    WHERE class_seed.date = attendance_seed.slot_date
      AND class_seed.date_time = attendance_seed.slot_time
);

INSERT INTO lesson_session (date, id_class, id_user_teacher)
SELECT attendance_seed.slot_date, class_seed.id_class, teacher_user.id_user
FROM (
    VALUES
        (DATE '2026-04-03', '19:15 - 20:45'),
        (DATE '2026-04-03', '21:00 - 22:30'),
        (DATE '2026-04-10', '19:15 - 20:45'),
        (DATE '2026-04-10', '21:00 - 22:30'),
        (DATE '2026-04-17', '19:15 - 20:45'),
        (DATE '2026-04-17', '21:00 - 22:30'),
        (DATE '2026-04-24', '19:15 - 20:45'),
        (DATE '2026-04-24', '21:00 - 22:30'),
        (DATE '2026-05-08', '19:15 - 20:45'),
        (DATE '2026-05-08', '21:00 - 22:30'),
        (DATE '2026-05-15', '19:15 - 20:45'),
        (DATE '2026-05-15', '21:00 - 22:30')
) AS attendance_seed(slot_date, slot_time)
JOIN class class_seed
    ON class_seed.date = attendance_seed.slot_date
   AND class_seed.date_time = attendance_seed.slot_time
JOIN "user" teacher_user
    ON teacher_user.email = 'professor@fluxo.com'
WHERE NOT EXISTS (
    SELECT 1
    FROM lesson_session lesson_session_seed
    WHERE lesson_session_seed.date = attendance_seed.slot_date
      AND lesson_session_seed.id_class = class_seed.id_class
      AND lesson_session_seed.id_user_teacher = teacher_user.id_user
);

INSERT INTO attendance_record (id_attendance, status, id_lesson_session, id_user_student)
SELECT attendance_seed.attendance_id, attendance_seed.status, lesson_session_seed.id_lesson_session, student_user.id_user
FROM (
    VALUES
        ('AT260403D1', DATE '2026-04-03', '19:15 - 20:45', 'PRESENTE'),
        ('AT260403D2', DATE '2026-04-03', '21:00 - 22:30', 'PRESENTE'),
        ('AT260410E1', DATE '2026-04-10', '19:15 - 20:45', 'PRESENTE'),
        ('AT260410E2', DATE '2026-04-10', '21:00 - 22:30', 'AUSENTE'),
        ('AT260417F1', DATE '2026-04-17', '19:15 - 20:45', 'PRESENTE'),
        ('AT260417F2', DATE '2026-04-17', '21:00 - 22:30', 'PRESENTE'),
        ('AT260424G1', DATE '2026-04-24', '19:15 - 20:45', 'AUSENTE'),
        ('AT260424G2', DATE '2026-04-24', '21:00 - 22:30', 'PRESENTE'),
        ('AT260508H1', DATE '2026-05-08', '19:15 - 20:45', 'PRESENTE'),
        ('AT260508H2', DATE '2026-05-08', '21:00 - 22:30', 'PRESENTE'),
        ('AT260515I1', DATE '2026-05-15', '19:15 - 20:45', 'AUSENTE'),
        ('AT260515I2', DATE '2026-05-15', '21:00 - 22:30', 'PRESENTE')
) AS attendance_seed(attendance_id, slot_date, slot_time, status)
JOIN class class_seed
    ON class_seed.date = attendance_seed.slot_date
   AND class_seed.date_time = attendance_seed.slot_time
JOIN lesson_session lesson_session_seed
    ON lesson_session_seed.date = attendance_seed.slot_date
   AND lesson_session_seed.id_class = class_seed.id_class
JOIN "user" student_user
    ON student_user.email = 'aluno@fluxo.com'
WHERE NOT EXISTS (
    SELECT 1
    FROM attendance_record attendance_record_seed
    WHERE attendance_record_seed.id_user_student = student_user.id_user
      AND attendance_record_seed.id_lesson_session = lesson_session_seed.id_lesson_session
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
SELECT u.id_user, p.id_project, 20252, 3, 'REGULAR', 9.0,
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

-- (Projeto Exemplo Secundario removido do historico do aluno: mapa limitado a 4 projetos, um por nivel AGES)


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
  

-- Relatorios de Andamento (RA) e Final (RF) do projeto atual nao sao seedados:
-- devem vir do upload do aluno (que substitui o anterior em vez de duplicar).

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
    '3',
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
  AND NOT EXISTS (SELECT 1 FROM report_review rr WHERE rr.id_report = r.id_report)
  ON CONFLICT DO NOTHING;


-- =====================================================================
-- SEED AMPLIADO: Mapa de Projetos (mais projetos, equipes e tecnologias)
-- =====================================================================

-- Estudantes adicionais (compoem as equipes dos projetos)
INSERT INTO "user" (name, enrollment_number, email, password, type)
VALUES
    ('Lucas Fernandes', '2300000001', 'lucas.fernandes@fluxo.com', '$2a$10$DKDlXaWzCbEbIP9hPxvLyeipgAQE3nRz5vsNrVDrNt0Dbf1OLLSL6', 'STUDENT'),
    ('Mariana Costa', '2300000002', 'mariana.costa@fluxo.com', '$2a$10$DKDlXaWzCbEbIP9hPxvLyeipgAQE3nRz5vsNrVDrNt0Dbf1OLLSL6', 'STUDENT'),
    ('Pedro Henrique', '2300000003', 'pedro.henrique@fluxo.com', '$2a$10$DKDlXaWzCbEbIP9hPxvLyeipgAQE3nRz5vsNrVDrNt0Dbf1OLLSL6', 'STUDENT'),
    ('Camila Rocha', '2300000004', 'camila.rocha@fluxo.com', '$2a$10$DKDlXaWzCbEbIP9hPxvLyeipgAQE3nRz5vsNrVDrNt0Dbf1OLLSL6', 'STUDENT'),
    ('Rafael Martins', '2300000005', 'rafael.martins@fluxo.com', '$2a$10$DKDlXaWzCbEbIP9hPxvLyeipgAQE3nRz5vsNrVDrNt0Dbf1OLLSL6', 'STUDENT'),
    ('Ana Beatriz', '2300000006', 'ana.beatriz@fluxo.com', '$2a$10$DKDlXaWzCbEbIP9hPxvLyeipgAQE3nRz5vsNrVDrNt0Dbf1OLLSL6', 'STUDENT'),
    ('Gabriel Souza', '2300000007', 'gabriel.souza@fluxo.com', '$2a$10$DKDlXaWzCbEbIP9hPxvLyeipgAQE3nRz5vsNrVDrNt0Dbf1OLLSL6', 'STUDENT'),
    ('Juliana Alves', '2300000008', 'juliana.alves@fluxo.com', '$2a$10$DKDlXaWzCbEbIP9hPxvLyeipgAQE3nRz5vsNrVDrNt0Dbf1OLLSL6', 'STUDENT')
ON CONFLICT (enrollment_number) DO NOTHING;

-- Catalogo de tecnologias
INSERT INTO technology (name)
VALUES
    ('React'), ('TypeScript'), ('Tailwind'), ('Spring Boot'), ('Node.js'),
    ('PostgreSQL'), ('Vue.js'), ('Angular'), ('Oracle'), ('Next.js'),
    ('NestJS'), ('Redis'), ('React Native'), ('Express'), ('MongoDB'),
    ('Flutter'), ('Django'), ('MySQL'), ('Docker')
ON CONFLICT (name) DO NOTHING;

-- Novos projetos (variando status, periodo e semestre)
INSERT INTO project (id_user_teacher, name, description, summary, status, period, semester_year, observation, git_lab_link, thumbnail_url)
SELECT
    prof.id_user, v.name, v.description, v.summary, v.status, v.period, v.semester_year,
    'Seed ampliado para o mapa de projetos', v.git_lab_link, v.thumbnail_url
FROM "user" prof
JOIN (VALUES
    ('ClinAgenda',
     'Sistema de agendamento online para clinicas e consultorios, com gestao de pacientes, agenda medica e notificacoes automaticas por e-mail e SMS.',
     'Agendamento online para clinicas com gestao de pacientes e notificacoes automaticas.',
     'CONCLUIDO', '2LM4LM', '2024/2', 'https://gitlab.com/fluxo/clinagenda',
     'https://images.unsplash.com/photo-1576091160550-2173dba999ef?q=80&w=1200&auto=format&fit=crop'),
    ('StockWise',
     'Gerenciador de estoque inteligente para pequenas e medias empresas, com controle de produtos, alertas de reposicao e integracao com notas fiscais.',
     'Gestao de estoque com alertas de reposicao e integracao com notas fiscais.',
     'CONCLUIDO', '2JK4JK', '2024/1', 'https://gitlab.com/fluxo/stockwise',
     'https://images.unsplash.com/photo-1553413077-190dd305871c?q=80&w=1200&auto=format&fit=crop'),
    ('AgendaMed',
     'Aplicativo de telemedicina com agendamento de consultas, prontuario eletronico e videochamadas integradas entre pacientes e profissionais de saude.',
     'Telemedicina com agendamento, prontuario eletronico e videochamadas.',
     'CONCLUIDO', '3MN5MN', '2025/1', 'https://gitlab.com/fluxo/agendamed',
     'https://images.unsplash.com/photo-1551076805-e1869033e561?q=80&w=1200&auto=format&fit=crop')
) AS v(name, description, summary, status, period, semester_year, git_lab_link, thumbnail_url) ON TRUE
WHERE prof.email = 'professor@fluxo.com'
  AND NOT EXISTS (SELECT 1 FROM project p WHERE p.name = v.name);

-- Completa thumbnail / semestre / resumo dos projetos ja existentes
UPDATE project SET
    summary = 'Plataforma web que centraliza a gestao academica e de portfolio dos alunos da AGES.',
    semester_year = '2026/1',
    thumbnail_url = 'https://images.unsplash.com/photo-1556761175-b413da4baf72?q=80&w=1200&auto=format&fit=crop'
WHERE name = 'Projeto Exemplo';

UPDATE project SET
    summary = 'Modulo complementar de testes e validacao da plataforma Fluxo AGES.',
    semester_year = '2026/1',
    thumbnail_url = 'https://images.unsplash.com/photo-1522071820081-009f0129c71c?q=80&w=1200&auto=format&fit=crop'
WHERE name = 'Projeto Exemplo Secundario';

UPDATE project SET
    summary = 'Ferramenta de acompanhamento de aprendizagem com dashboards de desempenho e relatorios pedagogicos.',
    semester_year = '2025/2',
    thumbnail_url = 'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?q=80&w=1200&auto=format&fit=crop'
WHERE name = 'EduTrack';

-- Vinculo projeto <-> tecnologias
INSERT INTO project_technology (id_project, id_technology)
SELECT p.id_project, t.id_technology
FROM (VALUES
    ('ClinAgenda','Vue.js'), ('ClinAgenda','Node.js'), ('ClinAgenda','PostgreSQL'),
    ('AgendaMed','React Native'), ('AgendaMed','Express'), ('AgendaMed','MongoDB'),
    ('Projeto Exemplo','React'), ('Projeto Exemplo','TypeScript'), ('Projeto Exemplo','Spring Boot'), ('Projeto Exemplo','PostgreSQL'),
    ('EduTrack','React'), ('EduTrack','Django'), ('EduTrack','MySQL')
) AS m(pname, tname)
JOIN project p ON p.name = m.pname
JOIN technology t ON t.name = m.tname
WHERE NOT EXISTS (
    SELECT 1 FROM project_technology pt
    WHERE pt.id_project = p.id_project AND pt.id_technology = t.id_technology
);

-- Equipes (student_historic): o aluno + colegas em cada projeto.
-- E o que faz o projeto aparecer no mapa e popula a contagem/lista de membros.
INSERT INTO student_historic (id_user_student, id_project, semester_year, ages_level, student_status, grade, id_class)
SELECT u.id_user, p.id_project, m.semester_year::smallint, m.ages_level, 'REGULAR', m.grade,
       (SELECT id_class FROM class ORDER BY id_class DESC LIMIT 1)
FROM (VALUES
    ('ClinAgenda','aluno@fluxo.com',20242,1,8.5),
    ('ClinAgenda','lucas.fernandes@fluxo.com',20242,1,8.0),
    ('ClinAgenda','mariana.costa@fluxo.com',20242,1,7.8),
    ('ClinAgenda','pedro.henrique@fluxo.com',20242,2,8.2),
    ('ClinAgenda','camila.rocha@fluxo.com',20242,2,9.0),
    ('ClinAgenda','rafael.martins@fluxo.com',20242,3,8.7),
    ('AgendaMed','aluno@fluxo.com',20251,2,8.6),
    ('AgendaMed','lucas.fernandes@fluxo.com',20251,2,8.1),
    ('AgendaMed','mariana.costa@fluxo.com',20251,2,7.9),
    ('AgendaMed','pedro.henrique@fluxo.com',20251,3,8.4),
    ('AgendaMed','camila.rocha@fluxo.com',20251,3,9.1),
    ('AgendaMed','rafael.martins@fluxo.com',20251,3,8.8),
    ('AgendaMed','ana.beatriz@fluxo.com',20251,1,7.7),
    ('EduTrack','camila.rocha@fluxo.com',20252,3,8.4),
    ('EduTrack','rafael.martins@fluxo.com',20252,3,8.6),
    ('EduTrack','ana.beatriz@fluxo.com',20252,2,7.9),
    ('EduTrack','gabriel.souza@fluxo.com',20252,2,8.0),
    ('Projeto Exemplo','aluno@fluxo.com',20261,4,9.0),
    ('Projeto Exemplo','lucas.fernandes@fluxo.com',20261,4,8.0),
    ('Projeto Exemplo','mariana.costa@fluxo.com',20261,3,7.8),
    ('Projeto Exemplo','pedro.henrique@fluxo.com',20261,3,7.6),
    ('Projeto Exemplo','juliana.alves@fluxo.com',20261,2,8.2)
) AS m(pname, email, semester_year, ages_level, grade)
JOIN project p ON p.name = m.pname
JOIN "user" u ON u.email = m.email
WHERE NOT EXISTS (
    SELECT 1 FROM student_historic sh
    WHERE sh.id_user_student = u.id_user AND sh.id_project = p.id_project
);

-- Seed de Agendamentos (US007)
INSERT INTO schedule (event, event_date, event_time, event_period)
SELECT seeded_schedule.event,
       seeded_schedule.event_date,
       seeded_schedule.event_time,
       seeded_schedule.event_period
FROM (
    VALUES
        ('Apresentação da AGES, projetos, orientadores e equipes. Apresentação e integração da equipe.', DATE '2026-03-02', TIME '17:30:00', 'JK_SEGQUA'),
        ('Apresentação pelo professor orientador do processo da AGES e do projeto. Preparação para a reunião com stakeholders.', DATE '2026-03-04', TIME '17:30:00', 'JK_SEGQUA'),
        ('Apresentação do projeto pelos stakeholders', DATE '2026-03-09', TIME '17:30:00', 'JK_SEGQUA'),
        ('Sprint 0 - Planejamento de user stories e dos mockups', DATE '2026-03-11', TIME '17:30:00', 'JK_SEGQUA'),
        ('Sprint 0 - Planejamento de user stories e dos mockups', DATE '2026-03-16', TIME '17:30:00', 'JK_SEGQUA'),
        ('Sprint 0 - Planejamento de user stories e dos mockups', DATE '2026-03-18', TIME '17:30:00', 'JK_SEGQUA'),
        ('Sprint 0 - Planejamento de user stories e dos mockups', DATE '2026-03-23', TIME '17:30:00', 'JK_SEGQUA'),
        ('Apresentação das user stories e mockups para os stakeholders e planning da Sprint 1', DATE '2026-03-25', TIME '17:30:00', 'JK_SEGQUA'),
        ('Desenvolvimento da Sprint 1', DATE '2026-03-30', TIME '17:30:00', 'JK_SEGQUA'),
        ('Desenvolvimento da Sprint 1', DATE '2026-04-01', TIME '17:30:00', 'JK_SEGQUA'),
        ('Revisão interna de backlog e refinamento da Sprint 1', DATE '2026-04-06', TIME '17:30:00', 'JK_SEGQUA'),
        ('Apresentação da Sprint 1 para stakeholders e planning da Sprint 2', DATE '2026-04-08', TIME '17:30:00', 'JK_SEGQUA'),
        ('Desenvolvimento da Sprint 2', DATE '2026-04-13', TIME '17:30:00', 'JK_SEGQUA'),
        ('Desenvolvimento da Sprint 2', DATE '2026-04-15', TIME '17:30:00', 'JK_SEGQUA'),
        ('Retrospectiva da Sprint 2 + ajustes de backlog', DATE '2026-04-20', TIME '17:30:00', 'JK_SEGQUA'),
        ('Apresentação da Sprint 2 para stakeholders e planning da Sprint 3', DATE '2026-04-22', TIME '17:30:00', 'JK_SEGQUA'),
        ('Desenvolvimento da Sprint 3', DATE '2026-04-27', TIME '17:30:00', 'JK_SEGQUA'),
        ('Desenvolvimento da Sprint 3', DATE '2026-04-29', TIME '17:30:00', 'JK_SEGQUA'),
        ('Workshop de arquitetura e integrações', DATE '2026-05-04', TIME '17:30:00', 'JK_SEGQUA'),
        ('Apresentação da Sprint 3 para stakeholders e planning da Sprint 4', DATE '2026-05-06', TIME '17:30:00', 'JK_SEGQUA'),
        ('Desenvolvimento da Sprint 4', DATE '2026-05-11', TIME '17:30:00', 'JK_SEGQUA'),
        ('Desenvolvimento da Sprint 4', DATE '2026-05-13', TIME '17:30:00', 'JK_SEGQUA'),
        ('Refinamento final e checklist de entrega', DATE '2026-05-18', TIME '17:30:00', 'JK_SEGQUA'),
        ('Entrega da Sprint 4 e retrospectiva', DATE '2026-05-20', TIME '17:30:00', 'JK_SEGQUA'),
        ('Desenvolvimento da Sprint 4', DATE '2026-06-18', TIME '17:30:00', 'JK_TERQUI'),
        ('Entrega FINAL do Projeto + Retrospectiva do Projeto + Entrega do Relatório da Sprint 4 no Fluxo AGES', DATE '2026-06-23', TIME '17:30:00', 'JK_TERQUI'),
        ('Retrospectiva GERAL AGES + Presença obrigatória + Entrega do Relatório Final (RF)', DATE '2026-06-25', TIME '17:30:00', 'JK_TERQUI'),
        ('Apresentação dos Projetos AGES para todos os times + Escolha do projeto destaque', DATE '2026-06-30', TIME '17:30:00', 'JK_TERQUI'),
        ('Reuniões one-to-one', DATE '2026-07-02', TIME '17:30:00', 'JK_TERQUI'),
        ('Reuniões one-to-one', DATE '2026-07-07', TIME '17:30:00', 'JK_TERQUI'),
        ('Reuniões one-to-one', DATE '2026-07-09', TIME '17:30:00', 'JK_TERQUI'),
        ('Acompanhamento de pendências pós-entrega', DATE '2026-07-14', TIME '17:30:00', 'JK_TERQUI'),
        ('Revisão final de documentação e memorial', DATE '2026-07-16', TIME '17:30:00', 'JK_TERQUI'),
        ('Mentoria técnica com orientadores', DATE '2026-07-21', TIME '17:30:00', 'JK_TERQUI'),
        ('Sessão de feedback com stakeholders', DATE '2026-07-23', TIME '17:30:00', 'JK_TERQUI'),
        ('Planejamento de melhorias contínuas', DATE '2026-07-28', TIME '17:30:00', 'JK_TERQUI'),
        ('Apresentação interna de lições aprendidas', DATE '2026-07-30', TIME '17:30:00', 'JK_TERQUI'),
        ('Apresentação da Sprint 3 para stakeholders e planning da Sprint 4', DATE '2026-06-29', TIME '19:00:00', 'LM_SEGQUA'),
        ('Retrospectiva da Sprint 3 + Entrega do Relatório da Sprint 3 no Fluxo AGES', DATE '2026-07-01', TIME '19:00:00', 'LM_SEGQUA'),
        ('Apresentação dos Projetos AGES para todos os times + Escolha do projeto destaque', DATE '2026-07-06', TIME '19:00:00', 'LM_SEGQUA'),
        ('Planejamento da Sprint 4 e alinhamento com orientadores', DATE '2026-07-08', TIME '19:00:00', 'LM_SEGQUA'),
        ('Desenvolvimento da Sprint 4', DATE '2026-07-13', TIME '19:00:00', 'LM_SEGQUA'),
        ('Desenvolvimento da Sprint 4', DATE '2026-07-15', TIME '19:00:00', 'LM_SEGQUA'),
        ('Revisão técnica do projeto e checklist de entrega', DATE '2026-07-20', TIME '19:00:00', 'LM_SEGQUA'),
        ('Apresentação parcial de progresso para stakeholders', DATE '2026-07-22', TIME '19:00:00', 'LM_SEGQUA'),
        ('Ajustes finais do projeto', DATE '2026-07-27', TIME '19:00:00', 'LM_SEGQUA'),
        ('Retrospectiva da Sprint 4 + preparação de entrega final', DATE '2026-07-29', TIME '19:00:00', 'LM_SEGQUA'),
        ('Entrega de documentação complementar', DATE '2026-08-03', TIME '19:00:00', 'LM_SEGQUA'),
        ('Ensaio de apresentação final', DATE '2026-08-05', TIME '19:00:00', 'LM_SEGQUA'),
        ('Apresentação final para banca interna', DATE '2026-08-10', TIME '19:00:00', 'LM_SEGQUA'),
        ('Revisão de feedback da banca', DATE '2026-08-12', TIME '19:00:00', 'LM_SEGQUA'),
        ('Planejamento de encerramento e checklist final', DATE '2026-08-17', TIME '19:00:00', 'LM_SEGQUA'),
        ('Entrega consolidada de artefatos', DATE '2026-08-19', TIME '19:00:00', 'LM_SEGQUA'),
        ('Entrega FINAL do Projeto + Retrospectiva do Projeto e o mais importante: PIZZA', DATE '2026-07-03', TIME '19:00:00', 'LMNP_SEXTA'),
        ('Sexta de acompanhamento com LM/NP', DATE '2026-07-10', TIME '19:00:00', 'LMNP_SEXTA'),
        ('Checkpoint semanal de pendências', DATE '2026-07-17', TIME '19:00:00', 'LMNP_SEXTA'),
        ('Acompanhamento de relatórios e entregas', DATE '2026-07-24', TIME '19:00:00', 'LMNP_SEXTA'),
        ('Fechamento de sprint e próximos passos', DATE '2026-07-31', TIME '19:00:00', 'LMNP_SEXTA'),
        ('Planejamento de agosto com LM/NP', DATE '2026-08-07', TIME '19:00:00', 'LMNP_SEXTA'),
        ('Sessão de revisão de entregas da semana', DATE '2026-08-14', TIME '19:00:00', 'LMNP_SEXTA'),
        ('Acompanhamento de ajustes finais', DATE '2026-08-21', TIME '19:00:00', 'LMNP_SEXTA'),
        ('Retrospectiva de encerramento do ciclo', DATE '2026-08-28', TIME '19:00:00', 'LMNP_SEXTA')
) AS seeded_schedule(event, event_date, event_time, event_period)
WHERE NOT EXISTS (
    SELECT 1
    FROM schedule existing_schedule
    WHERE existing_schedule.event = seeded_schedule.event
      AND existing_schedule.event_date = seeded_schedule.event_date
      AND existing_schedule.event_time = seeded_schedule.event_time
      AND existing_schedule.event_period = seeded_schedule.event_period
);

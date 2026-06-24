# Report Service Unit Tests

This document summarizes the new unit tests added for `ReportService` and `SprintReportService`.

## ReportService

- `uploadProgressReport`:
  - replaces an existing progress report when one already exists
  - creates a new progress report when none exists
- `uploadFinalReport`:
  - replaces only the current project's final report
  - keeps previous project reports untouched by only querying the current project
- `validateFile`:
  - throws `InvalidReportFileException` for empty files
  - throws `InvalidReportFileException` for invalid content type
  - throws `InvalidReportFileException` for files larger than 25MB

## SprintReportService

- `createSprintReport`:
  - creates a new sprint report when none exists for the authenticated student's current project and sprint
  - throws an exception when the authenticated student does not have a profile
  - throws an exception when the authenticated student does not have a current project
- `getMySprintReports`:
  - returns sprint reports ordered by sprint number
  - applies `projectId` filtering when provided

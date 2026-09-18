-- Hibernate validates int → INTEGER; MySQL TINYINT was rejected at ddl-auto=validate
ALTER TABLE doctor_schedules
    MODIFY COLUMN day_of_week INT NOT NULL COMMENT 'ISO: 1=Mon .. 7=Sun';

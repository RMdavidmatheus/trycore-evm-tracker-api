CREATE TABLE activities
(
    id                   UUID           NOT NULL,
    project_id           UUID           NOT NULL,
    name                 VARCHAR(150)   NOT NULL,
    planned_budget       DECIMAL(19, 4) NOT NULL,
    planned_percentage   DECIMAL(7, 4)  NOT NULL,
    completed_percentage DECIMAL(7, 4)  NOT NULL,
    actual_cost          DECIMAL(19, 4) NOT NULL,
    is_active            BOOLEAN        NOT NULL,
    created_at           TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at           TIMESTAMP(6) WITHOUT TIME ZONE,
    deleted_at           TIMESTAMP(6) WITHOUT TIME ZONE,
    CONSTRAINT pk_activities PRIMARY KEY (id)
);

CREATE TABLE projects
(
    id          UUID         NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    is_active   BOOLEAN      NOT NULL,
    created_at  TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP(6) WITHOUT TIME ZONE,
    deleted_at  TIMESTAMP(6) WITHOUT TIME ZONE,
    CONSTRAINT pk_projects PRIMARY KEY (id)
);

ALTER TABLE activities
    ADD CONSTRAINT FK_ACTIVITIES_ON_PROJECT FOREIGN KEY (project_id) REFERENCES projects (id);
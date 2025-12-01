-- MINDSOCCER - Schema initial
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE user_role AS ENUM ('PLAYER', 'REFEREE', 'AUTHOR', 'ADMIN');
CREATE TYPE match_status AS ENUM ('CREATED', 'LOBBY', 'IN_PROGRESS', 'PAUSED', 'FINISHED', 'CANCELLED');
CREATE TYPE team_side AS ENUM ('A', 'B');
CREATE TYPE round_type AS ENUM (
    -- 1ère Partie
    'SMASH_A', 'SMASH_B', 'CASCADE', 'PANIER', 'RELAIS', 'DUEL',
    'SAUT_PATRIOTIQUE', 'ECHAPPEE', 'ESTOCADE', 'MARATHON',
    -- 2ème Partie
    'JACKPOT', 'TRANSALT', 'CROSS_COUNTRY', 'CROSS_DICTIONARY',
    'TIRS_AU_BUT', 'CAPOEIRA', 'CIME', 'RANDONNEE_LEXICALE',
    'IDENTIFICATION', 'SPRINT_FINAL'
);

CREATE TABLE ms_user (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    handle VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'PLAYER',
    rating INTEGER DEFAULT 1000,
    country VARCHAR(3),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE team (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    captain_user_id UUID REFERENCES ms_user(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE match (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mode VARCHAR(50) NOT NULL DEFAULT 'CLASSIC',
    status match_status NOT NULL DEFAULT 'CREATED',
    rules_version VARCHAR(20) NOT NULL DEFAULT '1.0',
    region VARCHAR(50),
    referee_id UUID REFERENCES ms_user(id),
    team_a_score INTEGER DEFAULT 0,
    team_b_score INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE match_participant (
    match_id UUID NOT NULL REFERENCES match(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES ms_user(id),
    team_side team_side NOT NULL,
    is_captain BOOLEAN DEFAULT FALSE,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (match_id, user_id)
);

CREATE TABLE round (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    match_id UUID NOT NULL REFERENCES match(id) ON DELETE CASCADE,
    type round_type NOT NULL,
    round_index INTEGER NOT NULL,
    state_json JSONB DEFAULT '{}',
    theme_id UUID,
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE,
    timer_ms INTEGER DEFAULT 0
);

CREATE TABLE question (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rubric round_type NOT NULL,
    theme VARCHAR(100),
    locale VARCHAR(10) DEFAULT 'fr',
    difficulty INTEGER CHECK (difficulty BETWEEN 1 AND 5),
    format VARCHAR(50) DEFAULT 'TEXT',
    statement TEXT NOT NULL,
    choices_json JSONB,
    correct_answer TEXT NOT NULL,
    hints_json JSONB DEFAULT '[]',
    points_default INTEGER DEFAULT 10,
    source_ref TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_by UUID REFERENCES ms_user(id)
);

CREATE INDEX idx_question_rubric_theme ON question(rubric, theme, locale, difficulty);

CREATE TABLE round_question (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    round_id UUID NOT NULL REFERENCES round(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES question(id),
    question_index INTEGER NOT NULL,
    player_id UUID REFERENCES ms_user(id),
    team_side team_side,
    answer_given TEXT,
    is_correct BOOLEAN,
    points_awarded INTEGER DEFAULT 0,
    answered_at TIMESTAMP WITH TIME ZONE,
    time_ms INTEGER
);

CREATE TABLE penalty (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    match_id UUID NOT NULL REFERENCES match(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES ms_user(id),
    round_id UUID REFERENCES round(id),
    reason VARCHAR(255) NOT NULL,
    count INTEGER DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_penalty_match_user ON penalty(match_id, user_id);

CREATE TABLE suspension (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    match_id UUID NOT NULL REFERENCES match(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES ms_user(id),
    points_missed INTEGER NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ended_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE auction (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    round_id UUID NOT NULL REFERENCES round(id) ON DELETE CASCADE,
    team_side team_side NOT NULL,
    bid_points INTEGER NOT NULL,
    hint_level INTEGER DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE joker_use (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    round_id UUID NOT NULL REFERENCES round(id) ON DELETE CASCADE,
    helper_user_id UUID NOT NULL REFERENCES ms_user(id),
    target_user_id UUID NOT NULL REFERENCES ms_user(id),
    used_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE media (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type VARCHAR(50) NOT NULL,
    url TEXT NOT NULL,
    rights TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    match_id UUID REFERENCES match(id),
    user_id UUID REFERENCES ms_user(id),
    action VARCHAR(100) NOT NULL,
    payload JSONB,
    prev_hash VARCHAR(64),
    hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_audit_log_match ON audit_log(match_id, created_at);

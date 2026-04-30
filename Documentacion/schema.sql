CREATE DATABASE IF NOT EXISTS pointcheck_db;
USE pointcheck_db;

-- Table: users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(50) NOT NULL DEFAULT 'CLIENT',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_users_role (role),
    INDEX idx_users_active (active)
) ENGINE=InnoDB;

-- Table: professional_profiles
CREATE TABLE professional_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    display_name VARCHAR(150) NOT NULL,
    business_name VARCHAR(150),
    specialty VARCHAR(100),
    description TEXT,
    address VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(100),
    default_session_duration_minutes INT NOT NULL DEFAULT 60,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_prof_profile_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_prof_profiles_active (active)
) ENGINE=InnoDB;

-- Table: services
CREATE TABLE services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professional_profile_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10, 2),
    duration_minutes INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_services_prof_profile FOREIGN KEY (professional_profile_id) REFERENCES professional_profiles(id),
    INDEX idx_services_active (active)
) ENGINE=InnoDB;

-- Table: reservations
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    specialist_id BIGINT NOT NULL,
    service_id BIGINT,
    reservation_start DATETIME NOT NULL,
    reservation_end DATETIME,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(1000),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_reservations_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_reservations_specialist FOREIGN KEY (specialist_id) REFERENCES users(id),
    CONSTRAINT fk_reservations_service FOREIGN KEY (service_id) REFERENCES services(id),
    INDEX idx_reservations_start (reservation_start),
    INDEX idx_reservations_status (status)
) ENGINE=InnoDB;

-- Table: attentions
CREATE TABLE attentions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL UNIQUE,
    client_id BIGINT NOT NULL,
    specialist_id BIGINT NOT NULL,
    started_at DATETIME NOT NULL,
    finished_at DATETIME,
    duration_minutes INT,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    observations VARCHAR(2000),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_attentions_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_attentions_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_attentions_specialist FOREIGN KEY (specialist_id) REFERENCES users(id),
    INDEX idx_attentions_status (status)
) ENGINE=InnoDB;

-- Table: billing_records
CREATE TABLE billing_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    attention_id BIGINT,
    client_id BIGINT NOT NULL,
    specialist_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'CLP',
    payment_method VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    paid_at DATETIME,
    external_reference VARCHAR(255),
    notes VARCHAR(1000),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_billing_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_billing_attention FOREIGN KEY (attention_id) REFERENCES attentions(id),
    CONSTRAINT fk_billing_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_billing_specialist FOREIGN KEY (specialist_id) REFERENCES users(id),
    INDEX idx_billing_status (status)
) ENGINE=InnoDB;

-- Table: subscriptions
CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professional_profile_id BIGINT NOT NULL,
    plan_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_subscriptions_prof_profile FOREIGN KEY (professional_profile_id) REFERENCES professional_profiles(id),
    INDEX idx_subscriptions_status (status)
) ENGINE=InnoDB;

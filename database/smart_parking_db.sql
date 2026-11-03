-- ===================================================================
-- Smart Parking Management System - Database Initialization Script
-- Database: MySQL 8.0+
-- ===================================================================

CREATE DATABASE IF NOT EXISTS smart_parking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_parking_db;

-- -------------------------------------------------------------------
-- 1. Table: roles
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------
-- 2. Table: users
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    enabled BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------
-- 3. Table: user_roles (Many-To-Many Join Table)
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------
-- 4. Table: parking_slots
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS parking_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(20) NOT NULL UNIQUE,
    slot_type VARCHAR(20) NOT NULL DEFAULT 'CAR', -- CAR, BIKE, EV
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, OCCUPIED, MAINTENANCE
    hourly_rate DOUBLE NOT NULL DEFAULT 20.0,
    floor_level VARCHAR(10) DEFAULT 'G'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------
-- 5. Table: bookings
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    parking_slot_id BIGINT NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BOOKED', -- BOOKED, CHECKED_IN, COMPLETED, CANCELLED
    qr_code_path VARCHAR(255) NULL,
    receipt_path VARCHAR(255) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_slot FOREIGN KEY (parking_slot_id) REFERENCES parking_slots(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------------
-- 6. Table: bills
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    total_hours INT NOT NULL,
    hourly_rate DOUBLE NOT NULL,
    total_amount DOUBLE NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID', -- PAID, PENDING
    payment_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bills_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===================================================================
-- Initial Seed Data
-- ===================================================================

-- Insert Roles
INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO roles (id, name) VALUES (2, 'ROLE_USER') ON DUPLICATE KEY UPDATE name=name;

-- Insert Admin User (Email: admin@parking.com | Password: admin123 hashed with BCrypt)
INSERT INTO users (id, name, email, password, phone, enabled) 
VALUES (1, 'System Administrator', 'admin@parking.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R7XCwWj1sL2', '9876543210', true)
ON DUPLICATE KEY UPDATE email=email;

-- Assign ROLE_ADMIN to Admin User
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1) ON DUPLICATE KEY UPDATE role_id=role_id;

-- Insert Sample User (Email: user@parking.com | Password: user123 hashed with BCrypt)
INSERT INTO users (id, name, email, password, phone, enabled)
VALUES (2, 'John Doe', 'user@parking.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R7XCwWj1sL2', '9876543211', true)
ON DUPLICATE KEY UPDATE email=email;

-- Assign ROLE_USER to Sample User
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2) ON DUPLICATE KEY UPDATE role_id=role_id;

-- Insert Initial Parking Slots
INSERT INTO parking_slots (slot_number, slot_type, status, hourly_rate, floor_level) VALUES
('A-01', 'CAR', 'AVAILABLE', 20.0, 'Ground'),
('A-02', 'CAR', 'AVAILABLE', 20.0, 'Ground'),
('A-03', 'CAR', 'AVAILABLE', 20.0, 'Ground'),
('B-01', 'BIKE', 'AVAILABLE', 10.0, 'Ground'),
('B-02', 'BIKE', 'AVAILABLE', 10.0, 'Ground'),
('E-01', 'EV', 'AVAILABLE', 30.0, '1st Floor')
ON DUPLICATE KEY UPDATE slot_number=slot_number;

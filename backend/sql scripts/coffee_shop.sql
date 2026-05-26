-- Whistlestop Coffee Hut - Database Schema
-- Target: MySQL 8.0+

CREATE DATABASE IF NOT EXISTS coffee_shop1;
USE coffee_shop1;

-- Staff accounts (authenticated via JWT)
CREATE TABLE staff (
    staff_id          BIGINT NOT NULL AUTO_INCREMENT,
    username          VARCHAR(50) NOT NULL UNIQUE,
    staff_firstname   VARCHAR(50) NOT NULL,
    staff_lastname    VARCHAR(50) NOT NULL,
    staff_role        VARCHAR(50) NOT NULL,
    password_hash     CHAR(60) NOT NULL,
    PRIMARY KEY (staff_id)
);

-- Customer records (created at checkout)
CREATE TABLE customer (
    customer_id           BIGINT NOT NULL AUTO_INCREMENT,
    customer_firstname    VARCHAR(50) NOT NULL,
    customer_lastname     VARCHAR(50) NOT NULL,
    customer_phone_number CHAR(11) NOT NULL,
    PRIMARY KEY (customer_id)
);

-- Coffee shop stations (configurable opening hours)
CREATE TABLE station (
    station_id             BIGINT NOT NULL AUTO_INCREMENT,
    station_name           VARCHAR(100) NOT NULL,
    weekday_opening_hours  CHAR(11),
    saturday_opening_hours CHAR(11),
    closed_on_sunday       BOOLEAN DEFAULT 0,
    PRIMARY KEY (station_id)
);

-- Customer orders
CREATE TABLE purchase_order (
    order_id      BIGINT NOT NULL AUTO_INCREMENT,
    customer_id   BIGINT,
    station_id    BIGINT,
    staff_id      BIGINT,
    order_date    DATE,
    pickup_time   TIME,
    order_status  VARCHAR(20),
    is_archived   BOOLEAN DEFAULT 0,
    total_amount  DOUBLE,
    PRIMARY KEY (order_id),
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    FOREIGN KEY (station_id)  REFERENCES station (station_id),
    FOREIGN KEY (staff_id)    REFERENCES staff (staff_id)
);

-- Coffee menu items (e.g., Latte, Cappuccino)
CREATE TABLE menu_item (
    menu_item_id      BIGINT NOT NULL AUTO_INCREMENT,
    item_name         VARCHAR(100),
    item_description  VARCHAR(255),
    is_available      BOOLEAN DEFAULT 1,
    PRIMARY KEY (menu_item_id)
);

-- Size variants per menu item (REGULAR / LARGE with pricing)
CREATE TABLE menu_item_type (
    menu_item_type_id BIGINT NOT NULL AUTO_INCREMENT,
    menu_item_id      BIGINT,
    size_name         VARCHAR(10),
    price             DOUBLE,
    is_available      BOOLEAN DEFAULT 1,
    PRIMARY KEY (menu_item_type_id),
    FOREIGN KEY (menu_item_id) REFERENCES menu_item (menu_item_id)
);

-- Individual items within an order
CREATE TABLE order_item (
    purchase_order_id  BIGINT NOT NULL,
    menu_item_type_id  BIGINT NOT NULL,
    quantity           INT,
    unit_price         DOUBLE,
    line_total         DOUBLE,
    PRIMARY KEY (purchase_order_id, menu_item_type_id),
    FOREIGN KEY (purchase_order_id)  REFERENCES purchase_order (order_id),
    FOREIGN KEY (menu_item_type_id)  REFERENCES menu_item_type (menu_item_type_id)
);

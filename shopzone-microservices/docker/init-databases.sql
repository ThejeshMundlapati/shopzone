-- Create separate databases for each microservice
-- This runs on first PostgreSQL container creation

CREATE DATABASE shopzone_users;
CREATE DATABASE shopzone_orders;
CREATE DATABASE shopzone_payments;
CREATE DATABASE shopzone_notifications;

-- Grant access
GRANT ALL PRIVILEGES ON DATABASE shopzone_users TO shopzone_admin;
GRANT ALL PRIVILEGES ON DATABASE shopzone_orders TO shopzone_admin;
GRANT ALL PRIVILEGES ON DATABASE shopzone_payments TO shopzone_admin;
GRANT ALL PRIVILEGES ON DATABASE shopzone_notifications TO shopzone_admin;

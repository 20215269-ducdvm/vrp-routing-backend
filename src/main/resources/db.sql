CREATE TABLE vrp_problem (
                             id VARCHAR(36) PRIMARY KEY,
                             problem_type VARCHAR(10) NOT NULL, -- 'CVRP' or 'VRPTW'
                             customer_count INTEGER NOT NULL,
                             vehicle_capacity DECIMAL(10,2) NOT NULL,
                             vehicle_count INTEGER NOT NULL,
                             depot_lat DECIMAL(12,8) NOT NULL,
                             depot_lon DECIMAL(12,8) NOT NULL,
                             solomon_format TEXT, -- Dữ liệu solomon format
                             status VARCHAR(20) DEFAULT 'CREATED', -- CREATED, SOLVING, SOLVED, FAILED
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Khách hàng trong bài toán
CREATE TABLE vrp_customer (
                              id VARCHAR(36) PRIMARY KEY,
                              vrp_problem_id VARCHAR(36) NOT NULL,
                              customer_index INTEGER NOT NULL,
                              lat DECIMAL(12,8) NOT NULL,
                              lon DECIMAL(12,8) NOT NULL,
                              demand DECIMAL(10,2) NOT NULL,
                              service_time INTEGER DEFAULT 0, -- phút
                              time_window_start INTEGER, -- phút từ 0h, null = CVRP
                              time_window_end INTEGER,   -- phút từ 0h, null = CVRP

                              FOREIGN KEY (vrp_problem_id) REFERENCES vrp_problem(id) ON DELETE CASCADE
);

-- 3. Kết quả giải thuật
CREATE TABLE route_solution (
                                id VARCHAR(36) PRIMARY KEY,
                                vrp_problem_id VARCHAR(36) NOT NULL,
                                algorithm_name VARCHAR(50) NOT NULL,
                                total_cost DECIMAL(12,2),
                                total_distance DECIMAL(12,2),
                                vehicles_used INTEGER,
                                execution_time_seconds DECIMAL(8,2),
                                solution_data TEXT, -- JSON string of routes
                                status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, COMPLETED, FAILED
                                solved_at TIMESTAMP,

                                FOREIGN KEY (vrp_problem_id) REFERENCES vrp_problem(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_vrp_problem_type ON vrp_problem(problem_type);
CREATE INDEX idx_vrp_customer_problem ON vrp_customer(vrp_problem_id);
CREATE INDEX idx_route_solution_problem ON route_solution(vrp_problem_id);

-- ================================================
-- SAMPLE DATA
-- ================================================

-- Sample Problem
INSERT INTO vrp_problem (
    id, problem_type, customer_count, vehicle_capacity, vehicle_count,
    depot_lat, depot_lon, status
) VALUES (
             'vrp-sample-001',
             'VRPTW',
             3,
             100.0,
             2,
             10.754728,
             106.652314,
             'CREATED'
         );

-- Sample Customers
INSERT INTO vrp_customer (
    id, vrp_problem_id, customer_index, lat, lon, demand, service_time, time_window_start, time_window_end
) VALUES
      ('cust-001', 'vrp-sample-001', 1, 10.762622, 106.660172, 15.5, 10, 480, 600),
      ('cust-002', 'vrp-sample-001', 2, 10.775056, 106.702147, 20.0, 5, NULL, NULL),
      ('cust-003', 'vrp-sample-001', 3, 10.780123, 106.695234, 12.0, 8, 540, 720);
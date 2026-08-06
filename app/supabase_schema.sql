-- ==============================================================================
-- BALLY JUTE MILL - LORRY WEIGHMENT SYSTEM
-- SUPABASE DATABASE SCHEMA AND SQL QUERIES
-- Safe Execution: Uses IF NOT EXISTS to prevent modifying existing 64 tables.
-- ==============================================================================

-- Enable UUID extension if not enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ------------------------------------------------------------------------------
-- 1. MAIN TABLE: lorry_weighments
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.lorry_weighments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gate_pass VARCHAR(50) UNIQUE NOT NULL,
    entry_date VARCHAR(20) NOT NULL,
    type VARCHAR(10) DEFAULT 'IN',
    lorry_no VARCHAR(20) NOT NULL,
    chalan_no VARCHAR(50) NOT NULL,
    in_time VARCHAR(20) NOT NULL,
    party_name VARCHAR(100) NOT NULL,
    description VARCHAR(100) NOT NULL,
    quantity NUMERIC(10, 2) DEFAULT 0.0,
    unit VARCHAR(20) DEFAULT 'BALES',
    mokam VARCHAR(100),
    marka VARCHAR(100),
    
    -- Weighment Data Fields
    gate_gross_weight NUMERIC(12, 2) DEFAULT 0.0,
    gate_tare_weight NUMERIC(12, 2) DEFAULT 0.0,
    gate_net_weight NUMERIC(12, 2) DEFAULT 0.0,
    
    mill_gross_weight NUMERIC(12, 2) DEFAULT 0.0,
    mill_tare_weight NUMERIC(12, 2) DEFAULT 0.0,
    
    electric_gross_weight NUMERIC(12, 2) DEFAULT 0.0,
    electric_tare_weight NUMERIC(12, 2) DEFAULT 0.0,
    
    unload_status VARCHAR(50) DEFAULT 'Pending', -- 'Unloaded', 'Pending'
    status VARCHAR(50) NOT NULL DEFAULT 'Mill Weightment',
    
    -- Timestamps & Verification
    mill_remarks TEXT,
    out_date VARCHAR(20),
    out_time VARCHAR(20),
    out_remarks TEXT,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 2. CHILD ITEM TABLE: quality_items (Multi-item Challan Breakdown)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.quality_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gate_pass VARCHAR(50) NOT NULL REFERENCES public.lorry_weighments(gate_pass) ON DELETE CASCADE,
    quality VARCHAR(50) NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL,
    unit VARCHAR(20) NOT NULL DEFAULT 'BALES',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 3. INDEXES FOR HIGH-PERFORMANCE SEARCHING & DASHBOARDS
-- ------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_lorry_weighments_gate_pass ON public.lorry_weighments(gate_pass);
CREATE INDEX IF NOT EXISTS idx_lorry_weighments_lorry_no ON public.lorry_weighments(lorry_no);
CREATE INDEX IF NOT EXISTS idx_lorry_weighments_status ON public.lorry_weighments(status);
CREATE INDEX IF NOT EXISTS idx_quality_items_gate_pass ON public.quality_items(gate_pass);

-- ------------------------------------------------------------------------------
-- 4. AUTOMATIC TIMESTAMP TRIGGER
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_timestamp_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = NOW();
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS set_updated_at_lorry_weighments ON public.lorry_weighments;
CREATE TRIGGER set_updated_at_lorry_weighments
BEFORE UPDATE ON public.lorry_weighments
FOR EACH ROW
EXECUTE PROCEDURE update_timestamp_column();

-- ------------------------------------------------------------------------------
-- 5. ROW LEVEL SECURITY (RLS) POLICIES
-- Enable read/write access for Bally Jute Mill operations
-- ------------------------------------------------------------------------------
ALTER TABLE public.lorry_weighments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quality_items ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow anon select on lorry_weighments" ON public.lorry_weighments;
CREATE POLICY "Allow anon select on lorry_weighments" ON public.lorry_weighments FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow anon insert on lorry_weighments" ON public.lorry_weighments;
CREATE POLICY "Allow anon insert on lorry_weighments" ON public.lorry_weighments FOR INSERT WITH CHECK (true);

DROP POLICY IF EXISTS "Allow anon update on lorry_weighments" ON public.lorry_weighments;
CREATE POLICY "Allow anon update on lorry_weighments" ON public.lorry_weighments FOR UPDATE USING (true);

DROP POLICY IF EXISTS "Allow anon select on quality_items" ON public.quality_items;
CREATE POLICY "Allow anon select on quality_items" ON public.quality_items FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow anon insert on quality_items" ON public.quality_items;
CREATE POLICY "Allow anon insert on quality_items" ON public.quality_items FOR INSERT WITH CHECK (true);

-- ------------------------------------------------------------------------------
-- 6. SAMPLE INSERT DATA (Initial Lorry Records)
-- ------------------------------------------------------------------------------
INSERT INTO public.lorry_weighments (
    gate_pass, entry_date, type, lorry_no, chalan_no, in_time, 
    party_name, description, quantity, unit, gate_gross_weight, 
    gate_tare_weight, gate_net_weight, status, mokam, marka
) VALUES 
(
    'GP-20260801-00124', '01/08/2026', 'IN', 'WB23AB4567', 'CH-2026-1254', '10:45 AM',
    'Ambagan Traders', 'Jute', 120.00, 'BALES', 12580.00, 4580.00, 8000.00,
    'Mill Weightment', 'AMBAGAN', 'SHREE HARI'
),
(
    'GP-20260801-00123', '01/08/2026', 'IN', 'WB23CD7890', 'CH-2026-1253', '09:30 AM',
    'Shree Hari Jute', 'Jute Silver', 80.00, 'BALES', 11240.00, 4200.00, 7040.00,
    'Electric Weightment', 'BALLY', 'SHREE HARI'
),
(
    'GP-20260801-00122', '01/08/2026', 'IN', 'WB23EF1234', 'CH-2026-1252', '08:15 AM',
    'R.K. Traders', 'Jute', 100.00, 'BALES', 9800.00, 3800.00, 6000.00,
    'Mill Tare Pending', 'AMBAGAN', 'RK BRAND'
) ON CONFLICT (gate_pass) DO NOTHING;

-- Insert quality breakdown for GP-20260801-00124
INSERT INTO public.quality_items (gate_pass, quality, quantity, unit) VALUES
('GP-20260801-00124', 'TD5', 80.00, 'BALES'),
('GP-20260801-00124', 'WN3', 40.00, 'DRUMS')
ON CONFLICT DO NOTHING;

-- ==============================================================================
-- END OF SQL SCRIPT
-- ==============================================================================

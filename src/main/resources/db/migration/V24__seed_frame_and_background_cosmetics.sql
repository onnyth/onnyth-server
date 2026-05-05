-- V24: Add BACKGROUND and FRAME cosmetic categories and seed default free items
-- Extend the CosmeticCategory check constraint (if any) and seed rows

-- Default background colors (free)
INSERT INTO cosmetic_items (id, name, description, category, price, rarity, is_active, preview_url)
SELECT gen_random_uuid(), v.name, v.description, 'BACKGROUND', 0, 'FREE', true, v.preview_url
FROM (VALUES
    ('Deep Purple',     'Default deep purple background',  '#22162B'),
    ('Midnight Black',  'Sleek black background',          '#0D0D0D'),
    ('Ocean Dark',      'Deep ocean blue background',      '#0A1628'),
    ('Ember Dark',      'Dark ember red background',       '#1A0A0A'),
    ('Forest Dark',     'Deep forest green background',    '#0A1A0A')
) AS v(name, description, preview_url)
WHERE NOT EXISTS (
    SELECT 1 FROM cosmetic_items WHERE category = 'BACKGROUND' AND name = v.name
);

-- Default frames (free)
INSERT INTO cosmetic_items (id, name, description, category, price, rarity, is_active, preview_url)
SELECT gen_random_uuid(), v.name, v.description, 'FRAME', 0, 'FREE', true, null
FROM (VALUES
    ('Classic',   'Clean default white frame'),
    ('Gold Ring', 'Elegant gold rim frame'),
    ('Neon Pink', 'Vibrant neon pink glow frame'),
    ('Ice Blue',  'Crisp icy blue frame'),
    ('Void',      'Dark, mysterious void frame')
) AS v(name, description)
WHERE NOT EXISTS (
    SELECT 1 FROM cosmetic_items WHERE category = 'FRAME' AND name = v.name
);

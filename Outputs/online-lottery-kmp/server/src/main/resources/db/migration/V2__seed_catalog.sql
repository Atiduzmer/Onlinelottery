INSERT INTO lottery_games (code, name, category, sport_type, status, sales_enabled, display_order, config)
VALUES
    ('FOOTBALL', '竞彩足球', 'SPORTS', 'FOOTBALL', 'ACTIVE', true, 10, '{"theme":"green"}'),
    ('BASKETBALL', '竞彩篮球', 'SPORTS', 'BASKETBALL', 'ACTIVE', true, 20, '{"theme":"orange"}'),
    ('SUPER_LOTTO', '大乐透', 'NUMBER', NULL, 'ACTIVE', true, 30, '{"frontCount":5,"backCount":2}'),
    ('PICK_3', '排列三', 'NUMBER', NULL, 'ACTIVE', true, 40, '{"digits":3}'),
    ('PICK_5', '排列五', 'NUMBER', NULL, 'ACTIVE', true, 50, '{"digits":5}'),
    ('SEVEN_STAR', '七星彩', 'NUMBER', NULL, 'ACTIVE', true, 60, '{"digits":7}');

INSERT INTO lottery_play_types (game_id, code, name, unit_price_cents, max_multiple, rules)
SELECT id, 'WIN_DRAW_LOSE', '胜平负', 200, 99, '{"selections":["WIN","DRAW","LOSE"]}'
FROM lottery_games WHERE code = 'FOOTBALL';

INSERT INTO lottery_play_types (game_id, code, name, unit_price_cents, max_multiple, rules)
SELECT id, 'TOTAL_POINTS', '大小分', 200, 99, '{"lineRequired":true}'
FROM lottery_games WHERE code = 'BASKETBALL';

INSERT INTO lottery_play_types (game_id, code, name, unit_price_cents, max_multiple, rules)
SELECT id, 'STANDARD', '标准投注', 200, 99, '{"front":{"pick":5,"range":[1,35]},"back":{"pick":2,"range":[1,12]}}'
FROM lottery_games WHERE code = 'SUPER_LOTTO';

INSERT INTO lottery_play_types (game_id, code, name, unit_price_cents, max_multiple, rules)
SELECT id, 'DIRECT', '直选', 200, 99, jsonb_build_object('digits', (config->>'digits')::integer)
FROM lottery_games WHERE code IN ('PICK_3', 'PICK_5', 'SEVEN_STAR');

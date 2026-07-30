INSERT INTO users (user_no, nickname, status, kyc_status, risk_level)
VALUES ('LOCAL-0001', '彩运亨通', 'ACTIVE', 'VERIFIED', 'LOW');

INSERT INTO user_identities (
    user_id, real_name_encrypted, id_type, id_number_encrypted, id_number_hash, birth_date, status, verified_at
)
SELECT id, 'LOCAL_ENCRYPTED_NAME', 'LOCAL_SANDBOX', 'LOCAL_ENCRYPTED_ID', 'LOCAL-ID-HASH-0001', DATE '1990-01-01', 'VERIFIED', now()
FROM users WHERE user_no = 'LOCAL-0001';

INSERT INTO user_bank_cards (
    user_id, bank_code, card_token_encrypted, card_token_hash, card_last4, holder_name_encrypted, status, is_default
)
SELECT id, 'LOCAL_BANK', 'LOCAL_CARD_TOKEN', 'LOCAL-CARD-HASH-0001', '8888', 'LOCAL_ENCRYPTED_NAME', 'ACTIVE', true
FROM users WHERE user_no = 'LOCAL-0001';

INSERT INTO user_risk_profiles (user_id, risk_score, risk_level, flags, last_evaluated_at)
SELECT id, 10, 'LOW', '{"sandbox":true}', now() FROM users WHERE user_no = 'LOCAL-0001';

INSERT INTO user_growth_accounts (user_id, level_code, growth_points)
SELECT id, 'VIP3', 3520 FROM users WHERE user_no = 'LOCAL-0001';

INSERT INTO wallet_accounts (owner_type, owner_id, account_code, account_type, balance_cents)
SELECT 'USER', id, 'AVAILABLE', 'AVAILABLE', 888800 FROM users WHERE user_no = 'LOCAL-0001'
UNION ALL
SELECT 'USER', id, 'BETTING_FROZEN', 'BETTING_FROZEN', 0 FROM users WHERE user_no = 'LOCAL-0001'
UNION ALL
SELECT 'USER', id, 'PRIZE', 'PRIZE', 0 FROM users WHERE user_no = 'LOCAL-0001'
UNION ALL
SELECT 'USER', id, 'WITHDRAWAL_FROZEN', 'WITHDRAWAL_FROZEN', 0 FROM users WHERE user_no = 'LOCAL-0001';

INSERT INTO wallet_accounts (owner_type, owner_id, account_code, account_type, balance_cents)
VALUES
    ('PLATFORM', 1, 'PAYMENT_PENDING', 'PAYMENT_PENDING', 0),
    ('PLATFORM', 1, 'LOTTERY_SALES', 'LOTTERY_SALES', 0),
    ('PLATFORM', 1, 'PRIZE_SETTLEMENT', 'PRIZE_SETTLEMENT', 0),
    ('PLATFORM', 1, 'CHANNEL_CLEARING', 'CHANNEL_CLEARING', 0);

INSERT INTO coupon_templates (
    template_code, name, discount_type, discount_cents, minimum_stake_cents, valid_days, status
)
VALUES
    ('LOCAL-5', '满20减5元', 'FIXED', 500, 2000, 365, 'ACTIVE'),
    ('LOCAL-10', '满50减10元', 'FIXED', 1000, 5000, 365, 'ACTIVE');

INSERT INTO user_coupons (coupon_no, user_id, template_id, status, expires_at)
SELECT 'LOCAL-COUPON-001', u.id, c.id, 'AVAILABLE', now() + interval '365 days'
FROM users u, coupon_templates c WHERE u.user_no = 'LOCAL-0001' AND c.template_code = 'LOCAL-5'
UNION ALL
SELECT 'LOCAL-COUPON-002', u.id, c.id, 'AVAILABLE', now() + interval '365 days'
FROM users u, coupon_templates c WHERE u.user_no = 'LOCAL-0001' AND c.template_code = 'LOCAL-10';

INSERT INTO notifications (template_code, channel, title, content)
VALUES
    ('LOCAL-WELCOME', 'IN_APP', '欢迎使用本地沙盒', '充值、提现和投注会写入本机 PostgreSQL，不会产生真实资金交易。'),
    ('LOCAL-SAFETY', 'IN_APP', '理性购彩提醒', '请设置合理预算，量力而行。');

INSERT INTO user_notifications (notification_id, user_id, status)
SELECT n.id, u.id, 'DELIVERED'
FROM notifications n, users u
WHERE n.template_code IN ('LOCAL-WELCOME', 'LOCAL-SAFETY') AND u.user_no = 'LOCAL-0001';

INSERT INTO help_articles (category_code, slug, title, summary, content_markdown, display_order, status, published_at)
VALUES
    ('LOCAL', 'local-sandbox', '本地沙盒说明', '了解哪些操作会写入本机数据库', '本地沙盒使用测试资金，不连接真实支付与官方出票渠道。', 10, 'PUBLISHED', now()),
    ('BETTING', 'place-local-order', '如何测试投注', '选择号码或赛事后提交即可', '提交后会扣减测试余额，并在投注记录中生成本地测试票。', 20, 'PUBLISHED', now());

INSERT INTO referral_codes (owner_user_id, referral_code)
SELECT id, 'LOCAL8888' FROM users WHERE user_no = 'LOCAL-0001';

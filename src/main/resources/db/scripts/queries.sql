-- If you want to ensure existing values in users.role map to user_role, run an idempotent insert:
-- (Uncomment/run only if you have user rows and want to add missing role names into user_role)
INSERT INTO user_role (role)
SELECT DISTINCT TRIM(role)
FROM users
WHERE role IS NOT NULL
ON CONFLICT (role) DO NOTHING;

-- View roles:
SELECT * FROM user_role
ORDER BY role;

-- Check users with invalid roles (should return 0 rows if all good):
SELECT u.user_id, u.email, u.role
FROM users u
LEFT JOIN user_role r
ON u.role = r.role
WHERE r.role IS NULL;

select * from dev.user_role;
select * from dev.encryption_keys;
select * from dev.users;
-- delete from dev.users where user_id=9;
select * from dev.user_settings;
select * from dev.holiday;
select * from dev.leave_policy;
select * from dev.user_leave;
select * from dev.user_leave_balance;
select * from dev.office_visit;
-- delete from dev.office_visit;
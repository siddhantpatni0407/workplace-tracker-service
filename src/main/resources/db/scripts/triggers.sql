-- Trigger for INSERT
CREATE TRIGGER users_insert_trigger
AFTER INSERT ON users
FOR EACH ROW
EXECUTE FUNCTION log_user_changes();

-- Trigger for UPDATE
CREATE TRIGGER users_update_trigger
AFTER UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION log_user_changes();

-- Trigger for DELETE
CREATE TRIGGER users_delete_trigger
AFTER DELETE ON users
FOR EACH ROW
EXECUTE FUNCTION log_user_changes();

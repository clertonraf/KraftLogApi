-- Create password_reset_tokens table
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Create index for faster token lookup
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);

-- Create index for faster user lookup
CREATE INDEX idx_password_reset_user_id ON password_reset_tokens(user_id);

-- Create index for expiry date cleanup
CREATE INDEX idx_password_reset_expiry_date ON password_reset_tokens(expiry_date);

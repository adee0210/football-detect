-- Tạo extension UUID nếu chưa có
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tạo bảng roles
CREATE TABLE IF NOT EXISTS roles (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(20) NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng users
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(100),
  enabled BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng user_roles để lưu trữ quan hệ many-to-many giữa users và roles
CREATE TABLE IF NOT EXISTS user_roles (
  user_id UUID NOT NULL,
  role_id UUID NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Tạo bảng verification_tokens
CREATE TABLE IF NOT EXISTS verification_tokens (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL,
  token VARCHAR(255) NOT NULL UNIQUE,
  expiry_date TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tạo bảng videos
CREATE TABLE IF NOT EXISTS videos (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  video_type VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',
  
  -- Fields cho UPLOADED videos
  file_path VARCHAR(255),
  file_size BIGINT,
  duration INTEGER,
  thumbnail_path VARCHAR(255),
  processed_path VARCHAR(255),
  
  -- Fields cho YOUTUBE videos
  youtube_url VARCHAR(255),
  youtube_video_id VARCHAR(20),
  
  -- Fields chung
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  is_downloadable BOOLEAN NOT NULL DEFAULT TRUE,
  metadata JSONB,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  
  -- Ràng buộc để đảm bảo video YouTube có URL
  CONSTRAINT youtube_video_url_required CHECK (video_type != 'YOUTUBE' OR youtube_url IS NOT NULL),
  
  -- Ràng buộc để đảm bảo video tải lên có file_path
  CONSTRAINT uploaded_video_path_required CHECK (video_type != 'UPLOADED' OR file_path IS NOT NULL)
);

-- Tạo các chỉ mục cho videos
CREATE INDEX idx_videos_user_id ON videos(user_id);
CREATE INDEX idx_videos_video_type ON videos(video_type);
CREATE INDEX idx_videos_youtube_video_id ON videos(youtube_video_id);
CREATE INDEX idx_videos_user_id_video_type ON videos(user_id, video_type);

-- Tạo bảng video_processing_status
CREATE TABLE IF NOT EXISTS video_processing_status (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  video_id UUID NOT NULL,
  status VARCHAR(20) NOT NULL,
  progress INTEGER DEFAULT 0,
  message TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE
);

-- Chèn dữ liệu ban đầu cho bảng roles
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN') ON CONFLICT DO NOTHING;

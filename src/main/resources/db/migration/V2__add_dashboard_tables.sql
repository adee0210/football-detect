-- Tạo bảng dashboard_stats để lưu trữ thông số tổng quan
CREATE TABLE IF NOT EXISTS dashboard_stats (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL,
  total_videos INTEGER NOT NULL DEFAULT 0,
  total_uploaded_videos INTEGER NOT NULL DEFAULT 0,
  total_youtube_videos INTEGER NOT NULL DEFAULT 0,
  total_storage_used BIGINT NOT NULL DEFAULT 0,
  last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tạo chỉ mục để tối ưu truy vấn
CREATE INDEX idx_dashboard_stats_user_id ON dashboard_stats(user_id); 
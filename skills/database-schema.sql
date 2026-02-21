-- =====================================================
-- DATABASE SCHEMA - HỆ THỐNG WEB ĐỊA PHƯƠNG
-- PostgreSQL 14+
-- Hybrid Approach: Relational + JSONB
-- =====================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- For full-text search

-- =====================================================
-- 1. AUTHENTICATION & USERS
-- =====================================================

CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  phone VARCHAR(20),
  avatar_url TEXT,
  
  -- OAuth fields (optional)
  google_id VARCHAR(255) UNIQUE,
  facebook_id VARCHAR(255) UNIQUE,
  provider VARCHAR(20) DEFAULT 'local', -- 'local', 'google', 'facebook'
  
  role VARCHAR(20) NOT NULL DEFAULT 'user', 
    -- 'user', 'owner', 'admin'
  is_active BOOLEAN DEFAULT true,
  email_verified_at TIMESTAMP,
  
  preferences JSONB DEFAULT '{}',
    -- Example: {"language": "vi", "notifications": true, "theme": "light"}
  
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_google ON users(google_id) WHERE google_id IS NOT NULL;
CREATE INDEX idx_users_facebook ON users(facebook_id) WHERE facebook_id IS NOT NULL;

-- Password Reset Tokens
CREATE TABLE password_reset_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token VARCHAR(255) UNIQUE NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_expires ON password_reset_tokens(expires_at);

-- =====================================================
-- 2. CATEGORIES (Danh mục quán)
-- =====================================================

CREATE TABLE categories (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  slug VARCHAR(100) UNIQUE NOT NULL,
  description TEXT,
  icon VARCHAR(50), -- Icon name or emoji
  display_order INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_categories_slug ON categories(slug);

-- =====================================================
-- 3. RESTAURANTS (Quán ăn)
-- =====================================================

CREATE TABLE restaurants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL,
  
  -- Basic Info
  name VARCHAR(255) NOT NULL,
  slug VARCHAR(255) UNIQUE NOT NULL,
  description TEXT,
  
  -- Address & Location
  address TEXT NOT NULL,
  ward VARCHAR(100),        -- Phường/Xã
  district VARCHAR(100) NOT NULL, -- Quận/Huyện
  city VARCHAR(100) NOT NULL,     -- Thành phố
  latitude DECIMAL(10, 8),  -- For Google Maps
  longitude DECIMAL(11, 8),
  
  -- Contact
  phone VARCHAR(20),
  email VARCHAR(255),
  website TEXT,
  
  -- Media
  cover_image TEXT,
  images JSONB DEFAULT '[]', 
    -- ["url1.jpg", "url2.jpg"]
  
  -- Business Info (JSONB for flexibility)
  opening_hours JSONB DEFAULT '{}',
    -- {"mon": "8:00-22:00", "tue": "8:00-22:00", "wed": "closed", ...}
  facilities JSONB DEFAULT '[]',
    -- ["wifi", "parking", "air_conditioner", "outdoor_seating"]
  price_range VARCHAR(10), 
    -- "$", "$$", "$$$" or "20k-50k"
  
  -- Status & Metrics
  status VARCHAR(20) DEFAULT 'pending',
    -- 'pending', 'approved', 'rejected', 'inactive'
  view_count INTEGER DEFAULT 0,
  avg_rating DECIMAL(3,2) DEFAULT 0, -- Denormalized from reviews
  review_count INTEGER DEFAULT 0,    -- Denormalized
  
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_restaurants_owner ON restaurants(owner_id);
CREATE INDEX idx_restaurants_category ON restaurants(category_id);
CREATE INDEX idx_restaurants_district ON restaurants(district);
CREATE INDEX idx_restaurants_city ON restaurants(city);
CREATE INDEX idx_restaurants_status ON restaurants(status);
CREATE INDEX idx_restaurants_slug ON restaurants(slug);
CREATE INDEX idx_restaurants_location ON restaurants(latitude, longitude);
CREATE INDEX idx_restaurants_rating ON restaurants(avg_rating DESC);

-- Full-text search index
CREATE INDEX idx_restaurants_name_search ON restaurants 
  USING gin(to_tsvector('english', name));
CREATE INDEX idx_restaurants_desc_search ON restaurants 
  USING gin(to_tsvector('english', description));

-- =====================================================
-- 4. MENU ITEMS (Món ăn)
-- =====================================================

CREATE TABLE menu_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
  
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(10, 2) NOT NULL,
  
  category VARCHAR(100), 
    -- "Món chính", "Món phụ", "Đồ uống", "Tráng miệng"
    -- Can be normalized to a separate table later
  
  image_url TEXT,
  
  -- Attributes (flexible JSONB)
  attributes JSONB DEFAULT '{}',
    -- {"spicy_level": 3, "vegetarian": true, "size": "large", "calories": 500}
  
  -- Status
  is_available BOOLEAN DEFAULT true,
  is_signature BOOLEAN DEFAULT false, -- Món đặc trưng
  display_order INTEGER DEFAULT 0,
  
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_menu_restaurant ON menu_items(restaurant_id);
CREATE INDEX idx_menu_price ON menu_items(price);
CREATE INDEX idx_menu_category ON menu_items(category);
CREATE INDEX idx_menu_available ON menu_items(is_available);
CREATE INDEX idx_menu_signature ON menu_items(is_signature);

-- Full-text search for menu items
CREATE INDEX idx_menu_name_search ON menu_items 
  USING gin(to_tsvector('english', name));

-- =====================================================
-- 5. REVIEWS (Đánh giá)
-- =====================================================

CREATE TABLE reviews (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  
  rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
  title VARCHAR(255),
  content TEXT NOT NULL,
  
  images JSONB DEFAULT '[]',
    -- ["review_img1.jpg", "review_img2.jpg"]
  
  visit_date DATE,
  
  -- Moderation
  status VARCHAR(20) DEFAULT 'approved',
    -- 'pending', 'approved', 'rejected'
  
  -- Metrics
  helpful_count INTEGER DEFAULT 0,
  
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  
  -- One review per user per restaurant (can be removed if multiple reviews allowed)
  UNIQUE(restaurant_id, user_id)
);

CREATE INDEX idx_reviews_restaurant ON reviews(restaurant_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_status ON reviews(status);
CREATE INDEX idx_reviews_created ON reviews(created_at DESC);

-- Review Reactions (Helpful/Not Helpful)
CREATE TABLE review_reactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  review_id UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  reaction_type VARCHAR(20) NOT NULL,
    -- 'helpful', 'not_helpful'
  created_at TIMESTAMP DEFAULT NOW(),
  
  UNIQUE(review_id, user_id)
);

CREATE INDEX idx_review_reactions_review ON review_reactions(review_id);
CREATE INDEX idx_review_reactions_user ON review_reactions(user_id);

-- Review Replies (Owner response)
CREATE TABLE review_replies (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  review_id UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    -- Usually owner, but can be admin
  content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_review_replies_review ON review_replies(review_id);
CREATE INDEX idx_review_replies_user ON review_replies(user_id);

-- =====================================================
-- 6. BLOGS
-- =====================================================

CREATE TABLE blogs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  author_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  
  title VARCHAR(255) NOT NULL,
  slug VARCHAR(255) UNIQUE NOT NULL,
  content TEXT NOT NULL,
  excerpt TEXT,
  cover_image TEXT,
  images JSONB DEFAULT '[]',
  
  tags JSONB DEFAULT '[]', 
    -- ["địa phương", "ẩm thực", "du lịch"]
  
  status VARCHAR(20) DEFAULT 'pending',
    -- 'draft', 'pending', 'published', 'rejected'
  
  view_count INTEGER DEFAULT 0,
  like_count INTEGER DEFAULT 0,
  
  published_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_blogs_author ON blogs(author_id);
CREATE INDEX idx_blogs_status ON blogs(status);
CREATE INDEX idx_blogs_published ON blogs(published_at DESC);
CREATE INDEX idx_blogs_slug ON blogs(slug);

CREATE INDEX idx_blogs_title_search ON blogs 
  USING gin(to_tsvector('english', title));

-- Blog Comments
CREATE TABLE blog_comments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  blog_id UUID NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  parent_id UUID REFERENCES blog_comments(id) ON DELETE CASCADE,
    -- NULL for root comments, has value for replies
  content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_blog_comments_blog ON blog_comments(blog_id);
CREATE INDEX idx_blog_comments_parent ON blog_comments(parent_id);
CREATE INDEX idx_blog_comments_user ON blog_comments(user_id);

-- =====================================================
-- 7. BOOKMARKS (Yêu thích)
-- =====================================================

CREATE TABLE bookmarks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
  notes TEXT, -- User's personal notes
  created_at TIMESTAMP DEFAULT NOW(),
  
  UNIQUE(user_id, restaurant_id)
);

CREATE INDEX idx_bookmarks_user ON bookmarks(user_id);
CREATE INDEX idx_bookmarks_restaurant ON bookmarks(restaurant_id);

-- =====================================================
-- 8. CHATBOT CONVERSATION HISTORY
-- =====================================================

CREATE TABLE chat_conversations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    -- Can be NULL for anonymous users
  session_id VARCHAR(255) NOT NULL, -- Session tracking
  
  metadata JSONB DEFAULT '{}',
    -- {"user_agent": "...", "ip": "..."}
  
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_chat_conv_user ON chat_conversations(user_id);
CREATE INDEX idx_chat_conv_session ON chat_conversations(session_id);

CREATE TABLE chat_messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL REFERENCES chat_conversations(id) ON DELETE CASCADE,
  
  role VARCHAR(20) NOT NULL, -- 'user', 'assistant', 'system'
  content TEXT NOT NULL,
  
  intent VARCHAR(100), -- Detected intent: 'search_restaurant', 'ask_menu', etc.
  entities JSONB DEFAULT '{}',
    -- {"dish": "phở", "district": "quận 1", "price_max": 50000}
  
  restaurant_results JSONB DEFAULT '[]',
    -- Store restaurant IDs that were suggested
  
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_chat_msg_conv ON chat_messages(conversation_id);
CREATE INDEX idx_chat_msg_created ON chat_messages(created_at);

-- =====================================================
-- 9. TRIGGERS FOR AUTO-UPDATE
-- =====================================================

-- Auto update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_restaurants_updated_at BEFORE UPDATE ON restaurants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_menu_items_updated_at BEFORE UPDATE ON menu_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_reviews_updated_at BEFORE UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_blogs_updated_at BEFORE UPDATE ON blogs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Auto update restaurant avg_rating and review_count
CREATE OR REPLACE FUNCTION update_restaurant_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE restaurants
    SET 
        avg_rating = (
            SELECT COALESCE(AVG(rating), 0)
            FROM reviews
            WHERE restaurant_id = NEW.restaurant_id
            AND status = 'approved'
        ),
        review_count = (
            SELECT COUNT(*)
            FROM reviews
            WHERE restaurant_id = NEW.restaurant_id
            AND status = 'approved'
        )
    WHERE id = NEW.restaurant_id;
    
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_restaurant_rating_on_insert
    AFTER INSERT ON reviews
    FOR EACH ROW EXECUTE FUNCTION update_restaurant_rating();

CREATE TRIGGER update_restaurant_rating_on_update
    AFTER UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION update_restaurant_rating();

-- =====================================================
-- 10. SAMPLE DATA (Optional)
-- =====================================================

-- Insert default categories
INSERT INTO categories (name, slug, description, icon, display_order) VALUES
('Quán ăn sáng', 'quan-an-sang', 'Phở, bún, bánh mì, cháo...', '🥖', 1),
('Quán cơm', 'quan-com', 'Cơm tấm, cơm văn phòng, cơm niêu...', '🍚', 2),
('Quán nhậu', 'quan-nhau', 'Nhậu, bia tươi, lẩu, nướng...', '🍺', 3),
('Quán cafe', 'quan-cafe', 'Cafe, trà sữa, nước ép...', '☕', 4),
('Quán ăn vặt', 'quan-an-vat', 'Kem, chè, bánh ngọt...', '🍰', 5);

-- =====================================================
-- MIGRATION NOTES
-- =====================================================

/*
MIGRATION PATH (khi cần scale):

1. Menu Category Normalization:
   - Tạo table menu_categories
   - Migrate menu_items.category -> menu_items.category_id

2. Facilities Normalization:
   - Tạo table restaurant_facilities
   - Migrate restaurants.facilities JSONB -> junction table

3. Opening Hours Table:
   - Tạo table opening_hours_slots cho complex schedules
   
4. Search Optimization:
   - Migrate to Elasticsearch cho advanced search
   - Keep PostgreSQL for transactional data

5. Separate Menu Items Order History:
   - Add table menu_item_orders nếu cần track món nào được order nhiều
*/

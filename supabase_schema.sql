-- ====================================================================
-- BrainRotWatch - Supabase PostgreSQL Database Schema
-- Paste and run this script in your Supabase SQL Editor.
-- ====================================================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- --------------------------------------------------------------------
-- 1. USERS TABLE (Device & Account Profiles)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.users (
    id TEXT PRIMARY KEY,
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    friend_code TEXT UNIQUE NOT NULL DEFAULT substring(md5(random()::text) from 1 for 8),
    max_screen_time_minutes INT DEFAULT 120,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- If users table already existed with old auth.users reference, alter column type:
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'users_id_fkey' AND table_name = 'users'
    ) THEN
        ALTER TABLE public.users DROP CONSTRAINT users_id_fkey;
    END IF;
END $$;

-- --------------------------------------------------------------------
-- 2. COMMUNITIES TABLE (All communities are Invite-Only)
-- Access Types: 'OPEN' (instant join), 'CLOSED' (admin approval), 'DEMOCRATIC' (member voting)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.communities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    invite_code TEXT UNIQUE NOT NULL DEFAULT substring(md5(random()::text) from 1 for 8),
    access_type TEXT NOT NULL CHECK (access_type IN ('OPEN', 'CLOSED', 'DEMOCRATIC')),
    voting_threshold_percent INT DEFAULT 50 CHECK (voting_threshold_percent BETWEEN 1 AND 100),
    created_by TEXT REFERENCES public.users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- --------------------------------------------------------------------
-- 3. COMMUNITY MEMBERS TABLE
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.community_members (
    community_id UUID REFERENCES public.communities(id) ON DELETE CASCADE,
    user_id TEXT REFERENCES public.users(id) ON DELETE CASCADE,
    role TEXT NOT NULL DEFAULT 'MEMBER' CHECK (role IN ('ADMIN', 'MEMBER')),
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (community_id, user_id)
);

-- --------------------------------------------------------------------
-- 4. DIRECT USER FRIENDS TABLE (1-on-1 Friendships)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.user_friends (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id TEXT REFERENCES public.users(id) ON DELETE CASCADE,
    friend_id TEXT REFERENCES public.users(id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'BLOCKED')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, friend_id),
    CHECK (user_id <> friend_id)
);

-- --------------------------------------------------------------------
-- 5. COMMUNITY JOIN REQUESTS (For CLOSED and DEMOCRATIC communities)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.community_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    community_id UUID REFERENCES public.communities(id) ON DELETE CASCADE,
    applicant_id TEXT REFERENCES public.users(id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (community_id, applicant_id)
);

-- --------------------------------------------------------------------
-- 6. DEMOCRATIC VOTES TABLE (For DEMOCRATIC community admissions)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.community_votes (
    request_id UUID REFERENCES public.community_requests(id) ON DELETE CASCADE,
    voter_id TEXT REFERENCES public.users(id) ON DELETE CASCADE,
    vote TEXT NOT NULL CHECK (vote IN ('APPROVE', 'REJECT')),
    voted_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (request_id, voter_id)
);

-- --------------------------------------------------------------------
-- 7. DAILY USAGE TABLE (Daily screen-time sync)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.daily_usage (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id TEXT REFERENCES public.users(id) ON DELETE CASCADE,
    usage_date DATE NOT NULL DEFAULT CURRENT_DATE,
    total_minutes INT NOT NULL DEFAULT 0,
    encrypted_apps_data TEXT, -- Optional AES-256 encrypted app breakdown payload
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, usage_date)
);

-- --------------------------------------------------------------------
-- SUPABASE STORAGE BUCKET & POLICIES FOR AVATARS
-- --------------------------------------------------------------------
INSERT INTO storage.buckets (id, name, public)
VALUES ('avatars', 'avatars', true)
ON CONFLICT (id) DO NOTHING;

-- Storage Policies for Avatars
DROP POLICY IF EXISTS "Public Avatar Access" ON storage.objects;
DROP POLICY IF EXISTS "Allow Avatar Uploads" ON storage.objects;
DROP POLICY IF EXISTS "Allow Avatar Updates" ON storage.objects;

CREATE POLICY "Public Avatar Access" ON storage.objects
    FOR SELECT USING (bucket_id = 'avatars');

CREATE POLICY "Allow Avatar Uploads" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'avatars');

CREATE POLICY "Allow Avatar Updates" ON storage.objects
    FOR UPDATE USING (bucket_id = 'avatars');

-- --------------------------------------------------------------------
-- INDEXES FOR PERFORMANCE & FAST LEADERBOARD QUERIES
-- --------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_community_members_user ON public.community_members(user_id);
CREATE INDEX IF NOT EXISTS idx_user_friends_lookup ON public.user_friends(user_id, friend_id, status);
CREATE INDEX IF NOT EXISTS idx_daily_usage_user_date ON public.daily_usage(user_id, usage_date);
CREATE INDEX IF NOT EXISTS idx_communities_invite_code ON public.communities(invite_code);
CREATE INDEX IF NOT EXISTS idx_users_friend_code ON public.users(friend_code);

-- --------------------------------------------------------------------
-- TABLE PRIVILEGES FOR POSTGREST API (anon & authenticated roles)
-- --------------------------------------------------------------------
GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO anon, authenticated;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO anon, authenticated;

-- --------------------------------------------------------------------
-- ROW LEVEL SECURITY (RLS) POLICIES
-- --------------------------------------------------------------------
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.communities ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_friends ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_votes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_usage ENABLE ROW LEVEL SECURITY;

-- Drop existing user RLS policies to allow smooth device token access
DROP POLICY IF EXISTS "Users can view user profiles" ON public.users;
DROP POLICY IF EXISTS "Users can update own profile" ON public.users;
DROP POLICY IF EXISTS "Users can insert own profile" ON public.users;
DROP POLICY IF EXISTS "Allow public select on users" ON public.users;
DROP POLICY IF EXISTS "Allow public insert on users" ON public.users;
DROP POLICY IF EXISTS "Allow public update on users" ON public.users;

CREATE POLICY "Allow public select on users" ON public.users FOR SELECT USING (true);
CREATE POLICY "Allow public insert on users" ON public.users FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public update on users" ON public.users FOR UPDATE USING (true);

-- Direct Friends policies
DROP POLICY IF EXISTS "Users manage own friendships" ON public.user_friends;
CREATE POLICY "Allow public friendships" ON public.user_friends FOR ALL USING (true);

-- Communities policies
DROP POLICY IF EXISTS "Allow view communities" ON public.communities;
CREATE POLICY "Allow public communities" ON public.communities FOR ALL USING (true);

-- Community members policies
DROP POLICY IF EXISTS "Allow public community members" ON public.community_members;
CREATE POLICY "Allow public community members" ON public.community_members FOR ALL USING (true);

-- Daily usage policies
DROP POLICY IF EXISTS "Users manage own daily usage" ON public.daily_usage;
DROP POLICY IF EXISTS "View daily usage of friends or community members" ON public.daily_usage;
DROP POLICY IF EXISTS "Allow public daily usage" ON public.daily_usage;

CREATE POLICY "Allow public daily usage" ON public.daily_usage FOR ALL USING (true);

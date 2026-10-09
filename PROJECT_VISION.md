# BrainRotWatch Project Vision

## Important Notice For Future Agents

Do not alter this document unless the user explicitly grants permission.
Treat this file as the source of truth for the app's product intent and community rules.

##

BrainRotWatch is a mobile-first social screen-time app.

The core idea is to measure time spent in hand-picked, developer-selected apps with high brain-rot potential, ideally with finer-grained tracking for short-form video surfaces such as reels, shorts, and similar feeds when platform capabilities allow it.

The primary user experience is not a private dashboard. It is a shared social ranking system:

- Users can see their own screen-time totals and drill into which apps were used.
- Users can connect directly as Friends (1-on-1) via friend codes or requests to monitor each other's screen time without needing to join a community.
- Users can compare their screen time with friends and family inside Communities (group ranking).
- Community and Friends lists are sorted by daily screen time.
- Users can switch between communities and friends lists quickly and manage notification preferences per community or friend.

Friend Relationships:

- Users have a unique friend code that can be shared via direct link or QR/message.
- Friend requests require acceptance before screen-time data is shared between the two users.
- Friends can see each other's daily totals, app breakdown, and receive threshold alerts.

Community Access Rules:

- All communities are invite-only (there is no global community search function).
- Open communities allow anyone with an invite link or code to join immediately.
- Closed communities require admission approval by a group admin upon accepting an invite.
- Strictly democratic communities require a future-determined voting/approval threshold by existing members before a new user is admitted.

Sync Transparency & Anti-Cheating Controls:

- Background screen-time sync updates a timestamp on every upload.
- Leaderboards display a "Last Synced" indicator or warning badge (e.g. ⚠️ "Last synced 3h ago") if a user's background sync is stale (> 45 minutes inactive during waking hours).
- Server-side cron jobs (Supabase pg_cron / Edge Functions) monitor stale sync timestamps and send push notifications to users whose background sync appears paused or force-stopped during daytime hours.

Notification Behavior:

- Communities and Friends lists may notify users about the highest and lowest screen-time users for the day.
- Notifications may alert users when a friend or community member exceeds a user-defined screen-time threshold.
- Server-side notifications alert users if their background sync becomes stale.
- Any screen-time change should be visible to friends and community members.
- Notification volume and approval rules should be customizable and governed by admins or democratic approval where applicable.
- If a user opts out from the app, community, or changes their daily screen time limit, this should be broadcast to relevant friends and communities.

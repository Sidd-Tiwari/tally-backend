-- Run this once against your Render Postgres instance after it's created,
-- to add the two extra logical databases (Render only creates the one
-- named in render.yaml's `databaseName` field - auth_db - by default).
--
-- How to run it:
--   1. In the Render Dashboard, open your Postgres instance ("tally-postgres")
--   2. Copy the "External Connection String" (Connect menu)
--   3. From your own machine: psql "<paste connection string>"
--   4. Paste the two lines below and press enter
--
-- You only need to do this once - it survives redeploys since it lives in
-- the database, not in your app code.

CREATE DATABASE expense_db;
CREATE DATABASE habit_db;

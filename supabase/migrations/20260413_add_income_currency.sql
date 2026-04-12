-- Migration: Add income_currency column to user_wealth for multi-currency support
-- ISO 4217 currency codes (USD, EUR, GBP, PKR, INR, etc.)

ALTER TABLE public.user_wealth
  ADD COLUMN IF NOT EXISTS income_currency VARCHAR(3) NOT NULL DEFAULT 'USD';

COMMENT ON COLUMN public.user_wealth.income_currency IS 'ISO 4217 currency code for income bracket interpretation. Defaults to USD.';

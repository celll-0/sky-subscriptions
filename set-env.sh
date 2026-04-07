#!/usr/bin/env bash
# Load environment variables from .env into the current shell session.
# Usage: source ./set-env.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

if [ ! -f "$ENV_FILE" ]; then
  echo "Error: .env not found in $SCRIPT_DIR"
  echo "Copy .env.example to .env and fill in your values:"
  echo "  cp .env.example .env"
  return 1
fi

set -a
# shellcheck source=.env
source "$ENV_FILE"
set +a

echo "Environment variables loaded from $ENV_FILE"

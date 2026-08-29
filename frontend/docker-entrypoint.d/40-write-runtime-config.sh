#!/bin/sh
set -eu

api_url="${API_URL:-http://localhost:8020}"
escaped_api_url=$(printf '%s' "$api_url" | sed 's/\\/\\\\/g; s/"/\\"/g')

cat > /usr/share/nginx/html/assets/env.js <<EOF
window.__SMARTERP_CONFIG__ = {
  apiUrl: "${escaped_api_url}"
};
EOF

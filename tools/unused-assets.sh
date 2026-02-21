#!/usr/bin/env bash
set -euo pipefail

ASSETS_ROOT="${1:-src/main/resources/assets}"
USED_LIST="${2:-}"
OUTPUT="${3:-}"

if [[ ! -d "$ASSETS_ROOT" ]]; then
  echo "Assets root not found: $ASSETS_ROOT" >&2
  exit 1
fi

if [[ -z "$USED_LIST" ]]; then
  for candidate in \
    "run/client/resource-usage/used_resources.txt" \
    "run/resource-usage/used_resources.txt" \
    "run/server/resource-usage/used_resources.txt"
  do
    if [[ -f "$candidate" ]]; then
      USED_LIST="$candidate"
      break
    fi
  done
fi

if [[ -z "$USED_LIST" || ! -f "$USED_LIST" ]]; then
  echo "Used list not found. Pass it as arg2 or place it under run/client/resource-usage/used_resources.txt." >&2
  exit 1
fi

if [[ -z "$OUTPUT" ]]; then
  OUTPUT="$(dirname "$USED_LIST")/unused_assets.txt"
fi

mkdir -p "$(dirname "$OUTPUT")"

ASSETS_ROOT="$(cd "$ASSETS_ROOT" && pwd)"
USED_TMP="$(mktemp)"
ASSETS_TMP="$(mktemp)"
trap 'rm -f "$USED_TMP" "$ASSETS_TMP"' EXIT

while IFS= read -r line; do
  line="${line%%[$'\r\n']}"
  [[ -z "$line" ]] && continue
  [[ "$line" != *:* ]] && continue
  domain="${line%%:*}"
  path="${line#*:}"
  rel="${domain}/${path}"
  rel="${rel//\\//}"
  printf '%s\n' "$rel" >> "$USED_TMP"
  if [[ "$path" != *.mcmeta ]]; then
    printf '%s\n' "${domain}/${path}.mcmeta" >> "$USED_TMP"
  fi
done < "$USED_LIST"

find "$ASSETS_ROOT" -type f -print0 | while IFS= read -r -d '' file; do
  rel="${file#"$ASSETS_ROOT"/}"
  rel="${rel//\\//}"
  printf '%s\n' "$rel" >> "$ASSETS_TMP"
done

sort -u "$USED_TMP" -o "$USED_TMP"
sort -u "$ASSETS_TMP" -o "$ASSETS_TMP"
comm -23 "$ASSETS_TMP" "$USED_TMP" > "$OUTPUT"

count=$(wc -l < "$OUTPUT" | tr -d ' ')
echo "Wrote $count unused assets to $OUTPUT"

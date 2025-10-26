cat > check-packages.sh <<'EOF'
#!/usr/bin/env bash
set -euo pipefail
shopt -s globstar nullglob
base="src/main/java"
fail=0
for f in $base/**/*.java; do
  rel="${f#$base/}"
  dir_pkg=$(dirname "$rel" | tr '/' '.')
  pkg=$(grep -m1 '^package ' "$f" | sed 's/^package \(.*\);/\1/' | tr -d '\r' | xargs || true)
  if [[ -z "$pkg" ]]; then
    echo "❌ Geen package in: $f"; fail=1; continue
  fi
  if [[ "$pkg" != "$dir_pkg" ]]; then
    echo "❌ PACKAGE MISMATCH: $f"
    echo "   pad  -> $dir_pkg"
    echo "   code -> $pkg"
    fail=1
  else
    echo "✅ $f"
  fi
done
exit $fail
EOF
chmod +x check-packages.sh
./check-packages.sh
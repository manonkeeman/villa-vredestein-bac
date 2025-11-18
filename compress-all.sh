#!/bin/bash

INPUT_DIR=${1:-src/uploads}

if [ ! -d "$INPUT_DIR" ]; then
  echo "❌ Map niet gevonden: $INPUT_DIR"
  exit 1
fi

echo "🔍 Compressie gestart voor PDF’s in: $INPUT_DIR"
echo ""

for file in "$INPUT_DIR"/*.pdf; do
  if [ -f "$file" ]; then
    OUTPUT="${file%.pdf}-compressed.pdf"
    echo "📦 Compressing: $(basename "$file") → $(basename "$OUTPUT")"

    gs -sDEVICE=pdfwrite \
       -dCompatibilityLevel=1.6 \
       -dPDFSETTINGS=/ebook \
       -dNOPAUSE -dQUIET -dBATCH \
       -sOutputFile="$OUTPUT" "$file"

    echo "✔ Klaar"
    echo ""
  fi
done

echo "🎉 Alle PDF's zijn gecomprimeerd!"
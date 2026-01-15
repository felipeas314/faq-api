#!/bin/bash

BASE_URL="http://localhost:8080/api"

echo "================================"
echo "  Reindexando FAQs no Qdrant    "
echo "================================"
echo ""

# 1. Login
echo "[1/3] Fazendo login..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin.novamoeda",
    "password": "NovaMoeda@2024"
  }')

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "Erro ao fazer login"
  exit 1
fi
echo "Login OK"
echo ""

# 2. Buscar todas as FAQs
echo "[2/3] Buscando FAQs..."
FAQS=$(curl -s "$BASE_URL/faqs")
echo "FAQs encontradas: $(echo "$FAQS" | grep -o '"id"' | wc -l)"
echo ""

# 3. Atualizar cada FAQ (isso vai re-gerar o embedding)
echo "[3/3] Reindexando..."
echo "$FAQS" | grep -o '"id":"[^"]*"' | cut -d'"' -f4 | while read id; do
  # Pegar dados da FAQ
  FAQ=$(curl -s "$BASE_URL/faqs/$id")

  PERGUNTA=$(echo "$FAQ" | grep -o '"pergunta":"[^"]*"' | cut -d'"' -f4)
  RESPOSTA=$(echo "$FAQ" | grep -o '"resposta":"[^"]*"' | cut -d'"' -f4)
  CATEGORIA=$(echo "$FAQ" | grep -o '"categoria":"[^"]*"' | cut -d'"' -f4)

  # Fazer PUT para atualizar (vai reindexar)
  curl -s -X PUT "$BASE_URL/faqs/$id" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
      \"pergunta\": \"$PERGUNTA\",
      \"resposta\": \"$RESPOSTA\",
      \"categoria\": \"$CATEGORIA\",
      \"tags\": []
    }" > /dev/null

  echo "  Reindexado: $PERGUNTA"
done

echo ""
echo "================================"
echo "  Reindexação concluída!        "
echo "================================"

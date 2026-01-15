#!/bin/bash

BASE_URL="http://localhost:8080/api"

echo "================================"
echo "  FAQ Inteligente - Setup Data  "
echo "       Banco NovaMoeda          "
echo "================================"
echo ""

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 1. Criar usuário admin
echo -e "${BLUE}[1/3] Criando usuário admin...${NC}"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin.novamoeda",
    "password": "NovaMoeda@2024",
    "role": "ROLE_ADMIN"
  }')

echo "$REGISTER_RESPONSE" | grep -q "token" && echo -e "${GREEN}Usuário criado com sucesso!${NC}" || echo "Usuário já existe ou erro ao criar"
echo ""

# 2. Fazer login e pegar token
echo -e "${BLUE}[2/3] Fazendo login...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin.novamoeda",
    "password": "NovaMoeda@2024"
  }')

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "Erro ao fazer login. Resposta: $LOGIN_RESPONSE"
  exit 1
fi

echo -e "${GREEN}Login realizado com sucesso!${NC}"
echo ""

# 3. Cadastrar FAQs
echo -e "${BLUE}[3/3] Cadastrando FAQs...${NC}"
echo ""

# Função para criar FAQ
create_faq() {
  local pergunta="$1"
  local resposta="$2"
  local categoria="$3"
  local tags="$4"

  curl -s -X POST "$BASE_URL/faqs" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
      \"pergunta\": \"$pergunta\",
      \"resposta\": \"$resposta\",
      \"categoria\": \"$categoria\",
      \"tags\": $tags
    }" > /dev/null

  echo -e "  ${GREEN}✓${NC} $pergunta"
}

# === CONTA E ACESSO ===
echo "Categoria: Conta e Acesso"
create_faq \
  "Como recuperar minha senha do Internet Banking?" \
  "Acesse novamoeda.com.br e clique em 'Esqueci minha senha'. Você pode recuperar via SMS, e-mail cadastrado ou ligando para 3003-1234 (capitais) ou 0800 123 4567 (demais localidades). Tenha em mãos seu CPF e dados do cartão." \
  "conta" \
  '["senha", "internet banking", "acesso", "recuperar"]'

create_faq \
  "Como desbloquear meu cartão?" \
  "Você pode desbloquear seu cartão pelo app NovaMoeda em Cartões > Desbloquear, pelo Internet Banking, ou ligando para a Central de Atendimento 3003-1234. Para cartão de crédito novo, o desbloqueio é automático na primeira compra com chip e senha." \
  "conta" \
  '["cartão", "desbloquear", "bloqueado"]'

create_faq \
  "Como alterar meu limite do cartão de crédito?" \
  "Acesse o app NovaMoeda > Cartões > Limite. Você pode solicitar aumento de limite ou reduzir temporariamente. A análise para aumento considera seu relacionamento com o banco, renda e histórico de pagamentos. Clientes Premium podem falar diretamente com seu gerente." \
  "conta" \
  '["limite", "cartão", "crédito", "aumentar"]'

create_faq \
  "Como cadastrar ou alterar minha chave Pix?" \
  "No app NovaMoeda, acesse Pix > Minhas chaves. Você pode cadastrar CPF/CNPJ, e-mail, celular ou chave aleatória. Para alterar, exclua a chave atual e cadastre uma nova. Cada tipo de chave só pode estar vinculada a uma conta." \
  "conta" \
  '["pix", "chave", "cadastrar", "alterar"]'

echo ""

# === PAGAMENTOS E TRANSFERÊNCIAS ===
echo "Categoria: Pagamentos e Transferências"
create_faq \
  "Qual o limite para transferência Pix?" \
  "O limite padrão é de R\$ 1.000 por transação no período noturno (20h às 6h). Durante o dia, o limite segue o cadastrado na sua conta. Você pode personalizar seus limites Pix pelo app em Pix > Meus limites Pix. Alterações para aumento levam de 24h a 48h." \
  "pagamentos" \
  '["pix", "limite", "transferência", "noturno"]'

create_faq \
  "Como agendar um pagamento de boleto?" \
  "No app NovaMoeda, acesse Pagar > Boleto, escaneie ou digite o código de barras. Antes de confirmar, selecione 'Agendar' e escolha a data desejada. Você pode agendar para qualquer dia útil. Pagamentos agendados podem ser cancelados até às 22h do dia anterior." \
  "pagamentos" \
  '["boleto", "agendar", "pagamento"]'

create_faq \
  "Como fazer uma transferência internacional?" \
  "Para enviar dinheiro ao exterior, acesse o Internet Banking > Transferências > Internacional ou vá a uma agência. Você precisará dos dados bancários completos do beneficiário (SWIFT/BIC, IBAN). Há tarifas e IOF sobre a operação. O prazo é de 1 a 3 dias úteis." \
  "pagamentos" \
  '["transferência", "internacional", "exterior", "swift", "remessa"]'

create_faq \
  "O Pix funciona em feriados e finais de semana?" \
  "Sim! O Pix funciona 24 horas por dia, 7 dias por semana, inclusive feriados. As transferências são instantâneas e caem na conta do destinatário em até 10 segundos, independente do dia ou horário." \
  "pagamentos" \
  '["pix", "feriado", "fim de semana", "horário"]'

echo ""

# === EMPRÉSTIMOS E FINANCIAMENTOS ===
echo "Categoria: Empréstimos e Financiamentos"
create_faq \
  "Como solicitar um empréstimo pessoal?" \
  "Acesse o app NovaMoeda > Empréstimos > Simular. Você verá as opções disponíveis com taxas personalizadas. Escolha o valor e número de parcelas, depois confirme. A aprovação é imediata para clientes pré-aprovados e o dinheiro cai na conta em minutos." \
  "emprestimos" \
  '["empréstimo", "pessoal", "crédito", "solicitar"]'

create_faq \
  "Como antecipar parcelas do meu financiamento?" \
  "No app NovaMoeda, acesse Empréstimos e Financiamentos > selecione o contrato > Antecipar parcelas. Você pode antecipar da última para a primeira parcela, obtendo desconto nos juros. A antecipação pode ser feita a qualquer momento." \
  "emprestimos" \
  '["antecipar", "parcelas", "financiamento", "quitar"]'

create_faq \
  "Quais documentos preciso para financiamento imobiliário?" \
  "Para financiamento de imóvel você precisa: RG e CPF, comprovante de renda (3 últimos holerites ou IR), comprovante de residência, certidão de estado civil. Para autônomos, extratos bancários dos últimos 6 meses. A análise de crédito leva cerca de 5 dias úteis." \
  "emprestimos" \
  '["financiamento", "imobiliário", "casa", "documentos", "imóvel"]'

echo ""

# === INVESTIMENTOS ===
echo "Categoria: Investimentos"
create_faq \
  "Como começar a investir pelo NovaMoeda?" \
  "Acesse o app NovaMoeda > Investimentos > Começar a investir. Complete seu perfil de investidor respondendo algumas perguntas. Depois, explore as opções: CDB, Tesouro Direto, Fundos e Ações. Para iniciantes, recomendamos CDB com liquidez diária a partir de R\$ 1." \
  "investimentos" \
  '["investir", "começar", "aplicação", "CDB"]'

create_faq \
  "O que é e como funciona o Tesouro Direto?" \
  "Tesouro Direto são títulos públicos emitidos pelo Governo Federal. São considerados os investimentos mais seguros do Brasil. Há três tipos: Selic (pós-fixado), Prefixado e IPCA (atrelado à inflação). Você pode investir a partir de R\$ 30 pelo app NovaMoeda." \
  "investimentos" \
  '["tesouro", "direto", "título", "governo", "selic"]'

create_faq \
  "Como resgatar meu investimento?" \
  "No app NovaMoeda > Investimentos > Meus investimentos, selecione a aplicação e clique em Resgatar. CDBs com liquidez diária e Tesouro Selic permitem resgate a qualquer momento. Fundos podem ter prazo de cotização (D+0 a D+30). O valor cai na conta conforme o prazo do produto." \
  "investimentos" \
  '["resgatar", "investimento", "liquidez", "saque"]'

echo ""

# === SEGUROS ===
echo "Categoria: Seguros"
create_faq \
  "Como acionar o seguro do meu cartão?" \
  "Para acionar o seguro, ligue para a Central de Sinistros: 3003-1234 (capitais) ou 0800 123 4567. Tenha em mãos o número do cartão e documentos do sinistro. Para roubo/furto, é necessário B.O. O prazo para acionar é de até 30 dias após o evento." \
  "seguros" \
  '["seguro", "cartão", "sinistro", "acionar"]'

create_faq \
  "Meu seguro cobre roubo de celular?" \
  "Depende do seu plano. O Seguro Celular NovaMoeda cobre roubo, furto qualificado e danos acidentais. Verifique sua apólice no app > Seguros. Para contratar, acesse Seguros > Celular. A cobertura vale para aparelhos de até 2 anos de uso." \
  "seguros" \
  '["seguro", "celular", "roubo", "cobertura"]'

echo ""

# === ATENDIMENTO ===
echo "Categoria: Atendimento"
create_faq \
  "Quais são os canais de atendimento do NovaMoeda?" \
  "Você pode nos contatar por: App NovaMoeda (chat 24h), Internet Banking, SAC 0800 123 0000, Ouvidoria 0800 123 0001, WhatsApp (11) 3003-1234, ou em qualquer agência. Clientes Premium e Private têm canais exclusivos com seus gerentes." \
  "atendimento" \
  '["telefone", "contato", "atendimento", "SAC", "ouvidoria"]'

create_faq \
  "Como agendar atendimento em agência?" \
  "Pelo app NovaMoeda, acesse Menu > Agências > Agendar atendimento. Escolha o serviço, agência, data e horário disponível. Você receberá confirmação por push e e-mail. Também pode agendar pelo Internet Banking ou ligando para sua agência." \
  "atendimento" \
  '["agendar", "agência", "atendimento", "horário"]'

create_faq \
  "Como fazer uma reclamação ou sugestão?" \
  "Você pode registrar pelo app NovaMoeda > Ajuda > Fale conosco, pelo SAC 0800 123 0000, ou pela Ouvidoria 0800 123 0001 (se já tiver protocolo do SAC). Toda manifestação recebe um número de protocolo para acompanhamento. O prazo de resposta é de até 5 dias úteis." \
  "atendimento" \
  '["reclamação", "sugestão", "ouvidoria", "protocolo"]'

echo ""
echo "================================"
echo -e "${GREEN}Setup concluído com sucesso!${NC}"
echo "================================"
echo ""
echo "Você pode testar a busca semântica com:"
echo ""
echo "  curl -X POST http://localhost:8080/api/faqs/search \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"query\": \"esqueci minha senha do banco\"}'"
echo ""

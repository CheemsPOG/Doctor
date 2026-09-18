#!/usr/bin/env bash
# Index or check GitNexus for every direct child git repository under ctfx.
#
# Usage:
#   ./scripts/gitnexus-index-all.sh
#   ./scripts/gitnexus-index-all.sh --force
#   ./scripts/gitnexus-index-all.sh --status
#   ./scripts/gitnexus-index-all.sh --dry-run
#   ./scripts/gitnexus-index-all.sh --only authservice,eip-api
#   ./scripts/gitnexus-index-all.sh --exclude docs,wiki-doc
#
# Notes:
# - Run from ctfx root or from anywhere; the script auto-detects ctfx root.
# - Repos are indexed sequentially to avoid GitNexus registry/index contention.
# - Each child repo is indexed independently because ctfx is a repo collection,
#   not one git monorepo.

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

MODE="analyze"
DRY_RUN=0
LIST_AFTER=1
UPDATE_ROOT_AGENTS=1
ANALYZE_FLAGS=()
ONLY_CSV=""
EXCLUDE_CSV=""

print_help() {
  cat <<'EOF'
GitNexus index helper for ctfx repo collection.

Usage:
  ./scripts/gitnexus-index-all.sh [options]

Options:
  --root <path>             Override ctfx root path.
  --status                  Run `npx gitnexus status` in each repo instead of analyze.
  --force                   Pass `--force` to `npx gitnexus analyze`.
  --embeddings              Pass `--embeddings` to `npx gitnexus analyze`.
  --drop-embeddings         Pass `--drop-embeddings` to `npx gitnexus analyze`.
  --only a,b,c              Only process these repo directory names.
  --exclude a,b,c           Skip these repo directory names.
  --no-update-agents        Do not update ctfx/AGENTS.md managed GitNexus section.
  --no-list                 Do not run `npx gitnexus list` at the end.
  --dry-run                 Print repos/commands without executing.
  -h, --help                Show this help.

Examples:
  ./scripts/gitnexus-index-all.sh
  ./scripts/gitnexus-index-all.sh --force
  ./scripts/gitnexus-index-all.sh --status
  ./scripts/gitnexus-index-all.sh --only authservice,eip-api,pcweb-pro
  ./scripts/gitnexus-index-all.sh --exclude docs,wiki-doc
EOF
}

contains_csv() {
  local csv="$1"
  local value="$2"

  [[ -z "$csv" ]] && return 1

  local item
  IFS=',' read -r -a items <<<"$csv"
  for item in "${items[@]}"; do
    item="${item#"${item%%[![:space:]]*}"}"
    item="${item%"${item##*[![:space:]]}"}"
    [[ "$item" == "$value" ]] && return 0
  done
  return 1
}

gitnexus_repo_name_for() {
  local repo="$1"
  local repo_name
  local extracted_name

  repo_name="$(basename "$repo")"

  if [[ -f "$repo/AGENTS.md" ]]; then
    extracted_name="$(sed -n 's/.*indexed by GitNexus as \*\*\([^*]*\)\*\*.*/\1/p' "$repo/AGENTS.md" | head -1)"
    if [[ -n "$extracted_name" ]]; then
      echo "$extracted_name"
      return 0
    fi
  fi

  echo "$repo_name"
}

update_managed_block() {
  local file="$1"
  local section="$2"
  local start_marker="$3"
  local end_marker="$4"
  local insert_before_pattern="$5"
  local tmp_file

  tmp_file="$(mktemp "${file}.tmp.XXXXXX")"

  if [[ ! -f "$file" ]]; then
    {
      echo "# AGENTS.md — CTFX"
      echo ""
      cat "$section"
    } >"$file"
    return 0
  fi

  if grep -q "$start_marker" "$file" && grep -q "$end_marker" "$file"; then
    awk -v section="$section" -v start="$start_marker" -v end="$end_marker" '
      BEGIN {
        while ((getline line < section) > 0) {
          replacement = replacement line ORS
        }
        close(section)
      }
      $0 ~ start {
        printf "%s", replacement
        skip = 1
        next
      }
      $0 ~ end {
        skip = 0
        next
      }
      !skip { print }
    ' "$file" >"$tmp_file"
  elif [[ -n "$insert_before_pattern" ]]; then
    awk -v section="$section" -v pattern="$insert_before_pattern" '
      BEGIN {
        while ((getline line < section) > 0) {
          replacement = replacement line ORS
        }
        close(section)
        inserted = 0
      }
      $0 ~ pattern && !inserted {
        printf "%s\n", replacement
        inserted = 1
      }
      { print }
      END {
        if (!inserted) {
          printf "\n%s", replacement
        }
      }
    ' "$file" >"$tmp_file"
  else
    {
      cat "$file"
      echo ""
      echo "---"
      echo ""
      cat "$section"
    } >"$tmp_file"
  fi

  mv "$tmp_file" "$file"
}

update_context_routing() {
  local root="$1"
  local agents_file="$root/AGENTS.md"
  local tmp_section="$root/.ctfx-context-routing.tmp"
  local now

  now="$(date '+%Y-%m-%d %H:%M:%S %z')"

  {
    echo "<!-- ctfx-context-routing:start -->"
    echo ""
    echo "## 2.1 AI context routing map"
    echo ""
    echo "> Managed by \`scripts/gitnexus-index-all.sh\`. Keep detailed repo-specific rules in each child \`AGENTS.md\`; use this section to choose the right repo/context before deep dive."
    echo "> Last updated: $now"
    echo ""
    echo "### How to read context"
    echo ""
    echo "| Level | Read first | Purpose |"
    echo "|-------|------------|---------|"
    echo "| Workspace | \`ctfx/AGENTS.md\` | Understand CTFX domain, service ownership, cross-service boundaries |"
    echo "| Repo | \`ctfx/<repo>/AGENTS.md\` | Understand repo-specific GitNexus name, guardrails, stack, and local context |"
    echo "| Code graph | GitNexus repo name | Explore flows, symbols, callers, blast radius |"
    echo "| Business source | \`ctfx/docs/\` and \`ctfx/wiki-doc/\` | Confirm Japanese business specs, API/design docs, operational notes |"
    echo ""
    echo "Default prompt pattern:"
    echo ""
    echo "\`\`\`text"
    echo "Đọc @ctfx/AGENTS.md trước."
    echo "Task liên quan <capability>, đọc thêm @ctfx/<repo>/AGENTS.md."
    echo "Dùng GitNexus repo: <gitnexus-repo-name>."
    echo "\`\`\`"
    echo ""
    echo "### Capability ownership"
    echo ""
    echo "| Capability / business area | Primary repo(s) | Supporting repo(s) | GitNexus repo name(s) | Read when |"
    echo "|----------------------------|-----------------|--------------------|------------------------|-----------|"
    echo "| Customer login / JWT / core customer API | \`peach\` | \`authservice\`, \`docs\` | \`peach\`, \`mfa\`, \`docs\` | Login, customer API, JWT/session, mail/SMS code, customer account operations |"
    echo "| MFA / 2FA / trusted device | \`authservice\` | \`peach\`, \`docs\` | \`mfa\`, \`peach\`, \`docs\` | TOTP, email OTP, temptoken, trusted device, 2FA verification |"
    echo "| Account opening application | \`ur-api\`, \`ur-web\` | \`eip-api\`, \`eip-web\`, \`eip-gw\`, \`ogw\`, \`docs\` | \`ur-api\`, \`ur-web\`, \`eip-api\`, \`eip-web\`, \`eip-gw\`, \`ogw\`, \`docs\` | 口座開設申込, applicant registration, screening, web forms |"
    echo "| Internal portal / staff operations | \`eip-api\`, \`eip-web\` | \`eip-gw\`, \`ogw\`, \`docs\` | \`eip-api\`, \`eip-web\`, \`eip-gw\`, \`ogw\`, \`docs\` | 社内ポータル, internal account-opening operations, staff workflow |"
    echo "| eKYC / Salesforce outbound integration | \`ogw\` | \`eip-api\`, \`eip-gw\`, \`docs\` | \`ogw\`, \`eip-api\`, \`eip-gw\`, \`docs\` | eKYC identity data, Salesforce integration, outbound gateway behavior |"
    echo "| Trading web UI / pro trading screen | \`pcweb-pro\` | \`peach\`, \`proxy-server\`, \`smo_src\`, \`docs\` | \`pcweb-pro\`, \`peach\`, \`proxy-server\`, \`smo_src\`, \`docs\` | PC trading UI, order entry UI, chart/rate/order display |"
    echo "| My Page customer portal | \`mypage-dev\` | \`peach\`, \`docs\` | \`mypage-web\`, \`peach\`, \`docs\` | Customer self-service, account settings, balance/transfer/customer portal screens |"
    echo "| Order execution / trading core | \`smo_src\` | \`2501_swap\`, \`proxy-server\`, \`peach\`, \`docs\` | \`smo_src\`, \`2501_swap\`, \`proxy-server\`, \`peach\`, \`docs\` | 約定, order matching, losscut, rollover, swap, rate/session in Geode |"
    echo "| 2025-01 swap release branch | \`2501_swap\` | \`smo_src\`, \`docs\` | \`2501_swap\`, \`smo_src\`, \`docs\` | Release-specific swap/trading engine changes; confirm branch before editing |"
    echo "| High-speed order proxy | \`proxy-server\` | \`pcweb-pro\`, \`smo_src\`, \`docs\` | \`proxy-server\`, \`pcweb-pro\`, \`smo_src\`, \`docs\` | 注文系高速化, proxy routing, k8s-deployed order path |"
    echo "| Market news / economic indicators | \`ngw\` | \`docs\` | \`ngw\`, \`docs\` | FXi24, MarketWin24, 経済指標, market/news gateway |"
    echo "| Rate XML / historical XML distribution | \`xml-api\` | \`ngw\`, \`smo_src\`, \`docs\` | \`xml-api\`, \`ngw\`, \`smo_src\`, \`docs\` | レートXML配信, historical XML, public/partner rate distribution |"
    echo "| Push notification | \`onesignal\`, \`smo_src\` | \`peach\`, \`docs\` | \`onesignal\`, \`smo_src\`, \`peach\`, \`docs\` | OneSignal config, FCM/APNS/WEB push, notification delivery |"
    echo "| Mock backend / test stubs | \`mock-api\`, \`mock-api-gts\` | frontend/backend repos under test | \`mock-api\`, \`mock-api-gts\` | Mocked API behavior for dev/test |"
    echo "| WebView / auxiliary web wrappers | \`wvs-web\`, \`webview\`, \`pcweb\`, \`tradingview\` | \`peach\`, \`pcweb-pro\`, \`docs\` | \`wvs-web\`, \`webview\`, \`pcweb\`, \`tradingview\`, \`docs\` | Webview screens, wrapper branches, TradingView integration points |"
    echo "| Business specs / source of truth | \`docs\` | \`wiki-doc\` | \`docs\`, \`wiki-docs\` | API一覧, 概要設計書, CRUD表, テーブル定義書, API設計書, network specs |"
    echo "| Engineering wiki / onboarding | \`wiki-doc\` | all repos | \`wiki-docs\` | Team process, architecture notes, onboarding, dev process |"
    echo ""
    echo "### Cross-service flows"
    echo ""
    echo "| Flow | Typical path | First repos to inspect | Notes |"
    echo "|------|--------------|------------------------|-------|"
    echo "| Customer login with MFA | frontend -> \`peach\` -> \`authservice\` -> JWT/session | \`peach\`, \`authservice\`, \`docs\` | GitNexus repo for \`authservice\` is \`mfa\` |"
    echo "| Account opening | \`ur-web\` -> \`ur-api\` -> \`eip-api/eip-gw\` -> \`ogw\` | \`ur-web\`, \`ur-api\`, \`eip-api\`, \`eip-gw\`, \`ogw\`, \`docs\` | Check Japanese specs before changing flow |"
    echo "| Trading order | \`pcweb-pro\` -> \`peach\` / \`proxy-server\` -> \`smo_src\`/\`2501_swap\` | \`pcweb-pro\`, \`peach\`, \`proxy-server\`, \`smo_src\`, \`docs\` | Confirm whether target is current core or \`2501_swap\` branch |"
    echo "| Rate/news display | \`pcweb-pro\` / portals -> \`ngw\` / \`xml-api\` / SMO rate data | \`pcweb-pro\`, \`ngw\`, \`xml-api\`, \`smo_src\`, \`docs\` | Separate market/news gateway from trading core rate distribution |"
    echo "| Push/notification | SMO/customer API -> push provider config | \`smo_src\`, \`onesignal\`, \`peach\`, \`docs\` | Do not log tokens or secrets |"
    echo "| Staff account operations | \`eip-web\` -> \`eip-api\` -> \`eip-gw\`/\`ogw\` | \`eip-web\`, \`eip-api\`, \`eip-gw\`, \`ogw\`, \`docs\` | Internal portal scope; check permissions and audit needs |"
    echo ""
    echo "### GitNexus repo-name aliases"
    echo ""
    echo "Some folder names differ from GitNexus registered repo names."
    echo ""
    echo "| Folder | GitNexus repo name | Use this in tools |"
    echo "|--------|--------------------|-------------------|"

    local repo repo_name gitnexus_repo
    for repo in "${repos[@]}"; do
      repo_name="$(basename "$repo")"
      gitnexus_repo="$(gitnexus_repo_name_for "$repo")"
      if [[ "$repo_name" != "$gitnexus_repo" ]]; then
        echo "| \`$repo_name\` | \`$gitnexus_repo\` | \`repo: $gitnexus_repo\` |"
      fi
    done

    echo ""
    echo "If a folder is not listed above, use the folder name as the GitNexus repo name unless the child \`AGENTS.md\` says otherwise."
    echo ""
    echo "### Query examples"
    echo ""
    echo "\`\`\`text"
    echo "Login/MFA:"
    echo "  gitnexus_query(repo=\"peach\", query=\"login JWT mail SMS code customer authentication\")"
    echo "  gitnexus_query(repo=\"mfa\", query=\"temptoken TOTP email OTP trusted device verify\")"
    echo ""
    echo "Account opening:"
    echo "  gitnexus_query(repo=\"ur-api\", query=\"account opening applicant registration screening\")"
    echo "  gitnexus_query(repo=\"ur-web\", query=\"kouza account opening form validation\")"
    echo ""
    echo "Trading:"
    echo "  gitnexus_query(repo=\"pcweb-pro\", query=\"order entry rate position trading screen\")"
    echo "  gitnexus_query(repo=\"smo_src\", query=\"order matching losscut rollover swap geode\")"
    echo ""
    echo "Specs:"
    echo "  gitnexus_query(repo=\"docs\", query=\"概要設計書 API設計書 テーブル定義 MESSAGE http status\")"
    echo "\`\`\`"
    echo ""
    echo "<!-- ctfx-context-routing:end -->"
  } >"$tmp_section"

  update_managed_block \
    "$agents_file" \
    "$tmp_section" \
    '<!-- ctfx-context-routing:start -->' \
    '<!-- ctfx-context-routing:end -->' \
    '^## 3\. Stack tổng hợp'

  rm -f "$tmp_section"
}

update_root_agents() {
  local root="$1"
  local agents_file="$root/AGENTS.md"
  local tmp_section="$root/.gitnexus-agents-section.tmp"
  local tmp_file="$root/.gitnexus-agents.tmp"
  local now

  now="$(date '+%Y-%m-%d %H:%M:%S %z')"

  {
    echo "<!-- gitnexus-ctfx:start -->"
    echo "## GitNexus index map"
    echo ""
    echo "> Managed by \`scripts/gitnexus-index-all.sh\`. Edit repo business context outside this block."
    echo "> Last updated: $now"
    echo ""
    echo "CTFX is a folder containing multiple independent git repositories. Each child repo is indexed separately by GitNexus."
    echo ""
    echo "| Repo folder | GitNexus repo | GitNexus index | Repo AGENTS.md | CLAUDE.md | Suggested query scope |"
    echo "|-------------|---------------|----------------|----------------|-----------|-----------------------|"

    local repo repo_name gitnexus_repo index_state agents_state claude_state
    for repo in "${repos[@]}"; do
      repo_name="$(basename "$repo")"
      gitnexus_repo="$(gitnexus_repo_name_for "$repo")"

      if [[ -d "$repo/.gitnexus" ]]; then
        index_state="Present"
      else
        index_state="Missing"
      fi

      if [[ -f "$repo/AGENTS.md" ]]; then
        agents_state="Present"
      else
        agents_state="Missing"
      fi

      if [[ -f "$repo/CLAUDE.md" ]]; then
        claude_state="Present"
      else
        claude_state="Missing"
      fi

      echo "| \`$repo_name\` | \`$gitnexus_repo\` | $index_state | $agents_state | $claude_state | \`repo: $gitnexus_repo\` |"
    done

    echo ""
    echo "### How to use GitNexus in ctfx"
    echo ""
    echo "- Use this root \`AGENTS.md\` for workspace/service map and business-domain orientation."
    echo "- Use each child repo \`AGENTS.md\` for repo-specific stack, conventions, and GitNexus guardrails."
    echo "- Run GitNexus queries against the child repo name, not the \`ctfx\` folder."
    echo "- If GitNexus cannot find a repo after indexing, restart the MCP/Cursor session and run \`npx gitnexus list\`."
    echo ""
    echo "Common commands:"
    echo ""
    echo "\`\`\`bash"
    echo "cd /Users/hieu.bui/Documents/ctfx"
    echo "./scripts/gitnexus-index-all.sh --status"
    echo "./scripts/gitnexus-index-all.sh --only authservice,eip-api"
    echo "./scripts/gitnexus-index-all.sh --force"
    echo "\`\`\`"
    echo "<!-- gitnexus-ctfx:end -->"
  } >"$tmp_section"

  update_context_routing "$root"
  update_managed_block \
    "$agents_file" \
    "$tmp_section" \
    '<!-- gitnexus-ctfx:start -->' \
    '<!-- gitnexus-ctfx:end -->' \
    ''

  rm -f "$tmp_section"
  echo "updated: $agents_file"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --root)
      [[ $# -ge 2 ]] || { echo "Missing value for --root" >&2; exit 2; }
      ROOT_DIR="$2"
      shift 2
      ;;
    --status)
      MODE="status"
      shift
      ;;
    --force)
      ANALYZE_FLAGS+=("--force")
      shift
      ;;
    --embeddings)
      ANALYZE_FLAGS+=("--embeddings")
      shift
      ;;
    --drop-embeddings)
      ANALYZE_FLAGS+=("--drop-embeddings")
      shift
      ;;
    --only)
      [[ $# -ge 2 ]] || { echo "Missing value for --only" >&2; exit 2; }
      ONLY_CSV="$2"
      shift 2
      ;;
    --exclude)
      [[ $# -ge 2 ]] || { echo "Missing value for --exclude" >&2; exit 2; }
      EXCLUDE_CSV="$2"
      shift 2
      ;;
    --no-update-agents)
      UPDATE_ROOT_AGENTS=0
      shift
      ;;
    --no-list)
      LIST_AFTER=0
      shift
      ;;
    --dry-run)
      DRY_RUN=1
      shift
      ;;
    -h|--help)
      print_help
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      print_help >&2
      exit 2
      ;;
  esac
done

ROOT_DIR="$(cd "$ROOT_DIR" && pwd)"

if [[ ! -d "$ROOT_DIR" ]]; then
  echo "Root directory not found: $ROOT_DIR" >&2
  exit 2
fi

if ! command -v npx >/dev/null 2>&1; then
  echo "npx not found. Install Node.js/npm first." >&2
  exit 2
fi

echo "GitNexus mode : $MODE"
echo "CTFX root     : $ROOT_DIR"
echo "Dry run       : $DRY_RUN"
echo "Update AGENTS : $UPDATE_ROOT_AGENTS"
if [[ "$MODE" == "analyze" ]]; then
  if [[ ${#ANALYZE_FLAGS[@]} -gt 0 ]]; then
    echo "Analyze flags : ${ANALYZE_FLAGS[*]}"
  else
    echo "Analyze flags : (none)"
  fi
fi
[[ -n "$ONLY_CSV" ]] && echo "Only          : $ONLY_CSV"
[[ -n "$EXCLUDE_CSV" ]] && echo "Exclude       : $EXCLUDE_CSV"
echo ""

repos=()
while IFS= read -r git_dir; do
  repo_dir="$(dirname "$git_dir")"
  repo_name="$(basename "$repo_dir")"

  if [[ -n "$ONLY_CSV" ]] && ! contains_csv "$ONLY_CSV" "$repo_name"; then
    continue
  fi

  if contains_csv "$EXCLUDE_CSV" "$repo_name"; then
    continue
  fi

  repos+=("$repo_dir")
done < <(find "$ROOT_DIR" -mindepth 2 -maxdepth 2 -type d -name .git | sort)

if [[ ${#repos[@]} -eq 0 ]]; then
  echo "No direct child git repos found under: $ROOT_DIR" >&2
  exit 1
fi

echo "Repos to process (${#repos[@]}):"
for repo in "${repos[@]}"; do
  echo "  - $(basename "$repo")"
done
echo ""

ok=()
failed=()

for repo in "${repos[@]}"; do
  repo_name="$(basename "$repo")"
  echo "========================================"
  echo "Repo: $repo_name"
  echo "Path: $repo"

  if [[ "$MODE" == "status" ]]; then
    cmd=(npx gitnexus status)
  else
    cmd=(npx gitnexus analyze)
    if [[ ${#ANALYZE_FLAGS[@]} -gt 0 ]]; then
      cmd+=("${ANALYZE_FLAGS[@]}")
    fi
  fi

  echo "Cmd : ${cmd[*]}"

  if [[ "$DRY_RUN" -eq 1 ]]; then
    ok+=("$repo_name")
    continue
  fi

  (
    cd "$repo" || exit 1
    "${cmd[@]}"
  )
  exit_code=$?

  if [[ $exit_code -eq 0 ]]; then
    ok+=("$repo_name")
    echo "OK  : $repo_name"
  else
    failed+=("$repo_name")
    echo "FAIL: $repo_name (exit $exit_code)" >&2
  fi

  echo ""
done

echo "========================================"
echo "Summary"
echo "OK    (${#ok[@]}): ${ok[*]:-(none)}"
echo "FAIL  (${#failed[@]}): ${failed[*]:-(none)}"

if [[ "$DRY_RUN" -eq 0 && "$LIST_AFTER" -eq 1 ]]; then
  echo ""
  echo "Registered GitNexus repos:"
  npx gitnexus list || true
fi

if [[ "$DRY_RUN" -eq 0 && "$UPDATE_ROOT_AGENTS" -eq 1 ]]; then
  echo ""
  update_root_agents "$ROOT_DIR"
fi

if [[ ${#failed[@]} -gt 0 ]]; then
  exit 1
fi

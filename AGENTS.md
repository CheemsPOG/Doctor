# AGENTS.md — CTFX

<!-- ctfx-context-routing:start -->

## 2.1 AI context routing map

> Managed by `scripts/gitnexus-index-all.sh`. Keep detailed repo-specific rules in each child `AGENTS.md`; use this section to choose the right repo/context before deep dive.
> Last updated: 2026-07-22 08:45:34 +0700

### How to read context

| Level | Read first | Purpose |
|-------|------------|---------|
| Workspace | `ctfx/AGENTS.md` | Understand CTFX domain, service ownership, cross-service boundaries |
| Repo | `ctfx/<repo>/AGENTS.md` | Understand repo-specific GitNexus name, guardrails, stack, and local context |
| Code graph | GitNexus repo name | Explore flows, symbols, callers, blast radius |
| Business source | `ctfx/docs/` and `ctfx/wiki-doc/` | Confirm Japanese business specs, API/design docs, operational notes |

Default prompt pattern:

```text
Đọc @ctfx/AGENTS.md trước.
Task liên quan <capability>, đọc thêm @ctfx/<repo>/AGENTS.md.
Dùng GitNexus repo: <gitnexus-repo-name>.
```

### Capability ownership

| Capability / business area | Primary repo(s) | Supporting repo(s) | GitNexus repo name(s) | Read when |
|----------------------------|-----------------|--------------------|------------------------|-----------|
| Customer login / JWT / core customer API | `peach` | `authservice`, `docs` | `peach`, `mfa`, `docs` | Login, customer API, JWT/session, mail/SMS code, customer account operations |
| MFA / 2FA / trusted device | `authservice` | `peach`, `docs` | `mfa`, `peach`, `docs` | TOTP, email OTP, temptoken, trusted device, 2FA verification |
| Account opening application | `ur-api`, `ur-web` | `eip-api`, `eip-web`, `eip-gw`, `ogw`, `docs` | `ur-api`, `ur-web`, `eip-api`, `eip-web`, `eip-gw`, `ogw`, `docs` | 口座開設申込, applicant registration, screening, web forms |
| Internal portal / staff operations | `eip-api`, `eip-web` | `eip-gw`, `ogw`, `docs` | `eip-api`, `eip-web`, `eip-gw`, `ogw`, `docs` | 社内ポータル, internal account-opening operations, staff workflow |
| eKYC / Salesforce outbound integration | `ogw` | `eip-api`, `eip-gw`, `docs` | `ogw`, `eip-api`, `eip-gw`, `docs` | eKYC identity data, Salesforce integration, outbound gateway behavior |
| Trading web UI / pro trading screen | `pcweb-pro` | `peach`, `proxy-server`, `smo_src`, `docs` | `pcweb-pro`, `peach`, `proxy-server`, `smo_src`, `docs` | PC trading UI, order entry UI, chart/rate/order display |
| My Page customer portal | `mypage-dev` | `peach`, `docs` | `mypage-web`, `peach`, `docs` | Customer self-service, account settings, balance/transfer/customer portal screens |
| Order execution / trading core | `smo_src` | `2501_swap`, `proxy-server`, `peach`, `docs` | `smo_src`, `2501_swap`, `proxy-server`, `peach`, `docs` | 約定, order matching, losscut, rollover, swap, rate/session in Geode |
| 2025-01 swap release branch | `2501_swap` | `smo_src`, `docs` | `2501_swap`, `smo_src`, `docs` | Release-specific swap/trading engine changes; confirm branch before editing |
| High-speed order proxy | `proxy-server` | `pcweb-pro`, `smo_src`, `docs` | `proxy-server`, `pcweb-pro`, `smo_src`, `docs` | 注文系高速化, proxy routing, k8s-deployed order path |
| Market news / economic indicators | `ngw` | `docs` | `ngw`, `docs` | FXi24, MarketWin24, 経済指標, market/news gateway |
| Rate XML / historical XML distribution | `xml-api` | `ngw`, `smo_src`, `docs` | `xml-api`, `ngw`, `smo_src`, `docs` | レートXML配信, historical XML, public/partner rate distribution |
| Push notification | `onesignal`, `smo_src` | `peach`, `docs` | `onesignal`, `smo_src`, `peach`, `docs` | OneSignal config, FCM/APNS/WEB push, notification delivery |
| Mock backend / test stubs | `mock-api`, `mock-api-gts` | frontend/backend repos under test | `mock-api`, `mock-api-gts` | Mocked API behavior for dev/test |
| WebView / auxiliary web wrappers | `wvs-web`, `webview`, `pcweb`, `tradingview` | `peach`, `pcweb-pro`, `docs` | `wvs-web`, `webview`, `pcweb`, `tradingview`, `docs` | Webview screens, wrapper branches, TradingView integration points |
| Business specs / source of truth | `docs` | `wiki-doc` | `docs`, `wiki-docs` | API一覧, 概要設計書, CRUD表, テーブル定義書, API設計書, network specs |
| Engineering wiki / onboarding | `wiki-doc` | all repos | `wiki-docs` | Team process, architecture notes, onboarding, dev process |

### Cross-service flows

| Flow | Typical path | First repos to inspect | Notes |
|------|--------------|------------------------|-------|
| Customer login with MFA | frontend -> `peach` -> `authservice` -> JWT/session | `peach`, `authservice`, `docs` | GitNexus repo for `authservice` is `mfa` |
| Account opening | `ur-web` -> `ur-api` -> `eip-api/eip-gw` -> `ogw` | `ur-web`, `ur-api`, `eip-api`, `eip-gw`, `ogw`, `docs` | Check Japanese specs before changing flow |
| Trading order | `pcweb-pro` -> `peach` / `proxy-server` -> `smo_src`/`2501_swap` | `pcweb-pro`, `peach`, `proxy-server`, `smo_src`, `docs` | Confirm whether target is current core or `2501_swap` branch |
| Rate/news display | `pcweb-pro` / portals -> `ngw` / `xml-api` / SMO rate data | `pcweb-pro`, `ngw`, `xml-api`, `smo_src`, `docs` | Separate market/news gateway from trading core rate distribution |
| Push/notification | SMO/customer API -> push provider config | `smo_src`, `onesignal`, `peach`, `docs` | Do not log tokens or secrets |
| Staff account operations | `eip-web` -> `eip-api` -> `eip-gw`/`ogw` | `eip-web`, `eip-api`, `eip-gw`, `ogw`, `docs` | Internal portal scope; check permissions and audit needs |

### GitNexus repo-name aliases

Some folder names differ from GitNexus registered repo names.

| Folder | GitNexus repo name | Use this in tools |
|--------|--------------------|-------------------|

If a folder is not listed above, use the folder name as the GitNexus repo name unless the child `AGENTS.md` says otherwise.

### Query examples

```text
Login/MFA:
  gitnexus_query(repo="peach", query="login JWT mail SMS code customer authentication")
  gitnexus_query(repo="mfa", query="temptoken TOTP email OTP trusted device verify")

Account opening:
  gitnexus_query(repo="ur-api", query="account opening applicant registration screening")
  gitnexus_query(repo="ur-web", query="kouza account opening form validation")

Trading:
  gitnexus_query(repo="pcweb-pro", query="order entry rate position trading screen")
  gitnexus_query(repo="smo_src", query="order matching losscut rollover swap geode")

Specs:
  gitnexus_query(repo="docs", query="概要設計書 API設計書 テーブル定義 MESSAGE http status")
```

<!-- ctfx-context-routing:end -->

---

<!-- gitnexus-ctfx:start -->
## GitNexus index map

> Managed by `scripts/gitnexus-index-all.sh`. Edit repo business context outside this block.
> Last updated: 2026-07-22 08:45:34 +0700

CTFX is a folder containing multiple independent git repositories. Each child repo is indexed separately by GitNexus.

| Repo folder | GitNexus repo | GitNexus index | Repo AGENTS.md | CLAUDE.md | Suggested query scope |
|-------------|---------------|----------------|----------------|-----------|-----------------------|
| `be-doc-doctor-ri` | `be-doc-doctor-ri` | Present | Present | Present | `repo: be-doc-doctor-ri` |
| `doctor-ri-clinic-spec` | `doctor-ri-clinic-spec` | Present | Present | Present | `repo: doctor-ri-clinic-spec` |
| `fe-doc-doctor-ri` | `fe-doc-doctor-ri` | Present | Present | Present | `repo: fe-doc-doctor-ri` |

### How to use GitNexus in ctfx

- Use this root `AGENTS.md` for workspace/service map and business-domain orientation.
- Use each child repo `AGENTS.md` for repo-specific stack, conventions, and GitNexus guardrails.
- Run GitNexus queries against the child repo name, not the `ctfx` folder.
- If GitNexus cannot find a repo after indexing, restart the MCP/Cursor session and run `npx gitnexus list`.

Common commands:

```bash
cd /Users/hieu.bui/Documents/ctfx
./scripts/gitnexus-index-all.sh --status
./scripts/gitnexus-index-all.sh --only authservice,eip-api
./scripts/gitnexus-index-all.sh --force
```
<!-- gitnexus-ctfx:end -->

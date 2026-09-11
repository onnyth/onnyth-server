# Documentation Map

Start at [`/AGENTS.md`](../AGENTS.md). This folder holds the parts of the AI context system that don't
belong in `.agents/skills/` (per-feature technical reference) or `.agents/prd/` (product backlog and
sprint history).

| Need to... | Read |
|---|---|
| Understand a specific feature/module in detail | `.agents/skills/<feature>/SKILL.md` — start from `.agents/skills/SKILL.md` |
| Understand coding conventions before writing code | `.agents/skills/conventions/SKILL.md` |
| Understand *why* an architectural decision was made | `docs/decisions/` (this folder) |
| Know what's actually implemented right now | `docs/product/current-state.md` |
| Know about a specific gap, inconsistency, or piece of tech debt | `docs/known-issues.md` |
| See the full feature roadmap / backlog | `.agents/prd/product-backlog.md` |
| Execute a new sprint of work | `.agents/prd/ORCHESTRATOR.md` |

## Relationship between the three systems

```text
AGENTS.md               → entry point, rules, links out
.agents/skills/          → "how does X work today" (technical, per-feature/topic)
.agents/prd/             → "what are we building and when" (product/process)
docs/decisions/          → "why did we build it this way" (ADRs)
docs/product/            → "what exists right now" (verified snapshot, cross-checked against code)
docs/known-issues.md     → "where do the docs/code/tests disagree, and what's genuinely unfinished"
```

Skills and the product backlog are the **primary** sources for feature-level detail — this `docs/`
folder intentionally stays small and does not duplicate them. Only create a new file here if it's a
decision record, a current-state snapshot, or a cross-cutting issue that doesn't fit inside one skill.

# ADR 0001: Separate repository knowledge by purpose

## Status

Accepted

## Date

2026-08-13

## Context

Repository instructions had accumulated setup notes, volatile versions, feature contracts, implementation ledgers, and
detailed procedures. Because root agent instructions are loaded for ordinary work, this raised the context cost and
made stale duplication likely. Humans and agents also need different entry points, while executable configuration must
remain the authority for frequently changing facts.

## Decision

Keep `AGENTS.md` concise and limited to always-applicable repository guidance. Store durable architecture and
product-engineering decisions with rationale in indexed ADRs. Store repeatable agent workflows and detailed conditional
references as repository-local skills under `.agents/skills`. Store human setup and contribution workflow in
`CONTRIBUTING.md`.

Treat code, Gradle files, scripts, and CI as executable truth for volatile versions and behavior. Keep temporary plans,
progress ledgers, and review status in issues or pull requests. Do not introduce repository-pinned model, reasoning,
concurrency, custom-subagent, hook, or MCP configuration for this knowledge system.

## Consequences

Ordinary tasks load less context, detailed workflows remain discoverable on demand, and decisions retain rationale.
Contributors must choose the correct home for new knowledge and update links when a durable decision is superseded.

## Alternatives

- Keep all guidance in `AGENTS.md`: rejected because every task pays the loading cost.
- Put durable decisions in procedural skills: rejected because skills optimize execution, not decision history.
- Add automated policy gates: rejected in favor of contextual review.

## References

- `AGENTS.md`
- `CONTRIBUTING.md`
- `.agents/skills/`

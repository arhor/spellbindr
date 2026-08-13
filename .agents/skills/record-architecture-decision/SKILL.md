---
name: record-architecture-decision
description: >-
  Propose, accept, index, deprecate, or supersede a durable Spellbindr architecture or product-engineering decision.
  Use when a cross-cutting technical constraint, ownership boundary, persistence strategy, integration policy, or
  long-lived engineering tradeoff needs an ADR and rationale. Do not use for temporary plans, implementation ledgers,
  routine documentation, feature requirements, progress reports, small local code choices, or ordinary feature work.
---

# Record Architecture Decision

Maintain the repository's durable decision history without turning ADRs into plans or specifications.

## Qualify the candidate

Read `docs/adr/README.md` and only the related ADRs. Record an ADR when the choice is expected to constrain multiple
changes or contributors over time, has meaningful alternatives or consequences, and benefits from preserved rationale.

Reject the ADR form when the content is primarily:

- a temporary implementation sequence, checklist, milestone, or progress ledger;
- routine setup, contributor instructions, API documentation, or release notes;
- a product feature requirement without an engineering decision and alternatives;
- a local refactor or easily reversible implementation detail.

Route rejected material to an issue or pull request for temporary work, `CONTRIBUTING.md` for human workflow, a
repository skill for repeatable agent procedure, or executable configuration for volatile truth.

## Choose the lifecycle action

- **Propose**: allocate the next four-digit sequential ID, copy `docs/adr/template.md`, and use `Proposed`.
- **Accept**: change a reviewed proposal to `Accepted`; ensure the decision, consequences, and alternatives are clear.
- **Supersede**: create a new ADR, mark the old ADR `Superseded by ADR NNNN`, and add reciprocal references. Do not
  materially rewrite the old accepted decision.
- **Deprecate**: use `Deprecated` when the decision no longer applies and no replacement decision exists; explain why.
- **Index**: add or update the row in `docs/adr/README.md` with ID, linked title, exact status, and affected areas.

Use `YYYY-MM-DD` dates. Keep retrospective facts distinguishable from new decisions. Correct typos, broken links, or
minor clarifications in accepted ADRs only when their original meaning remains intact.

## Write the record

Use every template section: `Status`, `Date`, `Context`, `Decision`, `Consequences`, `Alternatives`, and `References`.
State the boundary precisely, include meaningful costs as well as benefits, and link authoritative code or earlier
ADRs. Avoid task lists, completion claims, volatile version pins, and duplicated procedural instructions.

Before handoff, verify sequential IDs, index consistency, reciprocal supersession links, relative references, and
`git diff --check`. Report why the candidate qualified as durable or where rejected material was routed.

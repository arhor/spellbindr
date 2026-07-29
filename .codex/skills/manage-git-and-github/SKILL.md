---
name: manage-git-and-github
description: Safely and consistently perform Git and GitHub operations. Use whenever a task involves inspecting repository status, branches, commits, tags, diffs, or history; creating, switching, rebasing, merging, or deleting branches; staging, committing, reverting, cherry-picking, restoring, fetching, pulling, or pushing changes; resolving conflicts; publishing local work; or working with GitHub pull requests, issues, reviews, comments, checks, Actions results, releases, or repository metadata.
---

# Manage Git and GitHub

Keep local Git work and GitHub-hosted work separate:

- Use the standard `git` CLI for all local repository state and operations, including status, diff, log, branch,
  switch, checkout, add, commit, fetch, pull, push, merge, rebase, cherry-pick, revert, restore, reset, and tag.
- Use GitHub MCP tools for all GitHub-hosted state and operations, including pull requests, reviews, unresolved
  threads, issues, comments, checks, Actions results, releases, labels, milestones, and repository metadata.
- Use `git push` to publish commits and branches. Use GitHub MCP to create or modify the corresponding pull request.
- Never use the `gh` CLI under any circumstances.
- Do not replace GitHub MCP with direct REST or GraphQL calls, `curl`, or browser automation unless the user
  explicitly requests another approach and the required MCP capability is unavailable. If MCP cannot perform a
  required operation, explain the gap and request direction instead of silently changing approaches.

## Inspect Before Changing

Before modifying repository state:

1. Run `git status --short --branch`.
2. Identify the current branch and relevant remote/tracking branch.
3. Inspect relevant staged and unstaged changes with `git diff` and `git diff --staged`.
4. Preserve unrelated user changes and avoid overwriting or discarding them.

Inspection and reporting requests do not authorize commits, pushes, pull requests, merges, issue changes, branch
deletion, workflow reruns, or other mutations.

## Commit Safely

Before committing:

1. Review the complete diff intended for the commit.
2. Stage only files relevant to the requested task.
3. Recheck the staged diff and exclude unrelated modifications.
4. Use a concise commit message describing the actual change.
5. Do not amend an existing commit unless the user requested it or the requested workflow clearly requires it.

## Fetch, Pull, and Push Safely

- Confirm the branch and remote before pulling or pushing.
- Never force-push by default.
- When history rewriting is genuinely required, explain why before doing it and use `--force-with-lease`, never
  `--force`.
- Do not push merely because a local change or commit exists; push only when the request includes publishing it.

## Protect History and User Work

- Avoid `git reset --hard`, branch deletion, forced updates, and discarding changes unless explicitly required.
- Preserve uncommitted work and prefer reversible operations.
- State the impact before an operation may remove commits or files.
- Inspect conflicts before resolving them. Preserve both intended changes, verify the resolution, and continue the
  merge, rebase, or cherry-pick only after all conflicts are addressed.
- Do not abort an operation if doing so could discard user work without first assessing and reporting the impact.

## Work With Pull Requests

Before creating a pull request:

1. Inspect the branch diff and relevant commit history with `git`.
2. Use GitHub MCP to check whether a pull request already exists.
3. Create a draft pull request unless the user explicitly asks for ready-for-review.
4. Derive the title and description from the actual changes.
5. Include a concise summary and testing information.
6. Never claim tests passed unless they were actually executed successfully.

## Address Reviews

1. Use GitHub MCP to retrieve review comments and unresolved threads.
2. Distinguish actionable feedback from questions, suggestions, resolved comments, and outdated comments.
3. Make requested code changes locally using normal repository tools.
4. Use `git` to commit and push fixes only when authorized.
5. Reply to or resolve a thread through GitHub MCP only after addressing the corresponding issue.

## Inspect Actions and Checks

- Use GitHub MCP to inspect check status and available failure information.
- Never use `gh run`, `gh pr checks`, or any other `gh` command.
- Correlate failures with local code and workflow configuration; do not overstate conclusions when logs are
  incomplete.
- Do not rerun, cancel, or modify workflows unless requested.

## Report the Outcome

After completing the task, report:

- the branch used;
- files or areas changed;
- any commit created;
- whether changes were pushed;
- any pull request or issue created or updated through GitHub MCP;
- tests or checks run and their actual results;
- remaining risks, conflicts, or unresolved review comments.

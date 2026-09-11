---
description: how to execute sprint work — plan → build → test → update skills
---

# Sprint Workflow

// turbo-all

Use this whenever you need to work on a sprint for the Onnyth Server.

## Prerequisites

Read these first:
1. `.agents/prd/ORCHESTRATOR.md` — the full operating manual
2. `.agents/skills/SKILL.md` — the skills index

## Steps

### 1. Boot Up — Read Skills
Read the following files:
- `.agents/skills/SKILL.md` (master skills index)
- `.agents/skills/conventions/SKILL.md` (coding standards)
- `.agents/skills/data-layer/SKILL.md` (entity/migration patterns)

### 2. Read the Backend Brief from Inbox
Read `.agents/prd/inbox/backend-brief.md` — this is the filled brief from the client process.
If no `backend-brief.md` exists in the inbox, notify the user: _"No backend brief found in inbox. Please drop a filled backend-brief.md in `.agents/prd/inbox/` and try again."_

### 3. Create Sprint Folder
- List existing sprint folders in `.agents/prd/sprints/` to find the next sprint number
- Extract the sprint name from the brief's title/goal
- Create `sprints/sprint-{N}--{name}/`
- Copy `backend-brief.md` into the sprint folder for archival

### 4. Generate Sprint Plan + Stories
Decompose the brief into stories following **ORCHESTRATOR Phase 1**:
- Create `sprint-plan.md` using the sprint-plan template
- Create story files in `stories/` using the story template
- Map brief §1→endpoints, §2→data model, §3→services
- Use §4 priorities (🔴/🟡) and §5 ordering
- **Present the plan to the user for approval before proceeding**

### 5. Pick Up the Next Story
Find the first unchecked `[ ]` story in `sprint-plan.md`. Read its story file and all **Related Skills**.

### 6. Plan the Implementation
Create an implementation plan for the story:
- Files to create/modify
- Flyway migrations to add
- Tests to write
- **Present to user for approval**

### 7. Execute the Tasks
Implement each task following conventions, data-layer, and testing skills.
Mark each task as `[x]` in the story file.

### 8. Verify
Run the test suite: `./mvnw test`

### 9. Update Skills
Update affected skill files per the **Skill Update Protocol** in ORCHESTRATOR.md.

### 10. Mark Story Done
Update story status to `DONE`, check it off in sprint plan.

### 11. Next Story or Sprint Complete?
- **More stories remain** → go back to **Step 5**
- **All stories done** → continue to **Step 12**

### 12. Sprint Cleanup
- Mark sprint-plan.md status as `COMPLETED`
- Update `product-backlog.md` with completed features
- **DELETE `inbox/backend-brief.md`** to clear the inbox for the next brief
- Notify the user: _"Sprint {N} complete. Inbox cleared — drop the next backend-brief.md whenever ready."_

# AGENTS.md

## Project Overview

DikenEngine is a Java-based 2D game engine and editor. The codebase includes runtime engine classes, game object nodes, services, rendering, GUI components, scripting support, resources, input handling, and studio/editor panels.

Primary package root:

```text
src/me/ramazanenescik04/diken
```

Bundled third-party JSON sources live under:

```text
src/org/json
```

Avoid editing bundled third-party code unless the task explicitly requires it.

## Repository Structure

- `src/me/ramazanenescik04/diken`: Core engine package.
- `src/me/ramazanenescik04/diken/game`: Game model, nodes, settings, world, instances, and services.
- `src/me/ramazanenescik04/diken/gui`: GUI primitives and editor-facing components.
- `src/me/ramazanenescik04/diken/input`: Input handling and listeners.
- `src/me/ramazanenescik04/diken/renderer`: Rendering support.
- `src/me/ramazanenescik04/diken/resource`: Image, sound, cursor, and IO resource utilities.
- `src/me/ramazanenescik04/diken/scripting`: Lua/script integration helpers.
- `src/me/ramazanenescik04/diken/studio`: DikenEngine Studio/editor UI.
- `src/me/ramazanenescik04/diken/tools`: Shared utility classes.

## Coding Guidelines

- Keep changes small, focused, and consistent with nearby code.
- Prefer existing engine abstractions such as `Node`, `Instance`, services, resources, and GUI components instead of introducing parallel systems.
- Preserve public APIs unless the requested change clearly requires an API update.
- Use Java naming conventions:
  - Classes and enums: `PascalCase`
  - Methods, fields, and local variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- Keep package names under `me.ramazanenescik04.diken` for engine code.
- Do not move classes between packages unless the task is explicitly about project organization.
- Add comments only where they explain non-obvious behavior, lifecycle constraints, rendering details, or engine/editor interactions.
- Avoid broad refactors while fixing a narrow bug.

## Engine Behavior

- Be careful with update loops, rendering paths, input dispatch, and editor tree/property synchronization. Small changes here can affect many workflows.
- When modifying `Node`, `World`, `Instance`, services, or editor panels, check both runtime behavior and Studio/editor behavior.
- Resource loading code should avoid unnecessary repeated IO and should fail with clear errors where possible.
- GUI changes should preserve existing Swing threading expectations. UI updates should happen on the Event Dispatch Thread when relevant.

## Build And Verification

This appears to be an Eclipse Java project. Prefer using the project's existing Eclipse configuration when available.

Before finishing code changes, run the most relevant local verification available, for example:

```powershell
javac -cp src -d bin (Get-ChildItem -Recurse -File -Filter *.java | ForEach-Object { $_.FullName })
```

If a full compile is not practical, compile the changed files and any directly affected dependencies.

When changing UI or engine behavior, also do a quick manual smoke check in the editor/runtime if the task depends on visible behavior.

## Working Rules For AI Agents

- Read the relevant source files before editing.
- Check whether user changes already exist in the working tree and do not overwrite them.
- Do not run destructive git commands such as `git reset --hard` or `git checkout --` unless the user explicitly asks for them.
- Use `rg` or fast file search when available.
- Keep generated files, build artifacts, and IDE metadata out of edits unless the task specifically involves them.
- Explain any skipped verification in the final response.

## Documentation Style

- Use concise Markdown.
- Keep project documentation practical and implementation-oriented.
- Match the existing project language where possible. If a file is already in Turkish, Turkish is acceptable; otherwise English is acceptable for general agent-facing documentation.


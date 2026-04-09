# Slash Command Output Schema

All slash command responses MUST include the following top-level fields:

- `command`
- `execution_id`
- `mode`
- `status`
- `inputs`
- `prechecks`
- `execution_log`
- `artifacts`
- `risks`
- `safety_checks`
- `stop_condition`
- `next_action`

Constraints:
- `mode` MUST be one of: `slash-command`, `slash-command-auto`.
- `status` MUST be one of: `completed`, `blocked`, `failed`.
- If a field value is unknown, set it to `null`.
- Unknown or malformed slash commands MUST return structured error output and stop.
package com.marginalia.scaffold

object SpecTemplates {

    fun readme(projectName: String, description: String): String = """
        |# $projectName
        |
        |$description
        |
        |## Repository Structure
        |
        |```
        |${projectName}-specs/
        |├── product-description.md
        |├── epics/
        |│   └── _index.md
        |└── stories/
        |    └── _index.md
        |```
    """.trimMargin()

    fun productDescription(projectName: String, description: String): String = """
        |# $projectName — Product Description
        |
        |## What is $projectName?
        |
        |$description
        |
        |## Target Users
        |
        |<!-- Who will use this? List specific user types or personas. -->
        |
        |- **TODO** — Describe your target users
        |
        |## Core Problem
        |
        |<!-- What problem does this solve? Why do existing solutions fall short? -->
        |
        |TODO — Describe the core problem this project addresses.
        |
        |## Key Features
        |
        |<!-- What are the main capabilities? Number them for clarity. -->
        |
        |1. **TODO** — Describe the first key feature
        |
        |## Success Criteria
        |
        |<!-- How will you know the project is successful? Be specific and measurable. -->
        |
        |- TODO — Define measurable success criteria
        |
        |## Constraints
        |
        |<!-- What technical, business, or design constraints apply? -->
        |
        |- TODO — List any constraints or limitations
    """.trimMargin()

    fun epicsIndex(): String = """
        |# Epics Index
        |
        || ID | Title | Status |
        ||----|-------|--------|
    """.trimMargin()

    fun storiesIndex(): String = """
        |# Stories Index
        |
        || ID | Title | Epic | Status |
        ||----|-------|------|--------|
    """.trimMargin()
}

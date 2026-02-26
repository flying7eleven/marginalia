# Changelog

All notable changes to the Marginalia plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added

- Plugin descriptor enabling IDE recognition and loading across all JetBrains IDEs
- Plugin icon visible in the New Project dialog and IDE menus
- Project configuration and scaffolding result data models
- Spec file templates for README, product description, epics index, and stories index
- Automatic git repository initialization for scaffolded projects
- Project scaffolder that creates dual-repo structure (specs + code) with templates and git init
- IDE integration for scaffolding with automatic file system refresh
- AI client interface for pluggable AI providers with Anthropic Claude implementation
- Interview engine that conducts a guided Q&A to generate a product description
- Chat UI with message bubbles, text input, Send and Generate Now buttons
- Interview dialog that runs the AI conversation and writes the product description on completion
- Automatic AI interview launch after project scaffolding (requires API key in settings)
- Persistent settings with secure API key storage via IDE PasswordSafe
